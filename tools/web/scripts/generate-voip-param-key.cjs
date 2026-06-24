// Regenerates the VoipParamKey catalogue (the sealed VoipParamKey interface, its
// VoipParamKeyCatalogue holder, and the per-namespace partition enums) under
// modules/lib/.../calls2/common directly from the wa-voip WASM module.
//
// This script is self-contained: its only input is the WASM binary. It parses the module
// itself (code + data sections), decodes the native reg_param_entry_impl descriptor-building
// instruction patterns, reads each tunable's dotted-path name straight from the data section,
// and emits the Java. It does not depend on any pre-parsed JSON, captured string table, text
// disassembly, or the RE MCP server.
//
// Usage (run from the repository root so the output path resolves):
//   node tools/web/scripts/generate-voip-param-key.cjs [path/to/wa-voip.wasm]
//
// How it works: the engine registers each param with a 20-byte voip_param_entry descriptor
//   { field_offset@0 (i32), value_len@4 (u16), type@6 (u8 ParamType 1..5), is_bwe@8 (u8),
//     arr_elem_len@9 (u8), arr_elem_type@10 (u8), group_id@12 (i32), name_ptr@16 (i32) }.
//   The registration functions build it with i32.const/i32.store* sequences; this script
//   finds each store to field offset 16, reconstructs the struct bytes from the surrounding
//   constant stores, and resolves the dotted-path name from the data segment the name pointer
//   points into. Keys are partitioned into several enums because a single enum holding all
//   ~3040 constants overflows the JVM 64KB <clinit> method-size limit.

const fs = require('fs');

const wasmPath = process.argv[2] || '.temp/voip-param/O4cDmmXP6rI.wasm';
const outDir = 'modules/lib/src/main/java/com/github/auties00/cobalt/calls2/common';
const CHUNK_SIZE = 700;

// ---------------------------------------------------------------------------------------------
// LEB128 readers. readU/readS return [value, nextPos]; skipLEB only advances the cursor.
// ---------------------------------------------------------------------------------------------
function readU(buf, p) {
  let result = 0;
  let shift = 0;
  let byte;
  do {
    byte = buf[p++];
    result += (byte & 0x7f) * 2 ** shift;
    shift += 7;
  } while (byte & 0x80);
  return [result, p];
}

function readS(buf, p) {
  let result = 0;
  let shift = 0;
  let byte;
  do {
    byte = buf[p++];
    result += (byte & 0x7f) * 2 ** shift;
    shift += 7;
  } while (byte & 0x80);
  if (shift < 53 && (byte & 0x40)) {
    result -= 2 ** shift;
  }
  return [result, p];
}

function skipLEB(buf, p) {
  while (buf[p] & 0x80) p++;
  return p + 1;
}

function skipMemarg(buf, p) {
  let align;
  [align, p] = readU(buf, p);
  if (align & 0x40) {
    p = skipLEB(buf, p);
  }
  return skipLEB(buf, p);
}

// ---------------------------------------------------------------------------------------------
// Module sections.
// ---------------------------------------------------------------------------------------------
function parseSections(buf) {
  if (buf.readUInt32LE(0) !== 0x6d736100) {
    throw new Error('not a WASM module (bad magic)');
  }
  let p = 8;
  const sections = [];
  while (p < buf.length) {
    const id = buf[p++];
    let size;
    [size, p] = readU(buf, p);
    sections.push({ id, start: p, end: p + size });
    p += size;
  }
  return sections;
}

