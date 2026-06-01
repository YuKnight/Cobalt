#!/usr/bin/env bash
#
# Builds every native dependency Cobalt's call + media + store layers link
# against, then links them all into ONE combined shared library per platform:
#   modules/lib/natives/bin/<classifier>/<libcobalt-native.*>
#
# Each dependency is built as a STATIC archive (no per-dep shared object is
# shipped). The final build_combined step pulls the archives together, forces
# in exactly the symbols Cobalt's FFM bindings resolve (the union of every
# --include-function in the per-dep generate.sh, materialised by the gen_exports
# function into a build-scratch list), exports only those, dead-strips everything unreachable from
# them, and strips the result. Because the jextract bindings resolve through
# SymbolLookup.loaderLookup(), a single System.load of this one file satisfies
# every binding regardless of which dependency a symbol originally came from.
#
# Outputs land under System.mapLibraryName-style names: libcobalt-native.so on
# Linux, libcobalt-native.dylib on darwin, cobalt-native.dll on Windows.
#
# Override any source tree by exporting <DEP>_SRC=/path/to/checkout before
# invocation. Otherwise the script clones the pinned ref into a temp dir per dep.
#
# Required tools on PATH (the workflow installs these per OS):
#   git python3 autoconf automake libtool pkg-config nasm yasm cmake
#   make and the host's C/C++ compiler.

set -Eeuo pipefail
trap 'echo "[build-natives] FAILED at ${BASH_SOURCE[0]}:${LINENO}: ${BASH_COMMAND}" >&2' ERR

case "$(uname -s)" in
    Linux)                OS=linux ;;
    Darwin)               OS=darwin ;;
    MINGW*|MSYS*|CYGWIN*) OS=windows ;;
    *) echo "unsupported OS: $(uname -s)" >&2; exit 1 ;;
esac
case "$(uname -m)" in
    x86_64|amd64)  ARCH=x86_64 ;;
    aarch64|arm64) ARCH=aarch64 ;;
    *) echo "unsupported arch: $(uname -m)" >&2; exit 1 ;;
esac
CLASSIFIER="$OS-$ARCH"

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"
DEPS="$ROOT/modules/lib/dependencies"
NATIVES="$ROOT/modules/lib/natives"
BUILD="${BUILD_CACHE:-/tmp/cobalt-natives-build}"
case "$BUILD" in
    *[[:space:]]*)
        echo "[build-natives] FAIL: BUILD path contains whitespace: '$BUILD' (autotools rejects this). Set BUILD_CACHE to a space-free path." >&2
        exit 1
        ;;
esac
mkdir -p "$BUILD"
JOBS="$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)"

# Every dependency compiles to a static archive with per-function/per-data
# sections so the final combined link can garbage-collect everything Cobalt
# never reaches. -fPIC is mandatory: the archives are linked into a shared
# library. -O2 is mandatory too: passing an explicit CFLAGS overrides each
# dependency's build-system default optimization level, so without it opus,
# openh264, speexdsp, vpx and webp would compile at -O0 (opus even warns it
# "will be very slow"), bloating the binary and crippling the realtime codecs.
# These apply to every build's CFLAGS/CXXFLAGS.
SECTIONS_CFLAGS="-O2 -ffunction-sections -fdata-sections -fPIC"

# On Windows (MinGW) the combined library must not leak a dependency on the
# toolchain's own runtime (libgcc_s_seh-1.dll, libstdc++-6.dll,
# libwinpthread-1.dll). Statically linking those runtimes once into the combined
# library eliminates the leak; the flags are no-ops elsewhere.
MINGW_CFLAGS=""
if [ "$OS" = windows ]; then
    MINGW_CFLAGS="-static-libgcc"
fi

EXTRA_CFLAGS="$SECTIONS_CFLAGS $MINGW_CFLAGS"

# C++ runtime each codec's static archive needs at its (static) link site.
# openh264 is C++, so FFmpeg's static dependency probe (and the final combined
# link) must resolve libstdc++/libc++; this is advertised via Libs.private in the
# pkg-config shims below.
case "$OS" in
    darwin) CXXLIB="-lc++" ;;
    *)      CXXLIB="-lstdc++" ;;
esac

OPUS_REPO=https://github.com/xiph/opus.git
OPUS_REF=v1.5.2

OPENH264_REPO=https://github.com/cisco/openh264.git
OPENH264_REF=v2.4.1

