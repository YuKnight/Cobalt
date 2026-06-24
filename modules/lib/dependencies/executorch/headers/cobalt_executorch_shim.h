/*
 * cobalt_executorch_shim.h
 *
 * Portable extern-C facade over ExecuTorch (the PyTorch Edge runtime) plus the
 * XNNPACK backend, for the Cobalt calls2 machine-learning bandwidth-estimation
 * stack. It re-exposes only the ExecuTorch surface the BWE inference path uses,
 * through PORTABLE SCALAR TYPES ONLY (the fixed-width <stdint.h> integers, float,
 * and opaque void*), so the jextract-generated Java binding is identical on every
 * host ABI.
 *
 * Why this shim exists: the wa-voip engine never calls the ExecuTorch C++ API
 * directly; it goes through a thin extern-C shim (ml_shim_*) that dispatches onto
 * a dynamically loaded ExecuTorch runtime through a function pointer
 * (create_executorch_dynamic_shim). The Cobalt port mirrors that ml_shim_*
 * surface 1:1 but COLLAPSES the dynamic-loading indirection: it links ExecuTorch
 * and XNNPACK statically into cobalt-native, so ml_shim_create_executorch calls
 * the ExecuTorch program loader and the XNNPACK backend directly. The Java
 * ExecuTorch.java binding never sees any of that; it only sees the four extern-C
 * entry points below. The ExecuTorch C++ types (Module, EValue, TensorImpl,
 * Program) are opaque structs whose size differs per build and whose API exchanges
 * std::vector / c10 shapes no portable binding can read; hiding every ExecuTorch
 * object behind one opaque void* handle and exchanging tensors as flat float32
 * arrays keeps the generated binding free of any ABI-sensitive or template shape.
 *
 * Reverse-engineering provenance (cwd-relative to re/calls/out/ff-tScznZ8P-full4/):
 * the four entry points mirror tree/xplat/wa-voip/wacall/network/src/bwe/bwe_ml.cc
 * fn4327 (create), fn4328 + fn4331 (forward), fn4330 (free), fn4335 (status), and
 * the dynamic runtime create_executorch_dynamic_shim is
 * tree/xplat/executorch/extension/fb/dynamic_shim/dynamic_shim.cpp fn693. See
 * re/calls2-spec/ML-BWE-RE.md section 1 for the full citation map.
 *
 * Tensor convention: the wa-voip forward (fn4331) builds a 2-D input tensor of
 * shape (dim0=rounds, dim1=features) that is row-major contiguous (XNNPACK
 * requires the last-dim stride to be 1). The Cobalt shim flattens that to one
 * contiguous input_len = rounds*features float array; the output is the model's
 * numel float values (for the congestion model, 2). The dtype on both sides is
 * float32.
 *
 * Symbol naming: every exported symbol is prefixed ml_ exactly as the wa-voip
 * shim names them (ml_shim_create_executorch / ml_shim_forward / ml_shim_free /
 * ml_get_shim_create_status), so the Cobalt names match the reversed source.
 *
 * Portability rule for this header: it uses ONLY int32_t/float/char/void*. It
 * never names an ExecuTorch type, and never uses bare `long`, `unsigned long` or
 * `long double`.
 */

#ifndef COBALT_EXECUTORCH_SHIM_H
#define COBALT_EXECUTORCH_SHIM_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Shim status codes returned by ml_get_shim_create_status and folded into the
 * negative error returns of ml_shim_forward. These remap the wa-voip engine's
 * model-state bytes (model+0x10 / model+0x14 in fn4327/fn4335) onto small stable
 * ints so the Java binding does not depend on the internal layout:
 *
 *   COBALT_ET_OK           0   the shim is created and the forward method loaded
 *   COBALT_ET_ERR_OPEN   (-1)  the .pte model file could not be opened
 *                              (RE "model not found", model+0x14 = 4)
 *   COBALT_ET_ERR_LOAD   (-2)  the program or the forward method failed to load
 *                              (RE state 5; fn4328 "failed to load forward method")
 *   COBALT_ET_ERR_BACKEND(-3)  XNNPACK init/register failed
 *                              (RE "Failed to initialize, XNNPACK status: 0x%x")
 *   COBALT_ET_ERR_RUNTIME(-4)  the forward execution itself failed
 */