// ---------------------------------------------------------------------------------------------
// Memory image with a NUL-terminated string reader.
//
// This emscripten module emits its data as passive segments materialized at runtime by
// memory.init; there are no active segments with static offsets. So the data section gives the
// raw segment bytes, and the code section's memory.init instructions give where each segment
// lands in linear memory. This reproduces both to map a linear address back to its bytes.
// ---------------------------------------------------------------------------------------------
function buildMemory(buf, sections) {
  const dataSection = sections.find((s) => s.id === 11);
  const codeSection = sections.find((s) => s.id === 10);
  const segments = [];

  // Parse the data segments. Active segments carry their own offset; passive segments are
  // retained by index for the memory.init pass below.
  const passive = [];
  let p = dataSection.start;
  let count;
  [count, p] = readU(buf, p);
  for (let i = 0; i < count; i++) {
    let flags;
    [flags, p] = readU(buf, p);
    if (flags === 1) {
      let len;
      [len, p] = readU(buf, p);
      passive.push(buf.subarray(p, p + len));
      p += len;
      continue;
    }
    if (flags === 2) {
      p = skipLEB(buf, p); // explicit memory index
    }
    let addr = null;
    if (buf[p] === 0x41) {
      [addr, p] = readS(buf, p + 1);
    } else {
      while (buf[p] !== 0x0b) p = skipLEB(buf, p);
    }
    if (buf[p] === 0x0b) p++;
    let len;
    [len, p] = readU(buf, p);
    if (addr !== null) {
      segments.push({ start: addr, bytes: buf.subarray(p, p + len) });
    }
    passive.push(null);
    p += len;
  }

  // Materialize passive segments at the linear addresses memory.init copies them to. Each
  // memory.init is preceded by three constants: dest address, source offset, and length.
  let q = codeSection.start;
  let bodyCount;
  [bodyCount, q] = readU(buf, q);
  for (let i = 0; i < bodyCount; i++) {
    let size;
    [size, q] = readU(buf, q);
    const bodyStart = q;
    const bodyEnd = q + size;
    q = bodyEnd;
    let ins;
    try {
      ins = decodeBody(buf, bodyStart, bodyEnd);
    } catch {
      continue;
    }
    for (let index = 0; index < ins.length; index++) {
      if (ins[index].k !== 'meminit') continue;
      const bytes = passive[ins[index].seg];
      if (!bytes) continue;
      const args = [];
      for (let j = index - 1; j >= 0 && args.length < 3; j--) {
        if (ins[j].k === 'const') args.push(ins[j].v);
      }
      if (args.length < 3) continue;
      const [length, offset, dest] = args;
      if (dest < 0 || offset < 0 || length < 0) continue;
      segments.push({ start: dest, bytes: bytes.subarray(offset, offset + length) });
    }
  }

  segments.sort((a, b) => a.start - b.start);
  return segments;
}

function readCString(segments, addr) {
  for (const segment of segments) {
    if (addr < segment.start || addr >= segment.start + segment.bytes.length) continue;
    let end = addr - segment.start;
    while (end < segment.bytes.length && segment.bytes[end] !== 0) end++;
    return segment.bytes.toString('latin1', addr - segment.start, end);
  }
  return null;
}