SPEEXDSP_REPO=https://github.com/xiph/speexdsp.git
SPEEXDSP_REF=SpeexDSP-1.2.1

USRSCTP_REPO=https://github.com/sctplab/usrsctp.git
USRSCTP_REF=master

LIBVPX_REPO=https://chromium.googlesource.com/webm/libvpx
LIBVPX_REF=v1.15.1

LIBWEBP_REPO=https://chromium.googlesource.com/webm/libwebp
LIBWEBP_REF=v1.5.0

FFMPEG_REPO=https://github.com/FFmpeg/FFmpeg.git
FFMPEG_REF=n7.1

MDBX_REPO=https://github.com/erthink/libmdbx.git
MDBX_REF=v0.14.2

log()  { echo "[build-natives] $*"; }
fail() { echo "[build-natives] FAIL: $*" >&2; exit 1; }

ensure_src() {
    local var="$1" repo="$2" ref="$3" dirname="$4"
    local val="${!var-}"
    if [ -n "$val" ]; then
        [ -d "$val" ] || fail "$var=$val does not exist"
        log "$var override: $val"
        return 0
    fi
    local workdir="$BUILD/$dirname"
    if [ -d "$workdir/.git" ] && git -C "$workdir" rev-parse --verify "$ref^{commit}" >/dev/null 2>&1; then
        log "$dirname cached at $ref"
        git -C "$workdir" -c advice.detachedHead=false checkout --force "$ref"
        git -C "$workdir" clean -fdx
    else
        rm -rf "$workdir"
        if ! git clone --depth 1 --branch "$ref" "$repo" "$workdir"; then
            log "shallow clone of $repo @ $ref failed, retrying full clone"
            rm -rf "$workdir"
            git clone "$repo" "$workdir"
            git -C "$workdir" checkout "$ref"
        fi
    fi
    printf -v "$var" '%s' "$workdir"
    export "$var"
}