#define COBALT_ET_OK            0
#define COBALT_ET_ERR_OPEN    (-1)
#define COBALT_ET_ERR_LOAD    (-2)
#define COBALT_ET_ERR_BACKEND (-3)
#define COBALT_ET_ERR_RUNTIME (-4)

/**
 * Loads an ExecuTorch .pte program from a filesystem path and returns an opaque
 * model handle.
 *
 * Mirrors ml_shim_create_executorch (fn4327): selects the ExecuTorch backend,
 * builds the program from model_path through the ExecuTorch program loader, and
 * (in the Cobalt static build) initialises and registers the XNNPACK backend. The
 * forward method is resolved lazily on the first ml_shim_forward, as fn4328 does.
 * The returned handle is opaque; on failure the function returns NULL and
 * ml_get_shim_create_status carries the reason.
 *
 * @param model_path the NUL-terminated filesystem path of the .pte program; must
 *                   not be NULL.
 * @return an opaque model handle on success, or NULL on failure (the reason is
 *         readable through ml_get_shim_create_status).
 */
void *ml_shim_create_executorch(const char *model_path);

/**
 * Runs one forward inference and copies the output tensor's float values into the
 * caller buffer.
 *
 * Mirrors fn4331: builds a row-major-contiguous 2-D float32 input tensor from the
 * flat input array, invokes the cached forward method, validates that the output
 * tensor holds exactly the expected number of elements, and copies that many
 * float32 values into output. The caller flattens the (rounds, features) 2-D
 * input to input_len = rounds*features contiguous floats; the wa-voip tensor is
 * row-major so the flatten is a straight copy.
 *
 * The number of output floats is the model's numel; the caller must size output
 * accordingly (the congestion model emits 2). A negative return is one of the
 * COBALT_ET_ERR_* codes; in particular the value mirrors fn4331's "Classification
 * model output size %d does not match expected %d" guard when the model's numel
 * does not match output_capacity.
 *
 * @param model           the handle from ml_shim_create_executorch; must not be
 *                        NULL and must be successfully created.
 * @param input           the flattened row-major input of input_len float32
 *                        values.
 * @param input_len       the number of input float32 values (rounds*features).
 * @param output          the buffer the output float32 values are written into.
 * @param output_capacity the capacity of output in float32 elements, which must
 *                        equal the model's numel.
 * @return the number of output float32 values written (>= 0) on success, or a
 *         negative COBALT_ET_ERR_* code on failure.
 */
int32_t ml_shim_forward(void *model, const float *input, int32_t input_len, float *output, int32_t output_capacity);

/**
 * Releases a model handle created by ml_shim_create_executorch.
 *
 * Mirrors ml_shim_free (fn4330): NULL-safe, and on a loaded handle invokes the
 * ExecuTorch destructor for the program and method. Must be called at most once
 * per handle.
 *
 * @param model the handle, or NULL.
 */
void ml_shim_free(void *model);

/**
 * Returns the creation/readiness status of the most recently created model.
 *
 * Mirrors ml_get_shim_create_status (fn4335): COBALT_ET_OK when the program is
 * loaded and the forward method is present, otherwise one of the
 * COBALT_ET_ERR_* codes describing why the load failed. The wa-voip loader polls
 * this immediately after ml_shim_create_executorch (fn4361) to decide whether the
 * model is usable.
 *
 * @return COBALT_ET_OK on success, or a negative COBALT_ET_ERR_* code.
 */
int32_t ml_get_shim_create_status(void);

#ifdef __cplusplus
}
#endif

#endif /* COBALT_EXECUTORCH_SHIM_H */