// ---------------------------------------------------------------------------------------------
// Instruction decode for one function body. Returns a list where each entry is one
// instruction; the entries this script reasons about are tagged, the rest are opaque ('o').
// Throws on an unrecognized opcode so a mis-decode aborts that one body rather than desyncing.
// ---------------------------------------------------------------------------------------------
function decodeBody(buf, start, end) {
  let p = start;
  let localGroups;
  [localGroups, p] = readU(buf, p);
  for (let i = 0; i < localGroups; i++) {
    p = skipLEB(buf, p); // local count
    p++; // value type
  }
  const ins = [];
  while (p < end) {
    const op = buf[p++];
    if (op === 0x41) { // i32.const
      let v;
      [v, p] = readS(buf, p);
      ins.push({ k: 'const', v });
    } else if (op === 0x36) { // i32.store
      let off;
      [off, p] = readStoreOffset(buf, p);
      ins.push({ k: 'store', w: 4, off });
    } else if (op === 0x3b) { // i32.store16
      let off;
      [off, p] = readStoreOffset(buf, p);
      ins.push({ k: 'store', w: 2, off });
    } else if (op === 0x3a) { // i32.store8
      let off;
      [off, p] = readStoreOffset(buf, p);
      ins.push({ k: 'store', w: 1, off });
    } else if (op === 0x47) { // i32.ne
      ins.push({ k: 'ne' });
    } else if (op === 0x42) { // i64.const
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x43) { // f32.const
      p += 4;
      ins.push({ k: 'o' });
    } else if (op === 0x44) { // f64.const
      p += 8;
      ins.push({ k: 'o' });
    } else if (op >= 0x28 && op <= 0x3e) { // remaining loads/stores: memarg
      p = skipMemarg(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x02 || op === 0x03 || op === 0x04) { // block/loop/if: blocktype
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x0c || op === 0x0d) { // br/br_if
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x0e) { // br_table
      let n;
      [n, p] = readU(buf, p);
      for (let i = 0; i <= n; i++) p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x10) { // call
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x11) { // call_indirect
      p = skipLEB(buf, p);
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x1c) { // select with types
      let n;
      [n, p] = readU(buf, p);
      p += n;
      ins.push({ k: 'o' });
    } else if (op >= 0x20 && op <= 0x26) { // local.*, global.*, table.get/set
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0x3f || op === 0x40) { // memory.size/grow
      p += 1;
      ins.push({ k: 'o' });
    } else if (op === 0xd0) { // ref.null
      p += 1;
      ins.push({ k: 'o' });
    } else if (op === 0xd2) { // ref.func
      p = skipLEB(buf, p);
      ins.push({ k: 'o' });
    } else if (op === 0xfc) { // misc prefix
      let sub;
      [sub, p] = readU(buf, p);
      if (sub === 8) { // memory.init <dataidx> <memidx>
        let seg;
        [seg, p] = readU(buf, p);
        p += 1; // memory index
        ins.push({ k: 'meminit', seg });
      } else {
        p = skipFC(buf, p, sub);
        ins.push({ k: 'o' });
      }
    } else if (op === 0xfd) { // SIMD prefix
      let sub;
      [sub, p] = readU(buf, p);
      p = skipFD(buf, p, sub);
      ins.push({ k: 'o' });
    } else if (op === 0xfe) { // atomics prefix
      let sub;
      [sub, p] = readU(buf, p);
      p = sub === 3 ? p + 1 : skipMemarg(buf, p);
      ins.push({ k: 'o' });
    } else if (NO_IMMEDIATE.has(op)) {
      ins.push({ k: 'o' });
    } else {
      throw new Error(`unhandled opcode 0x${op.toString(16)}`);
    }
  }
  return ins;
}

function readStoreOffset(buf, p) {
  let align;
  [align, p] = readU(buf, p);
  if (align & 0x40) {
    p = skipLEB(buf, p);
  }
  return readU(buf, p);
}

function skipFC(buf, p, sub) {
  switch (sub) {
    case 8: return skipLEB(buf, p) + 1; // memory.init
    case 9: return skipLEB(buf, p); // data.drop
    case 10: return p + 2; // memory.copy
    case 11: return p + 1; // memory.fill
    case 12: case 14: return skipLEB(buf, skipLEB(buf, p)); // table.init / table.copy
    case 13: case 15: case 16: case 17: return skipLEB(buf, p);
    default: return p; // 0..7 saturating truncations
  }
}

function skipFD(buf, p, sub) {
  if (sub <= 11) return skipMemarg(buf, p); // v128 loads/stores
  if (sub === 12 || sub === 13) return p + 16; // v128.const / i8x16.shuffle
  if (sub >= 21 && sub <= 34) return p + 1; // extract_lane / replace_lane
  if (sub >= 84 && sub <= 91) return skipMemarg(buf, p) + 1; // v128.load/store lane
  if (sub === 92 || sub === 93) return skipMemarg(buf, p); // v128.load32/64_zero
  return p; // arithmetic
}

// Opcodes that take no immediate operand (numeric, comparison, conversion, parametric).
const NO_IMMEDIATE = new Set([
  0x00, 0x01, 0x05, 0x0b, 0x0f, 0x1a, 0x1b, 0xd1,
]);
for (let op = 0x45; op <= 0xc4; op++) NO_IMMEDIATE.add(op);

// ---------------------------------------------------------------------------------------------
// Descriptor reconstruction. Mirrors the native struct layout: a store to field offset 16 is
// the registration anchor; the preceding constant stores fill the 20-byte descriptor.
// ---------------------------------------------------------------------------------------------
function constBefore(ins, index) {
  for (let i = index - 1; i >= 0 && i >= index - 8; i--) {
    if (ins[i].k === 'const' && ins[i].v >= 0) return ins[i].v;
  }
  return null;
}

function writeBytes(bytes, offset, value, width) {
  for (let i = 0; i < width; i++) {
    const absolute = offset + i;
    if (absolute >= 6 && absolute <= 10) {
      bytes[absolute - 6] = (value >>> (i * 8)) & 0xff;
    }
  }
}

function isParamName(name) {
  return name != null && /^(p|mvp|vp|tp)->/.test(name);
}

function reconstruct(ins, storeIndex, segments) {
  const windowStart = Math.max(0, storeIndex - 180);
  const nameWindowStart = Math.max(0, storeIndex - 90);

  let name = null;
  for (let i = storeIndex; i >= nameWindowStart && name == null; i--) {
    if (ins[i].k !== 'ne') continue;
    for (let j = i - 1; j >= nameWindowStart && j >= i - 6; j--) {
      if (ins[j].k !== 'const') continue;
      const candidate = readCString(segments, ins[j].v);
      name = isParamName(candidate) ? candidate : null;
      break;
    }
  }
  if (name == null) return null;

  const bytes = [0, 0, 0, 0, 0];
  let fieldOffset = null;
  let valueLen = null;
  let groupId = null;
  for (let i = windowStart; i <= storeIndex; i++) {
    const entry = ins[i];
    if (entry.k !== 'store') continue;
    const value = constBefore(ins, i);
    if (value == null) continue;
    if (entry.w === 4) {
      if (entry.off === 0) fieldOffset = value >>> 0;
      if (entry.off === 12) groupId = value >>> 0;
      writeBytes(bytes, entry.off, value, 4);
    } else if (entry.w === 2) {
      if (entry.off === 4) valueLen = value & 0xffff;
      writeBytes(bytes, entry.off, value, 2);
    } else {
      writeBytes(bytes, entry.off, value, 1);
    }
  }

  const type = bytes[0];
  const bwe = bytes[2];
  if (fieldOffset == null || valueLen == null || groupId == null) return null;
  if (type < 1 || type > 5) return null;
  if (bwe !== 0 && bwe !== 1) return null;

  return {
    name,
    valueLen,
    type,
    bweParam: bwe === 1,
    arrElemLen: bytes[3],
    arrElemType: bytes[4],
  };
}

// ---------------------------------------------------------------------------------------------
// Drive the extraction over every code body.
// ---------------------------------------------------------------------------------------------
function extractDescriptors(buf) {
  const sections = parseSections(buf);
  const codeSection = sections.find((s) => s.id === 10);
  const dataSection = sections.find((s) => s.id === 11);
  if (!codeSection || !dataSection) {
    throw new Error('module is missing a code or data section');
  }
  const segments = buildMemory(buf, sections);

  let p = codeSection.start;
  let bodyCount;
  [bodyCount, p] = readU(buf, p);
  const byName = new Map();
  for (let i = 0; i < bodyCount; i++) {
    let size;
    [size, p] = readU(buf, p);
    const bodyStart = p;
    const bodyEnd = p + size;
    p = bodyEnd;
    let ins;
    try {
      ins = decodeBody(buf, bodyStart, bodyEnd);
    } catch {
      continue; // a body we cannot fully decode cannot be a registration body
    }
    for (let index = 0; index < ins.length; index++) {
      const entry = ins[index];
      if (entry.k !== 'store' || entry.w !== 4 || entry.off !== 16) continue;
      const descriptor = reconstruct(ins, index, segments);
      if (descriptor && !byName.has(descriptor.name)) {
        byName.set(descriptor.name, descriptor);
      }
    }
  }
  return [...byName.values()].sort((a, b) => a.name.localeCompare(b.name));
}

// ---------------------------------------------------------------------------------------------
// Java code generation.
// ---------------------------------------------------------------------------------------------
const typeNames = new Map([
  [1, 'INTEGER'],
  [2, 'FLOAT'],
  [3, 'STRING'],
  [4, 'ARRAY'],
  [5, 'ARRAY_COUNT'],
]);

const javaKeywords = new Set([
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class', 'const', 'continue',
  'default', 'do', 'double', 'else', 'enum', 'extends', 'final', 'finally', 'float', 'for', 'goto', 'if',
  'implements', 'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'package', 'private',
  'protected', 'public', 'return', 'short', 'static', 'strictfp', 'super', 'switch', 'synchronized', 'this',
  'throw', 'throws', 'transient', 'try', 'void', 'volatile', 'while', 'true', 'false', 'null', 'var', 'yield',
  'record', 'sealed', 'permits', 'non-sealed',
]);

const seenConstants = new Set();
function constantNameFor(name) {
  const arrow = name.indexOf('->');
  const prefix = name.slice(0, arrow).toUpperCase();
  const rest = name.slice(arrow + 2);
  let base = `${prefix}_${rest}`
    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/([a-z0-9])([A-Z])/g, '$1_$2')
    .replace(/^_+|_+$/g, '')
    .replace(/_+/g, '_')
    .toUpperCase();
  if (!base) base = `${prefix}_PARAM`;
  if (/^[0-9]/.test(base) || javaKeywords.has(base.toLowerCase())) {
    base = `PARAM_${base}`;
  }
  let candidate = base;
  let suffix = 2;
  while (seenConstants.has(candidate)) {
    candidate = `${base}_${suffix++}`;
  }
  seenConstants.add(candidate);
  return candidate;
}