vendor_headers() {
    local src="$1" dest="$2"
    [ -d "$src" ] || fail "vendor_headers: source dir $src does not exist"
    mkdir -p "$dest"
    find "$dest" -maxdepth 1 -name '*.h' -type f -delete 2>/dev/null || true
    cp "$src"/*.h "$dest/"
}

build_opus() {
    log "opus (static)"
    ensure_src OPUS_SRC "$OPUS_REPO" "$OPUS_REF" opus
    [ -x "$OPUS_SRC/configure" ] || ( cd "$OPUS_SRC" && autoreconf -isf )
    local b="$BUILD/build-opus"
    rm -rf "$b" && mkdir -p "$b"
    ( cd "$b" && CFLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        "$OPUS_SRC/configure" --prefix="$b/inst" \
        --disable-shared --enable-static --with-pic \
        --disable-doc --disable-extra-programs \
        --disable-deep-plc --disable-dred )
    make -C "$b" -j "$JOBS"
    make -C "$b" install
    vendor_headers "$b/inst/include/opus" "$DEPS/libopus/headers/opus"
}

build_openh264() {
    log "openh264 (static)"
    ensure_src OPENH264_SRC "$OPENH264_REPO" "$OPENH264_REF" openh264
    local make_os make_arch
    case "$OS" in
        linux)   make_os=linux ;;
        darwin)  make_os=darwin ;;
        windows) make_os=mingw_nt ;;
    esac
    case "$ARCH" in
        x86_64)  make_arch=x86_64 ;;
        aarch64) make_arch=arm64 ;;
    esac
    make -C "$OPENH264_SRC" OS="$make_os" ARCH="$make_arch" clean 2>/dev/null || true
    # The Makefile's default target builds both libopenh264.a and the shared
    # object; the static-library target alone is enough and avoids the shared link.
    make -C "$OPENH264_SRC" OS="$make_os" ARCH="$make_arch" \
        CFLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        -j "$JOBS" libopenh264.a
    [ -f "$OPENH264_SRC/libopenh264.a" ] || fail "openh264 static archive not produced"
    vendor_headers "$OPENH264_SRC/codec/api/wels" "$DEPS/openh264/headers"
}

build_speexdsp() {
    log "speexdsp (static)"
    ensure_src SPEEXDSP_SRC "$SPEEXDSP_REPO" "$SPEEXDSP_REF" speexdsp
    [ -x "$SPEEXDSP_SRC/configure" ] || ( cd "$SPEEXDSP_SRC" && ./autogen.sh )
    local b="$BUILD/build-speexdsp"
    rm -rf "$b" && mkdir -p "$b"
    ( cd "$b" && CFLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        "$SPEEXDSP_SRC/configure" --prefix="$b/inst" \
        --disable-shared --enable-static --with-pic --disable-examples )
    make -C "$b" -j "$JOBS"
    make -C "$b" install
    vendor_headers "$b/inst/include/speex" "$DEPS/speexdsp/headers/speex"
}

build_usrsctp() {
    log "usrsctp (static)"
    ensure_src USRSCTP_SRC "$USRSCTP_REPO" "$USRSCTP_REF" usrsctp
    local b="$BUILD/build-usrsctp"
    rm -rf "$b" && mkdir -p "$b"
    cmake -S "$USRSCTP_SRC" -B "$b" \
        -DCMAKE_INSTALL_PREFIX="$b/inst" \
        -DCMAKE_BUILD_TYPE=Release \
        -DBUILD_SHARED_LIBS=OFF \
        -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
        -DCMAKE_C_FLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        -Dsctp_build_programs=OFF \
        -Dsctp_build_tests=OFF
    cmake --build "$b" -j "$JOBS"
    cmake --install "$b"
    vendor_headers "$b/inst/include" "$DEPS/usrsctp/headers"
    sed -i 's|void (\*)(const char \*format, \.\.\.)|void (*)(const char *format)|g' "$DEPS/usrsctp/headers/usrsctp.h"
}

build_libvpx() {
    log "libvpx (static)"
    ensure_src LIBVPX_SRC "$LIBVPX_REPO" "$LIBVPX_REF" libvpx
    local target
    case "$OS-$ARCH" in
        linux-x86_64)   target=x86_64-linux-gcc ;;
        linux-aarch64)  target=arm64-linux-gcc ;;
        windows-x86_64) target=x86_64-win64-gcc ;;
        windows-aarch64) target=arm64-win64-gcc ;;
        darwin-x86_64)
            local dv; dv=$(uname -r | cut -d. -f1)
            target="x86_64-darwin${dv}-gcc"
            ;;
        darwin-aarch64)
            local dv; dv=$(uname -r | cut -d. -f1)
            target="arm64-darwin${dv}-gcc"
            ;;
    esac
    local b="$BUILD/build-libvpx"
    rm -rf "$b" && mkdir -p "$b"
    ( cd "$b" && CFLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        "$LIBVPX_SRC/configure" \
        --target="$target" --prefix="$b/inst" \
        --disable-shared --enable-static --enable-pic \
        --enable-vp8 --enable-vp8-encoder --enable-vp8-decoder \
        --disable-vp9 --disable-vp9-encoder --disable-vp9-decoder \
        --disable-examples --disable-tools --disable-docs --disable-unit-tests )
    make -C "$b" -j "$JOBS"
    make -C "$b" install
    vendor_headers "$b/inst/include/vpx" "$DEPS/libvpx/headers/vpx"
}

build_libwebp() {
    log "libwebp (static)"
    ensure_src LIBWEBP_SRC "$LIBWEBP_REPO" "$LIBWEBP_REF" libwebp
    [ -x "$LIBWEBP_SRC/configure" ] || ( cd "$LIBWEBP_SRC" && ./autogen.sh )
    local b="$BUILD/build-libwebp"
    rm -rf "$b" && mkdir -p "$b"
    ( cd "$b" && CFLAGS="${CFLAGS:-} $EXTRA_CFLAGS" \
        "$LIBWEBP_SRC/configure" --prefix="$b/inst" \
        --disable-shared --enable-static --with-pic \
        --disable-libwebpmux --disable-libwebpdemux \
        --disable-cwebp --disable-dwebp \
        --disable-png --disable-jpeg --disable-tiff --disable-gif --disable-wic )
    make -C "$b" -j "$JOBS"
    make -C "$b" install
    vendor_headers "$b/inst/include/webp" "$DEPS/libwebp/headers/webp"
}

vendor_ffmpeg_headers() {
    local inst_include="$1"
    local dest="$DEPS/ffmpeg/headers"
    for lib in libavformat libavcodec libavdevice libavfilter libavutil libswscale libswresample; do
        local src="$inst_include/$lib"
        local out="$dest/$lib"
        [ -d "$src" ] || continue
        rm -rf "$out" && mkdir -p "$out"
        cp "$src"/*.h "$out/"
    done
    if [ -f "$FFMPEG_SRC/VERSION" ]; then
        cp "$FFMPEG_SRC/VERSION" "$dest/UPSTREAM_VERSION"
    elif [ -f "$FFMPEG_SRC/RELEASE" ]; then
        cp "$FFMPEG_SRC/RELEASE" "$dest/UPSTREAM_VERSION"
    fi
}

# Path to ffmpeg's install prefix, shared between build_ffmpeg (writer) and
# build_combined (reader, for the pkg-config closure).
FFMPEG_INST="$BUILD/build-ffmpeg/build/inst"
# Path to the pkg-config shims pointing at the codec static archives.
FFMPEG_PC_DIR="$BUILD/build-ffmpeg/pc"

build_ffmpeg() {
    log "ffmpeg (static)"
    ensure_src FFMPEG_SRC "$FFMPEG_REPO" "$FFMPEG_REF" ffmpeg
    local indevs=()
    case "$OS" in
        linux)   indevs+=(--enable-indev=v4l2 --enable-indev=kmsgrab --enable-indev=xcbgrab) ;;
        darwin)  indevs+=(--enable-indev=avfoundation) ;;
        windows) indevs+=(--enable-indev=dshow --enable-indev=gdigrab) ;;
    esac
    rm -rf "$FFMPEG_PC_DIR" && mkdir -p "$FFMPEG_PC_DIR"
    local opus_inst="$BUILD/build-opus/inst"
    local vpx_inst="$BUILD/build-libvpx/inst"
    local webp_inst="$BUILD/build-libwebp/inst"
    local h264_stage="$BUILD/build-openh264/stage"
    rm -rf "$h264_stage" && mkdir -p "$h264_stage/lib" "$h264_stage/include/wels"
    cp "$OPENH264_SRC/libopenh264.a" "$h264_stage/lib/"
    cp "$OPENH264_SRC"/codec/api/wels/*.h "$h264_stage/include/wels/"
    # $6 (optional) lists the static-link private dependencies of $lib. FFmpeg
    # reads Libs.private because we pass --pkg-config-flags=--static, so its
    # dependency probe links the static archive together with the C/C++ runtime
    # and math/thread libraries the archive references but does not itself provide.
    emit_pc() {
        local n="$1" lib="$2" ver="$3" libdir="$4" inc="$5" priv="${6:-}"
        cat > "$FFMPEG_PC_DIR/${n}.pc" <<EOF
libdir=$libdir
includedir=$inc

Name: $n
Description: $n
Version: $ver
Libs: -L\${libdir} -l$lib
Libs.private: $priv
Cflags: -I\${includedir}
EOF
    }
    emit_pc opus     opus     1.5.2  "$opus_inst/lib"  "$opus_inst/include/opus" "-lm"
    emit_pc vpx      vpx      1.15.1 "$vpx_inst/lib"   "$vpx_inst/include"       "-lm -lpthread"
    emit_pc openh264 openh264 2.4.1  "$h264_stage/lib" "$h264_stage/include"     "$CXXLIB -lm"
    emit_pc libwebp  webp     1.5.0  "$webp_inst/lib"  "$webp_inst/include"      "-lsharpyuv -lm -lpthread"

    local b="$BUILD/build-ffmpeg/build"
    rm -rf "$b" && mkdir -p "$b"
    ( cd "$b" && PKG_CONFIG_LIBDIR="$FFMPEG_PC_DIR" "$FFMPEG_SRC/configure" \
        --prefix="$b/inst" \
        --extra-cflags="${CFLAGS:-} $EXTRA_CFLAGS" \
        --disable-everything --disable-programs \
        --disable-doc --disable-htmlpages --disable-manpages --disable-podpages --disable-txtpages \
        --disable-network --enable-static --disable-shared \
        --enable-pic --enable-lto \
        --disable-iconv \
        --pkg-config-flags=--static \
        --enable-demuxer=mov,matroska,ogg,mp3,wav,flac,mp4,aac,image2,webp_pipe,jpeg_pipe \
        --enable-muxer=matroska,mov,wav,mp4,ipod,webp,image2,ogg,opus \
        --enable-parser=h264,aac,mpegaudio,vp8,opus \
        --enable-protocol=file \
        --enable-filter=scale,format,fps,crop,transpose,thumbnail \
        --enable-swscale --enable-swresample \
        --enable-avdevice \
        "${indevs[@]}" \
        --enable-encoder=mjpeg,aac \
        --enable-decoder=mjpeg,aac,flac,mp3,pcm_s16le,pcm_s16be,pcm_u8,vorbis \
        --enable-libopenh264 --enable-encoder=libopenh264 --enable-decoder=libopenh264 \
        --enable-libopus    --enable-encoder=libopus    --enable-decoder=libopus    \
        --enable-libvpx     --enable-encoder=libvpx_vp8 --enable-decoder=libvpx_vp8 \
        --enable-libwebp    --enable-encoder=libwebp    --enable-decoder=libwebp    \
        --disable-decoder=h264,opus,vp8 \
        --disable-encoder=vp9 )
    make -C "$b" -j "$JOBS"
    make -C "$b" install
    vendor_ffmpeg_headers "$b/inst/include"
}

build_mdbx() {
    log "mdbx (static)"
    ensure_src MDBX_SRC "$MDBX_REPO" "$MDBX_REF" mdbx
    local dist="$MDBX_SRC"
    [ -f "$dist/mdbx.c" ] || fail "mdbx amalgamated source (mdbx.c) not found under $MDBX_SRC"
    local b="$BUILD/build-mdbx"
    rm -rf "$b" && mkdir -p "$b"
    local cc="${CC:-cc}"
    local wrap="$DEPS/libmdbx/mdbx_openu.c"
    # No LIBMDBX_EXPORTS / MDBX_BUILD_SHARED_LIBRARY here: this is a static
    # archive feeding the combined library, whose exports are governed by the
    # version-script / .def, not by mdbx's own dllexport macros.
    "$cc" -O3 -DNDEBUG $EXTRA_CFLAGS -I "$dist" -c "$dist/mdbx.c" -o "$b/mdbx.o"
    "$cc" -O3 -DNDEBUG $EXTRA_CFLAGS -I "$dist" -c "$wrap"        -o "$b/mdbx_openu.o"
    ar rcs "$b/libmdbx.a" "$b/mdbx.o" "$b/mdbx_openu.o"
    vendor_headers "$dist" "$DEPS/libmdbx/headers"
}

# Derives the combined library's export symbol set into the build-scratch file
# $1 from the per-dep generate.sh --include-function flags. That list is exactly
# what the FFM bindings resolve at runtime through SymbolLookup.loaderLookup(), so
# it is exactly what the combined library must export (and nothing more, so the
# linker can dead-strip the rest). It is a pure build intermediate derived from
# committed inputs, so it is regenerated every build and never committed. netmonitor
# is excluded: it uses generate-{linux,macos,windows} scripts (not generate.sh) and
# binds OS libraries resolved through the native default lookup, never shipped by Cobalt.
gen_exports() {
    local out="$1"
    local gens
    mapfile -t gens < <(find "$DEPS" -maxdepth 2 -name generate.sh -type f | sort)
    [ "${#gens[@]}" -gt 0 ] || fail "no generate.sh under $DEPS/*/ to derive exports from"
    local g
    for g in "${gens[@]}"; do
        awk '{ for (i = 1; i <= NF; i++) if ($i == "--include-function") print $(i + 1) }' "$g"
    done | sort -u > "$out"
    [ -s "$out" ] || fail "gen_exports produced no symbols"
    log "exports: $(wc -l < "$out" | tr -d ' ') symbols -> $out"
}

