#!/usr/bin/env bash
#
# Builds a minimal FFmpeg shared-library set for the running host's
# classifier and drops the resulting .so / .dylib / .dll into
# modules/call-toolkit/dependencies/ffmpeg/bin/<classifier>/.
#
# What's enabled (justification):
#   - decoders aac/flac/mp3/pcm_s16le/pcm_s16be/pcm_u8/h264/opus/vorbis
#       — covers the formats VideoFileSource / AudioFileSource will be
#         asked to decode for call-bridge / call-recording flows
#   - demuxers mov/matroska/ogg/mp3/wav/flac/mp4/aac
#       — the corresponding container set
#   - muxers matroska/mov/wav
#       — what CallRecorder / VideoFileSink can output. MKV is the
#         primary recording target because it can mux Opus + VP8/H.264
#         without re-encoding (matches WhatsApp's wire codecs)
#   - input devices avfoundation/dshow/gdigrab/v4l2/kmsgrab/xcbgrab/
#                   pipewiregrab
#       — power CameraVideoSource / ScreenVideoSource per platform
#   - filters scale/format/fps + libswscale + libswresample
#       — runtime resampling and pixel-format conversion
#
# What's disabled:
#   - all encoders (we already ship libopus, libvpx, openh264,
#     libspeexdsp via the lib module — re-encoding through libavcodec
#     would only add weight)
#   - all networking protocols (libavformat must never open URLs)
#   - all programs / docs / static libs / debug symbols
#
# Output: ~10–15 MB stripped per classifier, six shared libs:
#   libavformat libavcodec libavdevice libavutil libswscale libswresample
#
# Prereqs: a working FFmpeg checkout, plus per-platform toolchain
# (autoconf/yasm/nasm on every host; pkg-config + libpipewire-dev
# on Linux for screen capture).

set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$DIR/../../.." && pwd)"
TOOLKIT="$ROOT/modules/call-toolkit"

if [ -z "${FFMPEG_SRC:-}" ]; then
    echo "FFMPEG_SRC must point at a checked-out FFmpeg tree (e.g. https://github.com/FFmpeg/FFmpeg)" >&2
    exit 1
fi
if [ ! -d "$FFMPEG_SRC" ]; then
    echo "FFMPEG_SRC=$FFMPEG_SRC does not exist or is not a directory" >&2
    exit 1
fi

uname_s="$(uname -s)"
uname_m="$(uname -m)"
case "$uname_s" in
    Linux*)  os=linux ;;
    Darwin*) os=darwin ;;
    MINGW*|MSYS*|CYGWIN*) os=windows ;;
    *) echo "unsupported host OS: $uname_s" >&2; exit 1 ;;
esac
case "$uname_m" in
    x86_64|amd64) arch=x86_64 ;;
    aarch64|arm64) arch=aarch64 ;;
    *) echo "unsupported host arch: $uname_m" >&2; exit 1 ;;
esac
CLASSIFIER="${os}-${arch}"
DEST="$TOOLKIT/dependencies/ffmpeg/bin/$CLASSIFIER"
mkdir -p "$DEST"

# Per-OS device toggles — the indev option set is platform-specific.
indevs=()
extra_configure=()
case "$os" in
    linux)
        indevs+=(--enable-indev=v4l2 --enable-indev=kmsgrab --enable-indev=xcbgrab)
        if pkg-config --exists libpipewire-0.3 2>/dev/null; then
            indevs+=(--enable-indev=pipewiregrab --enable-libpipewire)
        fi
        ;;
    darwin)
        indevs+=(--enable-indev=avfoundation)
        ;;
    windows)
        indevs+=(--enable-indev=dshow --enable-indev=gdigrab)
        ;;
esac

cd "$FFMPEG_SRC"
make distclean 2>/dev/null || true

./configure \
    --prefix="$DEST/ffmpeg-install" \
    --disable-everything \
    --disable-programs \
    --disable-doc \
    --disable-htmlpages \
    --disable-manpages \
    --disable-podpages \
    --disable-txtpages \
    --disable-network \
    --disable-static \
    --enable-shared \
    --enable-pic \
    --enable-lto \
    --enable-decoder=aac,flac,mp3,pcm_s16le,pcm_s16be,pcm_u8,h264,opus,vorbis \
    --enable-demuxer=mov,matroska,ogg,mp3,wav,flac,mp4,aac \
    --enable-parser=h264,aac,mpegaudio \
    --enable-muxer=matroska,mov,wav \
    --enable-protocol=file,pipe \
    --enable-filter=scale,format,fps \
    --enable-swscale \
    --enable-swresample \
    --enable-avdevice \
    "${indevs[@]}" \
    "${extra_configure[@]}"

make -j "$(getconf _NPROCESSORS_ONLN 2>/dev/null || echo 4)"
make install

# Flatten the platform-specific install layout into bin/<classifier>/.
case "$os" in
    linux)
        find "$DEST/ffmpeg-install/lib" -maxdepth 2 -type f -name 'lib*.so*' -exec cp -P {} "$DEST/" \;
        find "$DEST/" -maxdepth 1 -type f -name 'lib*.so*' -exec strip --strip-unneeded {} + 2>/dev/null || true
        ;;
    darwin)
        find "$DEST/ffmpeg-install/lib" -maxdepth 2 -type f -name 'lib*.dylib' -exec cp -P {} "$DEST/" \;
        find "$DEST/" -maxdepth 1 -type f -name 'lib*.dylib' -exec strip -x {} + 2>/dev/null || true
        ;;
    windows)
        find "$DEST/ffmpeg-install/bin" -maxdepth 2 -type f -name '*.dll' -exec cp {} "$DEST/" \;
        ;;
esac

rm -rf "$DEST/ffmpeg-install"

echo "Wrote FFmpeg shared libraries to $DEST"
ls -la "$DEST"
