#!/usr/bin/env bash
#
# Vendors FFmpeg's public headers from a checked-out source tree
# under modules/call-toolkit/dependencies/ffmpeg/headers/. Each
# library's headers go in their own subdirectory so jextract can
# resolve transitive `#include "libavformat/foo.h"` paths.
#
# Sources only the headers each library declares as installable in
# its Makefile (HEADERS = ...). FFmpeg's _internal.h variants and
# private helpers are skipped.
#
# Usage:
#   FFMPEG_SRC=/path/to/ffmpeg modules/call-toolkit/scripts/vendor-ffmpeg-headers.sh

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
DEST="${ROOT}/modules/call-toolkit/dependencies/ffmpeg/headers"

if [[ -z "${FFMPEG_SRC:-}" ]]; then
    echo "FFMPEG_SRC must point at a checked-out FFmpeg source tree" >&2
    exit 1
fi

# Parses the Makefile's HEADERS = ... continuation list and prints
# bare filenames one per line.
list_headers() {
    local mk="$1"
    awk '
        /^HEADERS[[:space:]]*=/ { capture = 1 }
        capture {
            # Strip the "HEADERS =" prefix on the first line
            sub(/^HEADERS[[:space:]]*=[[:space:]]*/, "", $0)
            # Stop when the line does not end with a backslash
            ends_with_bs = ($0 ~ /\\$/)
            sub(/\\$/, "", $0)
            for (i = 1; i <= NF; i++) print $i
            if (!ends_with_bs) exit
        }
    ' "$mk"
}

for lib in libavformat libavcodec libavdevice libavutil libswscale libswresample; do
    src="${FFMPEG_SRC}/${lib}"
    out="${DEST}/${lib}"
    if [[ ! -d "$src" ]]; then
        echo "skipping ${lib}: ${src} does not exist" >&2
        continue
    fi
    rm -rf "$out"
    mkdir -p "$out"
    count=0
    while IFS= read -r hdr; do
        [[ -z "$hdr" ]] && continue
        if [[ -f "${src}/${hdr}" ]]; then
            cp "${src}/${hdr}" "${out}/${hdr}"
            count=$((count + 1))
        fi
    done < <(list_headers "${src}/Makefile")
    echo "vendored ${count} headers for ${lib} → ${out}"
done

# Stamp the upstream version so the bindings can be regenerated
# against the same revision later.
if [[ -f "${FFMPEG_SRC}/VERSION" ]]; then
    cp "${FFMPEG_SRC}/VERSION" "${DEST}/UPSTREAM_VERSION"
fi
echo "done"
