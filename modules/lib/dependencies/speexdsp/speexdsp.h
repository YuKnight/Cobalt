/* Aggregator header passed to jextract for speexdsp.
 * Includes both the AEC and preprocess (AGC/NS/VAD) headers; the
 * resampler and jitter buffer are out of Cobalt's scope and excluded
 * via the jextract-maven-plugin's includeFunctions whitelist. */
#include "speex/speex_echo.h"
#include "speex/speex_preprocess.h"
