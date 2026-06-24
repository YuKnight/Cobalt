package com.github.auties00.cobalt.calls2.net.bwe;

import java.util.Set;

/**
 * The disabled machine-learning bandwidth-estimation engine: loads no model and always reports no
 * machine-learning signal, so the pure delay-based and sender-side estimator runs standalone.
 *
 * <p>This is the default engine. {@link #loadedModels()} is always empty, {@link #infer(MlBweSignals)}
 * returns {@link MlBweOutputs#DISABLED}, and {@link #close()} does nothing, so the combiner and rate
 * control behave as if no model exists.
 *
 * <p>The engine is stateless and therefore safe to share across threads, though the call session drives
 * one from the single transport thread by convention.
 *
 * @implNote This realizes the {@code should_load_* == false} path of the wa-voip engine
 * ({@code bwe/bwe_ml.cc}), where every model is gated off and the estimator runs without inference
 * (re/calls2-spec/ML-BWE-RE.md sec 2).
 */
public final class NoopMlBweEngine implements MlBweEngine {
    /**
     * Constructs the disabled engine.
     */
    public NoopMlBweEngine() {
    }

    /**
     * {@inheritDoc}
     *
     * @return always an empty set
     */
    @Override
    public Set<MlBweModelType> loadedModels() {
        return Set.of();
    }

    /**
     * {@inheritDoc}
     *
     * @param signals ignored; the disabled engine runs no model
     * @return always {@link MlBweOutputs#DISABLED}
     */
    @Override
    public MlBweOutputs infer(MlBweSignals signals) {
        return MlBweOutputs.DISABLED;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Does nothing, since the disabled engine holds no resources.
     */
    @Override
    public void close() {
    }
}
