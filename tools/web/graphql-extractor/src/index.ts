import { parseArgs } from "node:util";
import { writeFile } from "node:fs/promises";
import { resolve } from "node:path";
import { extractMexSchemas } from "./whatsapp/mex-extractor.js";
import type { Transport } from "./parser/types.js";

const { values: args } = parseArgs({
  options: {
    output: { type: "string", default: "schemas.json" },
  },
});

const outputPath = resolve(args.output!);

async function main(): Promise<void> {
  console.log("WhatsApp GraphQL Schema Extractor");
  console.log(`Output : ${outputPath}`);
  console.log("");

  const operations = await extractMexSchemas();

  const queries = operations.filter((o) => o.operationKind === "query").length;
  const mutations = operations.filter((o) => o.operationKind === "mutation").length;
  const has = (o: (typeof operations)[number], t: Transport) => o.transports.includes(t);
  const stanzaMex = operations.filter((o) => has(o, "stanza_mex")).length;
  const httpRelay = operations.filter((o) => has(o, "http_relay")).length;
  const httpComet = operations.filter((o) => has(o, "http_comet")).length;
  const unknown = operations.filter((o) => has(o, "unknown")).length;

  const payload = {
    extractedAt: new Date().toISOString(),
    count: operations.length,
    operations,
  };

  await writeFile(outputPath, JSON.stringify(payload, null, 2), "utf8");

  for (const op of operations) {
    console.log(
      `  ${op.name}: id=${op.id}, kind=${op.operationKind}, transports=${op.transports.join("+")}, vars=${op.variables.length}`,
    );
  }
  console.log("");
  console.log(
    `Wrote ${operations.length} operations (${queries} queries, ${mutations} mutations) to ${outputPath}`,
  );
  console.log(
    `Transports: ${stanzaMex} stanza_mex, ${httpRelay} http_relay, ${httpComet} http_comet, ${unknown} unknown`,
  );
}

main().catch((err: unknown) => {
  const message = err instanceof Error ? err.stack ?? err.message : String(err);
  console.error(`[FATAL] ${message}`);
  process.exit(1);
});
