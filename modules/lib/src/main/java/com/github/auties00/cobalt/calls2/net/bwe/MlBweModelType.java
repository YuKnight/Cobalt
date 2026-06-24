package com.github.auties00.cobalt.calls2.net.bwe;

/**
 * Enumerates the machine-learning bandwidth-estimation model types the {@link MlBweEngine} can load,
 * each a distinct inference task in the sender-side rate-control pipeline.
 *
 * <p>Each model is a separate inference task loaded on demand and gated by its own
 * {@code cc_enable_ml_*_inference} voip-param, so a build runs only the subset its configuration
 * enables. The constants are the model set recovered from the wa-voip engine: the congestion classifier,
 * two undershoot detectors, two high-definition-targeting regressors, the trendline regressor, the
 * packet-loss-concealment predictor, the QuickHD ramp model, and the offline reinforcement-learning
 * estimator. The audio-quality models the same runtime also hosts (NADL, AutoMOS, video mean-opinion-score,
 * video super-resolution, noise suppression) are not sender-side bandwidth-estimation models and are not
 * enumerated here.
 *
 * @implNote This implementation enumerates the model types selected by the {@code should_load_*} flags in
 * the wa-voip engine ({@code bwe/bwe_ml.cc} fn4361 lines 7662-7720, logged together at fn4414 line 4165
 * "should_load_plc ... should_load_udst ... should_load_udst2 ... should_load_tr ... should_load_cong ...
 * should_load_hd_target ... should_load_hd_target2"), resolved to model names by
 * {@code get_model_loader_and_name_by_type} (fn4437). The externally-visible set is confirmed by the live
 * {@code wavoip_ml_bwe_*_model_download_versions} AB-props (re/calls2-spec/ML-BWE-RE.md sec 2). The
 * gating voip-param per constant is named on each (re/calls2-spec/ML-BWE-RE.md sec 2, sec 5).
 */
public enum MlBweModelType {
    /**
     * Congestion classification: predicts a congestion probability the rate controller acts on, gated by
     * {@code cc_enable_ml_cong_inference}.
     *
     * <p>This is the one fully-recovered inference path: the model emits two float32 outputs and the
     * second is scaled to a probability and compared against a per-call threshold to yield a binary
     * congestion verdict (re/calls2-spec/ML-BWE-RE.md sec 4).
     */
    CONG,

    /**
     * Undershoot detection: classifies whether the estimate has undershot link capacity, gated by
     * {@code cc_enable_ml_undershoot_inference}.
     */
    UNDERSHOOT,

    /**
     * Generic-congestion undershoot detection (the {@code udst2} model), gated by
     * {@code cc_enable_ml_undershoot2_inference}.
     */
    GC_UNDERSHOOT,

    /**
     * High-definition targeting: predicts the bitrate the link can sustain for high-definition video,
     * gated by {@code cc_enable_ml_hd_targeting_inference}.
     */
    HD_TARGET,

    /**
     * Generic-congestion high-definition targeting (the {@code hd_targeting2} model), gated by
     * {@code cc_enable_ml_hd_targeting2_inference}.
     */
    GC_HD_TARGET,

    /**
     * Trendline regression: a learned alternative to the delay-based trendline slope, gated by
     * {@code cc_enable_ml_tr_inference}.
     */
    TR,

    /**
     * Packet-loss concealment: predicts the loss ratio for concealment decisions, gated by
     * {@code cc_enable_ml_plc_inference}.
     *
     * <p>The PLC model is loaded and run by the audio jitter-buffer path rather than the sender
     * bandwidth-estimation loop, so its feature vector and output are not recovered from
     * {@code bwe/bwe_ml.cc} (re/calls2-spec/ML-BWE-RE.md sec 2).
     */
    PLC,

    /**
     * QuickHD: drives an instant high-definition ramp-up when the link can carry it, gated by the QuickHD
     * model namespace ({@code wavoip_ml_bwe_quickhd_model_download_versions}).
     */
    QUICKHD,

    /**
     * Offline reinforcement-learning bandwidth estimation: predicts a target bitrate consumed when
     * {@code cc_use_offline_rl_bwe_as_target} is set, gated by {@code cc_enable_offline_rl_bwe_inference}.
     */
    OFFLINE_RL
}
