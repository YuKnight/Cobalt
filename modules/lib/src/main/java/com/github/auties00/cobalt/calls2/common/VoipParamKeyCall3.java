package com.github.auties00.cobalt.calls2.common;

/**
 * A partition of the {@code p-&gt;} voip-param registry keys.
 *
 * <p>This enum exists only to keep its generated static initializer within the JVM 64KB
 * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}
 * rather than this partition directly.
 */
enum VoipParamKeyCall3 implements VoipParamKey {
    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.enable}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_ENABLE("p->random_forced_probing_param.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.enable_dl}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_ENABLE_DL("p->random_forced_probing_param.enable_dl", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.forced_inc_ratio}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_FORCED_INC_RATIO("p->random_forced_probing_param.forced_inc_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.max_probing_time_ms}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_MAX_PROBING_TIME_MS("p->random_forced_probing_param.max_probing_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.min_interval_ms}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_MIN_INTERVAL_MS("p->random_forced_probing_param.min_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.probation_after_stop_ms}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_PROBATION_AFTER_STOP_MS("p->random_forced_probing_param.probation_after_stop_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;random_forced_probing_param.random_probing_rate}.
     */
    P_RANDOM_FORCED_PROBING_PARAM_RANDOM_PROBING_RATE("p->random_forced_probing_param.random_probing_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ratecontrol_maxrtt}.
     */
    P_RATECONTROL_MAXRTT("p->ratecontrol_maxrtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_init_bps}.
     */
    P_RBE_INIT_BPS("p->rbe_init_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_init_bps_use_vector}.
     */
    P_RBE_INIT_BPS_USE_VECTOR("p->rbe_init_bps_use_vector", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_init_bps_vector}.
     */
    P_RBE_INIT_BPS_VECTOR("p->rbe_init_bps_vector", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_instant_ramp_min_bps}.
     */
    P_RBE_INSTANT_RAMP_MIN_BPS("p->rbe_instant_ramp_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_instant_ramp_min_bps_vector}.
     */
    P_RBE_INSTANT_RAMP_MIN_BPS_VECTOR("p->rbe_instant_ramp_min_bps_vector", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_instant_ramp_target_bps}.
     */
    P_RBE_INSTANT_RAMP_TARGET_BPS("p->rbe_instant_ramp_target_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_instant_ramp_target_bps_vector}.
     */
    P_RBE_INSTANT_RAMP_TARGET_BPS_VECTOR("p->rbe_instant_ramp_target_bps_vector", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_instant_ramp_use_vector}.
     */
    P_RBE_INSTANT_RAMP_USE_VECTOR("p->rbe_instant_ramp_use_vector", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_network_fallback}.
     */
    P_RBE_NETWORK_FALLBACK("p->rbe_network_fallback", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_history_cap}.
     */
    P_RBE_TARGETING_VECTOR_HISTORY_CAP("p->rbe_targeting_vector_history_cap", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_history_threshold}.
     */
    P_RBE_TARGETING_VECTOR_HISTORY_THRESHOLD("p->rbe_targeting_vector_history_threshold", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_main}.
     */
    P_RBE_TARGETING_VECTOR_MAIN("p->rbe_targeting_vector_main", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_ml_cap}.
     */
    P_RBE_TARGETING_VECTOR_ML_CAP("p->rbe_targeting_vector_ml_cap", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_pp_flip_count_upper_bound}.
     */
    P_RBE_TARGETING_VECTOR_PP_FLIP_COUNT_UPPER_BOUND("p->rbe_targeting_vector_pp_flip_count_upper_bound", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_pp_high_cap}.
     */
    P_RBE_TARGETING_VECTOR_PP_HIGH_CAP("p->rbe_targeting_vector_pp_high_cap", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_pp_high_threshold}.
     */
    P_RBE_TARGETING_VECTOR_PP_HIGH_THRESHOLD("p->rbe_targeting_vector_pp_high_threshold", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_pp_low_cap}.
     */
    P_RBE_TARGETING_VECTOR_PP_LOW_CAP("p->rbe_targeting_vector_pp_low_cap", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rbe_targeting_vector_pp_low_threshold}.
     */
    P_RBE_TARGETING_VECTOR_PP_LOW_THRESHOLD("p->rbe_targeting_vector_pp_low_threshold", VoipParamType.ARRAY, 64, false),

    /**
     * Native descriptor for {@code p-&gt;rd_interval_only_plr_rtt}.
     */
    P_RD_INTERVAL_ONLY_PLR_RTT("p->rd_interval_only_plr_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;real_audio_bps_quant}.
     */
    P_REAL_AUDIO_BPS_QUANT("p->real_audio_bps_quant", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;receive_side_congestion_drop_pct}.
     */
    P_RECEIVE_SIDE_CONGESTION_DROP_PCT("p->receive_side_congestion_drop_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;receiver_side_automos_model_name}.
     */
    P_RECEIVER_SIDE_AUTOMOS_MODEL_NAME("p->receiver_side_automos_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;reconfig_rbwe_min_max_bitrate_for_av_upgrade}.
     */
    P_RECONFIG_RBWE_MIN_MAX_BITRATE_FOR_AV_UPGRADE("p->reconfig_rbwe_min_max_bitrate_for_av_upgrade", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;recreate_audio_proc_route_change}.
     */
    P_RECREATE_AUDIO_PROC_ROUTE_CHANGE("p->recreate_audio_proc_route_change", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;remote_est_weight}.
     */
    P_REMOTE_EST_WEIGHT("p->remote_est_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;report_aud_rc_dyn_fs}.
     */
    P_REPORT_AUD_RC_DYN_FS("p->report_aud_rc_dyn_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;report_client_nadl_fs}.
     */
    P_REPORT_CLIENT_NADL_FS("p->report_client_nadl_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;report_vid_rc_dyn_fs}.
     */
    P_REPORT_VID_RC_DYN_FS("p->report_vid_rc_dyn_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;resample}.
     */
    P_RESAMPLE("p->resample", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reserve_warp_in_vid_enc_mtu}.
     */
    P_RESERVE_WARP_IN_VID_ENC_MTU("p->reserve_warp_in_vid_enc_mtu", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_features_bitmap}.
     */
    P_RESET_BWE_FEATURES_BITMAP("p->reset_bwe_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_min_bitrate}.
     */
    P_RESET_BWE_MIN_BITRATE("p->reset_bwe_min_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_on_network_change_vid}.
     */
    P_RESET_BWE_ON_NETWORK_CHANGE_VID("p->reset_bwe_on_network_change_vid", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_opt_bitmap}.
     */
    P_RESET_BWE_OPT_BITMAP("p->reset_bwe_opt_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_pp_probing_params.ramp_up_using_pp}.
     */
    P_RESET_BWE_PP_PROBING_PARAMS_RAMP_UP_USING_PP("p->reset_bwe_pp_probing_params.ramp_up_using_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;reset_bwe_pp_probing_params.skip_first_n_pp}.
     */
    P_RESET_BWE_PP_PROBING_PARAMS_SKIP_FIRST_N_PP("p->reset_bwe_pp_probing_params.skip_first_n_pp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_dl_bwe_min_bitrate}.
     */
    P_RESET_DL_BWE_MIN_BITRATE("p->reset_dl_bwe_min_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_enable_warp_sfu_simulcast}.
     */
    P_RESET_ENABLE_WARP_SFU_SIMULCAST("p->reset_enable_warp_sfu_simulcast", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_hbh_tx_srtp}.
     */
    P_RESET_HBH_TX_SRTP("p->reset_hbh_tx_srtp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reset_rbwe_for_av_upgrade}.
     */
    P_RESET_RBWE_FOR_AV_UPGRADE("p->reset_rbwe_for_av_upgrade", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;reset_sfu_bwe_bitmap}.
     */
    P_RESET_SFU_BWE_BITMAP("p->reset_sfu_bwe_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reuse_2p_info_init_sfu_dl_bwe_multiplier}.
     */
    P_REUSE_2P_INFO_INIT_SFU_DL_BWE_MULTIPLIER("p->reuse_2p_info_init_sfu_dl_bwe_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;reuse_2p_info_init_sfu_ul_bwe_multiplier}.
     */
    P_REUSE_2P_INFO_INIT_SFU_UL_BWE_MULTIPLIER("p->reuse_2p_info_init_sfu_ul_bwe_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rexmt_threshold_rtt_multiplier}.
     */
    P_REXMT_THRESHOLD_RTT_MULTIPLIER("p->rexmt_threshold_rtt_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.br_change_weight}.
     */
    P_RL_BWE_PARAMS_BR_CHANGE_WEIGHT("p->rl_bwe_params.br_change_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.br_weight}.
     */
    P_RL_BWE_PARAMS_BR_WEIGHT("p->rl_bwe_params.br_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.clamp_to_floor}.
     */
    P_RL_BWE_PARAMS_CLAMP_TO_FLOOR("p->rl_bwe_params.clamp_to_floor", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.delay_weight}.
     */
    P_RL_BWE_PARAMS_DELAY_WEIGHT("p->rl_bwe_params.delay_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.ema_n}.
     */
    P_RL_BWE_PARAMS_EMA_N("p->rl_bwe_params.ema_n", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.epoch_duration_ms}.
     */
    P_RL_BWE_PARAMS_EPOCH_DURATION_MS("p->rl_bwe_params.epoch_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.epsilon}.
     */
    P_RL_BWE_PARAMS_EPSILON("p->rl_bwe_params.epsilon", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.epsilon_decay}.
     */
    P_RL_BWE_PARAMS_EPSILON_DECAY("p->rl_bwe_params.epsilon_decay", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.exploration_factor}.
     */
    P_RL_BWE_PARAMS_EXPLORATION_FACTOR("p->rl_bwe_params.exploration_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.hold_prob}.
     */
    P_RL_BWE_PARAMS_HOLD_PROB("p->rl_bwe_params.hold_prob", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.jitter_weight}.
     */
    P_RL_BWE_PARAMS_JITTER_WEIGHT("p->rl_bwe_params.jitter_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.learn_only}.
     */
    P_RL_BWE_PARAMS_LEARN_ONLY("p->rl_bwe_params.learn_only", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.loss_weight}.
     */
    P_RL_BWE_PARAMS_LOSS_WEIGHT("p->rl_bwe_params.loss_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.reward_alpha}.
     */
    P_RL_BWE_PARAMS_REWARD_ALPHA("p->rl_bwe_params.reward_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.reward_gamma}.
     */
    P_RL_BWE_PARAMS_REWARD_GAMMA("p->rl_bwe_params.reward_gamma", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rl_bwe_params.use_simple_rampup}.
     */
    P_RL_BWE_PARAMS_USE_SIMPLE_RAMPUP("p->rl_bwe_params.use_simple_rampup", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rtcp_interval_ms}.
     */
    P_RTCP_INTERVAL_MS("p->rtcp_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtcp_rtt_high_threshold_ms}.
     */
    P_RTCP_RTT_HIGH_THRESHOLD_MS("p->rtcp_rtt_high_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtcp_to_ice_rtt_ratio_threshold}.
     */
    P_RTCP_TO_ICE_RTT_RATIO_THRESHOLD("p->rtcp_to_ice_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_consecutive_increase_count}.
     */
    P_RTT_CONGESTION_CONSECUTIVE_INCREASE_COUNT("p->rtt_congestion_consecutive_increase_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_slope_threshold}.
     */
    P_RTT_CONGESTION_SLOPE_THRESHOLD("p->rtt_congestion_slope_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_slope_window_size}.
     */
    P_RTT_CONGESTION_SLOPE_WINDOW_SIZE("p->rtt_congestion_slope_window_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_step_ema}.
     */
    P_RTT_CONGESTION_STEP_EMA("p->rtt_congestion_step_ema", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_step_highest}.
     */
    P_RTT_CONGESTION_STEP_HIGHEST("p->rtt_congestion_step_highest", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_congestion_step_previous}.
     */
    P_RTT_CONGESTION_STEP_PREVIOUS("p->rtt_congestion_step_previous", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rtt_slide_window_size}.
     */
    P_RTT_SLIDE_WINDOW_SIZE("p->rtt_slide_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_aec_followed_by_ns}.
     */
    P_RUN_AEC_FOLLOWED_BY_NS("p->run_aec_followed_by_ns", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_agc_first}.
     */
    P_RUN_AGC_FIRST("p->run_agc_first", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_bwe_on_sender_side}.
     */
    P_RUN_BWE_ON_SENDER_SIDE("p->run_bwe_on_sender_side", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;run_metrics_with_ml_ns}.
     */
    P_RUN_METRICS_WITH_ML_NS("p->run_metrics_with_ml_ns", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_ml_ns_first}.
     */
    P_RUN_ML_NS_FIRST("p->run_ml_ns_first", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_rx_agc}.
     */
    P_RUN_RX_AGC("p->run_rx_agc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;run_sbwe_on_simulcast_pause_type}.
     */
    P_RUN_SBWE_ON_SIMULCAST_PAUSE_TYPE("p->run_sbwe_on_simulcast_pause_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_init_delay_ms}.
     */
    P_RX_INIT_DELAY_MS("p->rx_init_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_interval_to_reset_inferences_ms}.
     */
    P_RX_INTERVAL_TO_RESET_INFERENCES_MS("p->rx_interval_to_reset_inferences_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_max_inferences_per_interval}.
     */
    P_RX_MAX_INFERENCES_PER_INTERVAL("p->rx_max_inferences_per_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_max_queue_size_for_async_mode}.
     */
    P_RX_MAX_QUEUE_SIZE_FOR_ASYNC_MODE("p->rx_max_queue_size_for_async_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_pkt_cache_capacity}.
     */
    P_RX_PKT_CACHE_CAPACITY("p->rx_pkt_cache_capacity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_pkt_cache_stale_threshold_ms}.
     */
    P_RX_PKT_CACHE_STALE_THRESHOLD_MS("p->rx_pkt_cache_stale_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_process_single_chunk_ms}.
     */
    P_RX_PROCESS_SINGLE_CHUNK_MS("p->rx_process_single_chunk_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_query_samples_to_skip}.
     */
    P_RX_QUERY_SAMPLES_TO_SKIP("p->rx_query_samples_to_skip", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rx_should_delay_inference}.
     */
    P_RX_SHOULD_DELAY_INFERENCE("p->rx_should_delay_inference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rx_should_limit_inferences_per_interval}.
     */
    P_RX_SHOULD_LIMIT_INFERENCES_PER_INTERVAL("p->rx_should_limit_inferences_per_interval", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rx_throttled_kbps}.
     */
    P_RX_THROTTLED_KBPS("p->rx_throttled_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;rx_use_executorch}.
     */
    P_RX_USE_EXECUTORCH("p->rx_use_executorch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rx_use_mutex_for_inference}.
     */
    P_RX_USE_MUTEX_FOR_INFERENCE("p->rx_use_mutex_for_inference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;rx_wait_time_for_speech_frames_ms}.
     */
    P_RX_WAIT_TIME_FOR_SPEECH_FRAMES_MS("p->rx_wait_time_for_speech_frames_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sample_interval_ms}.
     */
    P_SAMPLE_INTERVAL_MS("p->sample_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sampling_rate}.
     */
    P_SAMPLING_RATE("p->sampling_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_bursty_plr_rtt_lowerbound}.
     */
    P_SBWE_BURSTY_PLR_RTT_LOWERBOUND("p->sbwe_bursty_plr_rtt_lowerbound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_bw_delay_product}.
     */
    P_SBWE_BW_DELAY_PRODUCT("p->sbwe_bw_delay_product", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_clamp_by_link_cap_est}.
     */
    P_SBWE_CLAMP_BY_LINK_CAP_EST("p->sbwe_clamp_by_link_cap_est", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;sbwe_cong_decr_factor_fast_rr}.
     */
    P_SBWE_CONG_DECR_FACTOR_FAST_RR("p->sbwe_cong_decr_factor_fast_rr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_ignore_rnd_plr_cong}.
     */
    P_SBWE_IGNORE_RND_PLR_CONG("p->sbwe_ignore_rnd_plr_cong", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_min_fast_rr_plr_interval_ms}.
     */
    P_SBWE_MIN_FAST_RR_PLR_INTERVAL_MS("p->sbwe_min_fast_rr_plr_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_min_fast_rr_rtt_interval_ms}.
     */
    P_SBWE_MIN_FAST_RR_RTT_INTERVAL_MS("p->sbwe_min_fast_rr_rtt_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_nonzero_rtt_count_thr}.
     */
    P_SBWE_NONZERO_RTT_COUNT_THR("p->sbwe_nonzero_rtt_count_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_plr_cong_decr_factor}.
     */
    P_SBWE_PLR_CONG_DECR_FACTOR("p->sbwe_plr_cong_decr_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_plr_cong_unknown_as_rnd}.
     */
    P_SBWE_PLR_CONG_UNKNOWN_AS_RND("p->sbwe_plr_cong_unknown_as_rnd", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_ramp_down_target_lower_multiplier}.
     */
    P_SBWE_RAMP_DOWN_TARGET_LOWER_MULTIPLIER("p->sbwe_ramp_down_target_lower_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_ramp_down_target_lower_multiplier_pl}.
     */
    P_SBWE_RAMP_DOWN_TARGET_LOWER_MULTIPLIER_PL("p->sbwe_ramp_down_target_lower_multiplier_pl", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_ramp_down_target_lower_recent_thr_msec}.
     */
    P_SBWE_RAMP_DOWN_TARGET_LOWER_RECENT_THR_MSEC("p->sbwe_ramp_down_target_lower_recent_thr_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_random_plr_rtt_upperbound}.
     */
    P_SBWE_RANDOM_PLR_RTT_UPPERBOUND("p->sbwe_random_plr_rtt_upperbound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_reset_inflection_point}.
     */
    P_SBWE_RESET_INFLECTION_POINT("p->sbwe_reset_inflection_point", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_skip_rtt_slope_for_rtcp_rr}.
     */
    P_SBWE_SKIP_RTT_SLOPE_FOR_RTCP_RR("p->sbwe_skip_rtt_slope_for_rtcp_rr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_unknown_plr_enforced_by_rtt}.
     */
    P_SBWE_UNKNOWN_PLR_ENFORCED_BY_RTT("p->sbwe_unknown_plr_enforced_by_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_use_combined_rtt_slide_window}.
     */
    P_SBWE_USE_COMBINED_RTT_SLIDE_WINDOW("p->sbwe_use_combined_rtt_slide_window", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_use_decr_factor_for_plr_cong}.
     */
    P_SBWE_USE_DECR_FACTOR_FOR_PLR_CONG("p->sbwe_use_decr_factor_for_plr_cong", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_use_plr_classifier}.
     */
    P_SBWE_USE_PLR_CLASSIFIER("p->sbwe_use_plr_classifier", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sbwe_use_remote_est}.
     */
    P_SBWE_USE_REMOTE_EST("p->sbwe_use_remote_est", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_clear_rtcp_fresh_threshold_ms}.
     */
    P_SCTP_BUFFER_CLEAR_RTCP_FRESH_THRESHOLD_MS("p->sctp_buffer_clear_rtcp_fresh_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_congestion_decrease_factor}.
     */
    P_SCTP_BUFFER_CONGESTION_DECREASE_FACTOR("p->sctp_buffer_congestion_decrease_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_congestion_persistence_count}.
     */
    P_SCTP_BUFFER_CONGESTION_PERSISTENCE_COUNT("p->sctp_buffer_congestion_persistence_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_high_threshold_bytes}.
     */
    P_SCTP_BUFFER_HIGH_THRESHOLD_BYTES("p->sctp_buffer_high_threshold_bytes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_low_threshold_bytes}.
     */
    P_SCTP_BUFFER_LOW_THRESHOLD_BYTES("p->sctp_buffer_low_threshold_bytes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sctp_buffer_threshold_factor_ms}.
     */
    P_SCTP_BUFFER_THRESHOLD_FACTOR_MS("p->sctp_buffer_threshold_factor_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;send_audio_level}.
     */
    P_SEND_AUDIO_LEVEL("p->send_audio_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;send_piggyback_ext_when_enabled}.
     */
    P_SEND_PIGGYBACK_EXT_WHEN_ENABLED("p->send_piggyback_ext_when_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;send_video_fec_immediately}.
     */
    P_SEND_VIDEO_FEC_IMMEDIATELY("p->send_video_fec_immediately", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.abs_rtt_congestion_threshold}.
     */
    P_SENDER_BWE_PARAMS_ABS_RTT_CONGESTION_THRESHOLD("p->sender_bwe_params.abs_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.combine_policy}.
     */
    P_SENDER_BWE_PARAMS_COMBINE_POLICY("p->sender_bwe_params.combine_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.force_sbwe_follow_encoder}.
     */
    P_SENDER_BWE_PARAMS_FORCE_SBWE_FOLLOW_ENCODER("p->sender_bwe_params.force_sbwe_follow_encoder", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.max_target_bitrate}.
     */
    P_SENDER_BWE_PARAMS_MAX_TARGET_BITRATE("p->sender_bwe_params.max_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.min_target_bitrate}.
     */
    P_SENDER_BWE_PARAMS_MIN_TARGET_BITRATE("p->sender_bwe_params.min_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.receive_side_congestion_drop_pct}.
     */
    P_SENDER_BWE_PARAMS_RECEIVE_SIDE_CONGESTION_DROP_PCT("p->sender_bwe_params.receive_side_congestion_drop_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.sbwe_nonzero_rtt_count_thr}.
     */
    P_SENDER_BWE_PARAMS_SBWE_NONZERO_RTT_COUNT_THR("p->sender_bwe_params.sbwe_nonzero_rtt_count_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.sbwe_ramp_down_target_lower_multiplier}.
     */
    P_SENDER_BWE_PARAMS_SBWE_RAMP_DOWN_TARGET_LOWER_MULTIPLIER("p->sender_bwe_params.sbwe_ramp_down_target_lower_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.sender_side_rc_high_pkt_loss}.
     */
    P_SENDER_BWE_PARAMS_SENDER_SIDE_RC_HIGH_PKT_LOSS("p->sender_bwe_params.sender_side_rc_high_pkt_loss", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_bwe_params.sender_side_rc_low_pkt_loss}.
     */
    P_SENDER_BWE_PARAMS_SENDER_SIDE_RC_LOW_PKT_LOSS("p->sender_bwe_params.sender_side_rc_low_pkt_loss", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_automos_model_name}.
     */
    P_SENDER_SIDE_AUTOMOS_MODEL_NAME("p->sender_side_automos_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_bwe_slide_window_size}.
     */
    P_SENDER_SIDE_BWE_SLIDE_WINDOW_SIZE("p->sender_side_bwe_slide_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_init_rate_increase_factor_fr}.
     */
    P_SENDER_SIDE_INIT_RATE_INCREASE_FACTOR_FR("p->sender_side_init_rate_increase_factor_fr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_policy}.
     */
    P_SENDER_SIDE_POLICY("p->sender_side_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rate_decrease_factor}.
     */
    P_SENDER_SIDE_RATE_DECREASE_FACTOR("p->sender_side_rate_decrease_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_high_pkt_loss_dl}.
     */
    P_SENDER_SIDE_RC_HIGH_PKT_LOSS_DL("p->sender_side_rc_high_pkt_loss_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_high_pkt_loss_ul}.
     */
    P_SENDER_SIDE_RC_HIGH_PKT_LOSS_UL("p->sender_side_rc_high_pkt_loss_ul", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_low_pkt_loss_dl}.
     */
    P_SENDER_SIDE_RC_LOW_PKT_LOSS_DL("p->sender_side_rc_low_pkt_loss_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_low_pkt_loss_ul}.
     */
    P_SENDER_SIDE_RC_LOW_PKT_LOSS_UL("p->sender_side_rc_low_pkt_loss_ul", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_min_adjustment_interval_ms}.
     */
    P_SENDER_SIDE_RC_MIN_ADJUSTMENT_INTERVAL_MS("p->sender_side_rc_min_adjustment_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sender_side_rc_min_rd_adjustment_interval_ms}.
     */
    P_SENDER_SIDE_RC_MIN_RD_ADJUSTMENT_INTERVAL_MS("p->sender_side_rc_min_rd_adjustment_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sframe_cipher_suite}.
     */
    P_SFRAME_CIPHER_SUITE("p->sframe_cipher_suite", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sfu_dl_freeze_as_cong_signal_interval_ms}.
     */
    P_SFU_DL_FREEZE_AS_CONG_SIGNAL_INTERVAL_MS("p->sfu_dl_freeze_as_cong_signal_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sfu_dl_freeze_as_cong_signal_threshold_ms}.
     */
    P_SFU_DL_FREEZE_AS_CONG_SIGNAL_THRESHOLD_MS("p->sfu_dl_freeze_as_cong_signal_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sfu_reorder_tolerance_ms}.
     */
    P_SFU_REORDER_TOLERANCE_MS("p->sfu_reorder_tolerance_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;shaper_stats_log_interval_ms}.
     */
    P_SHAPER_STATS_LOG_INTERVAL_MS("p->shaper_stats_log_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;shaper_time_until_next_process_ms}.
     */
    P_SHAPER_TIME_UNTIL_NEXT_PROCESS_MS("p->shaper_time_until_next_process_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;short_offset_precise}.
     */
    P_SHORT_OFFSET_PRECISE("p->short_offset_precise", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;simulcast_nack_race_condition_check}.
     */
    P_SIMULCAST_NACK_RACE_CONDITION_CHECK("p->simulcast_nack_race_condition_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;single_ml_driven_ramp_up}.
     */
    P_SINGLE_ML_DRIVEN_RAMP_UP("p->single_ml_driven_ramp_up", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;skip_bwe_dyn_param_in_main_update}.
     */
    P_SKIP_BWE_DYN_PARAM_IN_MAIN_UPDATE("p->skip_bwe_dyn_param_in_main_update", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;skip_forced_probing_during_mcp}.
     */
    P_SKIP_FORCED_PROBING_DURING_MCP("p->skip_forced_probing_during_mcp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;skip_forced_probing_during_pp}.
     */
    P_SKIP_FORCED_PROBING_DURING_PP("p->skip_forced_probing_during_pp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;skip_forced_probing_during_udst}.
     */
    P_SKIP_FORCED_PROBING_DURING_UDST("p->skip_forced_probing_during_udst", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;skip_forced_signaling}.
     */
    P_SKIP_FORCED_SIGNALING("p->skip_forced_signaling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;skip_ml_ns_for_voice_isolation}.
     */
    P_SKIP_ML_NS_FOR_VOICE_ISOLATION("p->skip_ml_ns_for_voice_isolation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;skip_rtt_min_cmp}.
     */
    P_SKIP_RTT_MIN_CMP("p->skip_rtt_min_cmp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;skip_when_no_echo}.
     */
    P_SKIP_WHEN_NO_ECHO("p->skip_when_no_echo", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;slc_goodput_coeff}.
     */
    P_SLC_GOODPUT_COEFF("p->slc_goodput_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;slc_hist_coeff}.
     */
    P_SLC_HIST_COEFF("p->slc_hist_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;slc_init_bwe_coeff}.
     */
    P_SLC_INIT_BWE_COEFF("p->slc_init_bwe_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;slc_offset}.
     */
    P_SLC_OFFSET("p->slc_offset", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;smooth_leveler_mode_factor}.
     */
    P_SMOOTH_LEVELER_MODE_FACTOR("p->smooth_leveler_mode_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;speaker_mode_policy}.
     */
    P_SPEAKER_MODE_POLICY("p->speaker_mode_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_adaptive_leveler_ec_thres}.
     */
    P_SPKR_ADAPTIVE_LEVELER_EC_THRES("p->spkr_adaptive_leveler_ec_thres", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_adaptive_leveler_mode}.
     */
    P_SPKR_ADAPTIVE_LEVELER_MODE("p->spkr_adaptive_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_adaptive_leveler_mode_hi_low}.
     */
    P_SPKR_ADAPTIVE_LEVELER_MODE_HI_LOW("p->spkr_adaptive_leveler_mode_hi_low", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_adaptive_leveler_mode_min_intensity}.
     */
    P_SPKR_ADAPTIVE_LEVELER_MODE_MIN_INTENSITY("p->spkr_adaptive_leveler_mode_min_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_adaptive_leveler_mode_rev}.
     */
    P_SPKR_ADAPTIVE_LEVELER_MODE_REV("p->spkr_adaptive_leveler_mode_rev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_bipolar_compression_en}.
     */
    P_SPKR_BIPOLAR_COMPRESSION_EN("p->spkr_bipolar_compression_en", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_leveler_intensity}.
     */
    P_SPKR_LEVELER_INTENSITY("p->spkr_leveler_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_min_wait_frames_transitions}.
     */
    P_SPKR_MIN_WAIT_FRAMES_TRANSITIONS("p->spkr_min_wait_frames_transitions", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;spkr_smooth_leveler_mode_factor}.
     */
    P_SPKR_SMOOTH_LEVELER_MODE_FACTOR("p->spkr_smooth_leveler_mode_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sru_fall_back_ceiling_mode}.
     */
    P_SRU_FALL_BACK_CEILING_MODE("p->sru_fall_back_ceiling_mode", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ss_downscale_with_buffer_check}.
     */
    P_SS_DOWNSCALE_WITH_BUFFER_CHECK("p->ss_downscale_with_buffer_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ss_encoder_max_res_tolerance}.
     */
    P_SS_ENCODER_MAX_RES_TOLERANCE("p->ss_encoder_max_res_tolerance", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;staggered_binds.enable}.
     */
    P_STAGGERED_BINDS_ENABLE("p->staggered_binds.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;staggered_binds.rank_by_c2r_rtt}.
     */
    P_STAGGERED_BINDS_RANK_BY_C2R_RTT("p->staggered_binds.rank_by_c2r_rtt", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;staggered_binds.wave_delay_ms}.
     */
    P_STAGGERED_BINDS_WAVE_DELAY_MS("p->staggered_binds.wave_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;staggered_binds.wave_size}.
     */
    P_STAGGERED_BINDS_WAVE_SIZE("p->staggered_binds.wave_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;start_ml_ns_process_at_frame}.
     */
    P_START_ML_NS_PROCESS_AT_FRAME("p->start_ml_ns_process_at_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;stop_probing_after_accept_received}.
     */
    P_STOP_PROBING_AFTER_ACCEPT_RECEIVED("p->stop_probing_after_accept_received", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;stop_probing_after_call_start}.
     */
    P_STOP_PROBING_AFTER_CALL_START("p->stop_probing_after_call_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;stop_probing_before_accept_send}.
     */
    P_STOP_PROBING_BEFORE_ACCEPT_SEND("p->stop_probing_before_accept_send", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;stop_ramp_up_when_media_undershoots}.
     */
    P_STOP_RAMP_UP_WHEN_MEDIA_UNDERSHOOTS("p->stop_ramp_up_when_media_undershoots", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;stop_ramp_up_when_media_undershoots_threshold}.
     */
    P_STOP_RAMP_UP_WHEN_MEDIA_UNDERSHOOTS_THRESHOLD("p->stop_ramp_up_when_media_undershoots_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strength_threshold}.
     */
    P_STRENGTH_THRESHOLD("p->strength_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strip_warp_header_for_packets_on_p2p}.
     */
    P_STRIP_WARP_HEADER_FOR_PACKETS_ON_P2P("p->strip_warp_header_for_packets_on_p2p", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.block_step_for_motion_analysis}.
     */
    P_STRM_PARAM_BLOCK_STEP_FOR_MOTION_ANALYSIS("p->strm_param.block_step_for_motion_analysis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.comb_psnr_ema_sample_size}.
     */
    P_STRM_PARAM_COMB_PSNR_EMA_SAMPLE_SIZE("p->strm_param.comb_psnr_ema_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.compound_npsi}.
     */
    P_STRM_PARAM_COMPOUND_NPSI("p->strm_param.compound_npsi", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.delay_fec_receiver_creation}.
     */
    P_STRM_PARAM_DELAY_FEC_RECEIVER_CREATION("p->strm_param.delay_fec_receiver_creation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.delay_fec_sender_creation}.
     */
    P_STRM_PARAM_DELAY_FEC_SENDER_CREATION("p->strm_param.delay_fec_sender_creation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_add_to_jb_lite}.
     */
    P_STRM_PARAM_ENABLE_ADD_TO_JB_LITE("p->strm_param.enable_add_to_jb_lite", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_capture_fps_median_filter}.
     */
    P_STRM_PARAM_ENABLE_CAPTURE_FPS_MEDIAN_FILTER("p->strm_param.enable_capture_fps_median_filter", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_decoder_state_reset}.
     */
    P_STRM_PARAM_ENABLE_DECODER_STATE_RESET("p->strm_param.enable_decoder_state_reset", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_droppable_no_fec}.
     */
    P_STRM_PARAM_ENABLE_DROPPABLE_NO_FEC("p->strm_param.enable_droppable_no_fec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_droppable_no_packet_cache}.
     */
    P_STRM_PARAM_ENABLE_DROPPABLE_NO_PACKET_CACHE("p->strm_param.enable_droppable_no_packet_cache", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_droppable_no_piggyback}.
     */
    P_STRM_PARAM_ENABLE_DROPPABLE_NO_PIGGYBACK("p->strm_param.enable_droppable_no_piggyback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_fec_no_piggyback}.
     */
    P_STRM_PARAM_ENABLE_FEC_NO_PIGGYBACK("p->strm_param.enable_fec_no_piggyback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_fec_recovered_pt_from_rtp_header}.
     */
    P_STRM_PARAM_ENABLE_FEC_RECOVERED_PT_FROM_RTP_HEADER("p->strm_param.enable_fec_recovered_pt_from_rtp_header", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_frame_num_continuation}.
     */
    P_STRM_PARAM_ENABLE_FRAME_NUM_CONTINUATION("p->strm_param.enable_frame_num_continuation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_jb_level_logging}.
     */
    P_STRM_PARAM_ENABLE_JB_LEVEL_LOGGING("p->strm_param.enable_jb_level_logging", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_nonkeyframe_maxresend_change}.
     */
    P_STRM_PARAM_ENABLE_NONKEYFRAME_MAXRESEND_CHANGE("p->strm_param.enable_nonkeyframe_maxresend_change", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_npsi_logs}.
     */
    P_STRM_PARAM_ENABLE_NPSI_LOGS("p->strm_param.enable_npsi_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_pli_for_crc_mismatch}.
     */
    P_STRM_PARAM_ENABLE_PLI_FOR_CRC_MISMATCH("p->strm_param.enable_pli_for_crc_mismatch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_pli_for_dec_err}.
     */
    P_STRM_PARAM_ENABLE_PLI_FOR_DEC_ERR("p->strm_param.enable_pli_for_dec_err", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_send_frame_num_rtp_ext}.
     */
    P_STRM_PARAM_ENABLE_SEND_FRAME_NUM_RTP_EXT("p->strm_param.enable_send_frame_num_rtp_ext", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_send_psnr}.
     */
    P_STRM_PARAM_ENABLE_SEND_PSNR("p->strm_param.enable_send_psnr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_send_vmos2_rtp_ext}.
     */
    P_STRM_PARAM_ENABLE_SEND_VMOS2_RTP_EXT("p->strm_param.enable_send_vmos2_rtp_ext", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_uvq_inf}.
     */
    P_STRM_PARAM_ENABLE_UVQ_INF("p->strm_param.enable_uvq_inf", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_verbose_pkt_logs}.
     */
    P_STRM_PARAM_ENABLE_VERBOSE_PKT_LOGS("p->strm_param.enable_verbose_pkt_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_vid_delay_fix}.
     */
    P_STRM_PARAM_ENABLE_VID_DELAY_FIX("p->strm_param.enable_vid_delay_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_vid_rtx_indication}.
     */
    P_STRM_PARAM_ENABLE_VID_RTX_INDICATION("p->strm_param.enable_vid_rtx_indication", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_video_corruption_fix}.
     */
    P_STRM_PARAM_ENABLE_VIDEO_CORRUPTION_FIX("p->strm_param.enable_video_corruption_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enable_vsr_inf}.
     */
    P_STRM_PARAM_ENABLE_VSR_INF("p->strm_param.enable_vsr_inf", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enc_latency_ema_alpha}.
     */
    P_STRM_PARAM_ENC_LATENCY_EMA_ALPHA("p->strm_param.enc_latency_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enc_thread_latency_ema_sample_size}.
     */
    P_STRM_PARAM_ENC_THREAD_LATENCY_EMA_SAMPLE_SIZE("p->strm_param.enc_thread_latency_ema_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enc_thread_latency_sample_interval}.
     */
    P_STRM_PARAM_ENC_THREAD_LATENCY_SAMPLE_INTERVAL("p->strm_param.enc_thread_latency_sample_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.enc_thread_max_latency}.
     */
    P_STRM_PARAM_ENC_THREAD_MAX_LATENCY("p->strm_param.enc_thread_max_latency", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.encode_mutex_early_unlock}.
     */
    P_STRM_PARAM_ENCODE_MUTEX_EARLY_UNLOCK("p->strm_param.encode_mutex_early_unlock", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.enable_fast_ramp_feature}.
     */
    P_STRM_PARAM_FR_ENABLE_FAST_RAMP_FEATURE("p->strm_param.fr.enable_fast_ramp_feature", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.enable_fr_basic}.
     */
    P_STRM_PARAM_FR_ENABLE_FR_BASIC("p->strm_param.fr.enable_fr_basic", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.enable_fr_build_ext}.
     */
    P_STRM_PARAM_FR_ENABLE_FR_BUILD_EXT("p->strm_param.fr.enable_fr_build_ext", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.enable_fr_bwe_clamp}.
     */
    P_STRM_PARAM_FR_ENABLE_FR_BWE_CLAMP("p->strm_param.fr.enable_fr_bwe_clamp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_bitrate_win_msec}.
     */
    P_STRM_PARAM_FR_FR_BITRATE_WIN_MSEC("p->strm_param.fr.fr_bitrate_win_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_bits_thr_for_peer_ramp_up}.
     */
    P_STRM_PARAM_FR_FR_BITS_THR_FOR_PEER_RAMP_UP("p->strm_param.fr.fr_bits_thr_for_peer_ramp_up", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_bwe_clamp_rx_bitrate_multiplier}.
     */
    P_STRM_PARAM_FR_FR_BWE_CLAMP_RX_BITRATE_MULTIPLIER("p->strm_param.fr.fr_bwe_clamp_rx_bitrate_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_disable_sync_event_handling}.
     */
    P_STRM_PARAM_FR_FR_DISABLE_SYNC_EVENT_HANDLING("p->strm_param.fr.fr_disable_sync_event_handling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_hbwe_init_bitrate_bps}.
     */
    P_STRM_PARAM_FR_FR_HBWE_INIT_BITRATE_BPS("p->strm_param.fr.fr_hbwe_init_bitrate_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_hbwe_target_bitrate_multiplier}.
     */
    P_STRM_PARAM_FR_FR_HBWE_TARGET_BITRATE_MULTIPLIER("p->strm_param.fr.fr_hbwe_target_bitrate_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_max_action_notification_count}.
     */
    P_STRM_PARAM_FR_FR_MAX_ACTION_NOTIFICATION_COUNT("p->strm_param.fr.fr_max_action_notification_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_max_target_bitrate_multiplier}.
     */
    P_STRM_PARAM_FR_FR_MAX_TARGET_BITRATE_MULTIPLIER("p->strm_param.fr.fr_max_target_bitrate_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_min_init_bitrate_bps}.
     */
    P_STRM_PARAM_FR_FR_MIN_INIT_BITRATE_BPS("p->strm_param.fr.fr_min_init_bitrate_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_msec_thr_coeff_of_rtt_for_peer_ramp_up}.
     */
    P_STRM_PARAM_FR_FR_MSEC_THR_COEFF_OF_RTT_FOR_PEER_RAMP_UP("p->strm_param.fr.fr_msec_thr_coeff_of_rtt_for_peer_ramp_up", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_msec_thr_for_peer_ramp_up}.
     */
    P_STRM_PARAM_FR_FR_MSEC_THR_FOR_PEER_RAMP_UP("p->strm_param.fr.fr_msec_thr_for_peer_ramp_up", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_no_fr_header_threshold_ms}.
     */
    P_STRM_PARAM_FR_FR_NO_FR_HEADER_THRESHOLD_MS("p->strm_param.fr.fr_no_fr_header_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_pl_max_out_of_order_distance}.
     */
    P_STRM_PARAM_FR_FR_PL_MAX_OUT_OF_ORDER_DISTANCE("p->strm_param.fr.fr_pl_max_out_of_order_distance", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_pl_ratio_window_size_ms}.
     */
    P_STRM_PARAM_FR_FR_PL_RATIO_WINDOW_SIZE_MS("p->strm_param.fr.fr_pl_ratio_window_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_pl_total_thr}.
     */
    P_STRM_PARAM_FR_FR_PL_TOTAL_THR("p->strm_param.fr.fr_pl_total_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_plr_perc_thr}.
     */
    P_STRM_PARAM_FR_FR_PLR_PERC_THR("p->strm_param.fr.fr_plr_perc_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_rtt_ema_alpha}.
     */
    P_STRM_PARAM_FR_FR_RTT_EMA_ALPHA("p->strm_param.fr.fr_rtt_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_rtt_msec_above_min_thr}.
     */
    P_STRM_PARAM_FR_FR_RTT_MSEC_ABOVE_MIN_THR("p->strm_param.fr.fr_rtt_msec_above_min_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_rtt_slide_window_size}.
     */
    P_STRM_PARAM_FR_FR_RTT_SLIDE_WINDOW_SIZE("p->strm_param.fr.fr_rtt_slide_window_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_rtt_slope_pts}.
     */
    P_STRM_PARAM_FR_FR_RTT_SLOPE_PTS("p->strm_param.fr.fr_rtt_slope_pts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_rtt_slope_threshold}.
     */
    P_STRM_PARAM_FR_FR_RTT_SLOPE_THRESHOLD("p->strm_param.fr.fr_rtt_slope_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_sbwe_mode_after_stop}.
     */
    P_STRM_PARAM_FR_FR_SBWE_MODE_AFTER_STOP("p->strm_param.fr.fr_sbwe_mode_after_stop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_sbwe_mode_reach_hbwe_target_bitrate}.
     */
    P_STRM_PARAM_FR_FR_SBWE_MODE_REACH_HBWE_TARGET_BITRATE("p->strm_param.fr.fr_sbwe_mode_reach_hbwe_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_skip_clamp_on_out_stop}.
     */
    P_STRM_PARAM_FR_FR_SKIP_CLAMP_ON_OUT_STOP("p->strm_param.fr.fr_skip_clamp_on_out_stop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_skip_ramp_up_pause}.
     */
    P_STRM_PARAM_FR_FR_SKIP_RAMP_UP_PAUSE("p->strm_param.fr.fr_skip_ramp_up_pause", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_hbwe_target_bitrate}.
     */
    P_STRM_PARAM_FR_FR_USE_HBWE_TARGET_BITRATE("p->strm_param.fr.fr_use_hbwe_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_in_cong_signal_for_out_ramp}.
     */
    P_STRM_PARAM_FR_FR_USE_IN_CONG_SIGNAL_FOR_OUT_RAMP("p->strm_param.fr.fr_use_in_cong_signal_for_out_ramp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_in_pl_signal_for_out_ramp}.
     */
    P_STRM_PARAM_FR_FR_USE_IN_PL_SIGNAL_FOR_OUT_RAMP("p->strm_param.fr.fr_use_in_pl_signal_for_out_ramp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_init_bitrate_for_in_ramp}.
     */
    P_STRM_PARAM_FR_FR_USE_INIT_BITRATE_FOR_IN_RAMP("p->strm_param.fr.fr_use_init_bitrate_for_in_ramp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_init_bitrate_for_out_ramp}.
     */
    P_STRM_PARAM_FR_FR_USE_INIT_BITRATE_FOR_OUT_RAMP("p->strm_param.fr.fr_use_init_bitrate_for_out_ramp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_packet_loss}.
     */
    P_STRM_PARAM_FR_FR_USE_PACKET_LOSS("p->strm_param.fr.fr_use_packet_loss", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.fr.fr_use_rtt_slope_stop}.
     */
    P_STRM_PARAM_FR_FR_USE_RTT_SLOPE_STOP("p->strm_param.fr.fr_use_rtt_slope_stop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.freeze_msg_settings.avg_num_freezes_interval_ms}.
     */
    P_STRM_PARAM_FREEZE_MSG_SETTINGS_AVG_NUM_FREEZES_INTERVAL_MS("p->strm_param.freeze_msg_settings.avg_num_freezes_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.freeze_msg_settings.freeze_and_cong_signal_max_diff_ms}.
     */
    P_STRM_PARAM_FREEZE_MSG_SETTINGS_FREEZE_AND_CONG_SIGNAL_MAX_DIFF_MS("p->strm_param.freeze_msg_settings.freeze_and_cong_signal_max_diff_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.freeze_msg_settings.min_msg_interval_ms}.
     */
    P_STRM_PARAM_FREEZE_MSG_SETTINGS_MIN_MSG_INTERVAL_MS("p->strm_param.freeze_msg_settings.min_msg_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.freeze_msg_settings.num_freezes_per_sec_threshold}.
     */
    P_STRM_PARAM_FREEZE_MSG_SETTINGS_NUM_FREEZES_PER_SEC_THRESHOLD("p->strm_param.freeze_msg_settings.num_freezes_per_sec_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.freeze_msg_settings.time_since_last_freeze_threshold_ms}.
     */
    P_STRM_PARAM_FREEZE_MSG_SETTINGS_TIME_SINCE_LAST_FREEZE_THRESHOLD_MS("p->strm_param.freeze_msg_settings.time_since_last_freeze_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.gcall_use_pli_timer}.
     */
    P_STRM_PARAM_GCALL_USE_PLI_TIMER("p->strm_param.gcall_use_pli_timer", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.imm_pli_after_kf_err}.
     */
    P_STRM_PARAM_IMM_PLI_AFTER_KF_ERR("p->strm_param.imm_pli_after_kf_err", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.check_reset_frame_all_pli}.
     */
    P_STRM_PARAM_JB_PARAM_CHECK_RESET_FRAME_ALL_PLI("p->strm_param.jb_param.check_reset_frame_all_pli", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.check_reset_frame_jb_covered}.
     */
    P_STRM_PARAM_JB_PARAM_CHECK_RESET_FRAME_JB_COVERED("p->strm_param.jb_param.check_reset_frame_jb_covered", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.empty_reset_ema_smoothing}.
     */
    P_STRM_PARAM_JB_PARAM_EMPTY_RESET_EMA_SMOOTHING("p->strm_param.jb_param.empty_reset_ema_smoothing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.empty_reset_pkts_per_frame_multiplier}.
     */
    P_STRM_PARAM_JB_PARAM_EMPTY_RESET_PKTS_PER_FRAME_MULTIPLIER("p->strm_param.jb_param.empty_reset_pkts_per_frame_multiplier", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.enable_new_vid_jb_framelist}.
     */
    P_STRM_PARAM_JB_PARAM_ENABLE_NEW_VID_JB_FRAMELIST("p->strm_param.jb_param.enable_new_vid_jb_framelist", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.grace_hold_ms}.
     */
    P_STRM_PARAM_JB_PARAM_GRACE_HOLD_MS("p->strm_param.jb_param.grace_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.indeterminate_means_missing}.
     */
    P_STRM_PARAM_JB_PARAM_INDETERMINATE_MEANS_MISSING("p->strm_param.jb_param.indeterminate_means_missing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.jb_max_covered_length_floor_ms}.
     */
    P_STRM_PARAM_JB_PARAM_JB_MAX_COVERED_LENGTH_FLOOR_MS("p->strm_param.jb_param.jb_max_covered_length_floor_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.jb_max_covered_length_ms}.
     */
    P_STRM_PARAM_JB_PARAM_JB_MAX_COVERED_LENGTH_MS("p->strm_param.jb_param.jb_max_covered_length_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.jb_max_covered_rtt_based}.
     */
    P_STRM_PARAM_JB_PARAM_JB_MAX_COVERED_RTT_BASED("p->strm_param.jb_param.jb_max_covered_rtt_based", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.jb_max_covered_rtt_multiplier}.
     */
    P_STRM_PARAM_JB_PARAM_JB_MAX_COVERED_RTT_MULTIPLIER("p->strm_param.jb_param.jb_max_covered_rtt_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.missing_start_frame_nack_estimate}.
     */
    P_STRM_PARAM_JB_PARAM_MISSING_START_FRAME_NACK_ESTIMATE("p->strm_param.jb_param.missing_start_frame_nack_estimate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.no_decodable_handling}.
     */
    P_STRM_PARAM_JB_PARAM_NO_DECODABLE_HANDLING("p->strm_param.jb_param.no_decodable_handling", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.jb_param.verify_got_frame}.
     */
    P_STRM_PARAM_JB_PARAM_VERIFY_GOT_FRAME("p->strm_param.jb_param.verify_got_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.max_npsi_timer_ms}.
     */
    P_STRM_PARAM_MAX_NPSI_TIMER_MS("p->strm_param.max_npsi_timer_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.max_npsi_timer_ms_newpkt}.
     */
    P_STRM_PARAM_MAX_NPSI_TIMER_MS_NEWPKT("p->strm_param.max_npsi_timer_ms_newpkt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.min_interval_rr}.
     */
    P_STRM_PARAM_MIN_INTERVAL_RR("p->strm_param.min_interval_rr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.min_interval_sr}.
     */
    P_STRM_PARAM_MIN_INTERVAL_SR("p->strm_param.min_interval_sr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.min_nack_resend_period_e2e_ms}.
     */
    P_STRM_PARAM_MIN_NACK_RESEND_PERIOD_E2E_MS("p->strm_param.min_nack_resend_period_e2e_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.min_nack_resend_period_ms}.
     */
    P_STRM_PARAM_MIN_NACK_RESEND_PERIOD_MS("p->strm_param.min_nack_resend_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.min_npsi_timer_ms}.
     */
    P_STRM_PARAM_MIN_NPSI_TIMER_MS("p->strm_param.min_npsi_timer_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.motion_analysis_interval_ms}.
     */
    P_STRM_PARAM_MOTION_ANALYSIS_INTERVAL_MS("p->strm_param.motion_analysis_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_continue_on_cache_miss}.
     */
    P_STRM_PARAM_NACK_CONTINUE_ON_CACHE_MISS("p->strm_param.nack_continue_on_cache_miss", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_default_resend_threshold_ms}.
     */
    P_STRM_PARAM_NACK_DEFAULT_RESEND_THRESHOLD_MS("p->strm_param.nack_default_resend_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_enabled}.
     */
    P_STRM_PARAM_NACK_ENABLED("p->strm_param.nack_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_pli_threshold_min}.
     */
    P_STRM_PARAM_NACK_PLI_THRESHOLD_MIN("p->strm_param.nack_pli_threshold_min", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_rtt_interactive_threshold}.
     */
    P_STRM_PARAM_NACK_RTT_INTERACTIVE_THRESHOLD("p->strm_param.nack_rtt_interactive_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_rtt_modifier_high}.
     */
    P_STRM_PARAM_NACK_RTT_MODIFIER_HIGH("p->strm_param.nack_rtt_modifier_high", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_rtt_modifier_low}.
     */
    P_STRM_PARAM_NACK_RTT_MODIFIER_LOW("p->strm_param.nack_rtt_modifier_low", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_skip_recovery_on_max_retries}.
     */
    P_STRM_PARAM_NACK_SKIP_RECOVERY_ON_MAX_RETRIES("p->strm_param.nack_skip_recovery_on_max_retries", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nack_skip_recovery_on_rate_limit}.
     */
    P_STRM_PARAM_NACK_SKIP_RECOVERY_ON_RATE_LIMIT("p->strm_param.nack_skip_recovery_on_rate_limit", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.no_sr_rr_piggyback}.
     */
    P_STRM_PARAM_NO_SR_RR_PIGGYBACK("p->strm_param.no_sr_rr_piggyback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nondroppable_count_to_stop_resend_droppable}.
     */
    P_STRM_PARAM_NONDROPPABLE_COUNT_TO_STOP_RESEND_DROPPABLE("p->strm_param.nondroppable_count_to_stop_resend_droppable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.nonkeyframe_maxresend_decrease_speed}.
     */
    P_STRM_PARAM_NONKEYFRAME_MAXRESEND_DECREASE_SPEED("p->strm_param.nonkeyframe_maxresend_decrease_speed", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.npsi_enabled}.
     */
    P_STRM_PARAM_NPSI_ENABLED("p->strm_param.npsi_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.npsi_old_timer_ms}.
     */
    P_STRM_PARAM_NPSI_OLD_TIMER_MS("p->strm_param.npsi_old_timer_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.npsi_rtt_mult}.
     */
    P_STRM_PARAM_NPSI_RTT_MULT("p->strm_param.npsi_rtt_mult", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.npsi_rtt_mult_sender}.
     */
    P_STRM_PARAM_NPSI_RTT_MULT_SENDER("p->strm_param.npsi_rtt_mult_sender", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.npsi_use_ema_rtt}.
     */
    P_STRM_PARAM_NPSI_USE_EMA_RTT("p->strm_param.npsi_use_ema_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.overshoot_rate_downgrade_threshold_pct}.
     */
    P_STRM_PARAM_OVERSHOOT_RATE_DOWNGRADE_THRESHOLD_PCT("p->strm_param.overshoot_rate_downgrade_threshold_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.overshoot_rate_ema_sample_size}.
     */
    P_STRM_PARAM_OVERSHOOT_RATE_EMA_SAMPLE_SIZE("p->strm_param.overshoot_rate_ema_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.overshoot_rate_sample_interval}.
     */
    P_STRM_PARAM_OVERSHOOT_RATE_SAMPLE_INTERVAL("p->strm_param.overshoot_rate_sample_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pixel_step_for_edge_analysis}.
     */
    P_STRM_PARAM_PIXEL_STEP_FOR_EDGE_ANALYSIS("p->strm_param.pixel_step_for_edge_analysis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_enabled}.
     */
    P_STRM_PARAM_PLI_ENABLED("p->strm_param.pli_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_freeze_timeout}.
     */
    P_STRM_PARAM_PLI_FREEZE_TIMEOUT("p->strm_param.pli_freeze_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_key_frame_pct}.
     */
    P_STRM_PARAM_PLI_KEY_FRAME_PCT("p->strm_param.pli_key_frame_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_max_threshold}.
     */
    P_STRM_PARAM_PLI_MAX_THRESHOLD("p->strm_param.pli_max_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_resp_throttle_time_ms}.
     */
    P_STRM_PARAM_PLI_RESP_THROTTLE_TIME_MS("p->strm_param.pli_resp_throttle_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_rtt_multiplier}.
     */
    P_STRM_PARAM_PLI_RTT_MULTIPLIER("p->strm_param.pli_rtt_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_rx_bwe_check_fix}.
     */
    P_STRM_PARAM_PLI_RX_BWE_CHECK_FIX("p->strm_param.pli_rx_bwe_check_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.pli_throttle_time_ms}.
     */
    P_STRM_PARAM_PLI_THROTTLE_TIME_MS("p->strm_param.pli_throttle_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.psnr_calc_end_bps}.
     */
    P_STRM_PARAM_PSNR_CALC_END_BPS("p->strm_param.psnr_calc_end_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.psnr_calc_start_bps}.
     */
    P_STRM_PARAM_PSNR_CALC_START_BPS("p->strm_param.psnr_calc_start_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.publish_dyn_params_to_dec}.
     */
    P_STRM_PARAM_PUBLISH_DYN_PARAMS_TO_DEC("p->strm_param.publish_dyn_params_to_dec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.rst_ltrp_size_on_res_switch}.
     */
    P_STRM_PARAM_RST_LTRP_SIZE_ON_RES_SWITCH("p->strm_param.rst_ltrp_size_on_res_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.rtcp_pkt_retxr_config.enabled_bitmask}.
     */
    P_STRM_PARAM_RTCP_PKT_RETXR_CONFIG_ENABLED_BITMASK("p->strm_param.rtcp_pkt_retxr_config.enabled_bitmask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.rtcp_pkt_retxr_config.num_retx}.
     */
    P_STRM_PARAM_RTCP_PKT_RETXR_CONFIG_NUM_RETX("p->strm_param.rtcp_pkt_retxr_config.num_retx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.rtcp_pkt_retxr_config.retx_time_ms}.
     */
    P_STRM_PARAM_RTCP_PKT_RETXR_CONFIG_RETX_TIME_MS("p->strm_param.rtcp_pkt_retxr_config.retx_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.rtt_based_pli_timer}.
     */
    P_STRM_PARAM_RTT_BASED_PLI_TIMER("p->strm_param.rtt_based_pli_timer", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.short_hd_duration_sec}.
     */
    P_STRM_PARAM_SHORT_HD_DURATION_SEC("p->strm_param.short_hd_duration_sec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.spatial_analysis_interval_ms}.
     */
    P_STRM_PARAM_SPATIAL_ANALYSIS_INTERVAL_MS("p->strm_param.spatial_analysis_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.start_all_ltrp}.
     */
    P_STRM_PARAM_START_ALL_LTRP("p->strm_param.start_all_ltrp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.use_channel_buf_for_fec_encoding}.
     */
    P_STRM_PARAM_USE_CHANNEL_BUF_FOR_FEC_ENCODING("p->strm_param.use_channel_buf_for_fec_encoding", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.use_nack_rtt_for_pli_threshold}.
     */
    P_STRM_PARAM_USE_NACK_RTT_FOR_PLI_THRESHOLD("p->strm_param.use_nack_rtt_for_pli_threshold", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.vid_jb_max_frames}.
     */
    P_STRM_PARAM_VID_JB_MAX_FRAMES("p->strm_param.vid_jb_max_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.vid_strm_max_packet_count}.
     */
    P_STRM_PARAM_VID_STRM_MAX_PACKET_COUNT("p->strm_param.vid_strm_max_packet_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.vid_strm_ts_nondroppable_pkt_cnt_threshold}.
     */
    P_STRM_PARAM_VID_STRM_TS_NONDROPPABLE_PKT_CNT_THRESHOLD("p->strm_param.vid_strm_ts_nondroppable_pkt_cnt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.video_frame_crc_sample_interval}.
     */
    P_STRM_PARAM_VIDEO_FRAME_CRC_SAMPLE_INTERVAL("p->strm_param.video_frame_crc_sample_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.vmos2_rtp_psnr_send_interval}.
     */
    P_STRM_PARAM_VMOS2_RTP_PSNR_SEND_INTERVAL("p->strm_param.vmos2_rtp_psnr_send_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.vsr_upsampling_ratio}.
     */
    P_STRM_PARAM_VSR_UPSAMPLING_RATIO("p->strm_param.vsr_upsampling_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.wait_frames_after_last_keyframe_to_stop_resend}.
     */
    P_STRM_PARAM_WAIT_FRAMES_AFTER_LAST_KEYFRAME_TO_STOP_RESEND("p->strm_param.wait_frames_after_last_keyframe_to_stop_resend", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;strm_param.webrtc_nack_check_pli_enabled}.
     */
    P_STRM_PARAM_WEBRTC_NACK_CHECK_PLI_ENABLED("p->strm_param.webrtc_nack_check_pli_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;sw_only_policy}.
     */
    P_SW_ONLY_POLICY("p->sw_only_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;sys_aud_pre_process_gain}.
     */
    P_SYS_AUD_PRE_PROCESS_GAIN("p->sys_aud_pre_process_gain", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tail_length}.
     */
    P_TAIL_LENGTH("p->tail_length", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;target_bitrate}.
     */
    P_TARGET_BITRATE("p->target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;target_level}.
     */
    P_TARGET_LEVEL("p->target_level", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tcp_ping_interval}.
     */
    P_TCP_PING_INTERVAL("p->tcp_ping_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_bwe_action}.
     */
    P_TFRC_SENDER_REPORT_BWE_ACTION("p->tfrc_sender_report_bwe_action", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_bwe_detailed_action}.
     */
    P_TFRC_SENDER_REPORT_BWE_DETAILED_ACTION("p->tfrc_sender_report_bwe_detailed_action", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_bwe_detailed_action_sfu_dl}.
     */
    P_TFRC_SENDER_REPORT_BWE_DETAILED_ACTION_SFU_DL("p->tfrc_sender_report_bwe_detailed_action_sfu_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_congestion_prob}.
     */
    P_TFRC_SENDER_REPORT_CONGESTION_PROB("p->tfrc_sender_report_congestion_prob", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_hd_targeting_prob}.
     */
    P_TFRC_SENDER_REPORT_HD_TARGETING_PROB("p->tfrc_sender_report_hd_targeting_prob", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_math_plr_type}.
     */
    P_TFRC_SENDER_REPORT_MATH_PLR_TYPE("p->tfrc_sender_report_math_plr_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_ml_metrics}.
     */
    P_TFRC_SENDER_REPORT_ML_METRICS("p->tfrc_sender_report_ml_metrics", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_ml_metrics_sfu_dl}.
     */
    P_TFRC_SENDER_REPORT_ML_METRICS_SFU_DL("p->tfrc_sender_report_ml_metrics_sfu_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_ml_plc_inference_result}.
     */
    P_TFRC_SENDER_REPORT_ML_PLC_INFERENCE_RESULT("p->tfrc_sender_report_ml_plc_inference_result", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_tr_result}.
     */
    P_TFRC_SENDER_REPORT_TR_RESULT("p->tfrc_sender_report_tr_result", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tfrc_sender_report_undershoot_type}.
     */
    P_TFRC_SENDER_REPORT_UNDERSHOOT_TYPE("p->tfrc_sender_report_undershoot_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;timeout_after_accept_for_tcp_alloc_msec}.
     */
    P_TIMEOUT_AFTER_ACCEPT_FOR_TCP_ALLOC_MSEC("p->timeout_after_accept_for_tcp_alloc_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;timeout_for_tcp_alloc_msec}.
     */
    P_TIMEOUT_FOR_TCP_ALLOC_MSEC("p->timeout_for_tcp_alloc_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tp_oob_params.enable_deduct_from_vid_stream}.
     */
    P_TP_OOB_PARAMS_ENABLE_DEDUCT_FROM_VID_STREAM("p->tp_oob_params.enable_deduct_from_vid_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tp_oob_params.enable_oob_deduction}.
     */
    P_TP_OOB_PARAMS_ENABLE_OOB_DEDUCTION("p->tp_oob_params.enable_oob_deduction", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tr_model_name}.
     */
    P_TR_MODEL_NAME("p->tr_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;treat_recovered_packet_as_normal_packet}.
     */
    P_TREAT_RECOVERED_PACKET_AS_NORMAL_PACKET("p->treat_recovered_packet_as_normal_packet", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;trigger_fec_with_uplink_plr}.
     */
    P_TRIGGER_FEC_WITH_UPLINK_PLR("p->trigger_fec_with_uplink_plr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_init_delay_ms}.
     */
    P_TX_INIT_DELAY_MS("p->tx_init_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_interval_to_reset_inferences_ms}.
     */
    P_TX_INTERVAL_TO_RESET_INFERENCES_MS("p->tx_interval_to_reset_inferences_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_max_inferences_per_interval}.
     */
    P_TX_MAX_INFERENCES_PER_INTERVAL("p->tx_max_inferences_per_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_max_queue_size_for_async_mode}.
     */
    P_TX_MAX_QUEUE_SIZE_FOR_ASYNC_MODE("p->tx_max_queue_size_for_async_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_participant_report_on_audio}.
     */
    P_TX_PARTICIPANT_REPORT_ON_AUDIO("p->tx_participant_report_on_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_process_single_chunk_ms}.
     */
    P_TX_PROCESS_SINGLE_CHUNK_MS("p->tx_process_single_chunk_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;tx_query_samples_to_skip}.
     */
    P_TX_QUERY_SAMPLES_TO_SKIP("p->tx_query_samples_to_skip", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tx_should_delay_inference}.
     */
    P_TX_SHOULD_DELAY_INFERENCE("p->tx_should_delay_inference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tx_should_limit_inferences_per_interval}.
     */
    P_TX_SHOULD_LIMIT_INFERENCES_PER_INTERVAL("p->tx_should_limit_inferences_per_interval", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tx_use_executorch}.
     */
    P_TX_USE_EXECUTORCH("p->tx_use_executorch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tx_use_mutex_for_inference}.
     */
    P_TX_USE_MUTEX_FOR_INFERENCE("p->tx_use_mutex_for_inference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;tx_wait_time_for_speech_frames_ms}.
     */
    P_TX_WAIT_TIME_FOR_SPEECH_FRAMES_MS("p->tx_wait_time_for_speech_frames_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_audio_stream_flag_bitmap}.
     */
    P_UAQC_AUDIO_STREAM_FLAG_BITMAP("p->uaqc_audio_stream_flag_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_fpp}.
     */
    P_UAQC_BANDWIDTH_MANAGED_FPP("p->uaqc_bandwidth_managed_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_mlow_dtx_hangover_ms}.
     */
    P_UAQC_BANDWIDTH_MANAGED_MLOW_DTX_HANGOVER_MS("p->uaqc_bandwidth_managed_mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_oob_factor}.
     */
    P_UAQC_BANDWIDTH_MANAGED_OOB_FACTOR("p->uaqc_bandwidth_managed_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_opus_vad_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_OPUS_VAD_THRESHOLD("p->uaqc_bandwidth_managed_opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_plr_lower_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_PLR_LOWER_THRESHOLD("p->uaqc_bandwidth_managed_plr_lower_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_plr_upper_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_PLR_UPPER_THRESHOLD("p->uaqc_bandwidth_managed_plr_upper_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_remb_lower_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_REMB_LOWER_THRESHOLD("p->uaqc_bandwidth_managed_remb_lower_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_remb_trend_down_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_REMB_TREND_DOWN_THRESHOLD("p->uaqc_bandwidth_managed_remb_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_remb_trend_up_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_REMB_TREND_UP_THRESHOLD("p->uaqc_bandwidth_managed_remb_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_remb_upper_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_REMB_UPPER_THRESHOLD("p->uaqc_bandwidth_managed_remb_upper_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_rtt_ratio_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_RTT_RATIO_THRESHOLD("p->uaqc_bandwidth_managed_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_rtt_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_RTT_THRESHOLD("p->uaqc_bandwidth_managed_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_rtt_trend_down_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_RTT_TREND_DOWN_THRESHOLD("p->uaqc_bandwidth_managed_rtt_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_rtt_trend_up_threshold}.
     */
    P_UAQC_BANDWIDTH_MANAGED_RTT_TREND_UP_THRESHOLD("p->uaqc_bandwidth_managed_rtt_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_target_bitrate}.
     */
    P_UAQC_BANDWIDTH_MANAGED_TARGET_BITRATE("p->uaqc_bandwidth_managed_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bandwidth_managed_target_bitrate_net_offset}.
     */
    P_UAQC_BANDWIDTH_MANAGED_TARGET_BITRATE_NET_OFFSET("p->uaqc_bandwidth_managed_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_bw_managed_neteq_delay_offset_ms}.
     */
    P_UAQC_BW_MANAGED_NETEQ_DELAY_OFFSET_MS("p->uaqc_bw_managed_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_codec_flag_bitmap}.
     */
    P_UAQC_CODEC_FLAG_BITMAP("p->uaqc_codec_flag_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_fpp}.
     */
    P_UAQC_DRAIN_FPP("p->uaqc_drain_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_neteq_delay_offset_ms}.
     */
    P_UAQC_DRAIN_NETEQ_DELAY_OFFSET_MS("p->uaqc_drain_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_oob_factor}.
     */
    P_UAQC_DRAIN_OOB_FACTOR("p->uaqc_drain_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_plr_lower_threshold}.
     */
    P_UAQC_DRAIN_PLR_LOWER_THRESHOLD("p->uaqc_drain_plr_lower_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_plr_upper_threshold}.
     */
    P_UAQC_DRAIN_PLR_UPPER_THRESHOLD("p->uaqc_drain_plr_upper_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_remb_lower_threshold}.
     */
    P_UAQC_DRAIN_REMB_LOWER_THRESHOLD("p->uaqc_drain_remb_lower_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_remb_trend_down_threshold}.
     */
    P_UAQC_DRAIN_REMB_TREND_DOWN_THRESHOLD("p->uaqc_drain_remb_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_remb_trend_up_threshold}.
     */
    P_UAQC_DRAIN_REMB_TREND_UP_THRESHOLD("p->uaqc_drain_remb_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_remb_upper_threshold}.
     */
    P_UAQC_DRAIN_REMB_UPPER_THRESHOLD("p->uaqc_drain_remb_upper_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_rtt_ratio_threshold}.
     */
    P_UAQC_DRAIN_RTT_RATIO_THRESHOLD("p->uaqc_drain_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_rtt_threshold}.
     */
    P_UAQC_DRAIN_RTT_THRESHOLD("p->uaqc_drain_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_rtt_trend_down_threshold}.
     */
    P_UAQC_DRAIN_RTT_TREND_DOWN_THRESHOLD("p->uaqc_drain_rtt_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_rtt_trend_up_threshold}.
     */
    P_UAQC_DRAIN_RTT_TREND_UP_THRESHOLD("p->uaqc_drain_rtt_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_target_bitrate}.
     */
    P_UAQC_DRAIN_TARGET_BITRATE("p->uaqc_drain_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_target_bitrate_net_offset}.
     */
    P_UAQC_DRAIN_TARGET_BITRATE_NET_OFFSET("p->uaqc_drain_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_drain_ultra_low_opus_vad_threshold}.
     */
    P_UAQC_DRAIN_ULTRA_LOW_OPUS_VAD_THRESHOLD("p->uaqc_drain_ultra_low_opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_enable_probing_setting_fix}.
     */
    P_UAQC_ENABLE_PROBING_SETTING_FIX("p->uaqc_enable_probing_setting_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_enable_redundancy_at_drain}.
     */
    P_UAQC_ENABLE_REDUNDANCY_AT_DRAIN("p->uaqc_enable_redundancy_at_drain", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_enable_redundancy_at_probing}.
     */
    P_UAQC_ENABLE_REDUNDANCY_AT_PROBING("p->uaqc_enable_redundancy_at_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_enable_redundancy_at_ultra_low}.
     */
    P_UAQC_ENABLE_REDUNDANCY_AT_ULTRA_LOW("p->uaqc_enable_redundancy_at_ultra_low", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_enable_rtt_kalman_filter}.
     */
    P_UAQC_ENABLE_RTT_KALMAN_FILTER("p->uaqc_enable_rtt_kalman_filter", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_compensation_pct}.
     */
    P_UAQC_HIGH_QUALITY_COMPENSATION_PCT("p->uaqc_high_quality_compensation_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_fpp}.
     */
    P_UAQC_HIGH_QUALITY_FPP("p->uaqc_high_quality_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_mlow_dtx_hangover_ms}.
     */
    P_UAQC_HIGH_QUALITY_MLOW_DTX_HANGOVER_MS("p->uaqc_high_quality_mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_oob_factor}.
     */
    P_UAQC_HIGH_QUALITY_OOB_FACTOR("p->uaqc_high_quality_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_opus_vad_threshold}.
     */
    P_UAQC_HIGH_QUALITY_OPUS_VAD_THRESHOLD("p->uaqc_high_quality_opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_plr_lower_threshold}.
     */
    P_UAQC_HIGH_QUALITY_PLR_LOWER_THRESHOLD("p->uaqc_high_quality_plr_lower_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_plr_upper_threshold}.
     */
    P_UAQC_HIGH_QUALITY_PLR_UPPER_THRESHOLD("p->uaqc_high_quality_plr_upper_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_remb_lower_threshold}.
     */
    P_UAQC_HIGH_QUALITY_REMB_LOWER_THRESHOLD("p->uaqc_high_quality_remb_lower_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_remb_trend_down_threshold}.
     */
    P_UAQC_HIGH_QUALITY_REMB_TREND_DOWN_THRESHOLD("p->uaqc_high_quality_remb_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_remb_trend_up_threshold}.
     */
    P_UAQC_HIGH_QUALITY_REMB_TREND_UP_THRESHOLD("p->uaqc_high_quality_remb_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_remb_upper_threshold}.
     */
    P_UAQC_HIGH_QUALITY_REMB_UPPER_THRESHOLD("p->uaqc_high_quality_remb_upper_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_rtt_ratio_threshold}.
     */
    P_UAQC_HIGH_QUALITY_RTT_RATIO_THRESHOLD("p->uaqc_high_quality_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_rtt_threshold}.
     */
    P_UAQC_HIGH_QUALITY_RTT_THRESHOLD("p->uaqc_high_quality_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_rtt_trend_down_threshold}.
     */
    P_UAQC_HIGH_QUALITY_RTT_TREND_DOWN_THRESHOLD("p->uaqc_high_quality_rtt_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_rtt_trend_up_threshold}.
     */
    P_UAQC_HIGH_QUALITY_RTT_TREND_UP_THRESHOLD("p->uaqc_high_quality_rtt_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_target_bitrate}.
     */
    P_UAQC_HIGH_QUALITY_TARGET_BITRATE("p->uaqc_high_quality_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_high_quality_target_bitrate_net_offset}.
     */
    P_UAQC_HIGH_QUALITY_TARGET_BITRATE_NET_OFFSET("p->uaqc_high_quality_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_hq_neteq_delay_offset_ms}.
     */
    P_UAQC_HQ_NETEQ_DELAY_OFFSET_MS("p->uaqc_hq_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_fpp}.
     */
    P_UAQC_LOSSY_FPP("p->uaqc_lossy_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_mlow_dtx_hangover_ms}.
     */
    P_UAQC_LOSSY_MLOW_DTX_HANGOVER_MS("p->uaqc_lossy_mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_neteq_delay_offset_ms}.
     */
    P_UAQC_LOSSY_NETEQ_DELAY_OFFSET_MS("p->uaqc_lossy_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_oob_factor}.
     */
    P_UAQC_LOSSY_OOB_FACTOR("p->uaqc_lossy_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_opus_vad_threshold}.
     */
    P_UAQC_LOSSY_OPUS_VAD_THRESHOLD("p->uaqc_lossy_opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_plr_lower_threshold}.
     */
    P_UAQC_LOSSY_PLR_LOWER_THRESHOLD("p->uaqc_lossy_plr_lower_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_plr_upper_threshold}.
     */
    P_UAQC_LOSSY_PLR_UPPER_THRESHOLD("p->uaqc_lossy_plr_upper_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_remb_lower_threshold}.
     */
    P_UAQC_LOSSY_REMB_LOWER_THRESHOLD("p->uaqc_lossy_remb_lower_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_remb_trend_down_threshold}.
     */
    P_UAQC_LOSSY_REMB_TREND_DOWN_THRESHOLD("p->uaqc_lossy_remb_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_remb_trend_up_threshold}.
     */
    P_UAQC_LOSSY_REMB_TREND_UP_THRESHOLD("p->uaqc_lossy_remb_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_remb_upper_threshold}.
     */
    P_UAQC_LOSSY_REMB_UPPER_THRESHOLD("p->uaqc_lossy_remb_upper_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_rtt_ratio_threshold}.
     */
    P_UAQC_LOSSY_RTT_RATIO_THRESHOLD("p->uaqc_lossy_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_rtt_threshold}.
     */
    P_UAQC_LOSSY_RTT_THRESHOLD("p->uaqc_lossy_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_rtt_trend_down_threshold}.
     */
    P_UAQC_LOSSY_RTT_TREND_DOWN_THRESHOLD("p->uaqc_lossy_rtt_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_rtt_trend_up_threshold}.
     */
    P_UAQC_LOSSY_RTT_TREND_UP_THRESHOLD("p->uaqc_lossy_rtt_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_target_bitrate}.
     */
    P_UAQC_LOSSY_TARGET_BITRATE("p->uaqc_lossy_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_lossy_target_bitrate_net_offset}.
     */
    P_UAQC_LOSSY_TARGET_BITRATE_NET_OFFSET("p->uaqc_lossy_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_low_quality_compensation_pct}.
     */
    P_UAQC_LOW_QUALITY_COMPENSATION_PCT("p->uaqc_low_quality_compensation_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_main_stream_min_bitrate}.
     */
    P_UAQC_MAIN_STREAM_MIN_BITRATE("p->uaqc_main_stream_min_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_no_rtcp_threshold_ms}.
     */
    P_UAQC_NO_RTCP_THRESHOLD_MS("p->uaqc_no_rtcp_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_oob_min_target_bitrate_bps}.
     */
    P_UAQC_OOB_MIN_TARGET_BITRATE_BPS("p->uaqc_oob_min_target_bitrate_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_plr_ema_attack_alpha}.
     */
    P_UAQC_PLR_EMA_ATTACK_ALPHA("p->uaqc_plr_ema_attack_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_plr_ema_impl}.
     */
    P_UAQC_PLR_EMA_IMPL("p->uaqc_plr_ema_impl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_plr_ema_release_alpha}.
     */
    P_UAQC_PLR_EMA_RELEASE_ALPHA("p->uaqc_plr_ema_release_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_plr_slope_points}.
     */
    P_UAQC_PLR_SLOPE_POINTS("p->uaqc_plr_slope_points", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_compensation_pct}.
     */
    P_UAQC_PROBING_COMPENSATION_PCT("p->uaqc_probing_compensation_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_enable_history_bitrate}.
     */
    P_UAQC_PROBING_ENABLE_HISTORY_BITRATE("p->uaqc_probing_enable_history_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_fpp}.
     */
    P_UAQC_PROBING_FPP("p->uaqc_probing_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_history_match_filter}.
     */
    P_UAQC_PROBING_HISTORY_MATCH_FILTER("p->uaqc_probing_history_match_filter", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_history_max_bitrate}.
     */
    P_UAQC_PROBING_HISTORY_MAX_BITRATE("p->uaqc_probing_history_max_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_history_min_bitrate}.
     */
    P_UAQC_PROBING_HISTORY_MIN_BITRATE("p->uaqc_probing_history_min_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_history_mode}.
     */
    P_UAQC_PROBING_HISTORY_MODE("p->uaqc_probing_history_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_history_scale_pct}.
     */
    P_UAQC_PROBING_HISTORY_SCALE_PCT("p->uaqc_probing_history_scale_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_max_red_level}.
     */
    P_UAQC_PROBING_MAX_RED_LEVEL("p->uaqc_probing_max_red_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_mlow_dtx_hangover_ms}.
     */
    P_UAQC_PROBING_MLOW_DTX_HANGOVER_MS("p->uaqc_probing_mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_neteq_delay_offset_ms}.
     */
    P_UAQC_PROBING_NETEQ_DELAY_OFFSET_MS("p->uaqc_probing_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_oob_factor}.
     */
    P_UAQC_PROBING_OOB_FACTOR("p->uaqc_probing_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_opus_vad_threshold}.
     */
    P_UAQC_PROBING_OPUS_VAD_THRESHOLD("p->uaqc_probing_opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_red_plr_threshold}.
     */
    P_UAQC_PROBING_RED_PLR_THRESHOLD("p->uaqc_probing_red_plr_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_remb_threshold}.
     */
    P_UAQC_PROBING_REMB_THRESHOLD("p->uaqc_probing_remb_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_target_bitrate}.
     */
    P_UAQC_PROBING_TARGET_BITRATE("p->uaqc_probing_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_target_bitrate_net_offset}.
     */
    P_UAQC_PROBING_TARGET_BITRATE_NET_OFFSET("p->uaqc_probing_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_probing_use_start_bitrate}.
     */
    P_UAQC_PROBING_USE_START_BITRATE("p->uaqc_probing_use_start_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_record_max_num}.
     */
    P_UAQC_RECORD_MAX_NUM("p->uaqc_record_max_num", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_record_min_duration_to_save}.
     */
    P_UAQC_RECORD_MIN_DURATION_TO_SAVE("p->uaqc_record_min_duration_to_save", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_red_level_n_plr_threshold}.
     */
    P_UAQC_RED_LEVEL_N_PLR_THRESHOLD("p->uaqc_red_level_n_plr_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_remb_check_flag}.
     */
    P_UAQC_REMB_CHECK_FLAG("p->uaqc_remb_check_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_remb_slope_points}.
     */
    P_UAQC_REMB_SLOPE_POINTS("p->uaqc_remb_slope_points", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_based_start_bitrate_enabled}.
     */
    P_UAQC_RTT_BASED_START_BITRATE_ENABLED("p->uaqc_rtt_based_start_bitrate_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_congestion_kf_with_ratio}.
     */
    P_UAQC_RTT_CONGESTION_KF_WITH_RATIO("p->uaqc_rtt_congestion_kf_with_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_congestion_step_kf_ms}.
     */
    P_UAQC_RTT_CONGESTION_STEP_KF_MS("p->uaqc_rtt_congestion_step_kf_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_ema_alpha}.
     */
    P_UAQC_RTT_EMA_ALPHA("p->uaqc_rtt_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_good_start_bitrate}.
     */
    P_UAQC_RTT_GOOD_START_BITRATE("p->uaqc_rtt_good_start_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_good_threshold_ms}.
     */
    P_UAQC_RTT_GOOD_THRESHOLD_MS("p->uaqc_rtt_good_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_kf_down_gain_scale_pct}.
     */
    P_UAQC_RTT_KF_DOWN_GAIN_SCALE_PCT("p->uaqc_rtt_kf_down_gain_scale_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_kf_up_gain_scale_pct}.
     */
    P_UAQC_RTT_KF_UP_GAIN_SCALE_PCT("p->uaqc_rtt_kf_up_gain_scale_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_poor_start_bitrate}.
     */
    P_UAQC_RTT_POOR_START_BITRATE("p->uaqc_rtt_poor_start_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_poor_threshold_ms}.
     */
    P_UAQC_RTT_POOR_THRESHOLD_MS("p->uaqc_rtt_poor_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_rtt_slope_points}.
     */
    P_UAQC_RTT_SLOPE_POINTS("p->uaqc_rtt_slope_points", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_slide_window_size}.
     */
    P_UAQC_SLIDE_WINDOW_SIZE("p->uaqc_slide_window_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_state_transition_hesitation_ms}.
     */
    P_UAQC_STATE_TRANSITION_HESITATION_MS("p->uaqc_state_transition_hesitation_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_target_bitrate_net_bitmap}.
     */
    P_UAQC_TARGET_BITRATE_NET_BITMAP("p->uaqc_target_bitrate_net_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ulb_neteq_delay_offset_ms}.
     */
    P_UAQC_ULB_NETEQ_DELAY_OFFSET_MS("p->uaqc_ulb_neteq_delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_fpp}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_FPP("p->uaqc_ultra_low_bandwidth_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_oob_factor}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_OOB_FACTOR("p->uaqc_ultra_low_bandwidth_oob_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_plr_lower_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_PLR_LOWER_THRESHOLD("p->uaqc_ultra_low_bandwidth_plr_lower_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_plr_upper_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_PLR_UPPER_THRESHOLD("p->uaqc_ultra_low_bandwidth_plr_upper_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_remb_lower_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_REMB_LOWER_THRESHOLD("p->uaqc_ultra_low_bandwidth_remb_lower_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_remb_trend_down_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_REMB_TREND_DOWN_THRESHOLD("p->uaqc_ultra_low_bandwidth_remb_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_remb_trend_up_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_REMB_TREND_UP_THRESHOLD("p->uaqc_ultra_low_bandwidth_remb_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_remb_upper_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_REMB_UPPER_THRESHOLD("p->uaqc_ultra_low_bandwidth_remb_upper_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_rtt_ratio_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_RTT_RATIO_THRESHOLD("p->uaqc_ultra_low_bandwidth_rtt_ratio_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_rtt_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_RTT_THRESHOLD("p->uaqc_ultra_low_bandwidth_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_rtt_trend_down_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_RTT_TREND_DOWN_THRESHOLD("p->uaqc_ultra_low_bandwidth_rtt_trend_down_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_rtt_trend_up_threshold}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_RTT_TREND_UP_THRESHOLD("p->uaqc_ultra_low_bandwidth_rtt_trend_up_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_target_bitrate}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_TARGET_BITRATE("p->uaqc_ultra_low_bandwidth_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_bandwidth_target_bitrate_net_offset}.
     */
    P_UAQC_ULTRA_LOW_BANDWIDTH_TARGET_BITRATE_NET_OFFSET("p->uaqc_ultra_low_bandwidth_target_bitrate_net_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_ultra_low_drain_mlow_dtx_hangover_ms}.
     */
    P_UAQC_ULTRA_LOW_DRAIN_MLOW_DTX_HANGOVER_MS("p->uaqc_ultra_low_drain_mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uaqc_use_rtt_ema}.
     */
    P_UAQC_USE_RTT_EMA("p->uaqc_use_rtt_ema", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;udp_bind_timeout_for_tcp_hostile_in_msec}.
     */
    P_UDP_BIND_TIMEOUT_FOR_TCP_HOSTILE_IN_MSEC("p->udp_bind_timeout_for_tcp_hostile_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;udp_bind_timeout_for_tcp_in_msec}.
     */
    P_UDP_BIND_TIMEOUT_FOR_TCP_IN_MSEC("p->udp_bind_timeout_for_tcp_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;udp_ping_on_tcp_interval}.
     */
    P_UDP_PING_ON_TCP_INTERVAL("p->udp_ping_on_tcp_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_aud_issue_thresh}.
     */
    P_UL_AUD_ISSUE_THRESH("p->ul_aud_issue_thresh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_fec_min_remaining_bw_kbps}.
     */
    P_UL_FEC_MIN_REMAINING_BW_KBPS("p->ul_fec_min_remaining_bw_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_fec_worst_dl_plr_thresh}.
     */
    P_UL_FEC_WORST_DL_PLR_THRESH("p->ul_fec_worst_dl_plr_thresh", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_lqm_calc_min_kbps}.
     */
    P_UL_LQM_CALC_MIN_KBPS("p->ul_lqm_calc_min_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_disable_min_kbps}.
     */
    P_UL_VID_DISABLE_MIN_KBPS("p->ul_vid_disable_min_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_disable_start_min_time_s}.
     */
    P_UL_VID_DISABLE_START_MIN_TIME_S("p->ul_vid_disable_start_min_time_s", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_disable_thresh}.
     */
    P_UL_VID_DISABLE_THRESH("p->ul_vid_disable_thresh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_max_pause_time_s}.
     */
    P_UL_VID_MAX_PAUSE_TIME_S("p->ul_vid_max_pause_time_s", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_min_kbps_offset}.
     */
    P_UL_VID_MIN_KBPS_OFFSET("p->ul_vid_min_kbps_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ul_vid_reenable_timer_s}.
     */
    P_UL_VID_REENABLE_TIMER_S("p->ul_vid_reenable_timer_s", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_a2a_ml_feature_val}.
     */
    P_UNDERSHOOT_A2A_ML_FEATURE_VAL("p->undershoot_a2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_a2i_ml_feature_val}.
     */
    P_UNDERSHOOT_A2I_ML_FEATURE_VAL("p->undershoot_a2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_default_platform_feature_val}.
     */
    P_UNDERSHOOT_DEFAULT_PLATFORM_FEATURE_VAL("p->undershoot_default_platform_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_i2a_ml_feature_val}.
     */
    P_UNDERSHOOT_I2A_ML_FEATURE_VAL("p->undershoot_i2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_i2i_ml_feature_val}.
     */
    P_UNDERSHOOT_I2I_ML_FEATURE_VAL("p->undershoot_i2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_model_name}.
     */
    P_UNDERSHOOT_MODEL_NAME("p->undershoot_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_model2_name}.
     */
    P_UNDERSHOOT_MODEL2_NAME("p->undershoot_model2_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;undershoot_web_ml_feature_val}.
     */
    P_UNDERSHOOT_WEB_ML_FEATURE_VAL("p->undershoot_web_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;update_dyn_video_settings_for_dl}.
     */
    P_UPDATE_DYN_VIDEO_SETTINGS_FOR_DL("p->update_dyn_video_settings_for_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;update_rbwe_options_for_av_upgrade}.
     */
    P_UPDATE_RBWE_OPTIONS_FOR_AV_UPGRADE("p->update_rbwe_options_for_av_upgrade", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;update_srtp_tx_delay}.
     */
    P_UPDATE_SRTP_TX_DELAY("p->update_srtp_tx_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;update_srtp_tx_delay_rekey_master}.
     */
    P_UPDATE_SRTP_TX_DELAY_REKEY_MASTER("p->update_srtp_tx_delay_rekey_master", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;update_ul_sbwe_vid_rc_params}.
     */
    P_UPDATE_UL_SBWE_VID_RC_PARAMS("p->update_ul_sbwe_vid_rc_params", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uplink_ml_feature_val}.
     */
    P_UPLINK_ML_FEATURE_VAL("p->uplink_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;use_alternate_relay}.
     */
    P_USE_ALTERNATE_RELAY("p->use_alternate_relay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_audio_module_br_est}.
     */
    P_USE_AUDIO_MODULE_BR_EST("p->use_audio_module_br_est", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_avg_echo_likelihood_for_agc}.
     */
    P_USE_AVG_ECHO_LIKELIHOOD_FOR_AGC("p->use_avg_echo_likelihood_for_agc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_batch_mode}.
     */
    P_USE_BATCH_MODE("p->use_batch_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_bt_mode_leveler_config}.
     */
    P_USE_BT_MODE_LEVELER_CONFIG("p->use_bt_mode_leveler_config", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_clean_capture}.
     */
    P_USE_CLEAN_CAPTURE("p->use_clean_capture", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_delay_for_rtt_congestion}.
     */
    P_USE_DELAY_FOR_RTT_CONGESTION("p->use_delay_for_rtt_congestion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_denoiser}.
     */
    P_USE_DENOISER("p->use_denoiser", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_denoiser_with_smpl_aec}.
     */
    P_USE_DENOISER_WITH_SMPL_AEC("p->use_denoiser_with_smpl_aec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_dynamic_samp_rate}.
     */
    P_USE_DYNAMIC_SAMP_RATE("p->use_dynamic_samp_rate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_executorch}.
     */
    P_USE_EXECUTORCH("p->use_executorch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_fec_rate_fix}.
     */
    P_USE_FEC_RATE_FIX("p->use_fec_rate_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_full_mode_beryl}.
     */
    P_USE_FULL_MODE_BERYL("p->use_full_mode_beryl", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_full_mode_beryl_speaker}.
     */
    P_USE_FULL_MODE_BERYL_SPEAKER("p->use_full_mode_beryl_speaker", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_gaussian_vad_noise_metric}.
     */
    P_USE_GAUSSIAN_VAD_NOISE_METRIC("p->use_gaussian_vad_noise_metric", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_hs_mode_leveler_config}.
     */
    P_USE_HS_MODE_LEVELER_CONFIG("p->use_hs_mode_leveler_config", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_last_udst_based_mult_decrease_target}.
     */
    P_USE_LAST_UDST_BASED_MULT_DECREASE_TARGET("p->use_last_udst_based_mult_decrease_target", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_last_udst_br}.
     */
    P_USE_LAST_UDST_BR("p->use_last_udst_br", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_legacy_agc_preprocess}.
     */
    P_USE_LEGACY_AGC_PREPROCESS("p->use_legacy_agc_preprocess", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_leveler}.
     */
    P_USE_LEVELER("p->use_leveler", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_leveler_bg_noise}.
     */
    P_USE_LEVELER_BG_NOISE("p->use_leveler_bg_noise", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_leveler_init_opt}.
     */
    P_USE_LEVELER_INIT_OPT("p->use_leveler_init_opt", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_low_int_leveler_on_echo}.
     */
    P_USE_LOW_INT_LEVELER_ON_ECHO("p->use_low_int_leveler_on_echo", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_lower_denoiser_threshold_with_ml_ns}.
     */
    P_USE_LOWER_DENOISER_THRESHOLD_WITH_ML_NS("p->use_lower_denoiser_threshold_with_ml_ns", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_max_echo_likelihood_for_agc}.
     */
    P_USE_MAX_ECHO_LIKELIHOOD_FOR_AGC("p->use_max_echo_likelihood_for_agc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns}.
     */
    P_USE_ML_NS("p->use_ml_ns", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_asp_load_safe_mode}.
     */
    P_USE_ML_NS_ASP_LOAD_SAFE_MODE("p->use_ml_ns_asp_load_safe_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_asp_work_thread}.
     */
    P_USE_ML_NS_ASP_WORK_THREAD("p->use_ml_ns_asp_work_thread", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_init_opt}.
     */
    P_USE_ML_NS_INIT_OPT("p->use_ml_ns_init_opt", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_init_safe_mode}.
     */
    P_USE_ML_NS_INIT_SAFE_MODE("p->use_ml_ns_init_safe_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_non_earpiece}.
     */
    P_USE_ML_NS_NON_EARPIECE("p->use_ml_ns_non_earpiece", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_pytorch_api_threads_guard}.
     */
    P_USE_ML_NS_PYTORCH_API_THREADS_GUARD("p->use_ml_ns_pytorch_api_threads_guard", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_ml_ns_use_pytorch_no_pthread_pool_guard}.
     */
    P_USE_ML_NS_USE_PYTORCH_NO_PTHREAD_POOL_GUARD("p->use_ml_ns_use_pytorch_no_pthread_pool_guard", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_mlow_codec}.
     */
    P_USE_MLOW_CODEC("p->use_mlow_codec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_per_ssrc_tx_key}.
     */
    P_USE_PER_SSRC_TX_KEY("p->use_per_ssrc_tx_key", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_plr_ema}.
     */
    P_USE_PLR_EMA("p->use_plr_ema", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_rbe}.
     */
    P_USE_RBE("p->use_rbe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_route_based_beryl_options}.
     */
    P_USE_ROUTE_BASED_BERYL_OPTIONS("p->use_route_based_beryl_options", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_sbwe_ramp_down_target_lower}.
     */
    P_USE_SBWE_RAMP_DOWN_TARGET_LOWER("p->use_sbwe_ramp_down_target_lower", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;use_smooth_leveler_mode}.
     */
    P_USE_SMOOTH_LEVELER_MODE("p->use_smooth_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_spkr_mode_leveler_config}.
     */
    P_USE_SPKR_MODE_LEVELER_CONFIG("p->use_spkr_mode_leveler_config", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_tf_leveler}.
     */
    P_USE_TF_LEVELER("p->use_tf_leveler", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_user_option_for_ml_ns}.
     */
    P_USE_USER_OPTION_FOR_ML_NS("p->use_user_option_for_ml_ns", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_user_option_for_ml_ns_bluetooth}.
     */
    P_USE_USER_OPTION_FOR_ML_NS_BLUETOOTH("p->use_user_option_for_ml_ns_bluetooth", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_user_option_for_ml_ns_headset}.
     */
    P_USE_USER_OPTION_FOR_ML_NS_HEADSET("p->use_user_option_for_ml_ns_headset", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_user_option_for_ml_ns_speaker}.
     */
    P_USE_USER_OPTION_FOR_ML_NS_SPEAKER("p->use_user_option_for_ml_ns_speaker", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_wa_ml_ns_intensity_impl}.
     */
    P_USE_WA_ML_NS_INTENSITY_IMPL("p->use_wa_ml_ns_intensity_impl", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;use_webrtc_latest}.
     */
    P_USE_WEBRTC_LATEST("p->use_webrtc_latest", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;uvq_frame_interval_ms}.
     */
    P_UVQ_FRAME_INTERVAL_MS("p->uvq_frame_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uvq_input_size}.
     */
    P_UVQ_INPUT_SIZE("p->uvq_input_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;uvq_model_name}.
     */
    P_UVQ_MODEL_NAME("p->uvq_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;uvq_use_luma}.
     */
    P_UVQ_USE_LUMA("p->uvq_use_luma", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;verify_sbwe_ramp_down_target_lower}.
     */
    P_VERIFY_SBWE_RAMP_DOWN_TARGET_LOWER("p->verify_sbwe_ramp_down_target_lower", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_max_br_pct}.
     */
    P_VID_MAX_BR_PCT("p->vid_max_br_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_nack_renack_probe_enabled}.
     */
    P_VID_NACK_RENACK_PROBE_ENABLED("p->vid_nack_renack_probe_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.enable_br_drop_fix}.
     */
    P_VID_UL_LQM_PARAMS_ENABLE_BR_DROP_FIX("p->vid_ul_lqm_params.enable_br_drop_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.enable_lqm_calc_logs}.
     */
    P_VID_UL_LQM_PARAMS_ENABLE_LQM_CALC_LOGS("p->vid_ul_lqm_params.enable_lqm_calc_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.good_lqm.max_plr_ema}.
     */
    P_VID_UL_LQM_PARAMS_GOOD_LQM_MAX_PLR_EMA("p->vid_ul_lqm_params.good_lqm.max_plr_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.good_lqm.max_rtt_ema_ms}.
     */
    P_VID_UL_LQM_PARAMS_GOOD_LQM_MAX_RTT_EMA_MS("p->vid_ul_lqm_params.good_lqm.max_rtt_ema_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.good_lqm.min_ave_bps}.
     */
    P_VID_UL_LQM_PARAMS_GOOD_LQM_MIN_AVE_BPS("p->vid_ul_lqm_params.good_lqm.min_ave_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.good_lqm.min_bps}.
     */
    P_VID_UL_LQM_PARAMS_GOOD_LQM_MIN_BPS("p->vid_ul_lqm_params.good_lqm.min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.lqm_sample_history_size}.
     */
    P_VID_UL_LQM_PARAMS_LQM_SAMPLE_HISTORY_SIZE("p->vid_ul_lqm_params.lqm_sample_history_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.min_lqm_samples}.
     */
    P_VID_UL_LQM_PARAMS_MIN_LQM_SAMPLES("p->vid_ul_lqm_params.min_lqm_samples", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.bps_ave_time_ms}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_BPS_AVE_TIME_MS("p->vid_ul_lqm_params.poor_lqm.bps_ave_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.bps_ema_size}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_BPS_EMA_SIZE("p->vid_ul_lqm_params.poor_lqm.bps_ema_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.bps_ignore_time_after_restart_ms}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_BPS_IGNORE_TIME_AFTER_RESTART_MS("p->vid_ul_lqm_params.poor_lqm.bps_ignore_time_after_restart_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.max_plr_ema}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_MAX_PLR_EMA("p->vid_ul_lqm_params.poor_lqm.max_plr_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.max_rtt_ema_ms}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_MAX_RTT_EMA_MS("p->vid_ul_lqm_params.poor_lqm.max_rtt_ema_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.min_ave_bps}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_MIN_AVE_BPS("p->vid_ul_lqm_params.poor_lqm.min_ave_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vid_ul_lqm_params.poor_lqm.min_bps}.
     */
    P_VID_UL_LQM_PARAMS_POOR_LQM_MIN_BPS("p->vid_ul_lqm_params.poor_lqm.min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_bitrate_actions}.
     */
    P_VIDEO_BITRATE_ACTIONS("p->video_bitrate_actions", VoipParamType.ARRAY, 128, false),

    /**
     * Native descriptor for {@code p-&gt;video_fec_on_pacer_egress}.
     */
    P_VIDEO_FEC_ON_PACER_EGRESS("p->video_fec_on_pacer_egress", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;video_hist_based_480p_time_threshold}.
     */
    P_VIDEO_HIST_BASED_480P_TIME_THRESHOLD("p->video_hist_based_480p_time_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_hist_based_720p_time_threshold}.
     */
    P_VIDEO_HIST_BASED_720P_TIME_THRESHOLD("p->video_hist_based_720p_time_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_hist_based_freeze_threshold}.
     */
    P_VIDEO_HIST_BASED_FREEZE_THRESHOLD("p->video_hist_based_freeze_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_hist_based_tx_pkt_loss_threshold}.
     */
    P_VIDEO_HIST_BASED_TX_PKT_LOSS_THRESHOLD("p->video_hist_based_tx_pkt_loss_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_rc_policy}.
     */
    P_VIDEO_RC_POLICY("p->video_rc_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_record_enable}.
     */
    P_VIDEO_RECORD_ENABLE("p->video_record_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_record_max_num_of_call_record}.
     */
    P_VIDEO_RECORD_MAX_NUM_OF_CALL_RECORD("p->video_record_max_num_of_call_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video_record_min_duration_to_save}.
     */
    P_VIDEO_RECORD_MIN_DURATION_TO_SAVE("p->video_record_min_duration_to_save", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video.max_cache_size}.
     */
    P_VIDEO_MAX_CACHE_SIZE("p->video.max_cache_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;video.max_rtx_resends}.
     */
    P_VIDEO_MAX_RTX_RESENDS("p->video.max_rtx_resends", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vmos_model_name}.
     */
    P_VMOS_MODEL_NAME("p->vmos_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_battery_level}.
     */
    P_VSR_DISABLE_BATTERY_LEVEL("p->vsr_disable_battery_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_hidden_state_updates}.
     */
    P_VSR_DISABLE_HIDDEN_STATE_UPDATES("p->vsr_disable_hidden_state_updates", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_max_inference_error_count}.
     */
    P_VSR_DISABLE_MAX_INFERENCE_ERROR_COUNT("p->vsr_disable_max_inference_error_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_max_inference_latency_ms}.
     */
    P_VSR_DISABLE_MAX_INFERENCE_LATENCY_MS("p->vsr_disable_max_inference_latency_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_min_psnr}.
     */
    P_VSR_DISABLE_MIN_PSNR("p->vsr_disable_min_psnr", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_thermal_frame_count}.
     */
    P_VSR_DISABLE_THERMAL_FRAME_COUNT("p->vsr_disable_thermal_frame_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_disable_thermal_level}.
     */
    P_VSR_DISABLE_THERMAL_LEVEL("p->vsr_disable_thermal_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_dummy_inf}.
     */
    P_VSR_DUMMY_INF("p->vsr_dummy_inf", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_enable_clipping}.
     */
    P_VSR_ENABLE_CLIPPING("p->vsr_enable_clipping", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_enable_hidden_reset_resolution_changes}.
     */
    P_VSR_ENABLE_HIDDEN_RESET_RESOLUTION_CHANGES("p->vsr_enable_hidden_reset_resolution_changes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_enable_psnr_calculation}.
     */
    P_VSR_ENABLE_PSNR_CALCULATION("p->vsr_enable_psnr_calculation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_has_hidden_state}.
     */
    P_VSR_HAS_HIDDEN_STATE("p->vsr_has_hidden_state", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_inference_latency_window_size}.
     */
    P_VSR_INFERENCE_LATENCY_WINDOW_SIZE("p->vsr_inference_latency_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_is_float32}.
     */
    P_VSR_IS_FLOAT32("p->vsr_is_float32", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_load_outside_lock}.
     */
    P_VSR_LOAD_OUTSIDE_LOCK("p->vsr_load_outside_lock", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_height}.
     */
    P_VSR_MODEL_HEIGHT("p->vsr_model_height", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_hidden_dim[0]}.
     */
    P_VSR_MODEL_HIDDEN_DIM_0("p->vsr_model_hidden_dim[0]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_hidden_dim[1]}.
     */
    P_VSR_MODEL_HIDDEN_DIM_1("p->vsr_model_hidden_dim[1]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_hidden_dim[2]}.
     */
    P_VSR_MODEL_HIDDEN_DIM_2("p->vsr_model_hidden_dim[2]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_hidden_dim[3]}.
     */
    P_VSR_MODEL_HIDDEN_DIM_3("p->vsr_model_hidden_dim[3]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_load_max_retry}.
     */
    P_VSR_MODEL_LOAD_MAX_RETRY("p->vsr_model_load_max_retry", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_load_retry_interval_s}.
     */
    P_VSR_MODEL_LOAD_RETRY_INTERVAL_S("p->vsr_model_load_retry_interval_s", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_name}.
     */
    P_VSR_MODEL_NAME("p->vsr_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_model_width}.
     */
    P_VSR_MODEL_WIDTH("p->vsr_model_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_psnr_sample_interval}.
     */
    P_VSR_PSNR_SAMPLE_INTERVAL("p->vsr_psnr_sample_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;vsr_use_optimized_frame_processing}.
     */
    P_VSR_USE_OPTIMIZED_FRAME_PROCESSING("p->vsr_use_optimized_frame_processing", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_max_integral}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_MAX_INTEGRAL("p->wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_max_integral", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_min_decrease_ratio}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_MIN_DECREASE_RATIO("p->wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_min_decrease_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_min_integral}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_MIN_INTEGRAL("p->wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_min_integral", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_signal_multiplier}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_SIGNAL_MULTIPLIER("p->wa_ml_cong_pid_controller_params .wa_pid_controller_ml_cong_signal_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_kd}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_KD("p->wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_kd", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_ki}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_KI("p->wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_ki", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_kp}.
     */
    P_WA_ML_CONG_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_ML_CONG_KP("p->wa_ml_cong_pid_controller_params.wa_pid_controller_ml_cong_kp", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_adjust_enable}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_INC_ADJUST_ENABLE("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_adjust_enable", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_factor_multiplier}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_INC_FACTOR_MULTIPLIER("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_factor_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_ratio_diff_to_reset}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_INC_RATIO_DIFF_TO_RESET("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_ratio_diff_to_reset", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_ratio_reset_by_init}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_INC_RATIO_RESET_BY_INIT("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_inc_ratio_reset_by_init", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_init_inc_factor_multiplier}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_INIT_INC_FACTOR_MULTIPLIER("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_init_inc_factor_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_setpoint_multiplier}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_SETPOINT_MULTIPLIER("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_setpoint_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_signal_multiplier}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_SIGNAL_MULTIPLIER("p->wa_pid_controller_params .wa_pid_controller_rtt_ramp_up_signal_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_enable}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_ENABLE("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_enable", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_kd}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_KD("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_kd", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_ki}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_KI("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_ki", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_kp}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_KP("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_kp", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_max_integral}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_MAX_INTEGRAL("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_max_integral", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_min_integral}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_MIN_INTEGRAL("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_min_integral", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_setpoint_alpha}.
     */
    P_WA_PID_CONTROLLER_PARAMS_WA_PID_CONTROLLER_RTT_RAMP_UP_SETPOINT_ALPHA("p->wa_pid_controller_params.wa_pid_controller_rtt_ramp_up_setpoint_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wa_storage_params.use_pj_file_api}.
     */
    P_WA_STORAGE_PARAMS_USE_PJ_FILE_API("p->wa_storage_params.use_pj_file_api", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;warp_seq_num_check_delay_ms}.
     */
    P_WARP_SEQ_NUM_CHECK_DELAY_MS("p->warp_seq_num_check_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;warp_seq_num_scheme}.
     */
    P_WARP_SEQ_NUM_SCHEME("p->warp_seq_num_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;web_detailed_call_transport_record_enable}.
     */
    P_WEB_DETAILED_CALL_TRANSPORT_RECORD_ENABLE("p->web_detailed_call_transport_record_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;web_ml_feature_val}.
     */
    P_WEB_ML_FEATURE_VAL("p->web_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.action_on_rtp_marker}.
     */
    P_WEBRTC_ACTION_ON_RTP_MARKER("p->webrtc.action_on_rtp_marker", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.bwe_mode}.
     */
    P_WEBRTC_BWE_MODE("p->webrtc.bwe_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.max_unknown_on_rate_increase}.
     */
    P_WEBRTC_MAX_UNKNOWN_ON_RATE_INCREASE("p->webrtc.max_unknown_on_rate_increase", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.override_fec_ssrc_with_rtp_ssrc}.
     */
    P_WEBRTC_OVERRIDE_FEC_SSRC_WITH_RTP_SSRC("p->webrtc.override_fec_ssrc_with_rtp_ssrc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.reset_oud_timestamp_on_bwe_reset}.
     */
    P_WEBRTC_RESET_OUD_TIMESTAMP_ON_BWE_RESET("p->webrtc.reset_oud_timestamp_on_bwe_reset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.reset_rcc_on_bwe_reset}.
     */
    P_WEBRTC_RESET_RCC_ON_BWE_RESET("p->webrtc.reset_rcc_on_bwe_reset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.start_remb_with_init_bwe}.
     */
    P_WEBRTC_START_REMB_WITH_INIT_BWE("p->webrtc.start_remb_with_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;webrtc.start_remb_with_init_bwe_multiplier}.
     */
    P_WEBRTC_START_REMB_WITH_INIT_BWE_MULTIPLIER("p->webrtc.start_remb_with_init_bwe_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;wifi_ml_feature_val}.
     */
    P_WIFI_ML_FEATURE_VAL("p->wifi_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;xra_echo_metrics_enabled}.
     */
    P_XRA_ECHO_METRICS_ENABLED("p->xra_echo_metrics_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;xra_pre_echo_metrics_enabled}.
     */
    P_XRA_PRE_ECHO_METRICS_ENABLED("p->xra_pre_echo_metrics_enabled", VoipParamType.INTEGER, 1, false);

    /**
     * The fully-qualified dotted path the engine addresses this tunable by.
     */
    private final String dottedPath;

    /**
     * The native descriptor value type for this tunable.
     */
    private final VoipParamType type;

    /**
     * The serialized byte width recorded in the native descriptor.
     */
    private final int byteWidth;

    /**
     * Whether the native descriptor marks this tunable as rate-control related.
     */
    private final boolean bweParam;

    /**
     * Constructs a key partition constant from its native descriptor fields.
     *
     * @param dottedPath the fully-qualified dotted path
     * @param type       the native descriptor value type
     * @param byteWidth   the serialized byte width
     * @param bweParam   whether this is a rate-control tunable
     */
    VoipParamKeyCall3(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {
        this.dottedPath = dottedPath;
        this.type = type;
        this.byteWidth = byteWidth;
        this.bweParam = bweParam;
    }

    @Override
    public String dottedPath() {
        return dottedPath;
    }

    @Override
    public VoipParamType type() {
        return type;
    }

    @Override
    public int byteWidth() {
        return byteWidth;
    }

    @Override
    public boolean bweParam() {
        return bweParam;
    }
}
