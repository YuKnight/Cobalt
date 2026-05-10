#!/usr/bin/env bash
# Regenerates modules/call-toolkit/src/main/resources/META-INF/native-checksums.json
# from the FFmpeg shared libraries currently sitting under
# modules/call-toolkit/dependencies/ffmpeg/bin/<classifier>/.
#
# Captures the current HEAD commit SHA into the manifest's
# `commitSha` field; the loader pins runtime downloads to that SHA
# so the URL is immutable across tag deletions / retargeting.
#
# Each manifest entry is keyed `ffmpeg-<lib>/<classifier>` so the
# loader (which keys on `<libraryName>/<classifier>`) finds them
# via NativeLibLoader.load("ffmpeg-avformat", ...) etc.
#
# Release flow:
#   1) commit any new/updated FFmpeg binaries                       (commit A)
#   2) modules/call-toolkit/scripts/regenerate-natives-manifest.sh -- records commitSha=A
#   3) git add modules/call-toolkit/src/main/resources/META-INF/native-checksums.json
#      && git commit                                                (commit B)
#   4) git tag vX.Y.Z B && git push --follow-tags
#
# Output:
#   modules/call-toolkit/src/main/resources/META-INF/native-checksums.json

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
DEPS="${ROOT}/modules/call-toolkit/dependencies/ffmpeg/bin"
OUT="${ROOT}/modules/call-toolkit/src/main/resources/META-INF/native-checksums.json"

VERSION=$(grep -m1 '<version>' "${ROOT}/pom.xml" | sed 's/[<\/]*version[> ]*//g' | tr -d ' ')
COMMIT_SHA=$(git -C "${ROOT}" rev-parse HEAD)
echo "Building checksum manifest for cobalt-call-toolkit v${VERSION} @ ${COMMIT_SHA}"

# Logical lib name → expected base filename (the classifier-specific
# extension is appended below). We ship the same six FFmpeg components
# on every classifier; the actual on-disk filename gets a version
# suffix on Linux/macOS (libavformat.so.61) which the matcher handles
# by globbing.
LIBS=(avformat avcodec avdevice avutil swscale swresample)
CLASSIFIERS=(linux-x86_64 linux-aarch64 darwin-x86_64 darwin-aarch64 windows-x86_64 windows-aarch64)

ENTRIES=()
for classifier in "${CLASSIFIERS[@]}"; do
    bin_dir="${DEPS}/${classifier}"
    if [[ ! -d "${bin_dir}" ]]; then
        continue
    fi
    case "${classifier}" in
        linux-*)   prefix=lib; pattern='*.so*' ;;
        darwin-*)  prefix=lib; pattern='*.dylib' ;;
        windows-*) prefix=;    pattern='*.dll' ;;
    esac
    for lib in "${LIBS[@]}"; do
        # Find the actual on-disk file matching the lib name. The
        # version suffix means a glob is the only reliable matcher.
        matches=()
        while IFS= read -r -d '' f; do
            matches+=("$f")
        done < <(find "${bin_dir}" -maxdepth 1 -type f -name "${prefix}${lib}*${pattern#\*}" -print0 2>/dev/null || true)
        if [[ ${#matches[@]} -eq 0 ]]; then
            continue
        fi
        # Pick the file with the longest name — the soname (libfoo.so.N)
        # is what we want to ship, not a symlink alias (libfoo.so).
        binary="${matches[0]}"
        for m in "${matches[@]}"; do
            if (( ${#m} > ${#binary} )); then
                binary="$m"
            fi
        done
        sha=$(sha256sum "${binary}" | awk '{print $1}')
        size=$(wc -c < "${binary}" | tr -d ' ')
        rel="modules/call-toolkit/dependencies/ffmpeg/bin/${classifier}/$(basename "${binary}")"
        ENTRIES+=("    \"ffmpeg-${lib}/${classifier}\": { \"sha256\": \"${sha}\", \"size\": ${size}, \"path\": \"${rel}\" }")
    done
done

{
    echo '{'
    echo "  \"version\": \"${VERSION}\","
    echo "  \"commitSha\": \"${COMMIT_SHA}\","
    echo '  "binaries": {'
    last_idx=$(( ${#ENTRIES[@]} - 1 ))
    for i in "${!ENTRIES[@]}"; do
        if (( i == last_idx )); then
            echo "${ENTRIES[$i]}"
        else
            echo "${ENTRIES[$i]},"
        fi
    done
    echo '  }'
    echo '}'
} > "${OUT}"

echo "Wrote ${OUT} (${#ENTRIES[@]} entries)"
