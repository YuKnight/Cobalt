package com.github.auties00.cobalt.calls2.common;

/**
 * A partition of the {@code mvp-&gt;/vp-&gt;} voip-param registry keys.
 *
 * <p>This enum exists only to keep its generated static initializer within the JVM 64KB
 * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}
 * rather than this partition directly.
 */
enum VoipParamKeyMedia1 implements VoipParamKey {
    /**
     * Native descriptor for {@code mvp-&gt;add_hbh_fec_ssrc_to_stream_descriptor}.
     */
    MVP_ADD_HBH_FEC_SSRC_TO_STREAM_DESCRIPTOR("mvp->add_hbh_fec_ssrc_to_stream_descriptor", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;afb_interval}.
     */
    MVP_AFB_INTERVAL("mvp->afb_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;always_update_audio_stream}.
     */
    MVP_ALWAYS_UPDATE_AUDIO_STREAM("mvp->always_update_audio_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_allocate_circ_buffer_for_rx_stream_only}.
     */
    MVP_APP_DATA_APP_DATA_ALLOCATE_CIRC_BUFFER_FOR_RX_STREAM_ONLY("mvp->app_data.app_data_allocate_circ_buffer_for_rx_stream_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_allow_dup_rtp_packets}.
     */
    MVP_APP_DATA_APP_DATA_ALLOW_DUP_RTP_PACKETS("mvp->app_data.app_data_allow_dup_rtp_packets", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_allow_out_of_order_rtp_packets}.
     */
    MVP_APP_DATA_APP_DATA_ALLOW_OUT_OF_ORDER_RTP_PACKETS("mvp->app_data.app_data_allow_out_of_order_rtp_packets", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_stream_resend_interval_ms}.
     */
    MVP_APP_DATA_APP_DATA_STREAM_RESEND_INTERVAL_MS("mvp->app_data.app_data_stream_resend_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_stream_version}.
     */
    MVP_APP_DATA_APP_DATA_STREAM_VERSION("mvp->app_data.app_data_stream_version", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.app_data_use_slab_allocator}.
     */
    MVP_APP_DATA_APP_DATA_USE_SLAB_ALLOCATOR("mvp->app_data.app_data_use_slab_allocator", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;app_data.enable_app_data_stream}.
     */
    MVP_APP_DATA_ENABLE_APP_DATA_STREAM("mvp->app_data.enable_app_data_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;aud_lqm_stats_calc_min_kbps}.
     */
    MVP_AUD_LQM_STATS_CALC_MIN_KBPS("mvp->aud_lqm_stats_calc_min_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;aud_stream_update_last_decode_ts_fix}.
     */
    MVP_AUD_STREAM_UPDATE_LAST_DECODE_TS_FIX("mvp->aud_stream_update_last_decode_ts_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_capping_pause_sn_window_msec}.
     */
    MVP_AUDIO_CAPPING_PAUSE_SN_WINDOW_MSEC("mvp->audio_capping_pause_sn_window_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_decoder_do_not_pad_zeros}.
     */
    MVP_AUDIO_DECODER_DO_NOT_PAD_ZEROS("mvp->audio_decoder_do_not_pad_zeros", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_level_history_param.capacity}.
     */
    MVP_AUDIO_LEVEL_HISTORY_PARAM_CAPACITY("mvp->audio_level_history_param.capacity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_level_history_param.history_duration_ms}.
     */
    MVP_AUDIO_LEVEL_HISTORY_PARAM_HISTORY_DURATION_MS("mvp->audio_level_history_param.history_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_level_history_param.num_lsb_to_zero}.
     */
    MVP_AUDIO_LEVEL_HISTORY_PARAM_NUM_LSB_TO_ZERO("mvp->audio_level_history_param.num_lsb_to_zero", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_metrics_stft_source}.
     */
    MVP_AUDIO_METRICS_STFT_SOURCE("mvp->audio_metrics_stft_source", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_nack_jitter_multiplier}.
     */
    MVP_AUDIO_NACK_JITTER_MULTIPLIER("mvp->audio_nack_jitter_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_nack_max_seq_req}.
     */
    MVP_AUDIO_NACK_MAX_SEQ_REQ("mvp->audio_nack_max_seq_req", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_nack_pri_fec}.
     */
    MVP_AUDIO_NACK_PRI_FEC("mvp->audio_nack_pri_fec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_nack_seq_min_delay}.
     */
    MVP_AUDIO_NACK_SEQ_MIN_DELAY("mvp->audio_nack_seq_min_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_piggyback_enable_cache}.
     */
    MVP_AUDIO_PIGGYBACK_ENABLE_CACHE("mvp->audio_piggyback_enable_cache", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_stream_ts_logger_log_period_ms}.
     */
    MVP_AUDIO_STREAM_TS_LOGGER_LOG_PERIOD_MS("mvp->audio_stream_ts_logger_log_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;audio_ts_jitter_use_frame_ts}.
     */
    MVP_AUDIO_TS_JITTER_USE_FRAME_TS("mvp->audio_ts_jitter_use_frame_ts", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;avsync_feedback_to_audio_fraction}.
     */
    MVP_AVSYNC_FEEDBACK_TO_AUDIO_FRACTION("mvp->avsync_feedback_to_audio_fraction", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;avsync_feedback_to_audio_max_threshold_ms}.
     */
    MVP_AVSYNC_FEEDBACK_TO_AUDIO_MAX_THRESHOLD_MS("mvp->avsync_feedback_to_audio_max_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;avsync_feedback_to_audio_min_threshold_ms}.
     */
    MVP_AVSYNC_FEEDBACK_TO_AUDIO_MIN_THRESHOLD_MS("mvp->avsync_feedback_to_audio_min_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;avsync_feedback_to_audio_update_interval_ms}.
     */
    MVP_AVSYNC_FEEDBACK_TO_AUDIO_UPDATE_INTERVAL_MS("mvp->avsync_feedback_to_audio_update_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;avsync_feedback_to_audio_weight}.
     */
    MVP_AVSYNC_FEEDBACK_TO_AUDIO_WEIGHT("mvp->avsync_feedback_to_audio_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;backup_signaling_over_rtcp_bitmap}.
     */
    MVP_BACKUP_SIGNALING_OVER_RTCP_BITMAP("mvp->backup_signaling_over_rtcp_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;burst_drain_enabled}.
     */
    MVP_BURST_DRAIN_ENABLED("mvp->burst_drain_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;bwe_update_before_applying_rc_dyn}.
     */
    MVP_BWE_UPDATE_BEFORE_APPLYING_RC_DYN("mvp->bwe_update_before_applying_rc_dyn", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;call_replayer_sampling_rate}.
     */
    MVP_CALL_REPLAYER_SAMPLING_RATE("mvp->call_replayer_sampling_rate", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;call_replayer_tag}.
     */
    MVP_CALL_REPLAYER_TAG("mvp->call_replayer_tag", VoipParamType.STRING, 150, false),

    /**
     * Native descriptor for {@code mvp-&gt;capture_dev_skip_dupe_frames}.
     */
    MVP_CAPTURE_DEV_SKIP_DUPE_FRAMES("mvp->capture_dev_skip_dupe_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;circ_buf_len_mutex_enabled}.
     */
    MVP_CIRC_BUF_LEN_MUTEX_ENABLED("mvp->circ_buf_len_mutex_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;codec_avatar_duplex_mode}.
     */
    MVP_CODEC_AVATAR_DUPLEX_MODE("mvp->codec_avatar_duplex_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;collect_pinning_view_stats}.
     */
    MVP_COLLECT_PINNING_VIEW_STATS("mvp->collect_pinning_view_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;compute_rx_audio_level}.
     */
    MVP_COMPUTE_RX_AUDIO_LEVEL("mvp->compute_rx_audio_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;conf_bridge_sampling_rate}.
     */
    MVP_CONF_BRIDGE_SAMPLING_RATE("mvp->conf_bridge_sampling_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;connected_video_gc_bad.jb_empty_pct_threshold}.
     */
    MVP_CONNECTED_VIDEO_GC_BAD_JB_EMPTY_PCT_THRESHOLD("mvp->connected_video_gc_bad.jb_empty_pct_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;connected_video_gc_bad.jb_lost_pct_threshold}.
     */
    MVP_CONNECTED_VIDEO_GC_BAD_JB_LOST_PCT_THRESHOLD("mvp->connected_video_gc_bad.jb_lost_pct_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;connected_video_gc_bad.jb_total_plc_pct_threshold}.
     */
    MVP_CONNECTED_VIDEO_GC_BAD_JB_TOTAL_PLC_PCT_THRESHOLD("mvp->connected_video_gc_bad.jb_total_plc_pct_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;connected_video_gc_bad.mte_delay_threshold}.
     */
    MVP_CONNECTED_VIDEO_GC_BAD_MTE_DELAY_THRESHOLD("mvp->connected_video_gc_bad.mte_delay_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;connected_video_gc_bad.mte_neteq_delay_threshold}.
     */
    MVP_CONNECTED_VIDEO_GC_BAD_MTE_NETEQ_DELAY_THRESHOLD("mvp->connected_video_gc_bad.mte_neteq_delay_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;data_channel_connection_timeout_ms}.
     */
    MVP_DATA_CHANNEL_CONNECTION_TIMEOUT_MS("mvp->data_channel_connection_timeout_ms", VoipParamType.INTEGER, 8, false),

    /**
     * Native descriptor for {@code mvp-&gt;disable_freeze_metrics_on_background}.
     */
    MVP_DISABLE_FREEZE_METRICS_ON_BACKGROUND("mvp->disable_freeze_metrics_on_background", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;disable_hbh_nack_p2p_to_relay}.
     */
    MVP_DISABLE_HBH_NACK_P2P_TO_RELAY("mvp->disable_hbh_nack_p2p_to_relay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;disable_hbh_nack_p2p_to_relay_v2}.
     */
    MVP_DISABLE_HBH_NACK_P2P_TO_RELAY_V2("mvp->disable_hbh_nack_p2p_to_relay_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;discard_hbh_rtcp_pkts_from_old_relay}.
     */
    MVP_DISCARD_HBH_RTCP_PKTS_FROM_OLD_RELAY("mvp->discard_hbh_rtcp_pkts_from_old_relay", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;driver_sampling_rate_max}.
     */
    MVP_DRIVER_SAMPLING_RATE_MAX("mvp->driver_sampling_rate_max", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtls_sctp_extra_header_size}.
     */
    MVP_DTLS_SCTP_EXTRA_HEADER_SIZE("mvp->dtls_sctp_extra_header_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtmf_clock_rate_khz}.
     */
    MVP_DTMF_CLOCK_RATE_KHZ("mvp->dtmf_clock_rate_khz", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtmf_event_default_duration_ms}.
     */
    MVP_DTMF_EVENT_DEFAULT_DURATION_MS("mvp->dtmf_event_default_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtmf_payload_type}.
     */
    MVP_DTMF_PAYLOAD_TYPE("mvp->dtmf_payload_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtx_disable_aggressive_report}.
     */
    MVP_DTX_DISABLE_AGGRESSIVE_REPORT("mvp->dtx_disable_aggressive_report", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtx_enable_nack_during_delay_reset}.
     */
    MVP_DTX_ENABLE_NACK_DURING_DELAY_RESET("mvp->dtx_enable_nack_during_delay_reset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtx_jb_avg_target_size}.
     */
    MVP_DTX_JB_AVG_TARGET_SIZE("mvp->dtx_jb_avg_target_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;dtx_play_saved_samples}.
     */
    MVP_DTX_PLAY_SAVED_SAMPLES("mvp->dtx_play_saved_samples", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;empty_frm_buf_on_decoder_disconnect}.
     */
    MVP_EMPTY_FRM_BUF_ON_DECODER_DISCONNECT("mvp->empty_frm_buf_on_decoder_disconnect", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_3p_group_call_openh264_320x240_fix}.
     */
    MVP_ENABLE_3P_GROUP_CALL_OPENH264_320X240_FIX("mvp->enable_3p_group_call_openh264_320x240_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_48khz_rtp_clock}.
     */
    MVP_ENABLE_48KHZ_RTP_CLOCK("mvp->enable_48khz_rtp_clock", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_additional_dtx_frames_at_call_start_ms}.
     */
    MVP_ENABLE_ADDITIONAL_DTX_FRAMES_AT_CALL_START_MS("mvp->enable_additional_dtx_frames_at_call_start_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_adj_enc_res_by_peer}.
     */
    MVP_ENABLE_ADJ_ENC_RES_BY_PEER("mvp->enable_adj_enc_res_by_peer", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_audio_capping_on_edgeray}.
     */
    MVP_ENABLE_AUDIO_CAPPING_ON_EDGERAY("mvp->enable_audio_capping_on_edgeray", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_audio_driver_early_init_for_callee}.
     */
    MVP_ENABLE_AUDIO_DRIVER_EARLY_INIT_FOR_CALLEE("mvp->enable_audio_driver_early_init_for_callee", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_audio_target_include_secondary}.
     */
    MVP_ENABLE_AUDIO_TARGET_INCLUDE_SECONDARY("mvp->enable_audio_target_include_secondary", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_audiodrop_on_peer_interrupted}.
     */
    MVP_ENABLE_AUDIODROP_ON_PEER_INTERRUPTED("mvp->enable_audiodrop_on_peer_interrupted", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_av_sync_at_dtx}.
     */
    MVP_ENABLE_AV_SYNC_AT_DTX("mvp->enable_av_sync_at_dtx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_av_sync_dtx_interpolation}.
     */
    MVP_ENABLE_AV_SYNC_DTX_INTERPOLATION("mvp->enable_av_sync_dtx_interpolation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_avsync_feedback_ingestion_neteq}.
     */
    MVP_ENABLE_AVSYNC_FEEDBACK_INGESTION_NETEQ("mvp->enable_avsync_feedback_ingestion_neteq", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_avsync_feedback_to_audio}.
     */
    MVP_ENABLE_AVSYNC_FEEDBACK_TO_AUDIO("mvp->enable_avsync_feedback_to_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_biz_calling_afb}.
     */
    MVP_ENABLE_BIZ_CALLING_AFB("mvp->enable_biz_calling_afb", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_bwe_send_ts_fix}.
     */
    MVP_ENABLE_BWE_SEND_TS_FIX("mvp->enable_bwe_send_ts_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_call_context_ts_logging}.
     */
    MVP_ENABLE_CALL_CONTEXT_TS_LOGGING("mvp->enable_call_context_ts_logging", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_client_ts_logger}.
     */
    MVP_ENABLE_CLIENT_TS_LOGGER("mvp->enable_client_ts_logger", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_clock_thread_pause_resume}.
     */
    MVP_ENABLE_CLOCK_THREAD_PAUSE_RESUME("mvp->enable_clock_thread_pause_resume", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_conf_bridge_ml_ns_override}.
     */
    MVP_ENABLE_CONF_BRIDGE_ML_NS_OVERRIDE("mvp->enable_conf_bridge_ml_ns_override", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_data_channel}.
     */
    MVP_ENABLE_DATA_CHANNEL("mvp->enable_data_channel", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_dec_mutex_fix}.
     */
    MVP_ENABLE_DEC_MUTEX_FIX("mvp->enable_dec_mutex_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_default_h264decoder}.
     */
    MVP_ENABLE_DEFAULT_H264DECODER("mvp->enable_default_h264decoder", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_device_clock_rate_update}.
     */
    MVP_ENABLE_DEVICE_CLOCK_RATE_UPDATE("mvp->enable_device_clock_rate_update", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_device_timestamps}.
     */
    MVP_ENABLE_DEVICE_TIMESTAMPS("mvp->enable_device_timestamps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_dtmf_rfc4733_support}.
     */
    MVP_ENABLE_DTMF_RFC4733_SUPPORT("mvp->enable_dtmf_rfc4733_support", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_dtx_follow_opus_standard}.
     */
    MVP_ENABLE_DTX_FOLLOW_OPUS_STANDARD("mvp->enable_dtx_follow_opus_standard", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_ev_thread_race_fix}.
     */
    MVP_ENABLE_EV_THREAD_RACE_FIX("mvp->enable_ev_thread_race_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_face_detection}.
     */
    MVP_ENABLE_FACE_DETECTION("mvp->enable_face_detection", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_fine_grained_peer_camera_pause_fs}.
     */
    MVP_ENABLE_FINE_GRAINED_PEER_CAMERA_PAUSE_FS("mvp->enable_fine_grained_peer_camera_pause_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_frame_info_based_fmt_update}.
     */
    MVP_ENABLE_FRAME_INFO_BASED_FMT_UPDATE("mvp->enable_frame_info_based_fmt_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_frame_merging_for_bot_calls}.
     */
    MVP_ENABLE_FRAME_MERGING_FOR_BOT_CALLS("mvp->enable_frame_merging_for_bot_calls", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_freeze_disable_in_call_screen_bg_hook}.
     */
    MVP_ENABLE_FREEZE_DISABLE_IN_CALL_SCREEN_BG_HOOK("mvp->enable_freeze_disable_in_call_screen_bg_hook", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_get_index_from_old_ctx}.
     */
    MVP_ENABLE_GET_INDEX_FROM_OLD_CTX("mvp->enable_get_index_from_old_ctx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_group_call_aspect_ratio_fix}.
     */
    MVP_ENABLE_GROUP_CALL_ASPECT_RATIO_FIX("mvp->enable_group_call_aspect_ratio_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_group_call_self_preview_size_for_ratio}.
     */
    MVP_ENABLE_GROUP_CALL_SELF_PREVIEW_SIZE_FOR_RATIO("mvp->enable_group_call_self_preview_size_for_ratio", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_compound_rtcp_fix}.
     */
    MVP_ENABLE_HBH_COMPOUND_RTCP_FIX("mvp->enable_hbh_compound_rtcp_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_data_channel}.
     */
    MVP_ENABLE_HBH_DATA_CHANNEL("mvp->enable_hbh_data_channel", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_nack_audio}.
     */
    MVP_ENABLE_HBH_NACK_AUDIO("mvp->enable_hbh_nack_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_nack_video}.
     */
    MVP_ENABLE_HBH_NACK_VIDEO("mvp->enable_hbh_nack_video", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_peer_pli_throttle_fix}.
     */
    MVP_ENABLE_HBH_PEER_PLI_THROTTLE_FIX("mvp->enable_hbh_peer_pli_throttle_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_pli_is_video}.
     */
    MVP_ENABLE_HBH_PLI_IS_VIDEO("mvp->enable_hbh_pli_is_video", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_pli_video}.
     */
    MVP_ENABLE_HBH_PLI_VIDEO("mvp->enable_hbh_pli_video", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_server_pli_throttle_fix}.
     */
    MVP_ENABLE_HBH_SERVER_PLI_THROTTLE_FIX("mvp->enable_hbh_server_pli_throttle_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_srtcp_smlv}.
     */
    MVP_ENABLE_HBH_SRTCP_SMLV("mvp->enable_hbh_srtcp_smlv", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_srtp_afb}.
     */
    MVP_ENABLE_HBH_SRTP_AFB("mvp->enable_hbh_srtp_afb", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hbh_srtp_afb_batch}.
     */
    MVP_ENABLE_HBH_SRTP_AFB_BATCH("mvp->enable_hbh_srtp_afb_batch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_hybrid_lazy_encoding}.
     */
    MVP_ENABLE_HYBRID_LAZY_ENCODING("mvp->enable_hybrid_lazy_encoding", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_init_quality_fs}.
     */
    MVP_ENABLE_INIT_QUALITY_FS("mvp->enable_init_quality_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_loss_info_ext}.
     */
    MVP_ENABLE_LOSS_INFO_EXT("mvp->enable_loss_info_ext", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_media_hbh_srtp}.
     */
    MVP_ENABLE_MEDIA_HBH_SRTP("mvp->enable_media_hbh_srtp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_media_platform_event_refactor}.
     */
    MVP_ENABLE_MEDIA_PLATFORM_EVENT_REFACTOR("mvp->enable_media_platform_event_refactor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_media_timeout_terminate_reason}.
     */
    MVP_ENABLE_MEDIA_TIMEOUT_TERMINATE_REASON("mvp->enable_media_timeout_terminate_reason", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_mlow_red}.
     */
    MVP_ENABLE_MLOW_RED("mvp->enable_mlow_red", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_no_audio_metrics}.
     */
    MVP_ENABLE_NO_AUDIO_METRICS("mvp->enable_no_audio_metrics", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_no_reconnecting_indicator_in_self_no_good_status}.
     */
    MVP_ENABLE_NO_RECONNECTING_INDICATOR_IN_SELF_NO_GOOD_STATUS("mvp->enable_no_reconnecting_indicator_in_self_no_good_status", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_oh264_kf_frame_mode}.
     */
    MVP_ENABLE_OH264_KF_FRAME_MODE("mvp->enable_oh264_kf_frame_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_peer_dec_active_time_fix}.
     */
    MVP_ENABLE_PEER_DEC_ACTIVE_TIME_FIX("mvp->enable_peer_dec_active_time_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_pp_flip_tracker}.
     */
    MVP_ENABLE_PP_FLIP_TRACKER("mvp->enable_pp_flip_tracker", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_reconnecting_all_grey_tile}.
     */
    MVP_ENABLE_RECONNECTING_ALL_GREY_TILE("mvp->enable_reconnecting_all_grey_tile", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_dtx_as_redundant}.
     */
    MVP_ENABLE_RED_DTX_AS_REDUNDANT("mvp->enable_red_dtx_as_redundant", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_dtx_carry_redundant}.
     */
    MVP_ENABLE_RED_DTX_CARRY_REDUNDANT("mvp->enable_red_dtx_carry_redundant", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_multi_level_threshold}.
     */
    MVP_ENABLE_RED_MULTI_LEVEL_THRESHOLD("mvp->enable_red_multi_level_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_pt_support_rx}.
     */
    MVP_ENABLE_RED_PT_SUPPORT_RX("mvp->enable_red_pt_support_rx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_pt_support_tx}.
     */
    MVP_ENABLE_RED_PT_SUPPORT_TX("mvp->enable_red_pt_support_tx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_red_ts_fix}.
     */
    MVP_ENABLE_RED_TS_FIX("mvp->enable_red_ts_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_render_queue}.
     */
    MVP_ENABLE_RENDER_QUEUE("mvp->enable_render_queue", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_rp_psnr_calc}.
     */
    MVP_ENABLE_RP_PSNR_CALC("mvp->enable_rp_psnr_calc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_rtx_indication}.
     */
    MVP_ENABLE_RTX_INDICATION("mvp->enable_rtx_indication", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_rx_subscription}.
     */
    MVP_ENABLE_RX_SUBSCRIPTION("mvp->enable_rx_subscription", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_rx_subscription_vid_quality_field}.
     */
    MVP_ENABLE_RX_SUBSCRIPTION_VID_QUALITY_FIELD("mvp->enable_rx_subscription_vid_quality_field", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_sampling_rate_overrides}.
     */
    MVP_ENABLE_SAMPLING_RATE_OVERRIDES("mvp->enable_sampling_rate_overrides", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_self_preview_size_for_ratio}.
     */
    MVP_ENABLE_SELF_PREVIEW_SIZE_FOR_RATIO("mvp->enable_self_preview_size_for_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_separate_keys_hbh_srtcp}.
     */
    MVP_ENABLE_SEPARATE_KEYS_HBH_SRTCP("mvp->enable_separate_keys_hbh_srtcp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_speaker_status_changed_events}.
     */
    MVP_ENABLE_SPEAKER_STATUS_CHANGED_EVENTS("mvp->enable_speaker_status_changed_events", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_speaker_status_rx}.
     */
    MVP_ENABLE_SPEAKER_STATUS_RX("mvp->enable_speaker_status_rx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_start_transport_media_on_p2p_connection}.
     */
    MVP_ENABLE_START_TRANSPORT_MEDIA_ON_P2P_CONNECTION("mvp->enable_start_transport_media_on_p2p_connection", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_stream_descriptor}.
     */
    MVP_ENABLE_STREAM_DESCRIPTOR("mvp->enable_stream_descriptor", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_stream_mutex_fix_for_aud_ev}.
     */
    MVP_ENABLE_STREAM_MUTEX_FIX_FOR_AUD_EV("mvp->enable_stream_mutex_fix_for_aud_ev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_stream_mutex_fix_for_vid_ev}.
     */
    MVP_ENABLE_STREAM_MUTEX_FIX_FOR_VID_EV("mvp->enable_stream_mutex_fix_for_vid_ev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_teardown_reorder_fix}.
     */
    MVP_ENABLE_TEARDOWN_REORDER_FIX("mvp->enable_teardown_reorder_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_transport_feedback}.
     */
    MVP_ENABLE_TRANSPORT_FEEDBACK("mvp->enable_transport_feedback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_ts_logger_pkt_loss_pattern}.
     */
    MVP_ENABLE_TS_LOGGER_PKT_LOSS_PATTERN("mvp->enable_ts_logger_pkt_loss_pattern", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_ts_logger_render_events}.
     */
    MVP_ENABLE_TS_LOGGER_RENDER_EVENTS("mvp->enable_ts_logger_render_events", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_ul_audio_lqm}.
     */
    MVP_ENABLE_UL_AUDIO_LQM("mvp->enable_ul_audio_lqm", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_ul_vid_pause_standalone}.
     */
    MVP_ENABLE_UL_VID_PAUSE_STANDALONE("mvp->enable_ul_vid_pause_standalone", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_uplink_prefetch_video}.
     */
    MVP_ENABLE_UPLINK_PREFETCH_VIDEO("mvp->enable_uplink_prefetch_video", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_valid_pause_check}.
     */
    MVP_ENABLE_VALID_PAUSE_CHECK("mvp->enable_valid_pause_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_vid_dev_set_preferred_driver}.
     */
    MVP_ENABLE_VID_DEV_SET_PREFERRED_DRIVER("mvp->enable_vid_dev_set_preferred_driver", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_vid_jb_dd_calc}.
     */
    MVP_ENABLE_VID_JB_DD_CALC("mvp->enable_vid_jb_dd_calc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_vid_jb_dd_err_recovery}.
     */
    MVP_ENABLE_VID_JB_DD_ERR_RECOVERY("mvp->enable_vid_jb_dd_err_recovery", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_vid_jb_dd_use_result}.
     */
    MVP_ENABLE_VID_JB_DD_USE_RESULT("mvp->enable_vid_jb_dd_use_result", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_vid_port_restart_stats_accumulate}.
     */
    MVP_ENABLE_VID_PORT_RESTART_STATS_ACCUMULATE("mvp->enable_vid_port_restart_stats_accumulate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_video_rtp_hdr_ext_stream_subscription}.
     */
    MVP_ENABLE_VIDEO_RTP_HDR_EXT_STREAM_SUBSCRIPTION("mvp->enable_video_rtp_hdr_ext_stream_subscription", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_video_simulcast}.
     */
    MVP_ENABLE_VIDEO_SIMULCAST("mvp->enable_video_simulcast", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_warp_hbh_fec_ssrc}.
     */
    MVP_ENABLE_WARP_HBH_FEC_SSRC("mvp->enable_warp_hbh_fec_ssrc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_webrtc_nack_requester}.
     */
    MVP_ENABLE_WEBRTC_NACK_REQUESTER("mvp->enable_webrtc_nack_requester", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_webrtc_video_jb}.
     */
    MVP_ENABLE_WEBRTC_VIDEO_JB("mvp->enable_webrtc_video_jb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enable_xr2d_codec_avatar_video_state}.
     */
    MVP_ENABLE_XR2D_CODEC_AVATAR_VIDEO_STATE("mvp->enable_xr2d_codec_avatar_video_state", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enc_fps_over_capture_fps_threshold}.
     */
    MVP_ENC_FPS_OVER_CAPTURE_FPS_THRESHOLD("mvp->enc_fps_over_capture_fps_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enc_res_align_base}.
     */
    MVP_ENC_RES_ALIGN_BASE("mvp->enc_res_align_base", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enforce_audio_reserve_bitrate}.
     */
    MVP_ENFORCE_AUDIO_RESERVE_BITRATE("mvp->enforce_audio_reserve_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;enforce_pools_for_media_platform_events_with_data}.
     */
    MVP_ENFORCE_POOLS_FOR_MEDIA_PLATFORM_EVENTS_WITH_DATA("mvp->enforce_pools_for_media_platform_events_with_data", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;fec_bw_downgrade_min_plr}.
     */
    MVP_FEC_BW_DOWNGRADE_MIN_PLR("mvp->fec_bw_downgrade_min_plr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;fix_first_frame_converter_resize}.
     */
    MVP_FIX_FIRST_FRAME_CONVERTER_RESIZE("mvp->fix_first_frame_converter_resize", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;fix_render_pause_duration_across_segments}.
     */
    MVP_FIX_RENDER_PAUSE_DURATION_ACROSS_SEGMENTS("mvp->fix_render_pause_duration_across_segments", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;force_3_2_aspect_ratio}.
     */
    MVP_FORCE_3_2_ASPECT_RATIO("mvp->force_3_2_aspect_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;force_passive_capture_dev_stream_role}.
     */
    MVP_FORCE_PASSIVE_CAPTURE_DEV_STREAM_ROLE("mvp->force_passive_capture_dev_stream_role", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;force_refresh_capture_port_camera}.
     */
    MVP_FORCE_REFRESH_CAPTURE_PORT_CAMERA("mvp->force_refresh_capture_port_camera", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;force_swb}.
     */
    MVP_FORCE_SWB("mvp->force_swb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;force_width}.
     */
    MVP_FORCE_WIDTH("mvp->force_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;frm_buf_convert_outside_lock}.
     */
    MVP_FRM_BUF_CONVERT_OUTSIDE_LOCK("mvp->frm_buf_convert_outside_lock", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;get_detailed_v2v_afl_stats}.
     */
    MVP_GET_DETAILED_V2V_AFL_STATS("mvp->get_detailed_v2v_afl_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;harmonic_fps_interval}.
     */
    MVP_HARMONIC_FPS_INTERVAL("mvp->harmonic_fps_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_nack_control_p2p}.
     */
    MVP_HBH_NACK_CONTROL_P2P("mvp->hbh_nack_control_p2p", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_srtp_peroidic_sync_roc_timeout_ms}.
     */
    MVP_HBH_SRTP_PEROIDIC_SYNC_ROC_TIMEOUT_MS("mvp->hbh_srtp_peroidic_sync_roc_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_srtp_states_sync_freq_ms}.
     */
    MVP_HBH_SRTP_STATES_SYNC_FREQ_MS("mvp->hbh_srtp_states_sync_freq_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_srtp_states_sync_num_pkt_resend}.
     */
    MVP_HBH_SRTP_STATES_SYNC_NUM_PKT_RESEND("mvp->hbh_srtp_states_sync_num_pkt_resend", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_srtp_states_sync_timeout_ms}.
     */
    MVP_HBH_SRTP_STATES_SYNC_TIMEOUT_MS("mvp->hbh_srtp_states_sync_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;hbh_warp_roc_sync_timeout_ms}.
     */
    MVP_HBH_WARP_ROC_SYNC_TIMEOUT_MS("mvp->hbh_warp_roc_sync_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;high_sample_rate_year_class_threshold}.
     */
    MVP_HIGH_SAMPLE_RATE_YEAR_CLASS_THRESHOLD("mvp->high_sample_rate_year_class_threshold", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.enable_imu_data_stream}.
     */
    MVP_IMU_DATA_ENABLE_IMU_DATA_STREAM("mvp->imu_data.enable_imu_data_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.enable_imu_data_stream_nack}.
     */
    MVP_IMU_DATA_ENABLE_IMU_DATA_STREAM_NACK("mvp->imu_data.enable_imu_data_stream_nack", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.enable_mock_imu_data_sender}.
     */
    MVP_IMU_DATA_ENABLE_MOCK_IMU_DATA_SENDER("mvp->imu_data.enable_mock_imu_data_sender", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.imu_data_circular_buffer_size}.
     */
    MVP_IMU_DATA_IMU_DATA_CIRCULAR_BUFFER_SIZE("mvp->imu_data.imu_data_circular_buffer_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.imu_data_fpp}.
     */
    MVP_IMU_DATA_IMU_DATA_FPP("mvp->imu_data.imu_data_fpp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;imu_data.imu_data_stream_enc_clock_rate_hz}.
     */
    MVP_IMU_DATA_IMU_DATA_STREAM_ENC_CLOCK_RATE_HZ("mvp->imu_data.imu_data_stream_enc_clock_rate_hz", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;init_quality_window_ms}.
     */
    MVP_INIT_QUALITY_WINDOW_MS("mvp->init_quality_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;init_rtp_ts_on_first_audio_frame}.
     */
    MVP_INIT_RTP_TS_ON_FIRST_AUDIO_FRAME("mvp->init_rtp_ts_on_first_audio_frame", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;initial_fpp}.
     */
    MVP_INITIAL_FPP("mvp->initial_fpp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb_ignore_start_of_call_empties}.
     */
    MVP_JB_IGNORE_START_OF_CALL_EMPTIES("mvp->jb_ignore_start_of_call_empties", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.audio_nack_enable_renack}.
     */
    MVP_JB_AUDIO_NACK_ENABLE_RENACK("mvp->jb.audio_nack_enable_renack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.audio_nack_recheck_on_recv}.
     */
    MVP_JB_AUDIO_NACK_RECHECK_ON_RECV("mvp->jb.audio_nack_recheck_on_recv", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.audio_nack_renack_min_interval_ms}.
     */
    MVP_JB_AUDIO_NACK_RENACK_MIN_INTERVAL_MS("mvp->jb.audio_nack_renack_min_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.audio_nack_renack_rtt_multiplier}.
     */
    MVP_JB_AUDIO_NACK_RENACK_RTT_MULTIPLIER("mvp->jb.audio_nack_renack_rtt_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.audio_nack_rtt_discount_factor}.
     */
    MVP_JB_AUDIO_NACK_RTT_DISCOUNT_FACTOR("mvp->jb.audio_nack_rtt_discount_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.jb_impl}.
     */
    MVP_JB_JB_IMPL("mvp->jb.jb_impl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_enable_ff}.
     */
    MVP_JB_NETEQ_ENABLE_FF("mvp->jb.neteq_enable_ff", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_enable_speaker_status}.
     */
    MVP_JB_NETEQ_ENABLE_SPEAKER_STATUS("mvp->jb.neteq_enable_speaker_status", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params .audio_jitbuf_buffer_limits_window_size_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_AUDIO_JITBUF_BUFFER_LIMITS_WINDOW_SIZE_MS("mvp->jb.neteq_field_trial_params .audio_jitbuf_buffer_limits_window_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params .audio_jitbuf_buffer_lower_limit_scale_percent}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_AUDIO_JITBUF_BUFFER_LOWER_LIMIT_SCALE_PERCENT("mvp->jb.neteq_field_trial_params .audio_jitbuf_buffer_lower_limit_scale_percent", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params .enable_group_decision_logic_for_bot_calls}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_GROUP_DECISION_LOGIC_FOR_BOT_CALLS("mvp->jb.neteq_field_trial_params .enable_group_decision_logic_for_bot_calls", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params .proactive_nack_max_num_missing_packets_predicted}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_PROACTIVE_NACK_MAX_NUM_MISSING_PACKETS_PREDICTED("mvp->jb.neteq_field_trial_params .proactive_nack_max_num_missing_packets_predicted", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.allow_red_jitter}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ALLOW_RED_JITTER("mvp->jb.neteq_field_trial_params.allow_red_jitter", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.allow_time_stretch_acceleration}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ALLOW_TIME_STRETCH_ACCELERATION("mvp->jb.neteq_field_trial_params.allow_time_stretch_acceleration", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.allow_time_stretch_for_high_latency}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ALLOW_TIME_STRETCH_FOR_HIGH_LATENCY("mvp->jb.neteq_field_trial_params.allow_time_stretch_for_high_latency", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.allow_time_stretch_threshold_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ALLOW_TIME_STRETCH_THRESHOLD_MS("mvp->jb.neteq_field_trial_params.allow_time_stretch_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.buffer_flush_max_length_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_BUFFER_FLUSH_MAX_LENGTH_MS("mvp->jb.neteq_field_trial_params.buffer_flush_max_length_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.codec_avatar_processing_delay_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_CODEC_AVATAR_PROCESSING_DELAY_MS("mvp->jb.neteq_field_trial_params.codec_avatar_processing_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.delay_offset_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_DELAY_OFFSET_MS("mvp->jb.neteq_field_trial_params.delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.dl_history_size_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_DL_HISTORY_SIZE_MS("mvp->jb.neteq_field_trial_params.dl_history_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.dm_history_size_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_DM_HISTORY_SIZE_MS("mvp->jb.neteq_field_trial_params.dm_history_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.effective_peak_period_fraction_perc}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_EFFECTIVE_PEAK_PERIOD_FRACTION_PERC("mvp->jb.neteq_field_trial_params.effective_peak_period_fraction_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_codec_plc}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_CODEC_PLC("mvp->jb.neteq_field_trial_params.enable_codec_plc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_custom_required_samples_for_acc}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_CUSTOM_REQUIRED_SAMPLES_FOR_ACC("mvp->jb.neteq_field_trial_params.enable_custom_required_samples_for_acc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_depack_multiframe}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_DEPACK_MULTIFRAME("mvp->jb.neteq_field_trial_params.enable_depack_multiframe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_peak_detector}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_PEAK_DETECTOR("mvp->jb.neteq_field_trial_params.enable_peak_detector", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_proactive_nack}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_PROACTIVE_NACK("mvp->jb.neteq_field_trial_params.enable_proactive_nack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.enable_silence_deletion}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_ENABLE_SILENCE_DELETION("mvp->jb.neteq_field_trial_params.enable_silence_deletion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.init_min_e2e_delay_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_INIT_MIN_E2E_DELAY_MS("mvp->jb.neteq_field_trial_params.init_min_e2e_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.lad_enabled_for_fec}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_LAD_ENABLED_FOR_FEC("mvp->jb.neteq_field_trial_params.lad_enabled_for_fec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.lad_enabled_for_nack}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_LAD_ENABLED_FOR_NACK("mvp->jb.neteq_field_trial_params.lad_enabled_for_nack", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.lad_max_lost_packet_list_size}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_LAD_MAX_LOST_PACKET_LIST_SIZE("mvp->jb.neteq_field_trial_params.lad_max_lost_packet_list_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.lad_nack_extra_insert_time_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_LAD_NACK_EXTRA_INSERT_TIME_MS("mvp->jb.neteq_field_trial_params.lad_nack_extra_insert_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.lad_nack_extra_receive_time_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_LAD_NACK_EXTRA_RECEIVE_TIME_MS("mvp->jb.neteq_field_trial_params.lad_nack_extra_receive_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.max_peak_period_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_MAX_PEAK_PERIOD_MS("mvp->jb.neteq_field_trial_params.max_peak_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.min_peaks_to_trigger}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_MIN_PEAKS_TO_TRIGGER("mvp->jb.neteq_field_trial_params.min_peaks_to_trigger", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.nack_rtt_limit_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_NACK_RTT_LIMIT_MS("mvp->jb.neteq_field_trial_params.nack_rtt_limit_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.neteq_field_trial_lookup_override}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_NETEQ_FIELD_TRIAL_LOOKUP_OVERRIDE("mvp->jb.neteq_field_trial_params.neteq_field_trial_lookup_override", VoipParamType.STRING, 512, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.neteq_field_trial_string_override}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_NETEQ_FIELD_TRIAL_STRING_OVERRIDE("mvp->jb.neteq_field_trial_params.neteq_field_trial_string_override", VoipParamType.STRING, 512, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.num_initial_packets}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_NUM_INITIAL_PACKETS("mvp->jb.neteq_field_trial_params.num_initial_packets", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.peak_detection_threshold}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_PEAK_DETECTION_THRESHOLD("mvp->jb.neteq_field_trial_params.peak_detection_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.preexpand_with_filtered_level_perc}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_PREEXPAND_WITH_FILTERED_LEVEL_PERC("mvp->jb.neteq_field_trial_params.preexpand_with_filtered_level_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.proactive_nack_iat_percentile}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_PROACTIVE_NACK_IAT_PERCENTILE("mvp->jb.neteq_field_trial_params.proactive_nack_iat_percentile", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.proactive_nack_margin_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_PROACTIVE_NACK_MARGIN_MS("mvp->jb.neteq_field_trial_params.proactive_nack_margin_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.red_optimizer_enabled}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_RED_OPTIMIZER_ENABLED("mvp->jb.neteq_field_trial_params.red_optimizer_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.skip_nack_with_fec}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_SKIP_NACK_WITH_FEC("mvp->jb.neteq_field_trial_params.skip_nack_with_fec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.smart_buffer_flush_enabled}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_SMART_BUFFER_FLUSH_ENABLED("mvp->jb.neteq_field_trial_params.smart_buffer_flush_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.smart_buffer_flush_multiplier}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_SMART_BUFFER_FLUSH_MULTIPLIER("mvp->jb.neteq_field_trial_params.smart_buffer_flush_multiplier", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.smart_buffer_flush_target_ms}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_SMART_BUFFER_FLUSH_TARGET_MS("mvp->jb.neteq_field_trial_params.smart_buffer_flush_target_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.underrun_forget_factor}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_UNDERRUN_FORGET_FACTOR("mvp->jb.neteq_field_trial_params.underrun_forget_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.underrun_quantile}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_UNDERRUN_QUANTILE("mvp->jb.neteq_field_trial_params.underrun_quantile", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.use_mute_in_audio_dropping}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_USE_MUTE_IN_AUDIO_DROPPING("mvp->jb.neteq_field_trial_params.use_mute_in_audio_dropping", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.use_muted_state_after_remote_hangup}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_USE_MUTED_STATE_AFTER_REMOTE_HANGUP("mvp->jb.neteq_field_trial_params.use_muted_state_after_remote_hangup", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_field_trial_params.use_span_samples_for_cng}.
     */
    MVP_JB_NETEQ_FIELD_TRIAL_PARAMS_USE_SPAN_SAMPLES_FOR_CNG("mvp->jb.neteq_field_trial_params.use_span_samples_for_cng", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_max_delay}.
     */
    MVP_JB_NETEQ_MAX_DELAY("mvp->jb.neteq_max_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_max_packets_in_buf}.
     */
    MVP_JB_NETEQ_MAX_PACKETS_IN_BUF("mvp->jb.neteq_max_packets_in_buf", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_min_delay}.
     */
    MVP_JB_NETEQ_MIN_DELAY("mvp->jb.neteq_min_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.neteq_use_20ms_get_period}.
     */
    MVP_JB_NETEQ_USE_20MS_GET_PERIOD("mvp->jb.neteq_use_20ms_get_period", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.skip_delay_update_for_rtx}.
     */
    MVP_JB_SKIP_DELAY_UPDATE_FOR_RTX("mvp->jb.skip_delay_update_for_rtx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;jb.update_initial_minimum_delay_in_neteq}.
     */
    MVP_JB_UPDATE_INITIAL_MINIMUM_DELAY_IN_NETEQ("mvp->jb.update_initial_minimum_delay_in_neteq", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;keep_conf_bridge_to_48}.
     */
    MVP_KEEP_CONF_BRIDGE_TO_48("mvp->keep_conf_bridge_to_48", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;keep_conf_bridge_to_wb}.
     */
    MVP_KEEP_CONF_BRIDGE_TO_WB("mvp->keep_conf_bridge_to_wb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;loss_info_ext_int_msec}.
     */
    MVP_LOSS_INFO_EXT_INT_MSEC("mvp->loss_info_ext_int_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;max_audio_ts_jitter_ms}.
     */
    MVP_MAX_AUDIO_TS_JITTER_MS("mvp->max_audio_ts_jitter_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;max_av_resync_duration_in_ms}.
     */
    MVP_MAX_AV_RESYNC_DURATION_IN_MS("mvp->max_av_resync_duration_in_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_control_stat.enable_bitrate_stat_for_field_stat}.
     */
    MVP_MEDIA_CONTROL_STAT_ENABLE_BITRATE_STAT_FOR_FIELD_STAT("mvp->media_control_stat.enable_bitrate_stat_for_field_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_control_stat.enable_jitter_stat_for_field_stat}.
     */
    MVP_MEDIA_CONTROL_STAT_ENABLE_JITTER_STAT_FOR_FIELD_STAT("mvp->media_control_stat.enable_jitter_stat_for_field_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_control_stat.enable_migrated_unified_api}.
     */
    MVP_MEDIA_CONTROL_STAT_ENABLE_MIGRATED_UNIFIED_API("mvp->media_control_stat.enable_migrated_unified_api", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_control_stat.enable_rtt_stat_for_field_stat}.
     */
    MVP_MEDIA_CONTROL_STAT_ENABLE_RTT_STAT_FOR_FIELD_STAT("mvp->media_control_stat.enable_rtt_stat_for_field_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_control_stat.use_aud_jitter_stat_default_peer_jid}.
     */
    MVP_MEDIA_CONTROL_STAT_USE_AUD_JITTER_STAT_DEFAULT_PEER_JID("mvp->media_control_stat.use_aud_jitter_stat_default_peer_jid", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;media_rx_timeout_ms}.
     */
    MVP_MEDIA_RX_TIMEOUT_MS("mvp->media_rx_timeout_ms", VoipParamType.INTEGER, 8, false),

    /**
     * Native descriptor for {@code mvp-&gt;min_audio_level_for_speech_tx}.
     */
    MVP_MIN_AUDIO_LEVEL_FOR_SPEECH_TX("mvp->min_audio_level_for_speech_tx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;min_audio_restarts_for_sampling_rate_check}.
     */
    MVP_MIN_AUDIO_RESTARTS_FOR_SAMPLING_RATE_CHECK("mvp->min_audio_restarts_for_sampling_rate_check", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;min_rx_buf_size_ms}.
     */
    MVP_MIN_RX_BUF_SIZE_MS("mvp->min_rx_buf_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;mlow_red_proactive_rtt_threshold_ms}.
     */
    MVP_MLOW_RED_PROACTIVE_RTT_THRESHOLD_MS("mvp->mlow_red_proactive_rtt_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;mlow_red_proactive_update_limit}.
     */
    MVP_MLOW_RED_PROACTIVE_UPDATE_LIMIT("mvp->mlow_red_proactive_update_limit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;mtu_size}.
     */
    MVP_MTU_SIZE("mvp->mtu_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ns_status_prst_afb_interval_ms}.
     */
    MVP_NS_STATUS_PRST_AFB_INTERVAL_MS("mvp->ns_status_prst_afb_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;num_vp_afb_after_pause}.
     */
    MVP_NUM_VP_AFB_AFTER_PAUSE("mvp->num_vp_afb_after_pause", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;oob_nack}.
     */
    MVP_OOB_NACK("mvp->oob_nack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;optimize_vid_port_frm_buf_allocation}.
     */
    MVP_OPTIMIZE_VID_PORT_FRM_BUF_ALLOCATION("mvp->optimize_vid_port_frm_buf_allocation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;opus_fec_cache_size}.
     */
    MVP_OPUS_FEC_CACHE_SIZE("mvp->opus_fec_cache_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;peer_dec_active_time_threshold_ms}.
     */
    MVP_PEER_DEC_ACTIVE_TIME_THRESHOLD_MS("mvp->peer_dec_active_time_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;peer_high_bwe}.
     */
    MVP_PEER_HIGH_BWE("mvp->peer_high_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;peer_low_bwe}.
     */
    MVP_PEER_LOW_BWE("mvp->peer_low_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;play_cb_skip_no_frame}.
     */
    MVP_PLAY_CB_SKIP_NO_FRAME("mvp->play_cb_skip_no_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;prioritize_hbh_pli_over_nack}.
     */
    MVP_PRIORITIZE_HBH_PLI_OVER_NACK("mvp->prioritize_hbh_pli_over_nack", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;process_hbh_pli}.
     */
    MVP_PROCESS_HBH_PLI("mvp->process_hbh_pli", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;process_packet_on_jbuf_reset}.
     */
    MVP_PROCESS_PACKET_ON_JBUF_RESET("mvp->process_packet_on_jbuf_reset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;propagate_audio_dup_on_remb}.
     */
    MVP_PROPAGATE_AUDIO_DUP_ON_REMB("mvp->propagate_audio_dup_on_remb", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;propagate_updated_settings_audio_jb}.
     */
    MVP_PROPAGATE_UPDATED_SETTINGS_AUDIO_JB("mvp->propagate_updated_settings_audio_jb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;propagate_updated_settings_stream_info}.
     */
    MVP_PROPAGATE_UPDATED_SETTINGS_STREAM_INFO("mvp->propagate_updated_settings_stream_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;psnr_calc_hw_scaler_interval}.
     */
    MVP_PSNR_CALC_HW_SCALER_INTERVAL("mvp->psnr_calc_hw_scaler_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;read_port_skip_no_frame}.
     */
    MVP_READ_PORT_SKIP_NO_FRAME("mvp->read_port_skip_no_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;reconnecting_option}.
     */
    MVP_RECONNECTING_OPTION("mvp->reconnecting_option", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;render_last_frm_copy_interval}.
     */
    MVP_RENDER_LAST_FRM_COPY_INTERVAL("mvp->render_last_frm_copy_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;render_settings.disable_av_sync_for_capi}.
     */
    MVP_RENDER_SETTINGS_DISABLE_AV_SYNC_FOR_CAPI("mvp->render_settings.disable_av_sync_for_capi", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;render_settings.enable_capi_av_sync_ts_reconciliation}.
     */
    MVP_RENDER_SETTINGS_ENABLE_CAPI_AV_SYNC_TS_RECONCILIATION("mvp->render_settings.enable_capi_av_sync_ts_reconciliation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;render_settings.n_packets_for_capi_av_sync_ts_reconciliation}.
     */
    MVP_RENDER_SETTINGS_N_PACKETS_FOR_CAPI_AV_SYNC_TS_RECONCILIATION("mvp->render_settings.n_packets_for_capi_av_sync_ts_reconciliation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;report_aud_lqm_stats}.
     */
    MVP_REPORT_AUD_LQM_STATS("mvp->report_aud_lqm_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;report_header_overhead_in_audio_rr}.
     */
    MVP_REPORT_HEADER_OVERHEAD_IN_AUDIO_RR("mvp->report_header_overhead_in_audio_rr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;reset_conf_mix_buf_on_downlink_only}.
     */
    MVP_RESET_CONF_MIX_BUF_ON_DOWNLINK_ONLY("mvp->reset_conf_mix_buf_on_downlink_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;reset_hbh_nack_check_init}.
     */
    MVP_RESET_HBH_NACK_CHECK_INIT("mvp->reset_hbh_nack_check_init", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;respect_initial_bitrate_estimate}.
     */
    MVP_RESPECT_INITIAL_BITRATE_ESTIMATE("mvp->respect_initial_bitrate_estimate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp_ignore_time_ms_after_res}.
     */
    MVP_RTCP_IGNORE_TIME_MS_AFTER_RES("mvp->rtcp_ignore_time_ms_after_res", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.cur_rx_bitrate_calc_win_sz}.
     */
    MVP_RTCP_CUR_RX_BITRATE_CALC_WIN_SZ("mvp->rtcp.cur_rx_bitrate_calc_win_sz", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.fix_audio_low_data_mode_receiver_device}.
     */
    MVP_RTCP_FIX_AUDIO_LOW_DATA_MODE_RECEIVER_DEVICE("mvp->rtcp.fix_audio_low_data_mode_receiver_device", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.fix_rx_remb_rst}.
     */
    MVP_RTCP_FIX_RX_REMB_RST("mvp->rtcp.fix_rx_remb_rst", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.init_stats_window_ms}.
     */
    MVP_RTCP_INIT_STATS_WINDOW_MS("mvp->rtcp.init_stats_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.loss_period_slide_window_size}.
     */
    MVP_RTCP_LOSS_PERIOD_SLIDE_WINDOW_SIZE("mvp->rtcp.loss_period_slide_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.plr_ema_alpha}.
     */
    MVP_RTCP_PLR_EMA_ALPHA("mvp->rtcp.plr_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.plr_max_disorder_dist}.
     */
    MVP_RTCP_PLR_MAX_DISORDER_DIST("mvp->rtcp.plr_max_disorder_dist", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.plr_min_disorder_dist}.
     */
    MVP_RTCP_PLR_MIN_DISORDER_DIST("mvp->rtcp.plr_min_disorder_dist", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.report_raw_rtt}.
     */
    MVP_RTCP_REPORT_RAW_RTT("mvp->rtcp.report_raw_rtt", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.rtt_ema_num_samples}.
     */
    MVP_RTCP_RTT_EMA_NUM_SAMPLES("mvp->rtcp.rtt_ema_num_samples", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.rtt_min_ema_alpha}.
     */
    MVP_RTCP_RTT_MIN_EMA_ALPHA("mvp->rtcp.rtt_min_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.rx_bitrate_ema_alpha}.
     */
    MVP_RTCP_RX_BITRATE_EMA_ALPHA("mvp->rtcp.rx_bitrate_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.use_new_cur_bitrate}.
     */
    MVP_RTCP_USE_NEW_CUR_BITRATE("mvp->rtcp.use_new_cur_bitrate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtcp.use_tp_fb_plr}.
     */
    MVP_RTCP_USE_TP_FB_PLR("mvp->rtcp.use_tp_fb_plr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtp_incoming_secure_buf}.
     */
    MVP_RTP_INCOMING_SECURE_BUF("mvp->rtp_incoming_secure_buf", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rtp_zero_ext_len_fix_enabled}.
     */
    MVP_RTP_ZERO_EXT_LEN_FIX_ENABLED("mvp->rtp_zero_ext_len_fix_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;run_rate_control_with_transport_feedback}.
     */
    MVP_RUN_RATE_CONTROL_WITH_TRANSPORT_FEEDBACK("mvp->run_rate_control_with_transport_feedback", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;rx_sub_vid_stream_resume_fix}.
     */
    MVP_RX_SUB_VID_STREAM_RESUME_FIX("mvp->rx_sub_vid_stream_resume_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;send_rtp_hdr_ext_stream_subscription_timeout_ms}.
     */
    MVP_SEND_RTP_HDR_EXT_STREAM_SUBSCRIPTION_TIMEOUT_MS("mvp->send_rtp_hdr_ext_stream_subscription_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.high_bw_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_HIGH_BW_THRESH("mvp->server_fec_plr_thresh.high_bw_thresh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.high_plr_pct_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_HIGH_PLR_PCT_THRESH("mvp->server_fec_plr_thresh.high_plr_pct_thresh", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.high_plr_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_HIGH_PLR_THRESH("mvp->server_fec_plr_thresh.high_plr_thresh", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.low_bw_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_LOW_BW_THRESH("mvp->server_fec_plr_thresh.low_bw_thresh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.low_plr_pct_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_LOW_PLR_PCT_THRESH("mvp->server_fec_plr_thresh.low_plr_pct_thresh", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.low_plr_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_LOW_PLR_THRESH("mvp->server_fec_plr_thresh.low_plr_thresh", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;server_fec_plr_thresh.majority_thresh}.
     */
    MVP_SERVER_FEC_PLR_THRESH_MAJORITY_THRESH("mvp->server_fec_plr_thresh.majority_thresh", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;set_rotation_in_capture_cb}.
     */
    MVP_SET_ROTATION_IN_CAPTURE_CB("mvp->set_rotation_in_capture_cb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;simulate_vpause}.
     */
    MVP_SIMULATE_VPAUSE("mvp->simulate_vpause", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;skip_pjmedia_vid_jb_for_webrtc}.
     */
    MVP_SKIP_PJMEDIA_VID_JB_FOR_WEBRTC("mvp->skip_pjmedia_vid_jb_for_webrtc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;skip_vid_jb_on_call_ending}.
     */
    MVP_SKIP_VID_JB_ON_CALL_ENDING("mvp->skip_vid_jb_on_call_ending", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_recv_init_bwe}.
     */
    MVP_SS_RECV_INIT_BWE("mvp->ss_recv_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_recv_init_bwe_cond_min_bwe}.
     */
    MVP_SS_RECV_INIT_BWE_COND_MIN_BWE("mvp->ss_recv_init_bwe_cond_min_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_recv_init_bwe_max_plr}.
     */
    MVP_SS_RECV_INIT_BWE_MAX_PLR("mvp->ss_recv_init_bwe_max_plr", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_sharer_init_bwe}.
     */
    MVP_SS_SHARER_INIT_BWE("mvp->ss_sharer_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_sharer_init_bwe_cond_min_bwe}.
     */
    MVP_SS_SHARER_INIT_BWE_COND_MIN_BWE("mvp->ss_sharer_init_bwe_cond_min_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_sharer_init_bwe_max_plr}.
     */
    MVP_SS_SHARER_INIT_BWE_MAX_PLR("mvp->ss_sharer_init_bwe_max_plr", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;ss_sharer_update_vid_scale_type}.
     */
    MVP_SS_SHARER_UPDATE_VID_SCALE_TYPE("mvp->ss_sharer_update_vid_scale_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;stft_metrics_interval_ms}.
     */
    MVP_STFT_METRICS_INTERVAL_MS("mvp->stft_metrics_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;stft_metrics_window_ms}.
     */
    MVP_STFT_METRICS_WINDOW_MS("mvp->stft_metrics_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;swap_dominant_speaker_freeze_v1_v2_reporting}.
     */
    MVP_SWAP_DOMINANT_SPEAKER_FREEZE_V1_V2_REPORTING("mvp->swap_dominant_speaker_freeze_v1_v2_reporting", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;swap_last_min_video_freeze_v1_v2_reporting}.
     */
    MVP_SWAP_LAST_MIN_VIDEO_FREEZE_V1_V2_REPORTING("mvp->swap_last_min_video_freeze_v1_v2_reporting", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;swap_video_init_freeze_v1_v2_reporting}.
     */
    MVP_SWAP_VIDEO_INIT_FREEZE_V1_V2_REPORTING("mvp->swap_video_init_freeze_v1_v2_reporting", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;swap_video_render_freeze_v1_v2_reporting}.
     */
    MVP_SWAP_VIDEO_RENDER_FREEZE_V1_V2_REPORTING("mvp->swap_video_render_freeze_v1_v2_reporting", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;transition_to_silence_thres_tx_ms}.
     */
    MVP_TRANSITION_TO_SILENCE_THRES_TX_MS("mvp->transition_to_silence_thres_tx_ms", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;transition_to_speech_thres_rx_ms}.
     */
    MVP_TRANSITION_TO_SPEECH_THRES_RX_MS("mvp->transition_to_speech_thres_rx_ms", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;transition_to_speech_thres_tx_ms}.
     */
    MVP_TRANSITION_TO_SPEECH_THRES_TX_MS("mvp->transition_to_speech_thres_tx_ms", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code mvp-&gt;tx_pl_perc_attack_ema_alpha}.
     */
    MVP_TX_PL_PERC_ATTACK_EMA_ALPHA("mvp->tx_pl_perc_attack_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;tx_pl_perc_release_ema_alpha}.
     */
    MVP_TX_PL_PERC_RELEASE_EMA_ALPHA("mvp->tx_pl_perc_release_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_atomic_audio_stream_pause}.
     */
    MVP_USE_ATOMIC_AUDIO_STREAM_PAUSE("mvp->use_atomic_audio_stream_pause", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_current_dec_active_time_for_res_switch_time}.
     */
    MVP_USE_CURRENT_DEC_ACTIVE_TIME_FOR_RES_SWITCH_TIME("mvp->use_current_dec_active_time_for_res_switch_time", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_ema_plr}.
     */
    MVP_USE_EMA_PLR("mvp->use_ema_plr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_ema_plr_for_rc_dyn}.
     */
    MVP_USE_EMA_PLR_FOR_RC_DYN("mvp->use_ema_plr_for_rc_dyn", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_freeze_disable_reasons}.
     */
    MVP_USE_FREEZE_DISABLE_REASONS("mvp->use_freeze_disable_reasons", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_hbh_rtt_for_audio_nacks}.
     */
    MVP_USE_HBH_RTT_FOR_AUDIO_NACKS("mvp->use_hbh_rtt_for_audio_nacks", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_hbh_rtt_for_video_nacks}.
     */
    MVP_USE_HBH_RTT_FOR_VIDEO_NACKS("mvp->use_hbh_rtt_for_video_nacks", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_maps_audio_processing}.
     */
    MVP_USE_MAPS_AUDIO_PROCESSING("mvp->use_maps_audio_processing", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_relay_rtt_as_init_rtt}.
     */
    MVP_USE_RELAY_RTT_AS_INIT_RTT("mvp->use_relay_rtt_as_init_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_total_dec_active_time_for_res_switch_time}.
     */
    MVP_USE_TOTAL_DEC_ACTIVE_TIME_FOR_RES_SWITCH_TIME("mvp->use_total_dec_active_time_for_res_switch_time", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_transport_rx_ts_for_reconnecting_ui_and_timeout}.
     */
    MVP_USE_TRANSPORT_RX_TS_FOR_RECONNECTING_UI_AND_TIMEOUT("mvp->use_transport_rx_ts_for_reconnecting_ui_and_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;use_vid_low_q}.
     */
    MVP_USE_VID_LOW_Q("mvp->use_vid_low_q", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;vid_pkt_reorder_pct}.
     */
    MVP_VID_PKT_REORDER_PCT("mvp->vid_pkt_reorder_pct", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;vid_port_renderer_buffer_count}.
     */
    MVP_VID_PORT_RENDERER_BUFFER_COUNT("mvp->vid_port_renderer_buffer_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;vid_stream_nack_throttle}.
     */
    MVP_VID_STREAM_NACK_THROTTLE("mvp->vid_stream_nack_throttle", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.audio_video_clock_src_sync}.
     */
    MVP_VIDEO_AUDIO_VIDEO_CLOCK_SRC_SYNC("mvp->video.audio_video_clock_src_sync", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.av_drift_at_render}.
     */
    MVP_VIDEO_AV_DRIFT_AT_RENDER("mvp->video.av_drift_at_render", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.av_sync_dtx_offset_ms}.
     */
    MVP_VIDEO_AV_SYNC_DTX_OFFSET_MS("mvp->video.av_sync_dtx_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.av_sync_threshold_ms}.
     */
    MVP_VIDEO_AV_SYNC_THRESHOLD_MS("mvp->video.av_sync_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_enhancement_bright_threshold}.
     */
    MVP_VIDEO_BRIGHTNESS_ENHANCEMENT_BRIGHT_THRESHOLD("mvp->video.brightness_enhancement_bright_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_enhancement_calculate_decframe_luminance}.
     */
    MVP_VIDEO_BRIGHTNESS_ENHANCEMENT_CALCULATE_DECFRAME_LUMINANCE("mvp->video.brightness_enhancement_calculate_decframe_luminance", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_enhancement_calculate_enhanced_luminance}.
     */
    MVP_VIDEO_BRIGHTNESS_ENHANCEMENT_CALCULATE_ENHANCED_LUMINANCE("mvp->video.brightness_enhancement_calculate_enhanced_luminance", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_enhancement_consecutive_frame_threshold}.
     */
    MVP_VIDEO_BRIGHTNESS_ENHANCEMENT_CONSECUTIVE_FRAME_THRESHOLD("mvp->video.brightness_enhancement_consecutive_frame_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_enhancement_dark_threshold}.
     */
    MVP_VIDEO_BRIGHTNESS_ENHANCEMENT_DARK_THRESHOLD("mvp->video.brightness_enhancement_dark_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.brightness_setting}.
     */
    MVP_VIDEO_BRIGHTNESS_SETTING("mvp->video.brightness_setting", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.codec_priority}.
     */
    MVP_VIDEO_CODEC_PRIORITY("mvp->video.codec_priority", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.composite_brightness_interval}.
     */
    MVP_VIDEO_COMPOSITE_BRIGHTNESS_INTERVAL("mvp->video.composite_brightness_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.composite_brightness_overexposure_threshold}.
     */
    MVP_VIDEO_COMPOSITE_BRIGHTNESS_OVEREXPOSURE_THRESHOLD("mvp->video.composite_brightness_overexposure_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.composite_brightness_pixel_step}.
     */
    MVP_VIDEO_COMPOSITE_BRIGHTNESS_PIXEL_STEP("mvp->video.composite_brightness_pixel_step", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.contrast_setting}.
     */
    MVP_VIDEO_CONTRAST_SETTING("mvp->video.contrast_setting", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.eager_video_preview_on_outgoing_call}.
     */
    MVP_VIDEO_EAGER_VIDEO_PREVIEW_ON_OUTGOING_CALL("mvp->video.eager_video_preview_on_outgoing_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.edge_sharpening_high_threshold}.
     */
    MVP_VIDEO_EDGE_SHARPENING_HIGH_THRESHOLD("mvp->video.edge_sharpening_high_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.edge_sharpening_low_threshold}.
     */
    MVP_VIDEO_EDGE_SHARPENING_LOW_THRESHOLD("mvp->video.edge_sharpening_low_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.edge_sharpening_pixel_step}.
     */
    MVP_VIDEO_EDGE_SHARPENING_PIXEL_STEP("mvp->video.edge_sharpening_pixel_step", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.lanczos_filter_setting}.
     */
    MVP_VIDEO_LANCZOS_FILTER_SETTING("mvp->video.lanczos_filter_setting", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.lock_video_orientation}.
     */
    MVP_VIDEO_LOCK_VIDEO_ORIENTATION("mvp->video.lock_video_orientation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.low_battery_notify_threshold_pct}.
     */
    MVP_VIDEO_LOW_BATTERY_NOTIFY_THRESHOLD_PCT("mvp->video.low_battery_notify_threshold_pct", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.max_capture_fps}.
     */
    MVP_VIDEO_MAX_CAPTURE_FPS("mvp->video.max_capture_fps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.min_capture_fps}.
     */
    MVP_VIDEO_MIN_CAPTURE_FPS("mvp->video.min_capture_fps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.saturation_setting}.
     */
    MVP_VIDEO_SATURATION_SETTING("mvp->video.saturation_setting", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.setup_video_stream_before_accept}.
     */
    MVP_VIDEO_SETUP_VIDEO_STREAM_BEFORE_ACCEPT("mvp->video.setup_video_stream_before_accept", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;video.sharpening_setting}.
     */
    MVP_VIDEO_SHARPENING_SETTING("mvp->video.sharpening_setting", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;wa_log_time_series}.
     */
    MVP_WA_LOG_TIME_SERIES("mvp->wa_log_time_series", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;wa_plr_ema_impl_types}.
     */
    MVP_WA_PLR_EMA_IMPL_TYPES("mvp->wa_plr_ema_impl_types", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;wa_zero_rate_sig}.
     */
    MVP_WA_ZERO_RATE_SIG("mvp->wa_zero_rate_sig", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;webrtc_jb_av_sync_interval_ms}.
     */
    MVP_WEBRTC_JB_AV_SYNC_INTERVAL_MS("mvp->webrtc_jb_av_sync_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;webrtc_jb_enable_av_sync}.
     */
    MVP_WEBRTC_JB_ENABLE_AV_SYNC("mvp->webrtc_jb_enable_av_sync", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;webrtc_jb_max_audio_sync_delay_ms}.
     */
    MVP_WEBRTC_JB_MAX_AUDIO_SYNC_DELAY_MS("mvp->webrtc_jb_max_audio_sync_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code mvp-&gt;webrtc_jb_max_relative_delay_ms}.
     */
    MVP_WEBRTC_JB_MAX_RELATIVE_DELAY_MS("mvp->webrtc_jb_max_relative_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;abtest_bucket}.
     */
    VP_ABTEST_BUCKET("vp->abtest_bucket", VoipParamType.STRING, 128, false),

    /**
     * Native descriptor for {@code vp-&gt;abtest_bucket_id_list}.
     */
    VP_ABTEST_BUCKET_ID_LIST("vp->abtest_bucket_id_list", VoipParamType.STRING, 256, false),

    /**
     * Native descriptor for {@code vp-&gt;allow_hosted_jid_match_regular_device_jid}.
     */
    VP_ALLOW_HOSTED_JID_MATCH_REGULAR_DEVICE_JID("vp->allow_hosted_jid_match_regular_device_jid", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;aud_reserve_on_mute_state_change}.
     */
    VP_AUD_RESERVE_ON_MUTE_STATE_CHANGE("vp->aud_reserve_on_mute_state_change", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;audio_callback_threshold}.
     */
    VP_AUDIO_CALLBACK_THRESHOLD("vp->audio_callback_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;audio_fps_max_threshold}.
     */
    VP_AUDIO_FPS_MAX_THRESHOLD("vp->audio_fps_max_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;audio_fps_min_threshold}.
     */
    VP_AUDIO_FPS_MIN_THRESHOLD("vp->audio_fps_min_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;audio_restart_before_fallback_count}.
     */
    VP_AUDIO_RESTART_BEFORE_FALLBACK_COUNT("vp->audio_restart_before_fallback_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;battery_low_threshold_pct}.
     */
    VP_BATTERY_LOW_THRESHOLD_PCT("vp->battery_low_threshold_pct", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;broadcast_interruption_state_on_stream_failure}.
     */
    VP_BROADCAST_INTERRUPTION_STATE_ON_STREAM_FAILURE("vp->broadcast_interruption_state_on_stream_failure", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;call_reaction_clear_interval_ms}.
     */
    VP_CALL_REACTION_CLEAR_INTERVAL_MS("vp->call_reaction_clear_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;call_reaction_clear_timer_frequency_ms}.
     */
    VP_CALL_REACTION_CLEAR_TIMER_FREQUENCY_MS("vp->call_reaction_clear_timer_frequency_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;call_reactions_retransmission_timeout_ms}.
     */
    VP_CALL_REACTIONS_RETRANSMISSION_TIMEOUT_MS("vp->call_reactions_retransmission_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;call_replayer_max_num_sources}.
     */
    VP_CALL_REPLAYER_MAX_NUM_SOURCES("vp->call_replayer_max_num_sources", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;call_replayer_use_v2}.
     */
    VP_CALL_REPLAYER_USE_V2("vp->call_replayer_use_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;caller_lonely_state_timeout_sec}.
     */
    VP_CALLER_LONELY_STATE_TIMEOUT_SEC("vp->caller_lonely_state_timeout_sec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;caller_timeout}.
     */
    VP_CALLER_TIMEOUT("vp->caller_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;connected_lonely_state_timer_intervals_ms}.
     */
    VP_CONNECTED_LONELY_STATE_TIMER_INTERVALS_MS("vp->connected_lonely_state_timer_intervals_ms", VoipParamType.ARRAY, 16, false),

    /**
     * Native descriptor for {@code vp-&gt;cpu_utilization_conf.cpu_over_utilization_threshold_in_pct}.
     */
    VP_CPU_UTILIZATION_CONF_CPU_OVER_UTILIZATION_THRESHOLD_IN_PCT("vp->cpu_utilization_conf.cpu_over_utilization_threshold_in_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;cpu_utilization_conf.cpu_sampling_duration_in_ms}.
     */
    VP_CPU_UTILIZATION_CONF_CPU_SAMPLING_DURATION_IN_MS("vp->cpu_utilization_conf.cpu_sampling_duration_in_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;cpu_utilization_conf.cpu_util_enable}.
     */
    VP_CPU_UTILIZATION_CONF_CPU_UTIL_ENABLE("vp->cpu_utilization_conf.cpu_util_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.audio_driver_dsp}.
     */
    VP_CR_EVENT_GATING_AUDIO_DRIVER_DSP("vp->cr_event_gating.audio_driver_dsp", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.audio_driver_dsp_1}.
     */
    VP_CR_EVENT_GATING_AUDIO_DRIVER_DSP_1("vp->cr_event_gating.audio_driver_dsp_1", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.audio_rate_control}.
     */
    VP_CR_EVENT_GATING_AUDIO_RATE_CONTROL("vp->cr_event_gating.audio_rate_control", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.audio_stream}.
     */
    VP_CR_EVENT_GATING_AUDIO_STREAM("vp->cr_event_gating.audio_stream", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.history}.
     */
    VP_CR_EVENT_GATING_HISTORY("vp->cr_event_gating.history", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.history_1}.
     */
    VP_CR_EVENT_GATING_HISTORY_1("vp->cr_event_gating.history_1", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.mcs_sfu}.
     */
    VP_CR_EVENT_GATING_MCS_SFU("vp->cr_event_gating.mcs_sfu", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.mcs_sfu_1}.
     */
    VP_CR_EVENT_GATING_MCS_SFU_1("vp->cr_event_gating.mcs_sfu_1", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.misc}.
     */
    VP_CR_EVENT_GATING_MISC("vp->cr_event_gating.misc", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.ml_feature}.
     */
    VP_CR_EVENT_GATING_ML_FEATURE("vp->cr_event_gating.ml_feature", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.ml_feature_1}.
     */
    VP_CR_EVENT_GATING_ML_FEATURE_1("vp->cr_event_gating.ml_feature_1", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.network_conditioner}.
     */
    VP_CR_EVENT_GATING_NETWORK_CONDITIONER("vp->cr_event_gating.network_conditioner", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.platform}.
     */
    VP_CR_EVENT_GATING_PLATFORM("vp->cr_event_gating.platform", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.relay}.
     */
    VP_CR_EVENT_GATING_RELAY("vp->cr_event_gating.relay", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.sender_bwe}.
     */
    VP_CR_EVENT_GATING_SENDER_BWE("vp->cr_event_gating.sender_bwe", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.signaling}.
     */
    VP_CR_EVENT_GATING_SIGNALING("vp->cr_event_gating.signaling", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.transport_network}.
     */
    VP_CR_EVENT_GATING_TRANSPORT_NETWORK("vp->cr_event_gating.transport_network", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.transport_network_1}.
     */
    VP_CR_EVENT_GATING_TRANSPORT_NETWORK_1("vp->cr_event_gating.transport_network_1", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.uaqc}.
     */
    VP_CR_EVENT_GATING_UAQC("vp->cr_event_gating.uaqc", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.video_decoding}.
     */
    VP_CR_EVENT_GATING_VIDEO_DECODING("vp->cr_event_gating.video_decoding", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.video_encoding}.
     */
    VP_CR_EVENT_GATING_VIDEO_ENCODING("vp->cr_event_gating.video_encoding", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;cr_event_gating.video_rate_control}.
     */
    VP_CR_EVENT_GATING_VIDEO_RATE_CONTROL("vp->cr_event_gating.video_rate_control", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_method[0]}.
     */
    VP_DEBUG_METRIC_METHOD_0("vp->debug_metric_method[0]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_method[1]}.
     */
    VP_DEBUG_METRIC_METHOD_1("vp->debug_metric_method[1]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_method[2]}.
     */
    VP_DEBUG_METRIC_METHOD_2("vp->debug_metric_method[2]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_method[3]}.
     */
    VP_DEBUG_METRIC_METHOD_3("vp->debug_metric_method[3]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_method[4]}.
     */
    VP_DEBUG_METRIC_METHOD_4("vp->debug_metric_method[4]", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_pcent[0]}.
     */
    VP_DEBUG_METRIC_PCENT_0("vp->debug_metric_pcent[0]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_pcent[1]}.
     */
    VP_DEBUG_METRIC_PCENT_1("vp->debug_metric_pcent[1]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_pcent[2]}.
     */
    VP_DEBUG_METRIC_PCENT_2("vp->debug_metric_pcent[2]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_pcent[3]}.
     */
    VP_DEBUG_METRIC_PCENT_3("vp->debug_metric_pcent[3]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric_pcent[4]}.
     */
    VP_DEBUG_METRIC_PCENT_4("vp->debug_metric_pcent[4]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric[0]}.
     */
    VP_DEBUG_METRIC_0("vp->debug_metric[0]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric[1]}.
     */
    VP_DEBUG_METRIC_1("vp->debug_metric[1]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric[2]}.
     */
    VP_DEBUG_METRIC_2("vp->debug_metric[2]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric[3]}.
     */
    VP_DEBUG_METRIC_3("vp->debug_metric[3]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;debug_metric[4]}.
     */
    VP_DEBUG_METRIC_4("vp->debug_metric[4]", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;disable_all_ltrp_event_verbose}.
     */
    VP_DISABLE_ALL_LTRP_EVENT_VERBOSE("vp->disable_all_ltrp_event_verbose", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;disable_late_rtcp_bye_on_teardown}.
     */
    VP_DISABLE_LATE_RTCP_BYE_ON_TEARDOWN("vp->disable_late_rtcp_bye_on_teardown", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;disable_reconnect_tone}.
     */
    VP_DISABLE_RECONNECT_TONE("vp->disable_reconnect_tone", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;dl_intest_model_after_call}.
     */
    VP_DL_INTEST_MODEL_AFTER_CALL("vp->dl_intest_model_after_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;dual_call_trigger}.
     */
    VP_DUAL_CALL_TRIGGER("vp->dual_call_trigger", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;eligible_bucket_id_list}.
     */
    VP_ELIGIBLE_BUCKET_ID_LIST("vp->eligible_bucket_id_list", VoipParamType.STRING, 1024, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_advanced_group_call_key_exchange}.
     */
    VP_ENABLE_ADVANCED_GROUP_CALL_KEY_EXCHANGE("vp->enable_advanced_group_call_key_exchange", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_android_high_res_capture}.
     */
    VP_ENABLE_ANDROID_HIGH_RES_CAPTURE("vp->enable_android_high_res_capture", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_app_data_controller}.
     */
    VP_ENABLE_APP_DATA_CONTROLLER("vp->enable_app_data_controller", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_app_data_stream_performance_test}.
     */
    VP_ENABLE_APP_DATA_STREAM_PERFORMANCE_TEST("vp->enable_app_data_stream_performance_test", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_apply_network_info_to_secondary_vid_stream}.
     */
    VP_ENABLE_APPLY_NETWORK_INFO_TO_SECONDARY_VID_STREAM("vp->enable_apply_network_info_to_secondary_vid_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_batch_drain_network_events}.
     */
    VP_ENABLE_BATCH_DRAIN_NETWORK_EVENTS("vp->enable_batch_drain_network_events", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_capabilities_ownership_fix}.
     */
    VP_ENABLE_CAPABILITIES_OWNERSHIP_FIX("vp->enable_capabilities_ownership_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_client_signaling_ts_logger}.
     */
    VP_ENABLE_CLIENT_SIGNALING_TS_LOGGER("vp->enable_client_signaling_ts_logger", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_cng}.
     */
    VP_ENABLE_CNG("vp->enable_cng", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_early_call_state_transition_on_end}.
     */
    VP_ENABLE_EARLY_CALL_STATE_TRANSITION_ON_END("vp->enable_early_call_state_transition_on_end", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_early_return_on_start_stream_error}.
     */
    VP_ENABLE_EARLY_RETURN_ON_START_STREAM_ERROR("vp->enable_early_return_on_start_stream_error", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_edgeray_dtls_active_mode}.
     */
    VP_ENABLE_EDGERAY_DTLS_ACTIVE_MODE("vp->enable_edgeray_dtls_active_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_encode_preset_latency_report}.
     */
    VP_ENABLE_ENCODE_PRESET_LATENCY_REPORT("vp->enable_encode_preset_latency_report", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_group_call}.
     */
    VP_ENABLE_GROUP_CALL("vp->enable_group_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_handle_relays_for_av_upgrade}.
     */
    VP_ENABLE_HANDLE_RELAYS_FOR_AV_UPGRADE("vp->enable_handle_relays_for_av_upgrade", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_hbh_srtcp_reset_on_hbh_key_change}.
     */
    VP_ENABLE_HBH_SRTCP_RESET_ON_HBH_KEY_CHANGE("vp->enable_hbh_srtcp_reset_on_hbh_key_change", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_hbh_srtp_reset_on_hbh_key_change}.
     */
    VP_ENABLE_HBH_SRTP_RESET_ON_HBH_KEY_CHANGE("vp->enable_hbh_srtp_reset_on_hbh_key_change", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_history_based_audio_device_preference}.
     */
    VP_ENABLE_HISTORY_BASED_AUDIO_DEVICE_PREFERENCE("vp->enable_history_based_audio_device_preference", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_hosted_jid_ssrc_calc}.
     */
    VP_ENABLE_HOSTED_JID_SSRC_CALC("vp->enable_hosted_jid_ssrc_calc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_ipv6_loopback_fix}.
     */
    VP_ENABLE_IPV6_LOOPBACK_FIX("vp->enable_ipv6_loopback_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_network_health_status_bc}.
     */
    VP_ENABLE_NETWORK_HEALTH_STATUS_BC("vp->enable_network_health_status_bc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_network_medium_bc}.
     */
    VP_ENABLE_NETWORK_MEDIUM_BC("vp->enable_network_medium_bc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_non_dyn_codec_param_fix}.
     */
    VP_ENABLE_NON_DYN_CODEC_PARAM_FIX("vp->enable_non_dyn_codec_param_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_peer_abtest_direct_ptr}.
     */
    VP_ENABLE_PEER_ABTEST_DIRECT_PTR("vp->enable_peer_abtest_direct_ptr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_pending_call}.
     */
    VP_ENABLE_PENDING_CALL("vp->enable_pending_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_periodical_aud_rr_processing}.
     */
    VP_ENABLE_PERIODICAL_AUD_RR_PROCESSING("vp->enable_periodical_aud_rr_processing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_pid_resubscription_fix}.
     */
    VP_ENABLE_PID_RESUBSCRIPTION_FIX("vp->enable_pid_resubscription_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_pip_failure_video_resume}.
     */
    VP_ENABLE_PIP_FAILURE_VIDEO_RESUME("vp->enable_pip_failure_video_resume", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_schedule_timer_in_app_data_controller_create}.
     */
    VP_ENABLE_SCHEDULE_TIMER_IN_APP_DATA_CONTROLLER_CREATE("vp->enable_schedule_timer_in_app_data_controller_create", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_software_mute_during_call_hold}.
     */
    VP_ENABLE_SOFTWARE_MUTE_DURING_CALL_HOLD("vp->enable_software_mute_during_call_hold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_speaker_ranking}.
     */
    VP_ENABLE_SPEAKER_RANKING("vp->enable_speaker_ranking", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_stale_acceptsent_supersede}.
     */
    VP_ENABLE_STALE_ACCEPTSENT_SUPERSEDE("vp->enable_stale_acceptsent_supersede", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_tee_mv1_mv2_data_channel_compatibility_mode}.
     */
    VP_ENABLE_TEE_MV1_MV2_DATA_CHANNEL_COMPATIBILITY_MODE("vp->enable_tee_mv1_mv2_data_channel_compatibility_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_thread_safe_sockaddr}.
     */
    VP_ENABLE_THREAD_SAFE_SOCKADDR("vp->enable_thread_safe_sockaddr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_unified_connected_lonely_state_timer}.
     */
    VP_ENABLE_UNIFIED_CONNECTED_LONELY_STATE_TIMER("vp->enable_unified_connected_lonely_state_timer", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_upd_codec_param_fix}.
     */
    VP_ENABLE_UPD_CODEC_PARAM_FIX("vp->enable_upd_codec_param_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_upd_strm_param_fix}.
     */
    VP_ENABLE_UPD_STRM_PARAM_FIX("vp->enable_upd_strm_param_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_vid_one_way_codec_dyn_rule}.
     */
    VP_ENABLE_VID_ONE_WAY_CODEC_DYN_RULE("vp->enable_vid_one_way_codec_dyn_rule", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_video_enabled_in_peer_row}.
     */
    VP_ENABLE_VIDEO_ENABLED_IN_PEER_ROW("vp->enable_video_enabled_in_peer_row", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_video_resume_after_hold_fix}.
     */
    VP_ENABLE_VIDEO_RESUME_AFTER_HOLD_FIX("vp->enable_video_resume_after_hold_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_voip_err_detector}.
     */
    VP_ENABLE_VOIP_ERR_DETECTOR("vp->enable_voip_err_detector", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_voip_err_detector_assert_debug_info}.
     */
    VP_ENABLE_VOIP_ERR_DETECTOR_ASSERT_DEBUG_INFO("vp->enable_voip_err_detector_assert_debug_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_voip_err_detector_for_all}.
     */
    VP_ENABLE_VOIP_ERR_DETECTOR_FOR_ALL("vp->enable_voip_err_detector_for_all", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_voip_err_detector_report_loc}.
     */
    VP_ENABLE_VOIP_ERR_DETECTOR_REPORT_LOC("vp->enable_voip_err_detector_report_loc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_vqm}.
     */
    VP_ENABLE_VQM("vp->enable_vqm", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;enable_webrtc_compatibility}.
     */
    VP_ENABLE_WEBRTC_COMPATIBILITY("vp->enable_webrtc_compatibility", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;exclude_lobby_from_link_callee_setup_t}.
     */
    VP_EXCLUDE_LOBBY_FROM_LINK_CALLEE_SETUP_T("vp->exclude_lobby_from_link_callee_setup_t", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;exclude_offer_processing_from_setup_time_when_not_critical}.
     */
    VP_EXCLUDE_OFFER_PROCESSING_FROM_SETUP_TIME_WHEN_NOT_CRITICAL("vp->exclude_offer_processing_from_setup_time_when_not_critical", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;exclude_peer_setup_error_from_connected}.
     */
    VP_EXCLUDE_PEER_SETUP_ERROR_FROM_CONNECTED("vp->exclude_peer_setup_error_from_connected", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fc_proto}.
     */
    VP_FC_PROTO("vp->fc_proto", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fix_reconnecting_state_count}.
     */
    VP_FIX_RECONNECTING_STATE_COUNT("vp->fix_reconnecting_state_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fix_reset_accumulated_stats}.
     */
    VP_FIX_RESET_ACCUMULATED_STATS("vp->fix_reset_accumulated_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fix_speaker_ranking_corrupted_stream_crash}.
     */
    VP_FIX_SPEAKER_RANKING_CORRUPTED_STREAM_CRASH("vp->fix_speaker_ranking_corrupted_stream_crash", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;force_rtp_ext_prof}.
     */
    VP_FORCE_RTP_EXT_PROF("vp->force_rtp_ext_prof", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fs_config.allowed_column_ids_csv}.
     */
    VP_FS_CONFIG_ALLOWED_COLUMN_IDS_CSV("vp->fs_config.allowed_column_ids_csv", VoipParamType.STRING, 512, false),

    /**
     * Native descriptor for {@code vp-&gt;fs_config.log_max_entries}.
     */
    VP_FS_CONFIG_LOG_MAX_ENTRIES("vp->fs_config.log_max_entries", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;fs_config.min_connected_participants}.
     */
    VP_FS_CONFIG_MIN_CONNECTED_PARTICIPANTS("vp->fs_config.min_connected_participants", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fs_config.peer_row_sampling_rate}.
     */
    VP_FS_CONFIG_PEER_ROW_SAMPLING_RATE("vp->fs_config.peer_row_sampling_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;fs_config.self_row_sampling_rate}.
     */
    VP_FS_CONFIG_SELF_ROW_SAMPLING_RATE("vp->fs_config.self_row_sampling_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;get_detailed_afl_stats}.
     */
    VP_GET_DETAILED_AFL_STATS("vp->get_detailed_afl_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;get_pip_stats}.
     */
    VP_GET_PIP_STATS("vp->get_pip_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;history_based_audio_device_change_threshold}.
     */
    VP_HISTORY_BASED_AUDIO_DEVICE_CHANGE_THRESHOLD("vp->history_based_audio_device_change_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;initial_connecting_sound_delay}.
     */
    VP_INITIAL_CONNECTING_SOUND_DELAY("vp->initial_connecting_sound_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;joining_sound_gap_in_ms}.
     */
    VP_JOINING_SOUND_GAP_IN_MS("vp->joining_sound_gap_in_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;max_audio_driver_restarts}.
     */
    VP_MAX_AUDIO_DRIVER_RESTARTS("vp->max_audio_driver_restarts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.aud_nack_type}.
     */
    VP_MEDIA_AUD_NACK_TYPE("vp->media.aud_nack_type", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.audio_encode_offload}.
     */
    VP_MEDIA_AUDIO_ENCODE_OFFLOAD("vp->media.audio_encode_offload", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.audio_health_report_interval_in_sec}.
     */
    VP_MEDIA_AUDIO_HEALTH_REPORT_INTERVAL_IN_SEC("vp->media.audio_health_report_interval_in_sec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.capi_enable_sfu_on_av_switch}.
     */
    VP_MEDIA_CAPI_ENABLE_SFU_ON_AV_SWITCH("vp->media.capi_enable_sfu_on_av_switch", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.clock_callback_process_step}.
     */
    VP_MEDIA_CLOCK_CALLBACK_PROCESS_STEP("vp->media.clock_callback_process_step", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;media.clock_thread_audio_prio_bitmap}.
     */
    VP_MEDIA_CLOCK_THREAD_AUDIO_PRIO_BITMAP("vp->media.clock_thread_audio_prio_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.conf_mix_cnt_limt}.
     */
    VP_MEDIA_CONF_MIX_CNT_LIMT("vp->media.conf_mix_cnt_limt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.connecting_tone_desc}.
     */
    VP_MEDIA_CONNECTING_TONE_DESC("vp->media.connecting_tone_desc", VoipParamType.STRING, 1024, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_aud_rec_thread_high_pri}.
     */
    VP_MEDIA_ENABLE_AUD_REC_THREAD_HIGH_PRI("vp->media.enable_aud_rec_thread_high_pri", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_audio_record_cb_drain}.
     */
    VP_MEDIA_ENABLE_AUDIO_RECORD_CB_DRAIN("vp->media.enable_audio_record_cb_drain", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_capture_port_recreate_fix}.
     */
    VP_MEDIA_ENABLE_CAPTURE_PORT_RECREATE_FIX("vp->media.enable_capture_port_recreate_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_dual_stream_screen_share}.
     */
    VP_MEDIA_ENABLE_DUAL_STREAM_SCREEN_SHARE("vp->media.enable_dual_stream_screen_share", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_gate_video_before_destroy}.
     */
    VP_MEDIA_ENABLE_GATE_VIDEO_BEFORE_DESTROY("vp->media.enable_gate_video_before_destroy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_init_codecs_on_upgrade}.
     */
    VP_MEDIA_ENABLE_INIT_CODECS_ON_UPGRADE("vp->media.enable_init_codecs_on_upgrade", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_media_endpt_thread_high_pri}.
     */
    VP_MEDIA_ENABLE_MEDIA_ENDPT_THREAD_HIGH_PRI("vp->media.enable_media_endpt_thread_high_pri", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_media_endpt_thread_high_pri_android}.
     */
    VP_MEDIA_ENABLE_MEDIA_ENDPT_THREAD_HIGH_PRI_ANDROID("vp->media.enable_media_endpt_thread_high_pri_android", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_reuse_bwe_last_video_segment}.
     */
    VP_MEDIA_ENABLE_REUSE_BWE_LAST_VIDEO_SEGMENT("vp->media.enable_reuse_bwe_last_video_segment", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.enable_vid_rec_thread_high_pri}.
     */
    VP_MEDIA_ENABLE_VID_REC_THREAD_HIGH_PRI("vp->media.enable_vid_rec_thread_high_pri", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.fail_v3_screen_share_and_show_app_update_dialog}.
     */
    VP_MEDIA_FAIL_V3_SCREEN_SHARE_AND_SHOW_APP_UPDATE_DIALOG("vp->media.fail_v3_screen_share_and_show_app_update_dialog", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.fix_capture_fmt_id_after_format_copy}.
     */
    VP_MEDIA_FIX_CAPTURE_FMT_ID_AFTER_FORMAT_COPY("vp->media.fix_capture_fmt_id_after_format_copy", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.io_buffer_duration_in_ms}.
     */
    VP_MEDIA_IO_BUFFER_DURATION_IN_MS("vp->media.io_buffer_duration_in_ms", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;media.ip_config}.
     */
    VP_MEDIA_IP_CONFIG("vp->media.ip_config", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.jb.neteq_field_trial_params.codec_settings_fix}.
     */
    VP_MEDIA_JB_NETEQ_FIELD_TRIAL_PARAMS_CODEC_SETTINGS_FIX("vp->media.jb.neteq_field_trial_params.codec_settings_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.keep_driver_at_native}.
     */
    VP_MEDIA_KEEP_DRIVER_AT_NATIVE("vp->media.keep_driver_at_native", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.max_streams_to_mix_grp_call}.
     */
    VP_MEDIA_MAX_STREAMS_TO_MIX_GRP_CALL("vp->media.max_streams_to_mix_grp_call", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code vp-&gt;media.mix_stream_with_speech_status}.
     */
    VP_MEDIA_MIX_STREAM_WITH_SPEECH_STATUS("vp->media.mix_stream_with_speech_status", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.aud_restart_silence_det_freq_ms}.
     */
    VP_MEDIA_SILENCE_DETECTION_AUD_RESTART_SILENCE_DET_FREQ_MS("vp->media.silence_detection.aud_restart_silence_det_freq_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.aud_restart_silence_det_init_ms}.
     */
    VP_MEDIA_SILENCE_DETECTION_AUD_RESTART_SILENCE_DET_INIT_MS("vp->media.silence_detection.aud_restart_silence_det_init_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.disable_audio_restart_record_silence}.
     */
    VP_MEDIA_SILENCE_DETECTION_DISABLE_AUDIO_RESTART_RECORD_SILENCE("vp->media.silence_detection.disable_audio_restart_record_silence", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.disable_report_fatal_record_silence}.
     */
    VP_MEDIA_SILENCE_DETECTION_DISABLE_REPORT_FATAL_RECORD_SILENCE("vp->media.silence_detection.disable_report_fatal_record_silence", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.enable_silence_detection}.
     */
    VP_MEDIA_SILENCE_DETECTION_ENABLE_SILENCE_DETECTION("vp->media.silence_detection.enable_silence_detection", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.ignore_first_n_silence_frame}.
     */
    VP_MEDIA_SILENCE_DETECTION_IGNORE_FIRST_N_SILENCE_FRAME("vp->media.silence_detection.ignore_first_n_silence_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.silence_detection.max_aud_restarts_record_silence}.
     */
    VP_MEDIA_SILENCE_DETECTION_MAX_AUD_RESTARTS_RECORD_SILENCE("vp->media.silence_detection.max_aud_restarts_record_silence", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.tp.enable_ioqueue_reset_at_end_call}.
     */
    VP_MEDIA_TP_ENABLE_IOQUEUE_RESET_AT_END_CALL("vp->media.tp.enable_ioqueue_reset_at_end_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.tp.use_send_rx_sub_from_cache_api}.
     */
    VP_MEDIA_TP_USE_SEND_RX_SUB_FROM_CACHE_API("vp->media.tp.use_send_rx_sub_from_cache_api", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.update_status_from_send_self_video_state_enabled}.
     */
    VP_MEDIA_UPDATE_STATUS_FROM_SEND_SELF_VIDEO_STATE_ENABLED("vp->media.update_status_from_send_self_video_state_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.use_transport_feedback_for_stats_only}.
     */
    VP_MEDIA_USE_TRANSPORT_FEEDBACK_FOR_STATS_ONLY("vp->media.use_transport_feedback_for_stats_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.vid_dec_nalus}.
     */
    VP_MEDIA_VID_DEC_NALUS("vp->media.vid_dec_nalus", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.vid_stream_drop_pkts_when_paused}.
     */
    VP_MEDIA_VID_STREAM_DROP_PKTS_WHEN_PAUSED("vp->media.vid_stream_drop_pkts_when_paused", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;media.vid_stream_pause_resume_jb_reset_threshold_ms}.
     */
    VP_MEDIA_VID_STREAM_PAUSE_RESUME_JB_RESET_THRESHOLD_MS("vp->media.vid_stream_pause_resume_jb_reset_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;media.video_upgrade_requestee_tone_desc}.
     */
    VP_MEDIA_VIDEO_UPGRADE_REQUESTEE_TONE_DESC("vp->media.video_upgrade_requestee_tone_desc", VoipParamType.STRING, 1024, false),

    /**
     * Native descriptor for {@code vp-&gt;merge_vid_strm_port_op}.
     */
    VP_MERGE_VID_STRM_PORT_OP("vp->merge_vid_strm_port_op", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;min_batt_drop_to_update_stats}.
     */
    VP_MIN_BATT_DROP_TO_UPDATE_STATS("vp->min_batt_drop_to_update_stats", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;min_num_participants_to_enable_rx_sub}.
     */
    VP_MIN_NUM_PARTICIPANTS_TO_ENABLE_RX_SUB("vp->min_num_participants_to_enable_rx_sub", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;min_time_first_audio_restart_ms}.
     */
    VP_MIN_TIME_FIRST_AUDIO_RESTART_MS("vp->min_time_first_audio_restart_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;mute_sender_on_hold}.
     */
    VP_MUTE_SENDER_ON_HOLD("vp->mute_sender_on_hold", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;net_medium_bc_async}.
     */
    VP_NET_MEDIUM_BC_ASYNC("vp->net_medium_bc_async", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;nhi_battery_low_en}.
     */
    VP_NHI_BATTERY_LOW_EN("vp->nhi_battery_low_en", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;notify_aud_process_on_mute_state_change}.
     */
    VP_NOTIFY_AUD_PROCESS_ON_MUTE_STATE_CHANGE("vp->notify_aud_process_on_mute_state_change", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;null_call_info_pool_refs_before_release}.
     */
    VP_NULL_CALL_INFO_POOL_REFS_BEFORE_RELEASE("vp->null_call_info_pool_refs_before_release", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;oboe_native_frames_per_buffer_enabled}.
     */
    VP_OBOE_NATIVE_FRAMES_PER_BUFFER_ENABLED("vp->oboe_native_frames_per_buffer_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;ohai_request_timeout_ms}.
     */
    VP_OHAI_REQUEST_TIMEOUT_MS("vp->ohai_request_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;optimize_user_enable}.
     */
    VP_OPTIMIZE_USER_ENABLE("vp->optimize_user_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;precise_rx_timestamps_mask}.
     */
    VP_PRECISE_RX_TIMESTAMPS_MASK("vp->precise_rx_timestamps_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;rekey_fanout_format}.
     */
    VP_REKEY_FANOUT_FORMAT("vp->rekey_fanout_format", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;relative_speech_activity_thresholds}.
     */
    VP_RELATIVE_SPEECH_ACTIVITY_THRESHOLDS("vp->relative_speech_activity_thresholds", VoipParamType.ARRAY, 12, false),

    /**
     * Native descriptor for {@code vp-&gt;reset_vid_dis_part_left}.
     */
    VP_RESET_VID_DIS_PART_LEFT("vp->reset_vid_dis_part_left", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;restart_audio_on_white_noise}.
     */
    VP_RESTART_AUDIO_ON_WHITE_NOISE("vp->restart_audio_on_white_noise", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;resume_device_after_pstn_call}.
     */
    VP_RESUME_DEVICE_AFTER_PSTN_CALL("vp->resume_device_after_pstn_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;ringback_mode}.
     */
    VP_RINGBACK_MODE("vp->ringback_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;send_rtcp_bye_time_strategy}.
     */
    VP_SEND_RTCP_BYE_TIME_STRATEGY("vp->send_rtcp_bye_time_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;send_transport_feedback_interval_ms}.
     */
    VP_SEND_TRANSPORT_FEEDBACK_INTERVAL_MS("vp->send_transport_feedback_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;srtp_decryption_failure_timeout_ms}.
     */
    VP_SRTP_DECRYPTION_FAILURE_TIMEOUT_MS("vp->srtp_decryption_failure_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;stop_probing_gp_av_upgrade}.
     */
    VP_STOP_PROBING_GP_AV_UPGRADE("vp->stop_probing_gp_av_upgrade", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;sub_fail_start_stream_fix_enabled}.
     */
    VP_SUB_FAIL_START_STREAM_FIX_ENABLED("vp->sub_fail_start_stream_fix_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;thread_watchdog_interval_ms}.
     */
    VP_THREAD_WATCHDOG_INTERVAL_MS("vp->thread_watchdog_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;thread_watchdog_timeout_ms}.
     */
    VP_THREAD_WATCHDOG_TIMEOUT_MS("vp->thread_watchdog_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;use_modified_batt_drop_calcs}.
     */
    VP_USE_MODIFIED_BATT_DROP_CALCS("vp->use_modified_batt_drop_calcs", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code vp-&gt;use_webtc_neon_funcs}.
     */
    VP_USE_WEBTC_NEON_FUNCS("vp->use_webtc_neon_funcs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;video_state_txn_id_recv_enforce}.
     */
    VP_VIDEO_STATE_TXN_ID_RECV_ENFORCE("vp->video_state_txn_id_recv_enforce", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;video_state_txn_id_send_enabled}.
     */
    VP_VIDEO_STATE_TXN_ID_SEND_ENABLED("vp->video_state_txn_id_send_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;voip_setting_version.release_type}.
     */
    VP_VOIP_SETTING_VERSION_RELEASE_TYPE("vp->voip_setting_version.release_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code vp-&gt;voip_setting_version.version_number}.
     */
    VP_VOIP_SETTING_VERSION_VERSION_NUMBER("vp->voip_setting_version.version_number", VoipParamType.INTEGER, 4, false);

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
    VoipParamKeyMedia1(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {
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
