package com.github.auties00.cobalt.calls2.common;

/**
 * A partition of the {@code p-&gt;} voip-param registry keys.
 *
 * <p>This enum exists only to keep its generated static initializer within the JVM 64KB
 * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}
 * rather than this partition directly.
 */
enum VoipParamKeyCall1 implements VoipParamKey {
    /**
     * Native descriptor for {@code p-&gt;a2a_ml_feature_val}.
     */
    P_A2A_ML_FEATURE_VAL("p->a2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;a2i_ml_feature_val}.
     */
    P_A2I_ML_FEATURE_VAL("p->a2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;abs_rtt_congestion_threshold}.
     */
    P_ABS_RTT_CONGESTION_THRESHOLD("p->abs_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;abs_rtt_on_hold_threshold}.
     */
    P_ABS_RTT_ON_HOLD_THRESHOLD("p->abs_rtt_on_hold_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_leveler_ec_thres}.
     */
    P_ADAPTIVE_LEVELER_EC_THRES("p->adaptive_leveler_ec_thres", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_leveler_mode}.
     */
    P_ADAPTIVE_LEVELER_MODE("p->adaptive_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_leveler_mode_hi_low}.
     */
    P_ADAPTIVE_LEVELER_MODE_HI_LOW("p->adaptive_leveler_mode_hi_low", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_leveler_mode_min_intensity}.
     */
    P_ADAPTIVE_LEVELER_MODE_MIN_INTENSITY("p->adaptive_leveler_mode_min_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_leveler_mode_rev}.
     */
    P_ADAPTIVE_LEVELER_MODE_REV("p->adaptive_leveler_mode_rev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.bitrate_threshold_for_switch}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_BITRATE_THRESHOLD_FOR_SWITCH("p->adaptive_probing_settings.bitrate_threshold_for_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.disable_probe_down}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_DISABLE_PROBE_DOWN("p->adaptive_probing_settings.disable_probe_down", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.down_learning_rate}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_DOWN_LEARNING_RATE("p->adaptive_probing_settings.down_learning_rate", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.ema_alpha}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_EMA_ALPHA("p->adaptive_probing_settings.ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.enable_dyn_switch2}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_ENABLE_DYN_SWITCH2("p->adaptive_probing_settings.enable_dyn_switch2", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.enable_dynamic_switch}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_ENABLE_DYNAMIC_SWITCH("p->adaptive_probing_settings.enable_dynamic_switch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.enable_ema_estimation}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_ENABLE_EMA_ESTIMATION("p->adaptive_probing_settings.enable_ema_estimation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.enable_train_probing}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_ENABLE_TRAIN_PROBING("p->adaptive_probing_settings.enable_train_probing", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.margin_for_hold}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MARGIN_FOR_HOLD("p->adaptive_probing_settings.margin_for_hold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.margin_for_up}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MARGIN_FOR_UP("p->adaptive_probing_settings.margin_for_up", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.max_bps}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MAX_BPS("p->adaptive_probing_settings.max_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.max_packet_pairs}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MAX_PACKET_PAIRS("p->adaptive_probing_settings.max_packet_pairs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.max_train_probing_pkts}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MAX_TRAIN_PROBING_PKTS("p->adaptive_probing_settings.max_train_probing_pkts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.max_train_probing_rounds}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MAX_TRAIN_PROBING_ROUNDS("p->adaptive_probing_settings.max_train_probing_rounds", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.min_bps}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_MIN_BPS("p->adaptive_probing_settings.min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.pct_down}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PCT_DOWN("p->adaptive_probing_settings.pct_down", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.pct_up}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PCT_UP("p->adaptive_probing_settings.pct_up", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.pdt_down}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PDT_DOWN("p->adaptive_probing_settings.pdt_down", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.pdt_up}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PDT_UP("p->adaptive_probing_settings.pdt_up", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.probe_next}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PROBE_NEXT("p->adaptive_probing_settings.probe_next", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.probing_pkt_size}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PROBING_PKT_SIZE("p->adaptive_probing_settings.probing_pkt_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.probing_reset_timeout_ms}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_PROBING_RESET_TIMEOUT_MS("p->adaptive_probing_settings.probing_reset_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.rtt_threshold_for_switch}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_RTT_THRESHOLD_FOR_SWITCH("p->adaptive_probing_settings.rtt_threshold_for_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.scheme}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_SCHEME("p->adaptive_probing_settings.scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.start_bps}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_START_BPS("p->adaptive_probing_settings.start_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.target_relaxation_factor}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_TARGET_RELAXATION_FACTOR("p->adaptive_probing_settings.target_relaxation_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.up_learning_rate}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_UP_LEARNING_RATE("p->adaptive_probing_settings.up_learning_rate", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.use_dl_recv_bytes}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_USE_DL_RECV_BYTES("p->adaptive_probing_settings.use_dl_recv_bytes", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_probing_settings.use_ul_server_ts}.
     */
    P_ADAPTIVE_PROBING_SETTINGS_USE_UL_SERVER_TS("p->adaptive_probing_settings.use_ul_server_ts", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_ramp_up_speed_bw_thresh_kbps}.
     */
    P_ADAPTIVE_RAMP_UP_SPEED_BW_THRESH_KBPS("p->adaptive_ramp_up_speed_bw_thresh_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_ramp_up_speed_decr_ratio}.
     */
    P_ADAPTIVE_RAMP_UP_SPEED_DECR_RATIO("p->adaptive_ramp_up_speed_decr_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adaptive_ramp_up_speed_min_incr_factor}.
     */
    P_ADAPTIVE_RAMP_UP_SPEED_MIN_INCR_FACTOR("p->adaptive_ramp_up_speed_min_incr_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additional_iter_threshold}.
     */
    P_ADDITIONAL_ITER_THRESHOLD("p->additional_iter_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additive_forced_probing_factor}.
     */
    P_ADDITIVE_FORCED_PROBING_FACTOR("p->additive_forced_probing_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additive_sender_bwe_inc_ceiling_multiplier}.
     */
    P_ADDITIVE_SENDER_BWE_INC_CEILING_MULTIPLIER("p->additive_sender_bwe_inc_ceiling_multiplier", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;additive_sender_bwe_inc_ema_weight}.
     */
    P_ADDITIVE_SENDER_BWE_INC_EMA_WEIGHT("p->additive_sender_bwe_inc_ema_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additive_sender_bwe_inc_near_max}.
     */
    P_ADDITIVE_SENDER_BWE_INC_NEAR_MAX("p->additive_sender_bwe_inc_near_max", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additive_sender_bwe_inc_skip_first_n}.
     */
    P_ADDITIVE_SENDER_BWE_INC_SKIP_FIRST_N("p->additive_sender_bwe_inc_skip_first_n", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;additive_sender_bwe_inc_use_ceiling_from_cc}.
     */
    P_ADDITIVE_SENDER_BWE_INC_USE_CEILING_FROM_CC("p->additive_sender_bwe_inc_use_ceiling_from_cc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;adjust_vid_bitrate_using_e2e_fec_ratio}.
     */
    P_ADJUST_VID_BITRATE_USING_E2E_FEC_RATIO("p->adjust_vid_bitrate_using_e2e_fec_ratio", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;aecm_echo_mode}.
     */
    P_AECM_ECHO_MODE("p->aecm_echo_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aecm_echo_speaker_mode}.
     */
    P_AECM_ECHO_SPEAKER_MODE("p->aecm_echo_speaker_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;agc_serialize_farend_on_capture}.
     */
    P_AGC_SERIALIZE_FAREND_ON_CAPTURE("p->agc_serialize_farend_on_capture", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;agc2_adaptive_digital_mode}.
     */
    P_AGC2_ADAPTIVE_DIGITAL_MODE("p->agc2_adaptive_digital_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;allow_dynamic_frame_size}.
     */
    P_ALLOW_DYNAMIC_FRAME_SIZE("p->allow_dynamic_frame_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;allow_exit_forced_probing_early}.
     */
    P_ALLOW_EXIT_FORCED_PROBING_EARLY("p->allow_exit_forced_probing_early", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;allow_no_delay_duplication}.
     */
    P_ALLOW_NO_DELAY_DUPLICATION("p->allow_no_delay_duplication", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;always_upd_last_udst_br}.
     */
    P_ALWAYS_UPD_LAST_UDST_BR("p->always_upd_last_udst_br", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;app_mode}.
     */
    P_APP_MODE("p->app_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;apply_2p_info_init_sfu_ul_bwe_multiplier_threshold}.
     */
    P_APPLY_2P_INFO_INIT_SFU_UL_BWE_MULTIPLIER_THRESHOLD("p->apply_2p_info_init_sfu_ul_bwe_multiplier_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;apply_hd_max_tgt_bitrate}.
     */
    P_APPLY_HD_MAX_TGT_BITRATE("p->apply_hd_max_tgt_bitrate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;apply_ramp_down_enc_params}.
     */
    P_APPLY_RAMP_DOWN_ENC_PARAMS("p->apply_ramp_down_enc_params", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;aud_issue_det_config.enabled_mask}.
     */
    P_AUD_ISSUE_DET_CONFIG_ENABLED_MASK("p->aud_issue_det_config.enabled_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_issue_det_config.hold_timer_ms}.
     */
    P_AUD_ISSUE_DET_CONFIG_HOLD_TIMER_MS("p->aud_issue_det_config.hold_timer_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_issue_det_config.plr_ema_size}.
     */
    P_AUD_ISSUE_DET_CONFIG_PLR_EMA_SIZE("p->aud_issue_det_config.plr_ema_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_issue_det_config.plr_threshold}.
     */
    P_AUD_ISSUE_DET_CONFIG_PLR_THRESHOLD("p->aud_issue_det_config.plr_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_share_mixer_release_time_ms}.
     */
    P_AUD_SHARE_MIXER_RELEASE_TIME_MS("p->aud_share_mixer_release_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_share_mixer_samp_rate}.
     */
    P_AUD_SHARE_MIXER_SAMP_RATE("p->aud_share_mixer_samp_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_share_mixer_target_gain}.
     */
    P_AUD_SHARE_MIXER_TARGET_GAIN("p->aud_share_mixer_target_gain", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.enable_br_drop_fix}.
     */
    P_AUD_UL_LQM_PARAMS_ENABLE_BR_DROP_FIX("p->aud_ul_lqm_params.enable_br_drop_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.enable_lqm_calc_logs}.
     */
    P_AUD_UL_LQM_PARAMS_ENABLE_LQM_CALC_LOGS("p->aud_ul_lqm_params.enable_lqm_calc_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.good_lqm.max_plr_ema}.
     */
    P_AUD_UL_LQM_PARAMS_GOOD_LQM_MAX_PLR_EMA("p->aud_ul_lqm_params.good_lqm.max_plr_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.good_lqm.max_rtt_ema_ms}.
     */
    P_AUD_UL_LQM_PARAMS_GOOD_LQM_MAX_RTT_EMA_MS("p->aud_ul_lqm_params.good_lqm.max_rtt_ema_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.good_lqm.min_ave_bps}.
     */
    P_AUD_UL_LQM_PARAMS_GOOD_LQM_MIN_AVE_BPS("p->aud_ul_lqm_params.good_lqm.min_ave_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.good_lqm.min_bps}.
     */
    P_AUD_UL_LQM_PARAMS_GOOD_LQM_MIN_BPS("p->aud_ul_lqm_params.good_lqm.min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.lqm_sample_history_size}.
     */
    P_AUD_UL_LQM_PARAMS_LQM_SAMPLE_HISTORY_SIZE("p->aud_ul_lqm_params.lqm_sample_history_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.min_lqm_samples}.
     */
    P_AUD_UL_LQM_PARAMS_MIN_LQM_SAMPLES("p->aud_ul_lqm_params.min_lqm_samples", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.bps_ave_time_ms}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_BPS_AVE_TIME_MS("p->aud_ul_lqm_params.poor_lqm.bps_ave_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.bps_ema_size}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_BPS_EMA_SIZE("p->aud_ul_lqm_params.poor_lqm.bps_ema_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.bps_ignore_time_after_restart_ms}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_BPS_IGNORE_TIME_AFTER_RESTART_MS("p->aud_ul_lqm_params.poor_lqm.bps_ignore_time_after_restart_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.max_plr_ema}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_MAX_PLR_EMA("p->aud_ul_lqm_params.poor_lqm.max_plr_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.max_rtt_ema_ms}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_MAX_RTT_EMA_MS("p->aud_ul_lqm_params.poor_lqm.max_rtt_ema_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.min_ave_bps}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_MIN_AVE_BPS("p->aud_ul_lqm_params.poor_lqm.min_ave_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;aud_ul_lqm_params.poor_lqm.min_bps}.
     */
    P_AUD_UL_LQM_PARAMS_POOR_LQM_MIN_BPS("p->aud_ul_lqm_params.poor_lqm.min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_bitrate_cap}.
     */
    P_AUDIO_BITRATE_CAP("p->audio_bitrate_cap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_bitrate_reserve}.
     */
    P_AUDIO_BITRATE_RESERVE("p->audio_bitrate_reserve", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_duplication_exploration_max_duration_ms}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_DUPLICATION_EXPLORATION_MAX_DURATION_MS("p->audio_duplication_exploration_params .audio_duplication_exploration_max_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_duplication_exploration_min_duration_ms}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_DUPLICATION_EXPLORATION_MIN_DURATION_MS("p->audio_duplication_exploration_params .audio_duplication_exploration_min_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_duplication_exploration_min_wait_time_ms}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_DUPLICATION_EXPLORATION_MIN_WAIT_TIME_MS("p->audio_duplication_exploration_params .audio_duplication_exploration_min_wait_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_duplication_off_exploration_prob_perc}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_DUPLICATION_OFF_EXPLORATION_PROB_PERC("p->audio_duplication_exploration_params .audio_duplication_off_exploration_prob_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_duplication_on_exploration_prob_perc}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_DUPLICATION_ON_EXPLORATION_PROB_PERC("p->audio_duplication_exploration_params .audio_duplication_on_exploration_prob_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .audio_resend_interval_for_exploration_msec}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_AUDIO_RESEND_INTERVAL_FOR_EXPLORATION_MSEC("p->audio_duplication_exploration_params .audio_resend_interval_for_exploration_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_duplication_exploration_params .enable_audio_duplication_exploration}.
     */
    P_AUDIO_DUPLICATION_EXPLORATION_PARAMS_ENABLE_AUDIO_DUPLICATION_EXPLORATION("p->audio_duplication_exploration_params .enable_audio_duplication_exploration", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_fec_disable_encoding}.
     */
    P_AUDIO_FEC_DISABLE_ENCODING("p->audio_fec_disable_encoding", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_nack_algo_mask}.
     */
    P_AUDIO_NACK_ALGO_MASK("p->audio_nack_algo_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_nack_disable_rtp_retransmit}.
     */
    P_AUDIO_NACK_DISABLE_RTP_RETRANSMIT("p->audio_nack_disable_rtp_retransmit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_nack_discarded_frames}.
     */
    P_AUDIO_NACK_DISCARDED_FRAMES("p->audio_nack_discarded_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_nack_max_jb_delay}.
     */
    P_AUDIO_NACK_MAX_JB_DELAY("p->audio_nack_max_jb_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_oob_fec_max_pkts}.
     */
    P_AUDIO_OOB_FEC_MAX_PKTS("p->audio_oob_fec_max_pkts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_oob_fec_min_pkts}.
     */
    P_AUDIO_OOB_FEC_MIN_PKTS("p->audio_oob_fec_min_pkts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_oob_fec_ratio}.
     */
    P_AUDIO_OOB_FEC_RATIO("p->audio_oob_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_piggyback_timeout_msec}.
     */
    P_AUDIO_PIGGYBACK_TIMEOUT_MSEC("p->audio_piggyback_timeout_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_priority}.
     */
    P_AUDIO_PRIORITY("p->audio_priority", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_record_clock_cb_threshold}.
     */
    P_AUDIO_RECORD_CLOCK_CB_THRESHOLD("p->audio_record_clock_cb_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_resend_interval_msec}.
     */
    P_AUDIO_RESEND_INTERVAL_MSEC("p->audio_resend_interval_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio_reserve_bps}.
     */
    P_AUDIO_RESERVE_BPS("p->audio_reserve_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio.max_cache_size}.
     */
    P_AUDIO_MAX_CACHE_SIZE("p->audio.max_cache_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;audio.max_rtx_resends}.
     */
    P_AUDIO_MAX_RTX_RESENDS("p->audio.max_rtx_resends", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bad_action}.
     */
    P_BAD_ACTION("p->bad_action", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_clamp_bitrate_bps}.
     */
    P_BAD_CLAMP_BITRATE_BPS("p->bad_clamp_bitrate_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_mode_duration_ms}.
     */
    P_BAD_MODE_DURATION_MS("p->bad_mode_duration_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_pp_clamp_multiplier}.
     */
    P_BAD_PP_CLAMP_MULTIPLIER("p->bad_pp_clamp_multiplier", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_ramp_down_percentage}.
     */
    P_BAD_RAMP_DOWN_PERCENTAGE("p->bad_ramp_down_percentage", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_reset_ml_tr_ts}.
     */
    P_BAD_RESET_ML_TR_TS("p->bad_reset_ml_tr_ts", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_bitrate_hi_threshold}.
     */
    P_BAD_TR_BITRATE_HI_THRESHOLD("p->bad_tr_bitrate_hi_threshold", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_bitrate_lo_threshold}.
     */
    P_BAD_TR_BITRATE_LO_THRESHOLD("p->bad_tr_bitrate_lo_threshold", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_probability_hi_threshold}.
     */
    P_BAD_TR_PROBABILITY_HI_THRESHOLD("p->bad_tr_probability_hi_threshold", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_probability_hi_threshold_transport}.
     */
    P_BAD_TR_PROBABILITY_HI_THRESHOLD_TRANSPORT("p->bad_tr_probability_hi_threshold_transport", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_probability_lo_threshold}.
     */
    P_BAD_TR_PROBABILITY_LO_THRESHOLD("p->bad_tr_probability_lo_threshold", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;bad_tr_probability_lo_threshold_transport}.
     */
    P_BAD_TR_PROBABILITY_LO_THRESHOLD_TRANSPORT("p->bad_tr_probability_lo_threshold_transport", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;beryl_aec_latest_enable}.
     */
    P_BERYL_AEC_LATEST_ENABLE("p->beryl_aec_latest_enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;beryl_aec_latest_enable_speaker}.
     */
    P_BERYL_AEC_LATEST_ENABLE_SPEAKER("p->beryl_aec_latest_enable_speaker", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;beryl_aec_prealloc_buffers}.
     */
    P_BERYL_AEC_PREALLOC_BUFFERS("p->beryl_aec_prealloc_buffers", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;beryl_opt_type}.
     */
    P_BERYL_OPT_TYPE("p->beryl_opt_type", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bipolar_compression_en}.
     */
    P_BIPOLAR_COMPRESSION_EN("p->bipolar_compression_en", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate}.
     */
    P_BITRATE("p->bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_multiplier_per_iter}.
     */
    P_BITRATE_MULTIPLIER_PER_ITER("p->bitrate_multiplier_per_iter", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_is_paused}.
     */
    P_BITRATE_PROBING_IS_PAUSED("p->bitrate_probing_is_paused", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_max_probed_bitrate}.
     */
    P_BITRATE_PROBING_MAX_PROBED_BITRATE("p->bitrate_probing_max_probed_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_max_wait_ms_for_initial_probing}.
     */
    P_BITRATE_PROBING_MAX_WAIT_MS_FOR_INITIAL_PROBING("p->bitrate_probing_max_wait_ms_for_initial_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_min_bitrate_for_initial_probing}.
     */
    P_BITRATE_PROBING_MIN_BITRATE_FOR_INITIAL_PROBING("p->bitrate_probing_min_bitrate_for_initial_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_min_pkts_for_cluster}.
     */
    P_BITRATE_PROBING_MIN_PKTS_FOR_CLUSTER("p->bitrate_probing_min_pkts_for_cluster", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_min_quiet_period}.
     */
    P_BITRATE_PROBING_MIN_QUIET_PERIOD("p->bitrate_probing_min_quiet_period", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bitrate_probing_min_wait_ms_for_initial_probing}.
     */
    P_BITRATE_PROBING_MIN_WAIT_MS_FOR_INITIAL_PROBING("p->bitrate_probing_min_wait_ms_for_initial_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;br_adj_factor}.
     */
    P_BR_ADJ_FACTOR("p->br_adj_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.audio_bitrate_reserve}.
     */
    P_BRC_PARAMS_AUDIO_BITRATE_RESERVE("p->brc_params.audio_bitrate_reserve", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.audio_reserve_for_muted_participant}.
     */
    P_BRC_PARAMS_AUDIO_RESERVE_FOR_MUTED_PARTICIPANT("p->brc_params.audio_reserve_for_muted_participant", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.bwa_rc_enabled}.
     */
    P_BRC_PARAMS_BWA_RC_ENABLED("p->brc_params.bwa_rc_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.do_s_nadl_client_bwa}.
     */
    P_BRC_PARAMS_DO_S_NADL_CLIENT_BWA("p->brc_params.do_s_nadl_client_bwa", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.hybrid_bwa_params.hybrid_sbwa_send_at_least_one_stream}.
     */
    P_BRC_PARAMS_HYBRID_BWA_PARAMS_HYBRID_SBWA_SEND_AT_LEAST_ONE_STREAM("p->brc_params.hybrid_bwa_params.hybrid_sbwa_send_at_least_one_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.hybrid_bwa_params.hybrid_update_freq_msec}.
     */
    P_BRC_PARAMS_HYBRID_BWA_PARAMS_HYBRID_UPDATE_FREQ_MSEC("p->brc_params.hybrid_bwa_params.hybrid_update_freq_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.ignore_sbwa_on_participant_mismatch_since_segment_start}.
     */
    P_BRC_PARAMS_IGNORE_SBWA_ON_PARTICIPANT_MISMATCH_SINCE_SEGMENT_START("p->brc_params.ignore_sbwa_on_participant_mismatch_since_segment_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.rc_data_request_list_derive_from_speaker_info}.
     */
    P_BRC_PARAMS_RC_DATA_REQUEST_LIST_DERIVE_FROM_SPEAKER_INFO("p->brc_params.rc_data_request_list_derive_from_speaker_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.rc_data_request_list_use_rx_subscription_info}.
     */
    P_BRC_PARAMS_RC_DATA_REQUEST_LIST_USE_RX_SUBSCRIPTION_INFO("p->brc_params.rc_data_request_list_use_rx_subscription_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_bwa_high_bwe_kbps_threshold}.
     */
    P_BRC_PARAMS_S_NADL_BWA_HIGH_BWE_KBPS_THRESHOLD("p->brc_params.s_nadl_bwa_high_bwe_kbps_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_bwa_low_bwe_kbps_threshold}.
     */
    P_BRC_PARAMS_S_NADL_BWA_LOW_BWE_KBPS_THRESHOLD("p->brc_params.s_nadl_bwa_low_bwe_kbps_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_max_reduction_percentage}.
     */
    P_BRC_PARAMS_S_NADL_MAX_REDUCTION_PERCENTAGE("p->brc_params.s_nadl_max_reduction_percentage", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_min_bandwidth_floor_kbps}.
     */
    P_BRC_PARAMS_S_NADL_MIN_BANDWIDTH_FLOOR_KBPS("p->brc_params.s_nadl_min_bandwidth_floor_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_nr_fec_plr_threshold}.
     */
    P_BRC_PARAMS_S_NADL_NR_FEC_PLR_THRESHOLD("p->brc_params.s_nadl_nr_fec_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_nr_fec_reserve_rate}.
     */
    P_BRC_PARAMS_S_NADL_NR_FEC_RESERVE_RATE("p->brc_params.s_nadl_nr_fec_reserve_rate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_plr_window_count}.
     */
    P_BRC_PARAMS_S_NADL_PLR_WINDOW_COUNT("p->brc_params.s_nadl_plr_window_count", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.s_nadl_plr_window_length_ms}.
     */
    P_BRC_PARAMS_S_NADL_PLR_WINDOW_LENGTH_MS("p->brc_params.s_nadl_plr_window_length_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.server_params.adjust_br_on_num_participant_diff}.
     */
    P_BRC_PARAMS_SERVER_PARAMS_ADJUST_BR_ON_NUM_PARTICIPANT_DIFF("p->brc_params.server_params.adjust_br_on_num_participant_diff", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.server_params.msec_to_wait_before_client_bwa_fallback}.
     */
    P_BRC_PARAMS_SERVER_PARAMS_MSEC_TO_WAIT_BEFORE_CLIENT_BWA_FALLBACK("p->brc_params.server_params.msec_to_wait_before_client_bwa_fallback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.server_params.sbwa_result_logging_freq_msec}.
     */
    P_BRC_PARAMS_SERVER_PARAMS_SBWA_RESULT_LOGGING_FREQ_MSEC("p->brc_params.server_params.sbwa_result_logging_freq_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.set_muted_participant_audio_reserve}.
     */
    P_BRC_PARAMS_SET_MUTED_PARTICIPANT_AUDIO_RESERVE("p->brc_params.set_muted_participant_audio_reserve", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.sfu_vid_rc_result_event_publish_flag}.
     */
    P_BRC_PARAMS_SFU_VID_RC_RESULT_EVENT_PUBLISH_FLAG("p->brc_params.sfu_vid_rc_result_event_publish_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.use_bwa_info_log}.
     */
    P_BRC_PARAMS_USE_BWA_INFO_LOG("p->brc_params.use_bwa_info_log", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.use_hybrid_server_bwa}.
     */
    P_BRC_PARAMS_USE_HYBRID_SERVER_BWA("p->brc_params.use_hybrid_server_bwa", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.use_server_bwa}.
     */
    P_BRC_PARAMS_USE_SERVER_BWA("p->brc_params.use_server_bwa", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;brc_params.verbose_logging_freq_msec}.
     */
    P_BRC_PARAMS_VERBOSE_LOGGING_FREQ_MSEC("p->brc_params.verbose_logging_freq_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_adaptive_leveler_ec_thres}.
     */
    P_BT_ADAPTIVE_LEVELER_EC_THRES("p->bt_adaptive_leveler_ec_thres", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_adaptive_leveler_mode}.
     */
    P_BT_ADAPTIVE_LEVELER_MODE("p->bt_adaptive_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bt_adaptive_leveler_mode_hi_low}.
     */
    P_BT_ADAPTIVE_LEVELER_MODE_HI_LOW("p->bt_adaptive_leveler_mode_hi_low", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bt_adaptive_leveler_mode_min_intensity}.
     */
    P_BT_ADAPTIVE_LEVELER_MODE_MIN_INTENSITY("p->bt_adaptive_leveler_mode_min_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_adaptive_leveler_mode_rev}.
     */
    P_BT_ADAPTIVE_LEVELER_MODE_REV("p->bt_adaptive_leveler_mode_rev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bt_bipolar_compression_en}.
     */
    P_BT_BIPOLAR_COMPRESSION_EN("p->bt_bipolar_compression_en", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bt_leveler_intensity}.
     */
    P_BT_LEVELER_INTENSITY("p->bt_leveler_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_min_wait_frames_transitions}.
     */
    P_BT_MIN_WAIT_FRAMES_TRANSITIONS("p->bt_min_wait_frames_transitions", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_smooth_leveler_mode_factor}.
     */
    P_BT_SMOOTH_LEVELER_MODE_FACTOR("p->bt_smooth_leveler_mode_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bt_use_smooth_leveler_mode}.
     */
    P_BT_USE_SMOOTH_LEVELER_MODE("p->bt_use_smooth_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;builtin_mode}.
     */
    P_BUILTIN_MODE("p->builtin_mode", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;bwa_min_vid_stream_reserve_bps}.
     */
    P_BWA_MIN_VID_STREAM_RESERVE_BPS("p->bwa_min_vid_stream_reserve_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_clamp_scheme_after_call_start}.
     */
    P_BWE_CLAMP_SCHEME_AFTER_CALL_START("p->bwe_clamp_scheme_after_call_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.additive_hold_ms}.
     */
    P_BWE_HOLD_PARAMS_ADDITIVE_HOLD_MS("p->bwe_hold_params.additive_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.congestion_hold_ms}.
     */
    P_BWE_HOLD_PARAMS_CONGESTION_HOLD_MS("p->bwe_hold_params.congestion_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.indefinite_hold}.
     */
    P_BWE_HOLD_PARAMS_INDEFINITE_HOLD("p->bwe_hold_params.indefinite_hold", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.init_hold_ms}.
     */
    P_BWE_HOLD_PARAMS_INIT_HOLD_MS("p->bwe_hold_params.init_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.mcp_hold_ms}.
     */
    P_BWE_HOLD_PARAMS_MCP_HOLD_MS("p->bwe_hold_params.mcp_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.recv_drop_hold_ms}.
     */
    P_BWE_HOLD_PARAMS_RECV_DROP_HOLD_MS("p->bwe_hold_params.recv_drop_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_hold_params.source_bitmap}.
     */
    P_BWE_HOLD_PARAMS_SOURCE_BITMAP("p->bwe_hold_params.source_bitmap", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_impl}.
     */
    P_BWE_IMPL("p->bwe_impl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_probing_mode}.
     */
    P_BWE_PROBING_MODE("p->bwe_probing_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;bwe_sampling_rate}.
     */
    P_BWE_SAMPLING_RATE("p->bwe_sampling_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;byte_multiplier_per_iter}.
     */
    P_BYTE_MULTIPLIER_PER_ITER("p->byte_multiplier_per_iter", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;camera_height}.
     */
    P_CAMERA_HEIGHT("p->camera_height", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;camera_width}.
     */
    P_CAMERA_WIDTH("p->camera_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cap_estimated_bitrate}.
     */
    P_CAP_ESTIMATED_BITRATE("p->cap_estimated_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cap_hist_init_bitrate}.
     */
    P_CAP_HIST_INIT_BITRATE("p->cap_hist_init_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_abs_rtt_congestion_threshold}.
     */
    P_CC_ABS_RTT_CONGESTION_THRESHOLD("p->cc_abs_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_enable_ramp_up_for_random_packet_loss}.
     */
    P_CC_BWE_ENABLE_RAMP_UP_FOR_RANDOM_PACKET_LOSS("p->cc_bwe_enable_ramp_up_for_random_packet_loss", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_ceiling_mode}.
     */
    P_CC_BWE_SLOW_RAMP_UP_CEILING_MODE("p->cc_bwe_slow_ramp_up_ceiling_mode", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_ceiling_multiplier}.
     */
    P_CC_BWE_SLOW_RAMP_UP_CEILING_MULTIPLIER("p->cc_bwe_slow_ramp_up_ceiling_multiplier", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_fallback_to_previous_n_sbwe}.
     */
    P_CC_BWE_SLOW_RAMP_UP_FALLBACK_TO_PREVIOUS_N_SBWE("p->cc_bwe_slow_ramp_up_fallback_to_previous_n_sbwe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_freeze_duration_after_fallback}.
     */
    P_CC_BWE_SLOW_RAMP_UP_FREEZE_DURATION_AFTER_FALLBACK("p->cc_bwe_slow_ramp_up_freeze_duration_after_fallback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_hold_on_period_in_sec}.
     */
    P_CC_BWE_SLOW_RAMP_UP_HOLD_ON_PERIOD_IN_SEC("p->cc_bwe_slow_ramp_up_hold_on_period_in_sec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_only_near_ceiling}.
     */
    P_CC_BWE_SLOW_RAMP_UP_ONLY_NEAR_CEILING("p->cc_bwe_slow_ramp_up_only_near_ceiling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_peer_rx_bitrate}.
     */
    P_CC_BWE_SLOW_RAMP_UP_PEER_RX_BITRATE("p->cc_bwe_slow_ramp_up_peer_rx_bitrate", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_bwe_slow_ramp_up_use_cur_tx_bitrate}.
     */
    P_CC_BWE_SLOW_RAMP_UP_USE_CUR_TX_BITRATE("p->cc_bwe_slow_ramp_up_use_cur_tx_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_cap_rl_bwe_to_max_target}.
     */
    P_CC_CAP_RL_BWE_TO_MAX_TARGET("p->cc_cap_rl_bwe_to_max_target", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_enable_ml_plc_inference}.
     */
    P_CC_ENABLE_ML_PLC_INFERENCE("p->cc_enable_ml_plc_inference", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_enable_offline_rl_bwe_inference}.
     */
    P_CC_ENABLE_OFFLINE_RL_BWE_INFERENCE("p->cc_enable_offline_rl_bwe_inference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_enable_per_model_platform_pair}.
     */
    P_CC_ENABLE_PER_MODEL_PLATFORM_PAIR("p->cc_enable_per_model_platform_pair", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_enable_pip_mode_improvement}.
     */
    P_CC_ENABLE_PIP_MODE_IMPROVEMENT("p->cc_enable_pip_mode_improvement", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_hd_targeting_model_input_feature_agg_mode}.
     */
    P_CC_HD_TARGETING_MODEL_INPUT_FEATURE_AGG_MODE("p->cc_hd_targeting_model_input_feature_agg_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_condition_type}.
     */
    P_CC_ML_CONG_CONDITION_TYPE("p->cc_ml_cong_condition_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_features_bitmap}.
     */
    P_CC_ML_CONG_FEATURES_BITMAP("p->cc_ml_cong_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_n_feature}.
     */
    P_CC_ML_CONG_N_FEATURE("p->cc_ml_cong_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_probability_threshold}.
     */
    P_CC_ML_CONG_PROBABILITY_THRESHOLD("p->cc_ml_cong_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_should_fill_zero_pp}.
     */
    P_CC_ML_CONG_SHOULD_FILL_ZERO_PP("p->cc_ml_cong_should_fill_zero_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_threshold_bitrate_bps}.
     */
    P_CC_ML_CONG_THRESHOLD_BITRATE_BPS("p->cc_ml_cong_threshold_bitrate_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_cong_ts_length}.
     */
    P_CC_ML_CONG_TS_LENGTH("p->cc_ml_cong_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting_check_time_ms}.
     */
    P_CC_ML_HD_TARGETING_CHECK_TIME_MS("p->cc_ml_hd_targeting_check_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting_features_bitmap}.
     */
    P_CC_ML_HD_TARGETING_FEATURES_BITMAP("p->cc_ml_hd_targeting_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting_n_feature}.
     */
    P_CC_ML_HD_TARGETING_N_FEATURE("p->cc_ml_hd_targeting_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting_probability_threshold}.
     */
    P_CC_ML_HD_TARGETING_PROBABILITY_THRESHOLD("p->cc_ml_hd_targeting_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting_ts_length}.
     */
    P_CC_ML_HD_TARGETING_TS_LENGTH("p->cc_ml_hd_targeting_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting2_features_bitmap}.
     */
    P_CC_ML_HD_TARGETING2_FEATURES_BITMAP("p->cc_ml_hd_targeting2_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting2_n_feature}.
     */
    P_CC_ML_HD_TARGETING2_N_FEATURE("p->cc_ml_hd_targeting2_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting2_probability_threshold}.
     */
    P_CC_ML_HD_TARGETING2_PROBABILITY_THRESHOLD("p->cc_ml_hd_targeting2_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_hd_targeting2_ts_length}.
     */
    P_CC_ML_HD_TARGETING2_TS_LENGTH("p->cc_ml_hd_targeting2_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_inference_num_threads}.
     */
    P_CC_ML_INFERENCE_NUM_THREADS("p->cc_ml_inference_num_threads", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_agg_mode_for_training}.
     */
    P_CC_ML_MODEL_AGG_MODE_FOR_TRAINING("p->cc_ml_model_agg_mode_for_training", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_load_max_retry}.
     */
    P_CC_ML_MODEL_LOAD_MAX_RETRY("p->cc_ml_model_load_max_retry", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_load_retry_interval}.
     */
    P_CC_ML_MODEL_LOAD_RETRY_INTERVAL("p->cc_ml_model_load_retry_interval", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_load_start_time_ms}.
     */
    P_CC_ML_MODEL_LOAD_START_TIME_MS("p->cc_ml_model_load_start_time_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_load_trigger_point}.
     */
    P_CC_ML_MODEL_LOAD_TRIGGER_POINT("p->cc_ml_model_load_trigger_point", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_model_only_load_max_retry}.
     */
    P_CC_ML_MODEL_ONLY_LOAD_MAX_RETRY("p->cc_ml_model_only_load_max_retry", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_multi_class_congestion_probability_threshold}.
     */
    P_CC_ML_MULTI_CLASS_CONGESTION_PROBABILITY_THRESHOLD("p->cc_ml_multi_class_congestion_probability_threshold", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_multi_class_stop_mcp_on_congestion}.
     */
    P_CC_ML_MULTI_CLASS_STOP_MCP_ON_CONGESTION("p->cc_ml_multi_class_stop_mcp_on_congestion", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_multi_class_undershoot_probability_threshold}.
     */
    P_CC_ML_MULTI_CLASS_UNDERSHOOT_PROBABILITY_THRESHOLD("p->cc_ml_multi_class_undershoot_probability_threshold", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_offline_rl_bwe_bitmap}.
     */
    P_CC_ML_OFFLINE_RL_BWE_BITMAP("p->cc_ml_offline_rl_bwe_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_offline_rl_bwe_n_feature}.
     */
    P_CC_ML_OFFLINE_RL_BWE_N_FEATURE("p->cc_ml_offline_rl_bwe_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_offline_rl_bwe_ts_length}.
     */
    P_CC_ML_OFFLINE_RL_BWE_TS_LENGTH("p->cc_ml_offline_rl_bwe_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_plc_n_feature}.
     */
    P_CC_ML_PLC_N_FEATURE("p->cc_ml_plc_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_plc_ts_length}.
     */
    P_CC_ML_PLC_TS_LENGTH("p->cc_ml_plc_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_pytorch_load_mode}.
     */
    P_CC_ML_PYTORCH_LOAD_MODE("p->cc_ml_pytorch_load_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_random_plc_probability_threshold}.
     */
    P_CC_ML_RANDOM_PLC_PROBABILITY_THRESHOLD("p->cc_ml_random_plc_probability_threshold", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_should_fill_zero_pp}.
     */
    P_CC_ML_SHOULD_FILL_ZERO_PP("p->cc_ml_should_fill_zero_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_tr_n_feature}.
     */
    P_CC_ML_TR_N_FEATURE("p->cc_ml_tr_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_tr_probability_threshold}.
     */
    P_CC_ML_TR_PROBABILITY_THRESHOLD("p->cc_ml_tr_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_tr_ts_length}.
     */
    P_CC_ML_TR_TS_LENGTH("p->cc_ml_tr_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot_features_bitmap}.
     */
    P_CC_ML_UNDERSHOOT_FEATURES_BITMAP("p->cc_ml_undershoot_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot_n_feature}.
     */
    P_CC_ML_UNDERSHOOT_N_FEATURE("p->cc_ml_undershoot_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot_num_classes}.
     */
    P_CC_ML_UNDERSHOOT_NUM_CLASSES("p->cc_ml_undershoot_num_classes", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot_probability_threshold}.
     */
    P_CC_ML_UNDERSHOOT_PROBABILITY_THRESHOLD("p->cc_ml_undershoot_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot_ts_length}.
     */
    P_CC_ML_UNDERSHOOT_TS_LENGTH("p->cc_ml_undershoot_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot2_features_bitmap}.
     */
    P_CC_ML_UNDERSHOOT2_FEATURES_BITMAP("p->cc_ml_undershoot2_features_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot2_n_feature}.
     */
    P_CC_ML_UNDERSHOOT2_N_FEATURE("p->cc_ml_undershoot2_n_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot2_probability_threshold}.
     */
    P_CC_ML_UNDERSHOOT2_PROBABILITY_THRESHOLD("p->cc_ml_undershoot2_probability_threshold", VoipParamType.INTEGER, 2, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_undershoot2_ts_length}.
     */
    P_CC_ML_UNDERSHOOT2_TS_LENGTH("p->cc_ml_undershoot2_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_hd_targeting2_for_inference}.
     */
    P_CC_ML_USE_HD_TARGETING2_FOR_INFERENCE("p->cc_ml_use_hd_targeting2_for_inference", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_automos}.
     */
    P_CC_ML_USE_NAMESPACE_V2_AUTOMOS("p->cc_ml_use_namespace_v2_automos", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_cong}.
     */
    P_CC_ML_USE_NAMESPACE_V2_CONG("p->cc_ml_use_namespace_v2_cong", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_gc_hd_target}.
     */
    P_CC_ML_USE_NAMESPACE_V2_GC_HD_TARGET("p->cc_ml_use_namespace_v2_gc_hd_target", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_gc_undershoot}.
     */
    P_CC_ML_USE_NAMESPACE_V2_GC_UNDERSHOOT("p->cc_ml_use_namespace_v2_gc_undershoot", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_hd_target}.
     */
    P_CC_ML_USE_NAMESPACE_V2_HD_TARGET("p->cc_ml_use_namespace_v2_hd_target", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_nadl}.
     */
    P_CC_ML_USE_NAMESPACE_V2_NADL("p->cc_ml_use_namespace_v2_nadl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_ns}.
     */
    P_CC_ML_USE_NAMESPACE_V2_NS("p->cc_ml_use_namespace_v2_ns", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_plc}.
     */
    P_CC_ML_USE_NAMESPACE_V2_PLC("p->cc_ml_use_namespace_v2_plc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_quickhd}.
     */
    P_CC_ML_USE_NAMESPACE_V2_QUICKHD("p->cc_ml_use_namespace_v2_quickhd", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_rl_bwe}.
     */
    P_CC_ML_USE_NAMESPACE_V2_RL_BWE("p->cc_ml_use_namespace_v2_rl_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_tr}.
     */
    P_CC_ML_USE_NAMESPACE_V2_TR("p->cc_ml_use_namespace_v2_tr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_undershoot}.
     */
    P_CC_ML_USE_NAMESPACE_V2_UNDERSHOOT("p->cc_ml_use_namespace_v2_undershoot", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_vmos}.
     */
    P_CC_ML_USE_NAMESPACE_V2_VMOS("p->cc_ml_use_namespace_v2_vmos", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_namespace_v2_vsr}.
     */
    P_CC_ML_USE_NAMESPACE_V2_VSR("p->cc_ml_use_namespace_v2_vsr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_ml_use_undershoot2_for_inference}.
     */
    P_CC_ML_USE_UNDERSHOOT2_FOR_INFERENCE("p->cc_ml_use_undershoot2_for_inference", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;cc_no_data_received_threshold}.
     */
    P_CC_NO_DATA_RECEIVED_THRESHOLD("p->cc_no_data_received_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_no_initial_rtt_threshold}.
     */
    P_CC_NO_INITIAL_RTT_THRESHOLD("p->cc_no_initial_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_no_rtcp_received_threshold}.
     */
    P_CC_NO_RTCP_RECEIVED_THRESHOLD("p->cc_no_rtcp_received_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_offline_rl_bwe_model_input_feature_agg_mode}.
     */
    P_CC_OFFLINE_RL_BWE_MODEL_INPUT_FEATURE_AGG_MODE("p->cc_offline_rl_bwe_model_input_feature_agg_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_packet_loss_percentage_approaching_multiplier}.
     */
    P_CC_PACKET_LOSS_PERCENTAGE_APPROACHING_MULTIPLIER("p->cc_packet_loss_percentage_approaching_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_packet_loss_percentage_heavy_multiplier}.
     */
    P_CC_PACKET_LOSS_PERCENTAGE_HEAVY_MULTIPLIER("p->cc_packet_loss_percentage_heavy_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_packet_loss_percentage_threshold}.
     */
    P_CC_PACKET_LOSS_PERCENTAGE_THRESHOLD("p->cc_packet_loss_percentage_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_plc_model_input_feature_agg_mode}.
     */
    P_CC_PLC_MODEL_INPUT_FEATURE_AGG_MODE("p->cc_plc_model_input_feature_agg_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_blend_alpha}.
     */
    P_CC_RL_BWE_BLEND_ALPHA("p->cc_rl_bwe_blend_alpha", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_check_range}.
     */
    P_CC_RL_BWE_CHECK_RANGE("p->cc_rl_bwe_check_range", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_check_rate_of_change}.
     */
    P_CC_RL_BWE_CHECK_RATE_OF_CHANGE("p->cc_rl_bwe_check_rate_of_change", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_check_tfrc_divergence}.
     */
    P_CC_RL_BWE_CHECK_TFRC_DIVERGENCE("p->cc_rl_bwe_check_tfrc_divergence", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_check_variance}.
     */
    P_CC_RL_BWE_CHECK_VARIANCE("p->cc_rl_bwe_check_variance", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_cooldown_duration_ms}.
     */
    P_CC_RL_BWE_COOLDOWN_DURATION_MS("p->cc_rl_bwe_cooldown_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_cooldown_enabled}.
     */
    P_CC_RL_BWE_COOLDOWN_ENABLED("p->cc_rl_bwe_cooldown_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_cooldown_trigger_count}.
     */
    P_CC_RL_BWE_COOLDOWN_TRIGGER_COUNT("p->cc_rl_bwe_cooldown_trigger_count", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_feedback_to_slide_window}.
     */
    P_CC_RL_BWE_FEEDBACK_TO_SLIDE_WINDOW("p->cc_rl_bwe_feedback_to_slide_window", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_max_change_ratio}.
     */
    P_CC_RL_BWE_MAX_CHANGE_RATIO("p->cc_rl_bwe_max_change_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_max_reasonable_bps}.
     */
    P_CC_RL_BWE_MAX_REASONABLE_BPS("p->cc_rl_bwe_max_reasonable_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_max_tfrc_ratio}.
     */
    P_CC_RL_BWE_MAX_TFRC_RATIO("p->cc_rl_bwe_max_tfrc_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_min_reasonable_bps}.
     */
    P_CC_RL_BWE_MIN_REASONABLE_BPS("p->cc_rl_bwe_min_reasonable_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_min_tfrc_ratio}.
     */
    P_CC_RL_BWE_MIN_TFRC_RATIO("p->cc_rl_bwe_min_tfrc_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_min_variance_bps}.
     */
    P_CC_RL_BWE_MIN_VARIANCE_BPS("p->cc_rl_bwe_min_variance_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_on_fail}.
     */
    P_CC_RL_BWE_ON_FAIL("p->cc_rl_bwe_on_fail", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_swap_nm_max_target}.
     */
    P_CC_RL_BWE_SWAP_NM_MAX_TARGET("p->cc_rl_bwe_swap_nm_max_target", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_tfrc_div_window_ms}.
     */
    P_CC_RL_BWE_TFRC_DIV_WINDOW_MS("p->cc_rl_bwe_tfrc_div_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rl_bwe_variance_ema_alpha}.
     */
    P_CC_RL_BWE_VARIANCE_EMA_ALPHA("p->cc_rl_bwe_variance_ema_alpha", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rtt_approaching_congestion_multiplier}.
     */
    P_CC_RTT_APPROACHING_CONGESTION_MULTIPLIER("p->cc_rtt_approaching_congestion_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_rtt_heavily_congestion_multiplier}.
     */
    P_CC_RTT_HEAVILY_CONGESTION_MULTIPLIER("p->cc_rtt_heavily_congestion_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_signal_mask_to_pause_sender_bwe_ramp_up}.
     */
    P_CC_SIGNAL_MASK_TO_PAUSE_SENDER_BWE_RAMP_UP("p->cc_signal_mask_to_pause_sender_bwe_ramp_up", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_skip_pytorch_impl_for_executorch}.
     */
    P_CC_SKIP_PYTORCH_IMPL_FOR_EXECUTORCH("p->cc_skip_pytorch_impl_for_executorch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_tr_model_input_feature_agg_mode}.
     */
    P_CC_TR_MODEL_INPUT_FEATURE_AGG_MODE("p->cc_tr_model_input_feature_agg_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_tx_and_peer_rx_slide_window_entries_to_eval}.
     */
    P_CC_TX_AND_PEER_RX_SLIDE_WINDOW_ENTRIES_TO_EVAL("p->cc_tx_and_peer_rx_slide_window_entries_to_eval", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_tx_and_peer_rx_slide_window_size}.
     */
    P_CC_TX_AND_PEER_RX_SLIDE_WINDOW_SIZE("p->cc_tx_and_peer_rx_slide_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_undershoot_model_input_feature_agg_mode}.
     */
    P_CC_UNDERSHOOT_MODEL_INPUT_FEATURE_AGG_MODE("p->cc_undershoot_model_input_feature_agg_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cc_use_mlp_model}.
     */
    P_CC_USE_MLP_MODEL("p->cc_use_mlp_model", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_use_nm_pair_feature}.
     */
    P_CC_USE_NM_PAIR_FEATURE("p->cc_use_nm_pair_feature", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_use_offline_rl_bwe_as_target}.
     */
    P_CC_USE_OFFLINE_RL_BWE_AS_TARGET("p->cc_use_offline_rl_bwe_as_target", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;cc_use_peer_rx_aud_bitrate}.
     */
    P_CC_USE_PEER_RX_AUD_BITRATE("p->cc_use_peer_rx_aud_bitrate", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ceiling_calculation}.
     */
    P_CEILING_CALCULATION("p->ceiling_calculation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ceiling_calculation_dl}.
     */
    P_CEILING_CALCULATION_DL("p->ceiling_calculation_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cellular_ml_feature_val}.
     */
    P_CELLULAR_ML_FEATURE_VAL("p->cellular_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cgu_metrics_timeout_ms}.
     */
    P_CGU_METRICS_TIMEOUT_MS("p->cgu_metrics_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;change_vid_rc_after_simulcast_ms}.
     */
    P_CHANGE_VID_RC_AFTER_SIMULCAST_MS("p->change_vid_rc_after_simulcast_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;check_sbwe_bottleneck_for_low_rbwe_threshold}.
     */
    P_CHECK_SBWE_BOTTLENECK_FOR_LOW_RBWE_THRESHOLD("p->check_sbwe_bottleneck_for_low_rbwe_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;clamp_sfu_ul_bwe_in_all_key_frame_mode}.
     */
    P_CLAMP_SFU_UL_BWE_IN_ALL_KEY_FRAME_MODE("p->clamp_sfu_ul_bwe_in_all_key_frame_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;codec_impl}.
     */
    P_CODEC_IMPL("p->codec_impl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;codec_rc_mode}.
     */
    P_CODEC_RC_MODE("p->codec_rc_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;codec_type}.
     */
    P_CODEC_TYPE("p->codec_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;coeff_plr_above1_count}.
     */
    P_COEFF_PLR_ABOVE1_COUNT("p->coeff_plr_above1_count", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;coeff_plr_ema}.
     */
    P_COEFF_PLR_EMA("p->coeff_plr_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;coeff_plr_ema_trend}.
     */
    P_COEFF_PLR_EMA_TREND("p->coeff_plr_ema_trend", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;coeff_plr_over_ema}.
     */
    P_COEFF_PLR_OVER_EMA("p->coeff_plr_over_ema", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;coeff_plr_stddev}.
     */
    P_COEFF_PLR_STDDEV("p->coeff_plr_stddev", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;collect_hbwe_stats}.
     */
    P_COLLECT_HBWE_STATS("p->collect_hbwe_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.allow_pli_under_all_ltrp}.
     */
    P_COMMON_PARAM_ALLOW_PLI_UNDER_ALL_LTRP("p->common_param.allow_pli_under_all_ltrp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_all_ltrp}.
     */
    P_COMMON_PARAM_ENABLE_ALL_LTRP("p->common_param.enable_all_ltrp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_av1_ltr}.
     */
    P_COMMON_PARAM_ENABLE_AV1_LTR("p->common_param.enable_av1_ltr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_lazy_realloc}.
     */
    P_COMMON_PARAM_ENABLE_LAZY_REALLOC("p->common_param.enable_lazy_realloc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_ltr_ack}.
     */
    P_COMMON_PARAM_ENABLE_LTR_ACK("p->common_param.enable_ltr_ack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_ltr_nack}.
     */
    P_COMMON_PARAM_ENABLE_LTR_NACK("p->common_param.enable_ltr_nack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_ltr_pool}.
     */
    P_COMMON_PARAM_ENABLE_LTR_POOL("p->common_param.enable_ltr_pool", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_ltrp_call_replayer}.
     */
    P_COMMON_PARAM_ENABLE_LTRP_CALL_REPLAYER("p->common_param.enable_ltrp_call_replayer", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_ltrp_logs}.
     */
    P_COMMON_PARAM_ENABLE_LTRP_LOGS("p->common_param.enable_ltrp_logs", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_new_ltr_hdr_protocol}.
     */
    P_COMMON_PARAM_ENABLE_NEW_LTR_HDR_PROTOCOL("p->common_param.enable_new_ltr_hdr_protocol", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_oh264_ltr}.
     */
    P_COMMON_PARAM_ENABLE_OH264_LTR("p->common_param.enable_oh264_ltr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_packetizer_logging}.
     */
    P_COMMON_PARAM_ENABLE_PACKETIZER_LOGGING("p->common_param.enable_packetizer_logging", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_rpsi_recovery}.
     */
    P_COMMON_PARAM_ENABLE_RPSI_RECOVERY("p->common_param.enable_rpsi_recovery", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.enable_vp9}.
     */
    P_COMMON_PARAM_ENABLE_VP9("p->common_param.enable_vp9", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.ios_enable_ltrp}.
     */
    P_COMMON_PARAM_IOS_ENABLE_LTRP("p->common_param.ios_enable_ltrp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.ios_on_demand_ack}.
     */
    P_COMMON_PARAM_IOS_ON_DEMAND_ACK("p->common_param.ios_on_demand_ack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.ios_render_i420}.
     */
    P_COMMON_PARAM_IOS_RENDER_I420("p->common_param.ios_render_i420", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.ios_vt_codec_rpsi_to_pli}.
     */
    P_COMMON_PARAM_IOS_VT_CODEC_RPSI_TO_PLI("p->common_param.ios_vt_codec_rpsi_to_pli", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.send_ltrp_fs}.
     */
    P_COMMON_PARAM_SEND_LTRP_FS("p->common_param.send_ltrp_fs", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.set_ltrp_target_bitrate}.
     */
    P_COMMON_PARAM_SET_LTRP_TARGET_BITRATE("p->common_param.set_ltrp_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.support_av1_in_gc}.
     */
    P_COMMON_PARAM_SUPPORT_AV1_IN_GC("p->common_param.support_av1_in_gc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;common_param.use_default_frame_context_for_ltrp}.
     */
    P_COMMON_PARAM_USE_DEFAULT_FRAME_CONTEXT_FOR_LTRP("p->common_param.use_default_frame_context_for_ltrp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;compression_gain}.
     */
    P_COMPRESSION_GAIN("p->compression_gain", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;compute_crest_factor}.
     */
    P_COMPUTE_CREST_FACTOR("p->compute_crest_factor", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;compute_sii_snr_metric}.
     */
    P_COMPUTE_SII_SNR_METRIC("p->compute_sii_snr_metric", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;compute_spectral_metrics}.
     */
    P_COMPUTE_SPECTRAL_METRICS("p->compute_spectral_metrics", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_after_simulcast_schedule}.
     */
    P_CONDS_COND_AFTER_SIMULCAST_SCHEDULE("p->conds.cond_after_simulcast_schedule", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_audio_in_trouble}.
     */
    P_CONDS_COND_AUDIO_IN_TROUBLE("p->conds.cond_audio_in_trouble", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_battery_drop_threshold}.
     */
    P_CONDS_COND_BATTERY_DROP_THRESHOLD("p->conds.cond_battery_drop_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_battery_low_threshold}.
     */
    P_CONDS_COND_BATTERY_LOW_THRESHOLD("p->conds.cond_battery_low_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_bitrate_hysteresis}.
     */
    P_CONDS_COND_BITRATE_HYSTERESIS("p->conds.cond_bitrate_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_codec_scheme}.
     */
    P_CONDS_COND_CODEC_SCHEME("p->conds.cond_codec_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_codec_type}.
     */
    P_CONDS_COND_CODEC_TYPE("p->conds.cond_codec_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_codecs_contain_any}.
     */
    P_CONDS_COND_CODECS_CONTAIN_ANY("p->conds.cond_codecs_contain_any", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_codecs_contain_only}.
     */
    P_CONDS_COND_CODECS_CONTAIN_ONLY("p->conds.cond_codecs_contain_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_congestion_signal_mask}.
     */
    P_CONDS_COND_CONGESTION_SIGNAL_MASK("p->conds.cond_congestion_signal_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_device_class_mask}.
     */
    P_CONDS_COND_DEVICE_CLASS_MASK("p->conds.cond_device_class_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_encoding_doc_screen_sharing}.
     */
    P_CONDS_COND_ENCODING_DOC_SCREEN_SHARING("p->conds.cond_encoding_doc_screen_sharing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_encoding_video_screen_sharing}.
     */
    P_CONDS_COND_ENCODING_VIDEO_SCREEN_SHARING("p->conds.cond_encoding_video_screen_sharing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_freq_rtt_cycle}.
     */
    P_CONDS_COND_FREQ_RTT_CYCLE("p->conds.cond_freq_rtt_cycle", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_high_init_rtt}.
     */
    P_CONDS_COND_HIGH_INIT_RTT("p->conds.cond_high_init_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_high_init_rtt_stddev}.
     */
    P_CONDS_COND_HIGH_INIT_RTT_STDDEV("p->conds.cond_high_init_rtt_stddev", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_hist_rtt_ratio}.
     */
    P_CONDS_COND_HIST_RTT_RATIO("p->conds.cond_hist_rtt_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_in_congestion}.
     */
    P_CONDS_COND_IN_CONGESTION("p->conds.cond_in_congestion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_is_sfu_downlink}.
     */
    P_CONDS_COND_IS_SFU_DOWNLINK("p->conds.cond_is_sfu_downlink", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_is_sfu_uplink}.
     */
    P_CONDS_COND_IS_SFU_UPLINK("p->conds.cond_is_sfu_uplink", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_is_speaker}.
     */
    P_CONDS_COND_IS_SPEAKER("p->conds.cond_is_speaker", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_jb_delay_hysteresis}.
     */
    P_CONDS_COND_JB_DELAY_HYSTERESIS("p->conds.cond_jb_delay_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_jb_last_delay_ema_alpha}.
     */
    P_CONDS_COND_JB_LAST_DELAY_EMA_ALPHA("p->conds.cond_jb_last_delay_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_low_quality_vid_mode}.
     */
    P_CONDS_COND_LOW_QUALITY_VID_MODE("p->conds.cond_low_quality_vid_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_ml_hd_targeting_type}.
     */
    P_CONDS_COND_ML_HD_TARGETING_TYPE("p->conds.cond_ml_hd_targeting_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_mte_combine_bad}.
     */
    P_CONDS_COND_MTE_COMBINE_BAD("p->conds.cond_mte_combine_bad", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_net_medium}.
     */
    P_CONDS_COND_NET_MEDIUM("p->conds.cond_net_medium", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_net_medium_pair}.
     */
    P_CONDS_COND_NET_MEDIUM_PAIR("p->conds.cond_net_medium_pair", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_new_codec_type}.
     */
    P_CONDS_COND_NEW_CODEC_TYPE("p->conds.cond_new_codec_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_ongoing_all_ltrp}.
     */
    P_CONDS_COND_ONGOING_ALL_LTRP("p->conds.cond_ongoing_all_ltrp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_packet_loss_hysteresis}.
     */
    P_CONDS_COND_PACKET_LOSS_HYSTERESIS("p->conds.cond_packet_loss_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_packet_loss_pct_ema_alpha}.
     */
    P_CONDS_COND_PACKET_LOSS_PCT_EMA_ALPHA("p->conds.cond_packet_loss_pct_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_packet_rx_loss_pct_ema_alpha}.
     */
    P_CONDS_COND_PACKET_RX_LOSS_PCT_EMA_ALPHA("p->conds.cond_packet_rx_loss_pct_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_param_for_congestion.cond_congestion_abs_rtt_thr}.
     */
    P_CONDS_COND_PARAM_FOR_CONGESTION_COND_CONGESTION_ABS_RTT_THR("p->conds.cond_param_for_congestion.cond_congestion_abs_rtt_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_param_for_congestion.cond_congestion_no_data_thr}.
     */
    P_CONDS_COND_PARAM_FOR_CONGESTION_COND_CONGESTION_NO_DATA_THR("p->conds.cond_param_for_congestion.cond_congestion_no_data_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_param_for_congestion.cond_congestion_no_init_rtt_thr}.
     */
    P_CONDS_COND_PARAM_FOR_CONGESTION_COND_CONGESTION_NO_INIT_RTT_THR("p->conds.cond_param_for_congestion.cond_congestion_no_init_rtt_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_param_for_congestion.cond_congestion_no_rtcp_thr}.
     */
    P_CONDS_COND_PARAM_FOR_CONGESTION_COND_CONGESTION_NO_RTCP_THR("p->conds.cond_param_for_congestion.cond_congestion_no_rtcp_thr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_android_percentage}.
     */
    P_CONDS_COND_PEER_ANDROID_PERCENTAGE("p->conds.cond_peer_android_percentage", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_cellular_percentage}.
     */
    P_CONDS_COND_PEER_CELLULAR_PERCENTAGE("p->conds.cond_peer_cellular_percentage", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_in_speaker_view}.
     */
    P_CONDS_COND_PEER_IN_SPEAKER_VIEW("p->conds.cond_peer_in_speaker_view", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_iphone_percentage}.
     */
    P_CONDS_COND_PEER_IPHONE_PERCENTAGE("p->conds.cond_peer_iphone_percentage", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_net_medium}.
     */
    P_CONDS_COND_PEER_NET_MEDIUM("p->conds.cond_peer_net_medium", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_platform_mask}.
     */
    P_CONDS_COND_PEER_PLATFORM_MASK("p->conds.cond_peer_platform_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_uaqc_state}.
     */
    P_CONDS_COND_PEER_UAQC_STATE("p->conds.cond_peer_uaqc_state", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_peer_wifi_percentage}.
     */
    P_CONDS_COND_PEER_WIFI_PERCENTAGE("p->conds.cond_peer_wifi_percentage", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_permanent_if_matched}.
     */
    P_CONDS_COND_PERMANENT_IF_MATCHED("p->conds.cond_permanent_if_matched", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pip_threshold_ms}.
     */
    P_CONDS_COND_PIP_THRESHOLD_MS("p->conds.cond_pip_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_platform_mask}.
     */
    P_CONDS_COND_PLATFORM_MASK("p->conds.cond_platform_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_plr_predictor_state}.
     */
    P_CONDS_COND_PLR_PREDICTOR_STATE("p->conds.cond_plr_predictor_state", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pp_bitrate_avg_hysteresis}.
     */
    P_CONDS_COND_PP_BITRATE_AVG_HYSTERESIS("p->conds.cond_pp_bitrate_avg_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pp_bitrate_cov_hysteresis}.
     */
    P_CONDS_COND_PP_BITRATE_COV_HYSTERESIS("p->conds.cond_pp_bitrate_cov_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pp_bitrate_last_hysteresis}.
     */
    P_CONDS_COND_PP_BITRATE_LAST_HYSTERESIS("p->conds.cond_pp_bitrate_last_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pp_bitrate_max_hysteresis}.
     */
    P_CONDS_COND_PP_BITRATE_MAX_HYSTERESIS("p->conds.cond_pp_bitrate_max_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pp_bitrate_min_hysteresis}.
     */
    P_CONDS_COND_PP_BITRATE_MIN_HYSTERESIS("p->conds.cond_pp_bitrate_min_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_pure_1x1_call}.
     */
    P_CONDS_COND_PURE_1X1_CALL("p->conds.cond_pure_1x1_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_android_device_class}.
     */
    P_CONDS_COND_RANGE_ANDROID_DEVICE_CLASS("p->conds.cond_range_android_device_class", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_aud_plr_ema}.
     */
    P_CONDS_COND_RANGE_AUD_PLR_EMA("p->conds.cond_range_aud_plr_ema", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_avg_loss_count}.
     */
    P_CONDS_COND_RANGE_AVG_LOSS_COUNT("p->conds.cond_range_avg_loss_count", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_avg_target_bitrate}.
     */
    P_CONDS_COND_RANGE_AVG_TARGET_BITRATE("p->conds.cond_range_avg_target_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_bwa_vid_target_bitrate}.
     */
    P_CONDS_COND_RANGE_BWA_VID_TARGET_BITRATE("p->conds.cond_range_bwa_vid_target_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_bwe}.
     */
    P_CONDS_COND_RANGE_BWE("p->conds.cond_range_bwe", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_cell_rsrq_bitrate}.
     */
    P_CONDS_COND_RANGE_CELL_RSRQ_BITRATE("p->conds.cond_range_cell_rsrq_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_cell_signal_bitrate}.
     */
    P_CONDS_COND_RANGE_CELL_SIGNAL_BITRATE("p->conds.cond_range_cell_signal_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_cell_sinr_bitrate}.
     */
    P_CONDS_COND_RANGE_CELL_SINR_BITRATE("p->conds.cond_range_cell_sinr_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_cell_sl_bitrate}.
     */
    P_CONDS_COND_RANGE_CELL_SL_BITRATE("p->conds.cond_range_cell_sl_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_congestion_ceiling_seen}.
     */
    P_CONDS_COND_RANGE_CONGESTION_CEILING_SEEN("p->conds.cond_range_congestion_ceiling_seen", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_cur_vid_rx_bitrate}.
     */
    P_CONDS_COND_RANGE_CUR_VID_RX_BITRATE("p->conds.cond_range_cur_vid_rx_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_dec_width}.
     */
    P_CONDS_COND_RANGE_DEC_WIDTH("p->conds.cond_range_dec_width", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_dl_bwe}.
     */
    P_CONDS_COND_RANGE_DL_BWE("p->conds.cond_range_dl_bwe", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_dl_bwe_div_by_ceiling}.
     */
    P_CONDS_COND_RANGE_DL_BWE_DIV_BY_CEILING("p->conds.cond_range_dl_bwe_div_by_ceiling", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_downlink_plr}.
     */
    P_CONDS_COND_RANGE_EMA_DOWNLINK_PLR("p->conds.cond_range_ema_downlink_plr", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_jb_last_delay}.
     */
    P_CONDS_COND_RANGE_EMA_JB_LAST_DELAY("p->conds.cond_range_ema_jb_last_delay", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_EMA_PACKET_LOSS_PCT("p->conds.cond_range_ema_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_rtt}.
     */
    P_CONDS_COND_RANGE_EMA_RTT("p->conds.cond_range_ema_rtt", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_rx_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_EMA_RX_PACKET_LOSS_PCT("p->conds.cond_range_ema_rx_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_uplink_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_EMA_UPLINK_PACKET_LOSS_PCT("p->conds.cond_range_ema_uplink_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ema_uplink_plr}.
     */
    P_CONDS_COND_RANGE_EMA_UPLINK_PLR("p->conds.cond_range_ema_uplink_plr", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_gcall_size}.
     */
    P_CONDS_COND_RANGE_GCALL_SIZE("p->conds.cond_range_gcall_size", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_goodput_peer_downlink}.
     */
    P_CONDS_COND_RANGE_GOODPUT_PEER_DOWNLINK("p->conds.cond_range_goodput_peer_downlink", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_based_480p_encoding}.
     */
    P_CONDS_COND_RANGE_HISTORY_BASED_480P_ENCODING("p->conds.cond_range_history_based_480p_encoding", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_based_720p_encoding}.
     */
    P_CONDS_COND_RANGE_HISTORY_BASED_720P_ENCODING("p->conds.cond_range_history_based_720p_encoding", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_based_bitrate}.
     */
    P_CONDS_COND_RANGE_HISTORY_BASED_BITRATE("p->conds.cond_range_history_based_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_based_bitrate_match_self_only}.
     */
    P_CONDS_COND_RANGE_HISTORY_BASED_BITRATE_MATCH_SELF_ONLY("p->conds.cond_range_history_based_bitrate_match_self_only", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_based_tx_pkt_loss_perc}.
     */
    P_CONDS_COND_RANGE_HISTORY_BASED_TX_PKT_LOSS_PERC("p->conds.cond_range_history_based_tx_pkt_loss_perc", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_history_v2_based_bitrate}.
     */
    P_CONDS_COND_RANGE_HISTORY_V2_BASED_BITRATE("p->conds.cond_range_history_v2_based_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_jb_avg_delay}.
     */
    P_CONDS_COND_RANGE_JB_AVG_DELAY("p->conds.cond_range_jb_avg_delay", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_jb_last_delay}.
     */
    P_CONDS_COND_RANGE_JB_LAST_DELAY("p->conds.cond_range_jb_last_delay", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_long_term_rtt}.
     */
    P_CONDS_COND_RANGE_LONG_TERM_RTT("p->conds.cond_range_long_term_rtt", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_network_type_bitrate}.
     */
    P_CONDS_COND_RANGE_NETWORK_TYPE_BITRATE("p->conds.cond_range_network_type_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_num_res_rampdowns}.
     */
    P_CONDS_COND_RANGE_NUM_RES_RAMPDOWNS("p->conds.cond_range_num_res_rampdowns", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_PACKET_LOSS_PCT("p->conds.cond_range_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_peer_device_class}.
     */
    P_CONDS_COND_RANGE_PEER_DEVICE_CLASS("p->conds.cond_range_peer_device_class", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_peer_screen_w}.
     */
    P_CONDS_COND_RANGE_PEER_SCREEN_W("p->conds.cond_range_peer_screen_w", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_bitrate_avg}.
     */
    P_CONDS_COND_RANGE_PP_BITRATE_AVG("p->conds.cond_range_pp_bitrate_avg", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_bitrate_cov}.
     */
    P_CONDS_COND_RANGE_PP_BITRATE_COV("p->conds.cond_range_pp_bitrate_cov", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_bitrate_last}.
     */
    P_CONDS_COND_RANGE_PP_BITRATE_LAST("p->conds.cond_range_pp_bitrate_last", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_bitrate_max}.
     */
    P_CONDS_COND_RANGE_PP_BITRATE_MAX("p->conds.cond_range_pp_bitrate_max", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_bitrate_min}.
     */
    P_CONDS_COND_RANGE_PP_BITRATE_MIN("p->conds.cond_range_pp_bitrate_min", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_flip_count_for_hd}.
     */
    P_CONDS_COND_RANGE_PP_FLIP_COUNT_FOR_HD("p->conds.cond_range_pp_flip_count_for_hd", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_pp_flip_freq_for_hd}.
     */
    P_CONDS_COND_RANGE_PP_FLIP_FREQ_FOR_HD("p->conds.cond_range_pp_flip_freq_for_hd", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_remb_div_by_peer_rx_br}.
     */
    P_CONDS_COND_RANGE_REMB_DIV_BY_PEER_RX_BR("p->conds.cond_range_remb_div_by_peer_rx_br", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_remb_minus_peer_rx_br}.
     */
    P_CONDS_COND_RANGE_REMB_MINUS_PEER_RX_BR("p->conds.cond_range_remb_minus_peer_rx_br", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_res_switch_freq}.
     */
    P_CONDS_COND_RANGE_RES_SWITCH_FREQ("p->conds.cond_range_res_switch_freq", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_rtt}.
     */
    P_CONDS_COND_RANGE_RTT("p->conds.cond_range_rtt", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_rx_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_RX_PACKET_LOSS_PCT("p->conds.cond_range_rx_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_sec_since_sfu_simulcast_capable}.
     */
    P_CONDS_COND_RANGE_SEC_SINCE_SFU_SIMULCAST_CAPABLE("p->conds.cond_range_sec_since_sfu_simulcast_capable", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_sec_since_start}.
     */
    P_CONDS_COND_RANGE_SEC_SINCE_START("p->conds.cond_range_sec_since_start", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_short_term_rtt}.
     */
    P_CONDS_COND_RANGE_SHORT_TERM_RTT("p->conds.cond_range_short_term_rtt", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_slr_output_bps}.
     */
    P_CONDS_COND_RANGE_SLR_OUTPUT_BPS("p->conds.cond_range_slr_output_bps", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_target_bitrate}.
     */
    P_CONDS_COND_RANGE_TARGET_BITRATE("p->conds.cond_range_target_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_target_total_bitrate}.
     */
    P_CONDS_COND_RANGE_TARGET_TOTAL_BITRATE("p->conds.cond_range_target_total_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_tgt_br_div_by_ceiling}.
     */
    P_CONDS_COND_RANGE_TGT_BR_DIV_BY_CEILING("p->conds.cond_range_tgt_br_div_by_ceiling", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_total_vid_target_bitrate}.
     */
    P_CONDS_COND_RANGE_TOTAL_VID_TARGET_BITRATE("p->conds.cond_range_total_vid_target_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_tr_seq_len}.
     */
    P_CONDS_COND_RANGE_TR_SEQ_LEN("p->conds.cond_range_tr_seq_len", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_tx_bwe}.
     */
    P_CONDS_COND_RANGE_TX_BWE("p->conds.cond_range_tx_bwe", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ul_bwe}.
     */
    P_CONDS_COND_RANGE_UL_BWE("p->conds.cond_range_ul_bwe", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_ul_bwe_div_by_ceiling}.
     */
    P_CONDS_COND_RANGE_UL_BWE_DIV_BY_CEILING("p->conds.cond_range_ul_bwe_div_by_ceiling", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_unused_link_bw}.
     */
    P_CONDS_COND_RANGE_UNUSED_LINK_BW("p->conds.cond_range_unused_link_bw", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_unused_uplink_bw}.
     */
    P_CONDS_COND_RANGE_UNUSED_UPLINK_BW("p->conds.cond_range_unused_uplink_bw", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_uplink_packet_loss_pct}.
     */
    P_CONDS_COND_RANGE_UPLINK_PACKET_LOSS_PCT("p->conds.cond_range_uplink_packet_loss_pct", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_uplink_rtt}.
     */
    P_CONDS_COND_RANGE_UPLINK_RTT("p->conds.cond_range_uplink_rtt", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_range_wifi_sl_bitrate}.
     */
    P_CONDS_COND_RANGE_WIFI_SL_BITRATE("p->conds.cond_range_wifi_sl_bitrate", VoipParamType.ARRAY, 8, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_redial_status}.
     */
    P_CONDS_COND_REDIAL_STATUS("p->conds.cond_redial_status", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_rtt_ema_alpha}.
     */
    P_CONDS_COND_RTT_EMA_ALPHA("p->conds.cond_rtt_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_rtt_hysteresis}.
     */
    P_CONDS_COND_RTT_HYSTERESIS("p->conds.cond_rtt_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_sbwe_in_bad}.
     */
    P_CONDS_COND_SBWE_IN_BAD("p->conds.cond_sbwe_in_bad", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_sbwe_in_mcp}.
     */
    P_CONDS_COND_SBWE_IN_MCP("p->conds.cond_sbwe_in_mcp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_screen_share_receiver}.
     */
    P_CONDS_COND_SCREEN_SHARE_RECEIVER("p->conds.cond_screen_share_receiver", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_self_in_speaker_view}.
     */
    P_CONDS_COND_SELF_IN_SPEAKER_VIEW("p->conds.cond_self_in_speaker_view", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_total_bitrate_hysteresis}.
     */
    P_CONDS_COND_TOTAL_BITRATE_HYSTERESIS("p->conds.cond_total_bitrate_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_tr_type}.
     */
    P_CONDS_COND_TR_TYPE("p->conds.cond_tr_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_tx_bwe_hysteresis}.
     */
    P_CONDS_COND_TX_BWE_HYSTERESIS("p->conds.cond_tx_bwe_hysteresis", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_udst_source_mask}.
     */
    P_CONDS_COND_UDST_SOURCE_MASK("p->conds.cond_udst_source_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_udst_type}.
     */
    P_CONDS_COND_UDST_TYPE("p->conds.cond_udst_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_vid_byte_throttling}.
     */
    P_CONDS_COND_VID_BYTE_THROTTLING("p->conds.cond_vid_byte_throttling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_video_quality_mode}.
     */
    P_CONDS_COND_VIDEO_QUALITY_MODE("p->conds.cond_video_quality_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conds.cond_vr_platform}.
     */
    P_CONDS_COND_VR_PLATFORM("p->conds.cond_vr_platform", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;cong_a2a_ml_feature_val}.
     */
    P_CONG_A2A_ML_FEATURE_VAL("p->cong_a2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cong_a2i_ml_feature_val}.
     */
    P_CONG_A2I_ML_FEATURE_VAL("p->cong_a2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cong_default_platform_feature_val}.
     */
    P_CONG_DEFAULT_PLATFORM_FEATURE_VAL("p->cong_default_platform_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cong_i2a_ml_feature_val}.
     */
    P_CONG_I2A_ML_FEATURE_VAL("p->cong_i2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cong_i2i_ml_feature_val}.
     */
    P_CONG_I2I_ML_FEATURE_VAL("p->cong_i2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;cong_model_name}.
     */
    P_CONG_MODEL_NAME("p->cong_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;cong_web_ml_feature_val}.
     */
    P_CONG_WEB_ML_FEATURE_VAL("p->cong_web_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_apply_cong_ceiling}.
     */
    P_CONSERVATIVE_MODE_APPLY_CONG_CEILING("p->conservative_mode_apply_cong_ceiling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_bw_thresh_kbps}.
     */
    P_CONSERVATIVE_MODE_BW_THRESH_KBPS("p->conservative_mode_bw_thresh_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_bw_thresh_kbps_lower_bound}.
     */
    P_CONSERVATIVE_MODE_BW_THRESH_KBPS_LOWER_BOUND("p->conservative_mode_bw_thresh_kbps_lower_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_ceiling_ema_alpha}.
     */
    P_CONSERVATIVE_MODE_CEILING_EMA_ALPHA("p->conservative_mode_ceiling_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_ceiling_num_samples}.
     */
    P_CONSERVATIVE_MODE_CEILING_NUM_SAMPLES("p->conservative_mode_ceiling_num_samples", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_ceiling_pct}.
     */
    P_CONSERVATIVE_MODE_CEILING_PCT("p->conservative_mode_ceiling_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_disable_by_init_bwe}.
     */
    P_CONSERVATIVE_MODE_DISABLE_BY_INIT_BWE("p->conservative_mode_disable_by_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_enabled}.
     */
    P_CONSERVATIVE_MODE_ENABLED("p->conservative_mode_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_explore_backoff_thresh_pct}.
     */
    P_CONSERVATIVE_MODE_EXPLORE_BACKOFF_THRESH_PCT("p->conservative_mode_explore_backoff_thresh_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_explore_max_wait_t}.
     */
    P_CONSERVATIVE_MODE_EXPLORE_MAX_WAIT_T("p->conservative_mode_explore_max_wait_t", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_explore_min_wait_t}.
     */
    P_CONSERVATIVE_MODE_EXPLORE_MIN_WAIT_T("p->conservative_mode_explore_min_wait_t", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_explore_stateful}.
     */
    P_CONSERVATIVE_MODE_EXPLORE_STATEFUL("p->conservative_mode_explore_stateful", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_explore_thresh_pct}.
     */
    P_CONSERVATIVE_MODE_EXPLORE_THRESH_PCT("p->conservative_mode_explore_thresh_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_init_bwe_lower_bound}.
     */
    P_CONSERVATIVE_MODE_INIT_BWE_LOWER_BOUND("p->conservative_mode_init_bwe_lower_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_init_bwe_upper_bound}.
     */
    P_CONSERVATIVE_MODE_INIT_BWE_UPPER_BOUND("p->conservative_mode_init_bwe_upper_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;conservative_mode_override_rd_target_lower_bound}.
     */
    P_CONSERVATIVE_MODE_OVERRIDE_RD_TARGET_LOWER_BOUND("p->conservative_mode_override_rd_target_lower_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;consider_remote_bwe_min_valid}.
     */
    P_CONSIDER_REMOTE_BWE_MIN_VALID("p->consider_remote_bwe_min_valid", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;consume_rtp_ext_plr}.
     */
    P_CONSUME_RTP_EXT_PLR("p->consume_rtp_ext_plr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_inter_blocks_thresh_perc}.
     */
    P_CONTENT_DETECTOR_INTER_BLOCKS_THRESH_PERC("p->content_detector_inter_blocks_thresh_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_interval_ms}.
     */
    P_CONTENT_DETECTOR_INTERVAL_MS("p->content_detector_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_luma_variance_thresh_perc}.
     */
    P_CONTENT_DETECTOR_LUMA_VARIANCE_THRESH_PERC("p->content_detector_luma_variance_thresh_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_skip_blocks_thresh_perc}.
     */
    P_CONTENT_DETECTOR_SKIP_BLOCKS_THRESH_PERC("p->content_detector_skip_blocks_thresh_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_static_blocks_thresh_perc}.
     */
    P_CONTENT_DETECTOR_STATIC_BLOCKS_THRESH_PERC("p->content_detector_static_blocks_thresh_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;content_detector_version}.
     */
    P_CONTENT_DETECTOR_VERSION("p->content_detector_version", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_gain}.
     */
    P_DECODER_GAIN("p->decoder_gain", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.allow_frame_timestamp_to_regress}.
     */
    P_DECODER_PARAM_ALLOW_FRAME_TIMESTAMP_TO_REGRESS("p->decoder_param.allow_frame_timestamp_to_regress", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.android_decoder_deq_out_time_ms}.
     */
    P_DECODER_PARAM_ANDROID_DECODER_DEQ_OUT_TIME_MS("p->decoder_param.android_decoder_deq_out_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.android_media_codec_init_height}.
     */
    P_DECODER_PARAM_ANDROID_MEDIA_CODEC_INIT_HEIGHT("p->decoder_param.android_media_codec_init_height", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.android_media_codec_init_width}.
     */
    P_DECODER_PARAM_ANDROID_MEDIA_CODEC_INIT_WIDTH("p->decoder_param.android_media_codec_init_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.av1_decoder_high_priority}.
     */
    P_DECODER_PARAM_AV1_DECODER_HIGH_PRIORITY("p->decoder_param.av1_decoder_high_priority", VoipParamType.STRING, 16, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.dav1d_dec_thread_count}.
     */
    P_DECODER_PARAM_DAV1D_DEC_THREAD_COUNT("p->decoder_param.dav1d_dec_thread_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.decoder_high_priority}.
     */
    P_DECODER_PARAM_DECODER_HIGH_PRIORITY("p->decoder_param.decoder_high_priority", VoipParamType.STRING, 16, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.disable_h264_dec_impl_mask}.
     */
    P_DECODER_PARAM_DISABLE_H264_DEC_IMPL_MASK("p->decoder_param.disable_h264_dec_impl_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_android_decoder_fallback}.
     */
    P_DECODER_PARAM_ENABLE_ANDROID_DECODER_FALLBACK("p->decoder_param.enable_android_decoder_fallback", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_android_decoder_long_drain}.
     */
    P_DECODER_PARAM_ENABLE_ANDROID_DECODER_LONG_DRAIN("p->decoder_param.enable_android_decoder_long_drain", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_async_fmt_update}.
     */
    P_DECODER_PARAM_ENABLE_ASYNC_FMT_UPDATE("p->decoder_param.enable_async_fmt_update", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_h265_dec}.
     */
    P_DECODER_PARAM_ENABLE_H265_DEC("p->decoder_param.enable_h265_dec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_h26x_nal_start_code_4bytes}.
     */
    P_DECODER_PARAM_ENABLE_H26X_NAL_START_CODE_4BYTES("p->decoder_param.enable_h26x_nal_start_code_4bytes", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_hw_dec_to_sw_fallback}.
     */
    P_DECODER_PARAM_ENABLE_HW_DEC_TO_SW_FALLBACK("p->decoder_param.enable_hw_dec_to_sw_fallback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_ios_dec_err_fix}.
     */
    P_DECODER_PARAM_ENABLE_IOS_DEC_ERR_FIX("p->decoder_param.enable_ios_dec_err_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_ios_h264_validate_frame}.
     */
    P_DECODER_PARAM_ENABLE_IOS_H264_VALIDATE_FRAME("p->decoder_param.enable_ios_h264_validate_frame", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_ios_sei_fix}.
     */
    P_DECODER_PARAM_ENABLE_IOS_SEI_FIX("p->decoder_param.enable_ios_sei_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_jb_kf_offset_fix}.
     */
    P_DECODER_PARAM_ENABLE_JB_KF_OFFSET_FIX("p->decoder_param.enable_jb_kf_offset_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.enable_vt_decode_err_handle}.
     */
    P_DECODER_PARAM_ENABLE_VT_DECODE_ERR_HANDLE("p->decoder_param.enable_vt_decode_err_handle", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.h264_bit_list_fix}.
     */
    P_DECODER_PARAM_H264_BIT_LIST_FIX("p->decoder_param.h264_bit_list_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.h264_emu_prevent_fix}.
     */
    P_DECODER_PARAM_H264_EMU_PREVENT_FIX("p->decoder_param.h264_emu_prevent_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.h264_ignore_ds_ref_lost}.
     */
    P_DECODER_PARAM_H264_IGNORE_DS_REF_LOST("p->decoder_param.h264_ignore_ds_ref_lost", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.h264_sps_fail_on_error}.
     */
    P_DECODER_PARAM_H264_SPS_FAIL_ON_ERROR("p->decoder_param.h264_sps_fail_on_error", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.ios_is_decoding_synchronous}.
     */
    P_DECODER_PARAM_IOS_IS_DECODING_SYNCHRONOUS("p->decoder_param.ios_is_decoding_synchronous", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.ios_zero_copy}.
     */
    P_DECODER_PARAM_IOS_ZERO_COPY("p->decoder_param.ios_zero_copy", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.ltrp_nack_pkt_cnt_wt}.
     */
    P_DECODER_PARAM_LTRP_NACK_PKT_CNT_WT("p->decoder_param.ltrp_nack_pkt_cnt_wt", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.max_nacks_throttled}.
     */
    P_DECODER_PARAM_MAX_NACKS_THROTTLED("p->decoder_param.max_nacks_throttled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.nack_if_pli_throttled}.
     */
    P_DECODER_PARAM_NACK_IF_PLI_THROTTLED("p->decoder_param.nack_if_pli_throttled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;decoder_param.nack_if_rpsi_throttled}.
     */
    P_DECODER_PARAM_NACK_IF_RPSI_THROTTLED("p->decoder_param.nack_if_rpsi_throttled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;default_policy}.
     */
    P_DEFAULT_POLICY("p->default_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;delay_based_bwe_bitrate_estimator_enabled}.
     */
    P_DELAY_BASED_BWE_BITRATE_ESTIMATOR_ENABLED("p->delay_based_bwe_bitrate_estimator_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;delay_based_bwe_pp_feed_type}.
     */
    P_DELAY_BASED_BWE_PP_FEED_TYPE("p->delay_based_bwe_pp_feed_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;delay_based_bwe_trendline_filter_enabled}.
     */
    P_DELAY_BASED_BWE_TRENDLINE_FILTER_ENABLED("p->delay_based_bwe_trendline_filter_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;delay_based_bwe_use_pp}.
     */
    P_DELAY_BASED_BWE_USE_PP("p->delay_based_bwe_use_pp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;delay_ms}.
     */
    P_DELAY_MS("p->delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;denoiser_intensity}.
     */
    P_DENOISER_INTENSITY("p->denoiser_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;denoiser_intensity_with_ml_ns}.
     */
    P_DENOISER_INTENSITY_WITH_ML_NS("p->denoiser_intensity_with_ml_ns", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;detailed_call_transport_record_audit}.
     */
    P_DETAILED_CALL_TRANSPORT_RECORD_AUDIT("p->detailed_call_transport_record_audit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;detailed_call_transport_record_enable}.
     */
    P_DETAILED_CALL_TRANSPORT_RECORD_ENABLE("p->detailed_call_transport_record_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;detailed_call_transport_record_max_num_of_call_record}.
     */
    P_DETAILED_CALL_TRANSPORT_RECORD_MAX_NUM_OF_CALL_RECORD("p->detailed_call_transport_record_max_num_of_call_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;detailed_call_transport_record_min_duration_to_save}.
     */
    P_DETAILED_CALL_TRANSPORT_RECORD_MIN_DURATION_TO_SAVE("p->detailed_call_transport_record_min_duration_to_save", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_agc}.
     */
    P_DISABLE_AGC("p->disable_agc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_conservative_mode_in_group_segment}.
     */
    P_DISABLE_CONSERVATIVE_MODE_IN_GROUP_SEGMENT("p->disable_conservative_mode_in_group_segment", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_duplicate_clamping}.
     */
    P_DISABLE_DUPLICATE_CLAMPING("p->disable_duplicate_clamping", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_e2e_fec_fix}.
     */
    P_DISABLE_E2E_FEC_FIX("p->disable_e2e_fec_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_inference_at_high_latency}.
     */
    P_DISABLE_INFERENCE_AT_HIGH_LATENCY("p->disable_inference_at_high_latency", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_ml_ns_high_cpu_threshold}.
     */
    P_DISABLE_ML_NS_HIGH_CPU_THRESHOLD("p->disable_ml_ns_high_cpu_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_remb_rules}.
     */
    P_DISABLE_REMB_RULES("p->disable_remb_rules", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_rtcp_remb}.
     */
    P_DISABLE_RTCP_REMB("p->disable_rtcp_remb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_rtcp_remb_in_videostream}.
     */
    P_DISABLE_RTCP_REMB_IN_VIDEOSTREAM("p->disable_rtcp_remb_in_videostream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_rtt_congestion_detection_with_ice_rtt}.
     */
    P_DISABLE_RTT_CONGESTION_DETECTION_WITH_ICE_RTT("p->disable_rtt_congestion_detection_with_ice_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_simulcast_under_bad_network}.
     */
    P_DISABLE_SIMULCAST_UNDER_BAD_NETWORK("p->disable_simulcast_under_bad_network", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;disable_stop_mcp_on_get_stats}.
     */
    P_DISABLE_STOP_MCP_ON_GET_STATS("p->disable_stop_mcp_on_get_stats", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_sw_ns_when_builtin_available}.
     */
    P_DISABLE_SW_NS_WHEN_BUILTIN_AVAILABLE("p->disable_sw_ns_when_builtin_available", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;disable_zero_warp_roc}.
     */
    P_DISABLE_ZERO_WARP_ROC("p->disable_zero_warp_roc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dl_bwe_combine_policy}.
     */
    P_DL_BWE_COMBINE_POLICY("p->dl_bwe_combine_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dl_forced_probing_ts_length}.
     */
    P_DL_FORCED_PROBING_TS_LENGTH("p->dl_forced_probing_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dl_max_target_bitrate}.
     */
    P_DL_MAX_TARGET_BITRATE("p->dl_max_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dl_min_target_bitrate}.
     */
    P_DL_MIN_TARGET_BITRATE("p->dl_min_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dl_undershoot_model_name}.
     */
    P_DL_UNDERSHOOT_MODEL_NAME("p->dl_undershoot_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;dont_connect_for_paused_vid}.
     */
    P_DONT_CONNECT_FOR_PAUSED_VID("p->dont_connect_for_paused_vid", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dont_report_ds_fs_if_never_speak}.
     */
    P_DONT_REPORT_DS_FS_IF_NEVER_SPEAK("p->dont_report_ds_fs_if_never_speak", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;double_rtt_congestion_threshold}.
     */
    P_DOUBLE_RTT_CONGESTION_THRESHOLD("p->double_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;double_rtt_multiplier}.
     */
    P_DOUBLE_RTT_MULTIPLIER("p->double_rtt_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;double_tx_delay_congestion_threshold}.
     */
    P_DOUBLE_TX_DELAY_CONGESTION_THRESHOLD("p->double_tx_delay_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;downlink_ml_feature_val}.
     */
    P_DOWNLINK_ML_FEATURE_VAL("p->downlink_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;downlink_sender_side_rate_increase_factor_fr}.
     */
    P_DOWNLINK_SENDER_SIDE_RATE_INCREASE_FACTOR_FR("p->downlink_sender_side_rate_increase_factor_fr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;drain_on_pause}.
     */
    P_DRAIN_ON_PAUSE("p->drain_on_pause", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;drain_on_resume}.
     */
    P_DRAIN_ON_RESUME("p->drain_on_resume", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.allow_tcp}.
     */
    P_DYN_ALLOC_TIMEOUT_ALLOW_TCP("p->dyn_alloc_timeout.allow_tcp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.backoff_base}.
     */
    P_DYN_ALLOC_TIMEOUT_BACKOFF_BASE("p->dyn_alloc_timeout.backoff_base", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.burst}.
     */
    P_DYN_ALLOC_TIMEOUT_BURST("p->dyn_alloc_timeout.burst", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.c2r_rtt_factor}.
     */
    P_DYN_ALLOC_TIMEOUT_C2R_RTT_FACTOR("p->dyn_alloc_timeout.c2r_rtt_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.c2r_rtt_strategy}.
     */
    P_DYN_ALLOC_TIMEOUT_C2R_RTT_STRATEGY("p->dyn_alloc_timeout.c2r_rtt_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.debug_bitmap}.
     */
    P_DYN_ALLOC_TIMEOUT_DEBUG_BITMAP("p->dyn_alloc_timeout.debug_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.default_rtt_estimate}.
     */
    P_DYN_ALLOC_TIMEOUT_DEFAULT_RTT_ESTIMATE("p->dyn_alloc_timeout.default_rtt_estimate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.enable}.
     */
    P_DYN_ALLOC_TIMEOUT_ENABLE("p->dyn_alloc_timeout.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.enable_L1330464PRV}.
     */
    P_DYN_ALLOC_TIMEOUT_ENABLE_L1330464_PRV("p->dyn_alloc_timeout.enable_L1330464PRV", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.get_rtt_from_alloc_err}.
     */
    P_DYN_ALLOC_TIMEOUT_GET_RTT_FROM_ALLOC_ERR("p->dyn_alloc_timeout.get_rtt_from_alloc_err", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.include_ping}.
     */
    P_DYN_ALLOC_TIMEOUT_INCLUDE_PING("p->dyn_alloc_timeout.include_ping", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.max_relay_bind_timeout_ms}.
     */
    P_DYN_ALLOC_TIMEOUT_MAX_RELAY_BIND_TIMEOUT_MS("p->dyn_alloc_timeout.max_relay_bind_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.max_relay_election_timeout_ms}.
     */
    P_DYN_ALLOC_TIMEOUT_MAX_RELAY_ELECTION_TIMEOUT_MS("p->dyn_alloc_timeout.max_relay_election_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.min_relay_bind_timeout_ms}.
     */
    P_DYN_ALLOC_TIMEOUT_MIN_RELAY_BIND_TIMEOUT_MS("p->dyn_alloc_timeout.min_relay_bind_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.oibwe_alloc_strategy}.
     */
    P_DYN_ALLOC_TIMEOUT_OIBWE_ALLOC_STRATEGY("p->dyn_alloc_timeout.oibwe_alloc_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_alloc_timeout.rebind_rtt_factor}.
     */
    P_DYN_ALLOC_TIMEOUT_REBIND_RTT_FACTOR("p->dyn_alloc_timeout.rebind_rtt_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dyn_enc_fmt_change_wait_fix}.
     */
    P_DYN_ENC_FMT_CHANGE_WAIT_FIX("p->dyn_enc_fmt_change_wait_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_bwe_check_time_ms}.
     */
    P_DYNAMIC_INIT_BWE_CHECK_TIME_MS("p->dynamic_init_bwe_check_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_bwe_fallback_plr_threshold}.
     */
    P_DYNAMIC_INIT_BWE_FALLBACK_PLR_THRESHOLD("p->dynamic_init_bwe_fallback_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_bwe_fallback_rtt_threshold_ms}.
     */
    P_DYNAMIC_INIT_BWE_FALLBACK_RTT_THRESHOLD_MS("p->dynamic_init_bwe_fallback_rtt_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_bwe_fallback_value}.
     */
    P_DYNAMIC_INIT_BWE_FALLBACK_VALUE("p->dynamic_init_bwe_fallback_value", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_dl_bwe_check_time_ms}.
     */
    P_DYNAMIC_INIT_DL_BWE_CHECK_TIME_MS("p->dynamic_init_dl_bwe_check_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_dl_bwe_fallback_plr_threshold}.
     */
    P_DYNAMIC_INIT_DL_BWE_FALLBACK_PLR_THRESHOLD("p->dynamic_init_dl_bwe_fallback_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_dl_bwe_fallback_rtt_threshold_ms}.
     */
    P_DYNAMIC_INIT_DL_BWE_FALLBACK_RTT_THRESHOLD_MS("p->dynamic_init_dl_bwe_fallback_rtt_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamic_init_dl_bwe_fallback_value}.
     */
    P_DYNAMIC_INIT_DL_BWE_FALLBACK_VALUE("p->dynamic_init_dl_bwe_fallback_value", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;dynamically_update_denoiser_intensity}.
     */
    P_DYNAMICALLY_UPDATE_DENOISER_INTENSITY("p->dynamically_update_denoiser_intensity", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_consistent_low_count}.
     */
    P_EARLY_RECV_CONSISTENT_LOW_COUNT("p->early_recv_consistent_low_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_floor_bitrate}.
     */
    P_EARLY_RECV_FLOOR_BITRATE("p->early_recv_floor_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_loss_override}.
     */
    P_EARLY_RECV_LOSS_OVERRIDE("p->early_recv_loss_override", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_only_mode}.
     */
    P_EARLY_RECV_ONLY_MODE("p->early_recv_only_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_sender_bwe_threshold}.
     */
    P_EARLY_RECV_SENDER_BWE_THRESHOLD("p->early_recv_sender_bwe_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_slope_convergence}.
     */
    P_EARLY_RECV_SLOPE_CONVERGENCE("p->early_recv_slope_convergence", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_slope_stable_threshold}.
     */
    P_EARLY_RECV_SLOPE_STABLE_THRESHOLD("p->early_recv_slope_stable_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_time_fallback_ms}.
     */
    P_EARLY_RECV_TIME_FALLBACK_MS("p->early_recv_time_fallback_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_recv_use_floor}.
     */
    P_EARLY_RECV_USE_FLOOR("p->early_recv_use_floor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;early_rtt_computation}.
     */
    P_EARLY_RTT_COMPUTATION("p->early_rtt_computation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ec_strength_threshold}.
     */
    P_EC_STRENGTH_THRESHOLD("p->ec_strength_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ec_type}.
     */
    P_EC_TYPE("p->ec_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;echo_confidence_hist_enabled}.
     */
    P_ECHO_CONFIDENCE_HIST_ENABLED("p->echo_confidence_hist_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;echo_detector_mode}.
     */
    P_ECHO_DETECTOR_MODE("p->echo_detector_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;echo_off_threshold}.
     */
    P_ECHO_OFF_THRESHOLD("p->echo_off_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;echo_on_threshold}.
     */
    P_ECHO_ON_THRESHOLD("p->echo_on_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;echo_on_threshold_sw_only}.
     */
    P_ECHO_ON_THRESHOLD_SW_ONLY("p->echo_on_threshold_sw_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;echo_sup_opt_fix}.
     */
    P_ECHO_SUP_OPT_FIX("p->echo_sup_opt_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;ecn_ce_per_sec_threshold}.
     */
    P_ECN_CE_PER_SEC_THRESHOLD("p->ecn_ce_per_sec_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ecn_pct_ce_threshold}.
     */
    P_ECN_PCT_CE_THRESHOLD("p->ecn_pct_ce_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ecn_slide_window_size}.
     */
    P_ECN_SLIDE_WINDOW_SIZE("p->ecn_slide_window_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enabl_ml_udst_ratio_calc}.
     */
    P_ENABL_ML_UDST_RATIO_CALC("p->enabl_ml_udst_ratio_calc", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;enable}.
     */
    P_ENABLE("p->enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_adaptive_probing}.
     */
    P_ENABLE_ADAPTIVE_PROBING("p->enable_adaptive_probing", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_aud_rc_param_ts_logging}.
     */
    P_ENABLE_AUD_RC_PARAM_TS_LOGGING("p->enable_aud_rc_param_ts_logging", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_aud_share_mixer}.
     */
    P_ENABLE_AUD_SHARE_MIXER("p->enable_aud_share_mixer", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_aud_share_resampling}.
     */
    P_ENABLE_AUD_SHARE_RESAMPLING("p->enable_aud_share_resampling", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_device_restart_record}.
     */
    P_ENABLE_AUDIO_DEVICE_RESTART_RECORD("p->enable_audio_device_restart_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_dtx_encryption}.
     */
    P_ENABLE_AUDIO_DTX_ENCRYPTION("p->enable_audio_dtx_encryption", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_oob_fec_feature}.
     */
    P_ENABLE_AUDIO_OOB_FEC_FEATURE("p->enable_audio_oob_fec_feature", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_oob_fec_for_sender}.
     */
    P_ENABLE_AUDIO_OOB_FEC_FOR_SENDER("p->enable_audio_oob_fec_for_sender", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_piggyback_feature}.
     */
    P_ENABLE_AUDIO_PIGGYBACK_FEATURE("p->enable_audio_piggyback_feature", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_pkt_piggyback_for_sender}.
     */
    P_ENABLE_AUDIO_PKT_PIGGYBACK_FOR_SENDER("p->enable_audio_pkt_piggyback_for_sender", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_audio_sender_bwe}.
     */
    P_ENABLE_AUDIO_SENDER_BWE("p->enable_audio_sender_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_av1_to_h264_fallback}.
     */
    P_ENABLE_AV1_TO_H264_FALLBACK("p->enable_av1_to_h264_fallback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bad_call_prevention}.
     */
    P_ENABLE_BAD_CALL_PREVENTION("p->enable_bad_call_prevention", VoipParamType.INTEGER, 1, true),

    /**
     * Native descriptor for {@code p-&gt;enable_bitrate_probing}.
     */
    P_ENABLE_BITRATE_PROBING("p->enable_bitrate_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_both_et_pt_lib_loading}.
     */
    P_ENABLE_BOTH_ET_PT_LIB_LOADING("p->enable_both_et_pt_lib_loading", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_ceiling_calc_by_link_capacity}.
     */
    P_ENABLE_BWE_CEILING_CALC_BY_LINK_CAPACITY("p->enable_bwe_ceiling_calc_by_link_capacity", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_ceiling_calc_by_turning_pt}.
     */
    P_ENABLE_BWE_CEILING_CALC_BY_TURNING_PT("p->enable_bwe_ceiling_calc_by_turning_pt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_dyn_param}.
     */
    P_ENABLE_BWE_DYN_PARAM("p->enable_bwe_dyn_param", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_dyn_param_in_congestion_detection}.
     */
    P_ENABLE_BWE_DYN_PARAM_IN_CONGESTION_DETECTION("p->enable_bwe_dyn_param_in_congestion_detection", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_hold}.
     */
    P_ENABLE_BWE_HOLD("p->enable_bwe_hold", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_init_ts_check}.
     */
    P_ENABLE_BWE_INIT_TS_CHECK("p->enable_bwe_init_ts_check", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_bwe_set_history_bitrate_fix}.
     */
    P_ENABLE_BWE_SET_HISTORY_BITRATE_FIX("p->enable_bwe_set_history_bitrate_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_call_participant_record_saving}.
     */
    P_ENABLE_CALL_PARTICIPANT_RECORD_SAVING("p->enable_call_participant_record_saving", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_call_record_query_test}.
     */
    P_ENABLE_CALL_RECORD_QUERY_TEST("p->enable_call_record_query_test", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_call_state_v1_record_saving}.
     */
    P_ENABLE_CALL_STATE_V1_RECORD_SAVING("p->enable_call_state_v1_record_saving", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_calling_audio_record}.
     */
    P_ENABLE_CALLING_AUDIO_RECORD("p->enable_calling_audio_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_cbr}.
     */
    P_ENABLE_CBR("p->enable_cbr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_cc_bwe_slow_ramp_up}.
     */
    P_ENABLE_CC_BWE_SLOW_RAMP_UP("p->enable_cc_bwe_slow_ramp_up", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;enable_cc_bwe_slow_ramp_up_sfu_ul}.
     */
    P_ENABLE_CC_BWE_SLOW_RAMP_UP_SFU_UL("p->enable_cc_bwe_slow_ramp_up_sfu_ul", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_check_for_warp_header}.
     */
    P_ENABLE_CHECK_FOR_WARP_HEADER("p->enable_check_for_warp_header", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_conditions_on_downlink}.
     */
    P_ENABLE_CONDITIONS_ON_DOWNLINK("p->enable_conditions_on_downlink", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_crypto_scope}.
     */
    P_ENABLE_CRYPTO_SCOPE("p->enable_crypto_scope", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_diff_ec_metrics}.
     */
    P_ENABLE_DIFF_EC_METRICS("p->enable_diff_ec_metrics", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_dl_forced_probing}.
     */
    P_ENABLE_DL_FORCED_PROBING("p->enable_dl_forced_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_dl_model_load_at_bwe_create}.
     */
    P_ENABLE_DL_MODEL_LOAD_AT_BWE_CREATE("p->enable_dl_model_load_at_bwe_create", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_downlink_relay_latency_only}.
     */
    P_ENABLE_DOWNLINK_RELAY_LATENCY_ONLY("p->enable_downlink_relay_latency_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_dtx}.
     */
    P_ENABLE_DTX("p->enable_dtx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_dynamic_init_bwe_fallback}.
     */
    P_ENABLE_DYNAMIC_INIT_BWE_FALLBACK("p->enable_dynamic_init_bwe_fallback", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_dynamic_init_dl_bwe_fallback}.
     */
    P_ENABLE_DYNAMIC_INIT_DL_BWE_FALLBACK("p->enable_dynamic_init_dl_bwe_fallback", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ecn_bwe}.
     */
    P_ENABLE_ECN_BWE("p->enable_ecn_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_empty_rtt_check}.
     */
    P_ENABLE_EMPTY_RTT_CHECK("p->enable_empty_rtt_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_encode_with_secondary}.
     */
    P_ENABLE_ENCODE_WITH_SECONDARY("p->enable_encode_with_secondary", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_enh_scaling}.
     */
    P_ENABLE_ENH_SCALING("p->enable_enh_scaling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_executorch_lib_loading}.
     */
    P_ENABLE_EXECUTORCH_LIB_LOADING("p->enable_executorch_lib_loading", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fast_remb}.
     */
    P_ENABLE_FAST_REMB("p->enable_fast_remb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fast_rr}.
     */
    P_ENABLE_FAST_RR("p->enable_fast_rr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_feature_length_bitmap_check}.
     */
    P_ENABLE_FEATURE_LENGTH_BITMAP_CHECK("p->enable_feature_length_bitmap_check", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fec}.
     */
    P_ENABLE_FEC("p->enable_fec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fec_for_key_frames}.
     */
    P_ENABLE_FEC_FOR_KEY_FRAMES("p->enable_fec_for_key_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fix_for_tx_buf_release}.
     */
    P_ENABLE_FIX_FOR_TX_BUF_RELEASE("p->enable_fix_for_tx_buf_release", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fix_vid_stream_resume_after_direct_bwa}.
     */
    P_ENABLE_FIX_VID_STREAM_RESUME_AFTER_DIRECT_BWA("p->enable_fix_vid_stream_resume_after_direct_bwa", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_for_screen_sharer_only}.
     */
    P_ENABLE_FOR_SCREEN_SHARER_ONLY("p->enable_for_screen_sharer_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_forced_probing}.
     */
    P_ENABLE_FORCED_PROBING("p->enable_forced_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_fr_on_non_default_init_value}.
     */
    P_ENABLE_FR_ON_NON_DEFAULT_INIT_VALUE("p->enable_fr_on_non_default_init_value", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_frame_dropper}.
     */
    P_ENABLE_FRAME_DROPPER("p->enable_frame_dropper", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_frame_dropper_delta_frame_fix}.
     */
    P_ENABLE_FRAME_DROPPER_DELTA_FRAME_FIX("p->enable_frame_dropper_delta_frame_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_gc_uaqc}.
     */
    P_ENABLE_GC_UAQC("p->enable_gc_uaqc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_gc_uaqc_reset_on_gc_transition}.
     */
    P_ENABLE_GC_UAQC_RESET_ON_GC_TRANSITION("p->enable_gc_uaqc_reset_on_gc_transition", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_goodput_in_sbwe}.
     */
    P_ENABLE_GOODPUT_IN_SBWE("p->enable_goodput_in_sbwe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_group_call_history_based_rtt}.
     */
    P_ENABLE_GROUP_CALL_HISTORY_BASED_RTT("p->enable_group_call_history_based_rtt", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_fec_destroy_race_fix}.
     */
    P_ENABLE_HBH_FEC_DESTROY_RACE_FIX("p->enable_hbh_fec_destroy_race_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_fec_rx}.
     */
    P_ENABLE_HBH_FEC_RX("p->enable_hbh_fec_rx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_fec_splitter_tx}.
     */
    P_ENABLE_HBH_FEC_SPLITTER_TX("p->enable_hbh_fec_splitter_tx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_fec_srtp_tx}.
     */
    P_ENABLE_HBH_FEC_SRTP_TX("p->enable_hbh_fec_srtp_tx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_srtp}.
     */
    P_ENABLE_HBH_SRTP("p->enable_hbh_srtp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hbh_srtp_tx_fix}.
     */
    P_ENABLE_HBH_SRTP_TX_FIX("p->enable_hbh_srtp_tx_fix", VoipParamType.INTEGER, 1, false);

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
    VoipParamKeyCall1(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {
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