# Writes the platform-specific export-control file from exports.txt and echoes
# its path. ELF: a version script. Mach-O: an _underscored symbol list. PE: a
# .def EXPORTS block.
write_export_file() {
    local exports="$1" b="$2"
    case "$OS" in
        linux)
            local vs="$b/exports.map"
            { echo '{'; echo '  global:'; sed 's/^/    /; s/$/;/' "$exports"; \
              echo '  local:'; echo '    *;'; echo '};'; } > "$vs"
            echo "$vs"
            ;;
        darwin)
            local sl="$b/exports.syms"
            sed 's/^/_/' "$exports" > "$sl"
            echo "$sl"
            ;;
        windows)
            local def="$b/exports.def"
            { echo 'EXPORTS'; cat "$exports"; } > "$def"
            echo "$def"
            ;;
    esac
}

build_combined() {
    log "combined libcobalt-native"
    local b="$BUILD/build-combined"
    rm -rf "$b" && mkdir -p "$b"

    local exports="$b/exports.txt"
    gen_exports "$exports"

    # Static archives that are NOT part of ffmpeg's pkg-config closure; ffmpeg's
    # closure (resolved via --static below) already drags in opus/vpx/openh264/webp.
    local extra_archives=(
        "$BUILD/build-speexdsp/inst/lib/libspeexdsp.a"
        "$BUILD/build-usrsctp/inst/lib/libusrsctp.a"
        "$BUILD/build-mdbx/libmdbx.a"
    )
    local a
    for a in "${extra_archives[@]}"; do
        [ -f "$a" ] || fail "missing static archive: $a"
    done

    # ffmpeg's complete static link closure (libav* + the codec archives wired
    # through the pkg-config shims + every system library ffmpeg needs).
    local ff_libs
    ff_libs=$(PKG_CONFIG_PATH="$FFMPEG_INST/lib/pkgconfig:$FFMPEG_PC_DIR" \
        pkg-config --static --libs \
        libavdevice libavfilter libavformat libavcodec libswscale libswresample libavutil) \
        || fail "pkg-config failed to resolve ffmpeg static closure"

    # Force each bound symbol in as a link root so its archive member is pulled
    # even when ffmpeg itself never references it (e.g. the realtime opus/vpx/h264
    # paths Cobalt drives directly). gc-sections then drops everything else.
    local uflags
    case "$OS" in
        darwin) uflags=$(sed 's/^/-Wl,-u,_/' "$exports" | tr '\n' ' ') ;;
        *)      uflags=$(sed 's/^/-Wl,-u,/'  "$exports" | tr '\n' ' ') ;;
    esac

    local expfile; expfile=$(write_export_file "$exports" "$b")
    local cxx="${CXX:-c++}"
    local out

    case "$OS" in
        linux)
            out="$b/libcobalt-native.so"
            # shellcheck disable=SC2086
            "$cxx" -shared -fPIC $uflags \
                -Wl,--version-script="$expfile" \
                -Wl,--gc-sections -Wl,--no-undefined \
                -Wl,-soname,libcobalt-native.so \
                -static-libgcc -static-libstdc++ \
                -Wl,--start-group \
                    "${extra_archives[@]}" $ff_libs \
                -Wl,--end-group \
                -lpthread -lm -ldl \
                -o "$out"
            strip --strip-unneeded "$out"
            ;;
        darwin)
            out="$b/libcobalt-native.dylib"
            # shellcheck disable=SC2086
            "$cxx" -dynamiclib -fPIC $uflags \
                -Wl,-exported_symbols_list,"$expfile" \
                -Wl,-dead_strip \
                -Wl,-install_name,@rpath/libcobalt-native.dylib \
                "${extra_archives[@]}" $ff_libs \
                -framework CoreFoundation -framework CoreMedia \
                -framework CoreVideo -framework AVFoundation \
                -framework AudioToolbox -framework VideoToolbox \
                -framework CoreServices -framework Security \
                -lc++ -lm \
                -o "$out"
            strip -x "$out"
            ;;
        windows)
            out="$b/cobalt-native.dll"
            # shellcheck disable=SC2086
            "$cxx" -shared $uflags \
                "$expfile" \
                -Wl,--gc-sections -Wl,--no-undefined \
                -static-libgcc -static-libstdc++ \
                -Wl,-Bstatic -lpthread -Wl,-Bdynamic \
                -Wl,--start-group \
                    "${extra_archives[@]}" $ff_libs \
                -Wl,--end-group \
                -lws2_32 -liphlpapi -lntdll -lwinmm -lbcrypt -lsecur32 -lole32 -loleaut32 -lstrmiids -luuid -lgdi32 \
                -o "$out"
            strip -s "$out"
            ;;
    esac

    [ -f "$out" ] || fail "combined library not produced: $out"
    local dest="$NATIVES/bin/$CLASSIFIER"
    mkdir -p "$dest"
    find "$dest" -maxdepth 1 \( -type f -o -type l \) ! -name '.gitkeep' -delete 2>/dev/null || true
    cp "$out" "$dest/$(basename "$out")"
    log "wrote $dest/$(basename "$out") ($(wc -c < "$out" | tr -d ' ') bytes)"
}

build_opus
build_openh264
build_speexdsp
build_usrsctp
build_libvpx
build_libwebp
build_ffmpeg
build_mdbx
build_combined
log "done $CLASSIFIER"