function javaString(value) {
  return JSON.stringify(value)
    .replace(/\\u003e/g, '>')
    .replace(/\\u003c/g, '<')
    .replace(/\\u0026/g, '&')
    .replace(/\\u0027/g, "'");
}

function escapeJavadoc(value) {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function namespaceOf(name) {
  if (name.startsWith('p->')) return 'Call';
  if (name.startsWith('mvp->') || name.startsWith('vp->')) return 'Media';
  return 'Transport';
}

function writeFile(name, lines) {
  fs.writeFileSync(`${outDir}/${name}.java`, lines.join('\n') + '\n', 'utf8');
}

function generate(entries) {
  const byNamespace = new Map();
  for (const entry of entries) {
    const ns = namespaceOf(entry.name);
    if (!byNamespace.has(ns)) byNamespace.set(ns, []);
    byNamespace.get(ns).push(entry);
  }

  const partitions = [];
  for (const [ns, nsEntries] of byNamespace) {
    const partCount = Math.ceil(nsEntries.length / CHUNK_SIZE);
    for (let part = 0; part < partCount; part++) {
      const chunk = nsEntries.slice(part * CHUNK_SIZE, (part + 1) * CHUNK_SIZE);
      partitions.push({ className: `VoipParamKey${ns}${part + 1}`, namespace: ns, chunk });
    }
  }
  const permits = partitions.map((partition) => partition.className).join(', ');

  const iface = [];
  iface.push('package com.github.auties00.cobalt.calls2.common;');
  iface.push('');
  iface.push('import java.util.List;');
  iface.push('import java.util.Optional;');
  iface.push('');
  iface.push('/**');
  iface.push(' * Enumerates the native voip-param registry keys recovered from WhatsApp Web\'s');
  iface.push(' * {@code voip_param_entry} descriptor writes.');
  iface.push(' *');
  iface.push(' * <p>The keys are generated directly from the wa-voip WASM module. Each key is backed by an');
  iface.push(' * observed write to the 20-byte native descriptor shape used by {@code reg_param_entry_impl};');
  iface.push(' * the value type, byte width, and rate-control flag are copied from those writes, not');
  iface.push(' * inferred from names or JSON values.');
  iface.push(' *');
  iface.push(' * <p>The catalogue contains descriptor entries only. JSON section roots such as');
  iface.push(' * {@code p-&gt;aec} and metadata fields such as {@code voip_settings_version} are not native');
  iface.push(' * descriptor entries and are retained by {@link VoipParamJsonDeserializer} only as flattened');
  iface.push(' * unmodelled values.');
  iface.push(' *');
  iface.push(' * <p>The keys are partitioned across the permitted enums by namespace ({@code p-&gt;},');
  iface.push(' * {@code mvp-&gt;}/{@code vp-&gt;}, {@code tp-&gt;}); the {@code p-&gt;} namespace is split further so');
  iface.push(' * that no generated enum\'s static initializer exceeds the JVM 64KB method-size limit. The');
  iface.push(' * full key set is the union of every partition, exposed through {@link #values()}.');
  iface.push(' */');
  iface.push(`public sealed interface VoipParamKey permits ${permits} {`);
  iface.push('    /**');
  iface.push('     * Returns the fully-qualified dotted path the engine addresses this tunable by.');
  iface.push('     *');
  iface.push('     * @return the dotted path, such as {@code "p-&gt;conds.cond_range_ul_bwe"}');
  iface.push('     */');
  iface.push('    String dottedPath();');
  iface.push('');
  iface.push('    /**');
  iface.push('     * Returns the native descriptor value type for this tunable.');
  iface.push('     *');
  iface.push('     * @return the native descriptor value type');
  iface.push('     */');
  iface.push('    VoipParamType type();');
  iface.push('');
  iface.push('    /**');
  iface.push('     * Returns the serialized byte width recorded in the native descriptor.');
  iface.push('     *');
  iface.push('     * @return the serialized byte width');
  iface.push('     */');
  iface.push('    int byteWidth();');
  iface.push('');
  iface.push('    /**');
  iface.push('     * Returns whether the native descriptor marks this tunable as rate-control related.');
  iface.push('     *');
  iface.push('     * @return {@code true} if this is a rate-control tunable, {@code false} otherwise');
  iface.push('     */');
  iface.push('    boolean bweParam();');
  iface.push('');
  iface.push('    /**');
  iface.push('     * Returns every modelled key, unioned across all partitions in generation order.');
  iface.push('     *');
  iface.push('     * @return an unmodifiable list of all modelled keys');
  iface.push('     */');
  iface.push('    static List<VoipParamKey> values() {');
  iface.push('        return VoipParamKeyCatalogue.ALL;');
  iface.push('    }');
  iface.push('');
  iface.push('    /**');
  iface.push('     * Returns the key whose {@linkplain #dottedPath() dotted path} equals the given value.');
  iface.push('     *');
  iface.push('     * @param dottedPath the dotted path to resolve');
  iface.push('     * @return the matching key, or {@link Optional#empty()} if the path is not modelled');
  iface.push('     */');
  iface.push('    static Optional<VoipParamKey> ofDottedPath(String dottedPath) {');
  iface.push('        return Optional.ofNullable(VoipParamKeyCatalogue.BY_DOTTED_PATH.get(dottedPath));');
  iface.push('    }');
  iface.push('}');
  writeFile('VoipParamKey', iface);

  const cat = [];
  cat.push('package com.github.auties00.cobalt.calls2.common;');
  cat.push('');
  cat.push('import java.util.Arrays;');
  cat.push('import java.util.List;');
  cat.push('import java.util.Map;');
  cat.push('import java.util.stream.Collectors;');
  cat.push('import java.util.stream.Stream;');
  cat.push('');
  cat.push('/**');
  cat.push(' * Aggregates the partitioned {@link VoipParamKey} catalogue into the union views the public');
  cat.push(' * accessors return.');
  cat.push(' *');
  cat.push(' * <p>The key set is generated into several enum partitions to keep each enum\'s static');
  cat.push(' * initializer within the JVM 64KB method-size limit; this holder unions them once at class');
  cat.push(' * load so {@link VoipParamKey#values()} and {@link VoipParamKey#ofDottedPath(String)} answer');
  cat.push(' * without rescanning each partition.');
  cat.push(' */');
  cat.push('final class VoipParamKeyCatalogue {');
  cat.push('    /**');
  cat.push('     * Every modelled key, unioned across all partitions in generation order.');
  cat.push('     */');
  cat.push('    static final List<VoipParamKey> ALL = Stream.<VoipParamKey[]>of(');
  partitions.forEach((partition, index) => {
    const comma = index === partitions.length - 1 ? '' : ',';
    cat.push(`            ${partition.className}.values()${comma}`);
  });
  cat.push('    ).flatMap(Arrays::stream).toList();');
  cat.push('');
  cat.push('    /**');
  cat.push('     * The dotted-path lookup over {@link #ALL}.');
  cat.push('     */');
  cat.push('    static final Map<String, VoipParamKey> BY_DOTTED_PATH = ALL.stream()');
  cat.push('            .collect(Collectors.toUnmodifiableMap(VoipParamKey::dottedPath, key -> key));');
  cat.push('');
  cat.push('    /**');
  cat.push('     * Prevents instantiation of this static holder.');
  cat.push('     */');
  cat.push('    private VoipParamKeyCatalogue() {');
  cat.push('    }');
  cat.push('}');
  writeFile('VoipParamKeyCatalogue', cat);

  const nsLabels = { Call: 'p-&gt;', Media: 'mvp-&gt;/vp-&gt;', Transport: 'tp-&gt;' };
  for (const partition of partitions) {
    const { className, namespace, chunk } = partition;
    const out = [];
    out.push('package com.github.auties00.cobalt.calls2.common;');
    out.push('');
    out.push('/**');
    out.push(` * A partition of the {@code ${nsLabels[namespace]}} voip-param registry keys.`);
    out.push(' *');
    out.push(' * <p>This enum exists only to keep its generated static initializer within the JVM 64KB');
    out.push(' * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}');
    out.push(' * rather than this partition directly.');
    out.push(' */');
    out.push(`enum ${className} implements VoipParamKey {`);
    chunk.forEach((entry, index) => {
      const typeName = typeNames.get(entry.type);
      const constantName = constantNameFor(entry.name);
      const terminator = index === chunk.length - 1 ? ';' : ',';
      out.push('    /**');
      out.push(`     * Native descriptor for {@code ${escapeJavadoc(entry.name)}}.`);
      out.push('     */');
      out.push(`    ${constantName}(${javaString(entry.name)}, VoipParamType.${typeName}, ${entry.valueLen}, ${entry.bweParam ? 'true' : 'false'})${terminator}`);
      if (index !== chunk.length - 1) out.push('');
    });
    out.push('');
    out.push('    /**');
    out.push('     * The fully-qualified dotted path the engine addresses this tunable by.');
    out.push('     */');
    out.push('    private final String dottedPath;');
    out.push('');
    out.push('    /**');
    out.push('     * The native descriptor value type for this tunable.');
    out.push('     */');
    out.push('    private final VoipParamType type;');
    out.push('');
    out.push('    /**');
    out.push('     * The serialized byte width recorded in the native descriptor.');
    out.push('     */');
    out.push('    private final int byteWidth;');
    out.push('');
    out.push('    /**');
    out.push('     * Whether the native descriptor marks this tunable as rate-control related.');
    out.push('     */');
    out.push('    private final boolean bweParam;');
    out.push('');
    out.push('    /**');
    out.push('     * Constructs a key partition constant from its native descriptor fields.');
    out.push('     *');
    out.push('     * @param dottedPath the fully-qualified dotted path');
    out.push('     * @param type       the native descriptor value type');
    out.push('     * @param byteWidth   the serialized byte width');
    out.push('     * @param bweParam   whether this is a rate-control tunable');
    out.push('     */');
    out.push(`    ${className}(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {`);
    out.push('        this.dottedPath = dottedPath;');
    out.push('        this.type = type;');
    out.push('        this.byteWidth = byteWidth;');
    out.push('        this.bweParam = bweParam;');
    out.push('    }');
    out.push('');
    out.push('    @Override');
    out.push('    public String dottedPath() {');
    out.push('        return dottedPath;');
    out.push('    }');
    out.push('');
    out.push('    @Override');
    out.push('    public VoipParamType type() {');
    out.push('        return type;');
    out.push('    }');
    out.push('');
    out.push('    @Override');
    out.push('    public int byteWidth() {');
    out.push('        return byteWidth;');
    out.push('    }');
    out.push('');
    out.push('    @Override');
    out.push('    public boolean bweParam() {');
    out.push('        return bweParam;');
    out.push('    }');
    out.push('}');
    writeFile(className, out);
  }

  return partitions;
}

// ---------------------------------------------------------------------------------------------
// Entry point.
// ---------------------------------------------------------------------------------------------
const wasm = fs.readFileSync(wasmPath);
const descriptors = extractDescriptors(wasm);
if (descriptors.length < 1000) {
  throw new Error(`only ${descriptors.length} descriptors recovered from ${wasmPath}; extraction likely failed`);
}
const partitions = generate(descriptors);
console.log(`Recovered ${descriptors.length} descriptors from ${wasmPath} across ${partitions.length} partitions:`);
for (const partition of partitions) {
  console.log(`  ${partition.className}: ${partition.chunk.length}`);
}
