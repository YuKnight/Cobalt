package com.github.auties00.cobalt.calls2.common;

/**
 * A partition of the {@code p-&gt;} voip-param registry keys.
 *
 * <p>This enum exists only to keep its generated static initializer within the JVM 64KB
 * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}
 * rather than this partition directly.
 */
enum VoipParamKeyCall2 implements VoipParamKey {
    /**
     * Native descriptor for {@code p-&gt;enable_hist_based}.
     */
    P_ENABLE_HIST_BASED("p->enable_hist_based", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_historical_relay_verbose_logging}.
     */
    P_ENABLE_HISTORICAL_RELAY_VERBOSE_LOGGING("p->enable_historical_relay_verbose_logging", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_history_based_info}.
     */
    P_ENABLE_HISTORY_BASED_INFO("p->enable_history_based_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_history_based_info_fix}.
     */
    P_ENABLE_HISTORY_BASED_INFO_FIX("p->enable_history_based_info_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_history_based_min_rtt}.
     */
    P_ENABLE_HISTORY_BASED_MIN_RTT("p->enable_history_based_min_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_history_video_record_info}.
     */
    P_ENABLE_HISTORY_VIDEO_RECORD_INFO("p->enable_history_video_record_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_hostile_network_timeouts}.
     */
    P_ENABLE_HOSTILE_NETWORK_TIMEOUTS("p->enable_hostile_network_timeouts", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_init_fallback_based_on_dl_pp}.
     */
    P_ENABLE_INIT_FALLBACK_BASED_ON_DL_PP("p->enable_init_fallback_based_on_dl_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_init_fallback_based_on_pp}.
     */
    P_ENABLE_INIT_FALLBACK_BASED_ON_PP("p->enable_init_fallback_based_on_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_init_info}.
     */
    P_ENABLE_INIT_INFO("p->enable_init_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_init_info_recalculation_fix}.
     */
    P_ENABLE_INIT_INFO_RECALCULATION_FIX("p->enable_init_info_recalculation_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_init_pp_probing}.
     */
    P_ENABLE_INIT_PP_PROBING("p->enable_init_pp_probing", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_late_arriving_history_fix}.
     */
    P_ENABLE_LATE_ARRIVING_HISTORY_FIX("p->enable_late_arriving_history_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_libyuv_extra_asserts}.
     */
    P_ENABLE_LIBYUV_EXTRA_ASSERTS("p->enable_libyuv_extra_asserts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_limit_bw_with_ceiling}.
     */
    P_ENABLE_LIMIT_BW_WITH_CEILING("p->enable_limit_bw_with_ceiling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_lqm_check}.
     */
    P_ENABLE_LQM_CHECK("p->enable_lqm_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_max_bwe_fix}.
     */
    P_ENABLE_MAX_BWE_FIX("p->enable_max_bwe_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_mcp_stop_fix}.
     */
    P_ENABLE_MCP_STOP_FIX("p->enable_mcp_stop_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_mcs_fs_peer_device_jid_fix}.
     */
    P_ENABLE_MCS_FS_PEER_DEVICE_JID_FIX("p->enable_mcs_fs_peer_device_jid_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_media_codec_types}.
     */
    P_ENABLE_MEDIA_CODEC_TYPES("p->enable_media_codec_types", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ml_plc_for_audio}.
     */
    P_ENABLE_ML_PLC_FOR_AUDIO("p->enable_ml_plc_for_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ml_udst}.
     */
    P_ENABLE_ML_UDST("p->enable_ml_udst", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;enable_nadl_input_log}.
     */
    P_ENABLE_NADL_INPUT_LOG("p->enable_nadl_input_log", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_nadl_output_log}.
     */
    P_ENABLE_NADL_OUTPUT_LOG("p->enable_nadl_output_log", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_one_side_mode}.
     */
    P_ENABLE_ONE_SIDE_MODE("p->enable_one_side_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_peer_bwe_based_clamping}.
     */
    P_ENABLE_PEER_BWE_BASED_CLAMPING("p->enable_peer_bwe_based_clamping", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_peer_clamping_on_init_update}.
     */
    P_ENABLE_PEER_CLAMPING_ON_INIT_UPDATE("p->enable_peer_clamping_on_init_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_peer_clamping_on_init_update_v2}.
     */
    P_ENABLE_PEER_CLAMPING_ON_INIT_UPDATE_V2("p->enable_peer_clamping_on_init_update_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_peer_platform_fix}.
     */
    P_ENABLE_PEER_PLATFORM_FIX("p->enable_peer_platform_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_periodic_warp_roc_for_hbh}.
     */
    P_ENABLE_PERIODIC_WARP_ROC_FOR_HBH("p->enable_periodic_warp_roc_for_hbh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_preset_algo_fix}.
     */
    P_ENABLE_PRESET_ALGO_FIX("p->enable_preset_algo_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_psnr_resolution_ctrl}.
     */
    P_ENABLE_PSNR_RESOLUTION_CTRL("p->enable_psnr_resolution_ctrl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ptedge_lib_loading}.
     */
    P_ENABLE_PTEDGE_LIB_LOADING("p->enable_ptedge_lib_loading", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd}.
     */
    P_ENABLE_QUICKHD("p->enable_quickhd", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_instant_ramp_up}.
     */
    P_ENABLE_QUICKHD_INSTANT_RAMP_UP("p->enable_quickhd_instant_ramp_up", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_ml}.
     */
    P_ENABLE_QUICKHD_ML("p->enable_quickhd_ml", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_ml_bwe_override}.
     */
    P_ENABLE_QUICKHD_ML_BWE_OVERRIDE("p->enable_quickhd_ml_bwe_override", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_slc}.
     */
    P_ENABLE_QUICKHD_SLC("p->enable_quickhd_slc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_slr}.
     */
    P_ENABLE_QUICKHD_SLR("p->enable_quickhd_slr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_quickhd_slr_bwe_override}.
     */
    P_ENABLE_QUICKHD_SLR_BWE_OVERRIDE("p->enable_quickhd_slr_bwe_override", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_rbe}.
     */
    P_ENABLE_RBE("p->enable_rbe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_receive_peer_uaqc_state}.
     */
    P_ENABLE_RECEIVE_PEER_UAQC_STATE("p->enable_receive_peer_uaqc_state", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_receiver_side_automos}.
     */
    P_ENABLE_RECEIVER_SIDE_AUTOMOS("p->enable_receiver_side_automos", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_redial_after_cer_metric}.
     */
    P_ENABLE_REDIAL_AFTER_CER_METRIC("p->enable_redial_after_cer_metric", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_redial_metric}.
     */
    P_ENABLE_REDIAL_METRIC("p->enable_redial_metric", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_redial_metric_for_bwe}.
     */
    P_ENABLE_REDIAL_METRIC_FOR_BWE("p->enable_redial_metric_for_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_reuse_2p_bwe_for_sfu}.
     */
    P_ENABLE_REUSE_2P_BWE_FOR_SFU("p->enable_reuse_2p_bwe_for_sfu", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_rl_bwe}.
     */
    P_ENABLE_RL_BWE("p->enable_rl_bwe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_rs_fec_receiver_reset}.
     */
    P_ENABLE_RS_FEC_RECEIVER_RESET("p->enable_rs_fec_receiver_reset", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_rtt_min_ema}.
     */
    P_ENABLE_RTT_MIN_EMA("p->enable_rtt_min_ema", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_rx_lufs_metric}.
     */
    P_ENABLE_RX_LUFS_METRIC("p->enable_rx_lufs_metric", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_screen_content_detection}.
     */
    P_ENABLE_SCREEN_CONTENT_DETECTION("p->enable_screen_content_detection", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sctp_buffer_congestion_detection}.
     */
    P_ENABLE_SCTP_BUFFER_CONGESTION_DETECTION("p->enable_sctp_buffer_congestion_detection", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_self_ul_rbwe}.
     */
    P_ENABLE_SELF_UL_RBWE("p->enable_self_ul_rbwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_selfside_frame_rotation}.
     */
    P_ENABLE_SELFSIDE_FRAME_ROTATION("p->enable_selfside_frame_rotation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_send_uaqc_state_to_peer}.
     */
    P_ENABLE_SEND_UAQC_STATE_TO_PEER("p->enable_send_uaqc_state_to_peer", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sender_side_automos}.
     */
    P_ENABLE_SENDER_SIDE_AUTOMOS("p->enable_sender_side_automos", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sending_bwe_rst_when_fpp_changes}.
     */
    P_ENABLE_SENDING_BWE_RST_WHEN_FPP_CHANGES("p->enable_sending_bwe_rst_when_fpp_changes", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_separate_congestion_thresholds}.
     */
    P_ENABLE_SEPARATE_CONGESTION_THRESHOLDS("p->enable_separate_congestion_thresholds", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_seq_wraparound_fix}.
     */
    P_ENABLE_SEQ_WRAPAROUND_FIX("p->enable_seq_wraparound_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sframe}.
     */
    P_ENABLE_SFRAME("p->enable_sframe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sframe_rx}.
     */
    P_ENABLE_SFRAME_RX("p->enable_sframe_rx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_sframe_tx}.
     */
    P_ENABLE_SFRAME_TX("p->enable_sframe_tx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_share_mixer_pipeline_rate}.
     */
    P_ENABLE_SHARE_MIXER_PIPELINE_RATE("p->enable_share_mixer_pipeline_rate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_short_offset_build}.
     */
    P_ENABLE_SHORT_OFFSET_BUILD("p->enable_short_offset_build", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_simulcast_subscription_throttle_kf}.
     */
    P_ENABLE_SIMULCAST_SUBSCRIPTION_THROTTLE_KF("p->enable_simulcast_subscription_throttle_kf", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_slide_window_min_rtt}.
     */
    P_ENABLE_SLIDE_WINDOW_MIN_RTT("p->enable_slide_window_min_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_srtp_hbh_fec_rx}.
     */
    P_ENABLE_SRTP_HBH_FEC_RX("p->enable_srtp_hbh_fec_rx", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_srtp_hbh_srtp}.
     */
    P_ENABLE_SRTP_HBH_SRTP("p->enable_srtp_hbh_srtp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_srtp_hbh_srtp_set_index_bitmasks}.
     */
    P_ENABLE_SRTP_HBH_SRTP_SET_INDEX_BITMASKS("p->enable_srtp_hbh_srtp_set_index_bitmasks", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ss_enc_downscale_fix}.
     */
    P_ENABLE_SS_ENC_DOWNSCALE_FIX("p->enable_ss_enc_downscale_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_standalone_warp_pr}.
     */
    P_ENABLE_STANDALONE_WARP_PR("p->enable_standalone_warp_pr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_standalone_warp_pr_during_multipop}.
     */
    P_ENABLE_STANDALONE_WARP_PR_DURING_MULTIPOP("p->enable_standalone_warp_pr_during_multipop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_stop_mcp_without_fr}.
     */
    P_ENABLE_STOP_MCP_WITHOUT_FR("p->enable_stop_mcp_without_fr", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;enable_tcp_ping_bitmap}.
     */
    P_ENABLE_TCP_PING_BITMAP("p->enable_tcp_ping_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_tensor_mem_reallocation}.
     */
    P_ENABLE_TENSOR_MEM_REALLOCATION("p->enable_tensor_mem_reallocation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_time_info_in_sbwe}.
     */
    P_ENABLE_TIME_INFO_IN_SBWE("p->enable_time_info_in_sbwe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_transport_attach_fix}.
     */
    P_ENABLE_TRANSPORT_ATTACH_FIX("p->enable_transport_attach_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_transport_rtx_enc}.
     */
    P_ENABLE_TRANSPORT_RTX_ENC("p->enable_transport_rtx_enc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_transport_seq_num_ext}.
     */
    P_ENABLE_TRANSPORT_SEQ_NUM_EXT("p->enable_transport_seq_num_ext", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_tx_lufs_metric}.
     */
    P_ENABLE_TX_LUFS_METRIC("p->enable_tx_lufs_metric", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc}.
     */
    P_ENABLE_UAQC("p->enable_uaqc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_in_vid}.
     */
    P_ENABLE_UAQC_IN_VID("p->enable_uaqc_in_vid", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_level_n_strategy}.
     */
    P_ENABLE_UAQC_LEVEL_N_STRATEGY("p->enable_uaqc_level_n_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_lossy_state}.
     */
    P_ENABLE_UAQC_LOSSY_STATE("p->enable_uaqc_lossy_state", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_no_rtcp_check}.
     */
    P_ENABLE_UAQC_NO_RTCP_CHECK("p->enable_uaqc_no_rtcp_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_peer_neteq}.
     */
    P_ENABLE_UAQC_PEER_NETEQ("p->enable_uaqc_peer_neteq", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_plr_ema}.
     */
    P_ENABLE_UAQC_PLR_EMA("p->enable_uaqc_plr_ema", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_record}.
     */
    P_ENABLE_UAQC_RECORD("p->enable_uaqc_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uaqc_ts_logger}.
     */
    P_ENABLE_UAQC_TS_LOGGER("p->enable_uaqc_ts_logger", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_udst_dyn_check}.
     */
    P_ENABLE_UDST_DYN_CHECK("p->enable_udst_dyn_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_ul_vid_disable}.
     */
    P_ENABLE_UL_VID_DISABLE("p->enable_ul_vid_disable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uvq_detailed_tracking}.
     */
    P_ENABLE_UVQ_DETAILED_TRACKING("p->enable_uvq_detailed_tracking", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uvq_load}.
     */
    P_ENABLE_UVQ_LOAD("p->enable_uvq_load", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uvq_ts_log}.
     */
    P_ENABLE_UVQ_TS_LOG("p->enable_uvq_ts_log", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uwp_agc2}.
     */
    P_ENABLE_UWP_AGC2("p->enable_uwp_agc2", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_uwp_ns_metrics}.
     */
    P_ENABLE_UWP_NS_METRICS("p->enable_uwp_ns_metrics", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_verbose_aud_pkt_logs}.
     */
    P_ENABLE_VERBOSE_AUD_PKT_LOGS("p->enable_verbose_aud_pkt_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_verbose_logging}.
     */
    P_ENABLE_VERBOSE_LOGGING("p->enable_verbose_logging", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_verbose_ml_logging}.
     */
    P_ENABLE_VERBOSE_ML_LOGGING("p->enable_verbose_ml_logging", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_verbose_paced_pkt_logs}.
     */
    P_ENABLE_VERBOSE_PACED_PKT_LOGS("p->enable_verbose_paced_pkt_logs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_vid_red_aware_ares}.
     */
    P_ENABLE_VID_RED_AWARE_ARES("p->enable_vid_red_aware_ares", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_video_hist_based_instant_ramp_up}.
     */
    P_ENABLE_VIDEO_HIST_BASED_INSTANT_RAMP_UP("p->enable_video_hist_based_instant_ramp_up", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_vmos}.
     */
    P_ENABLE_VMOS("p->enable_vmos", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_vsr_inf_update_lock}.
     */
    P_ENABLE_VSR_INF_UPDATE_LOCK("p->enable_vsr_inf_update_lock", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_vsr_load}.
     */
    P_ENABLE_VSR_LOAD("p->enable_vsr_load", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_approx_peer_ul_plr}.
     */
    P_ENABLE_WARP_APPROX_PEER_UL_PLR("p->enable_warp_approx_peer_ul_plr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_mi_remaining_len_check}.
     */
    P_ENABLE_WARP_MI_REMAINING_LEN_CHECK("p->enable_warp_mi_remaining_len_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_roc_for_hbh}.
     */
    P_ENABLE_WARP_ROC_FOR_HBH("p->enable_warp_roc_for_hbh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_rtx_indication}.
     */
    P_ENABLE_WARP_RTX_INDICATION("p->enable_warp_rtx_indication", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_rtx_indicator_fix}.
     */
    P_ENABLE_WARP_RTX_INDICATOR_FIX("p->enable_warp_rtx_indicator_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_sfu}.
     */
    P_ENABLE_WARP_SFU("p->enable_warp_sfu", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_sfu_mock_brc}.
     */
    P_ENABLE_WARP_SFU_MOCK_BRC("p->enable_warp_sfu_mock_brc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_warp_sfu_simulcast}.
     */
    P_ENABLE_WARP_SFU_SIMULCAST("p->enable_warp_sfu_simulcast", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enable_zero_rtt_check_slide_window}.
     */
    P_ENABLE_ZERO_RTT_CHECK_SLIDE_WINDOW("p->enable_zero_rtt_check_slide_window", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enabled}.
     */
    P_ENABLED("p->enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enabled_for_video_upgrade}.
     */
    P_ENABLED_FOR_VIDEO_UPGRADE("p->enabled_for_video_upgrade", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enabled_match_self_only}.
     */
    P_ENABLED_MATCH_SELF_ONLY("p->enabled_match_self_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_latency_handling_fixes}.
     */
    P_ENC_LATENCY_HANDLING_FIXES("p->enc_latency_handling_fixes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_complexity_step}.
     */
    P_ENC_P_ADJ_COMPLEXITY_STEP("p->enc_p_adj_complexity_step", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_cpu_step}.
     */
    P_ENC_P_ADJ_CPU_STEP("p->enc_p_adj_cpu_step", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_max_enc_latency}.
     */
    P_ENC_P_ADJ_MAX_ENC_LATENCY("p->enc_p_adj_max_enc_latency", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_min_complexity}.
     */
    P_ENC_P_ADJ_MIN_COMPLEXITY("p->enc_p_adj_min_complexity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_min_frames_for_valid_stats}.
     */
    P_ENC_P_ADJ_MIN_FRAMES_FOR_VALID_STATS("p->enc_p_adj_min_frames_for_valid_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_min_multiples_of_width_base}.
     */
    P_ENC_P_ADJ_MIN_MULTIPLES_OF_WIDTH_BASE("p->enc_p_adj_min_multiples_of_width_base", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_p_adj_min_vpx_cpu}.
     */
    P_ENC_P_ADJ_MIN_VPX_CPU("p->enc_p_adj_min_vpx_cpu", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;enc_psnr_downgrade_threshold}.
     */
    P_ENC_PSNR_DOWNGRADE_THRESHOLD("p->enc_psnr_downgrade_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encode_fmt_wait_ms}.
     */
    P_ENCODE_FMT_WAIT_MS("p->encode_fmt_wait_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_complexity}.
     */
    P_ENCODER_COMPLEXITY("p->encoder_complexity", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.blocking_fmt_change_event}.
     */
    P_ENCODER_PARAM_BLOCKING_FMT_CHANGE_EVENT("p->encoder_param.blocking_fmt_change_event", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.comb_psnr_sample_interval}.
     */
    P_ENCODER_PARAM_COMB_PSNR_SAMPLE_INTERVAL("p->encoder_param.comb_psnr_sample_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_fragmentation_fix}.
     */
    P_ENCODER_PARAM_ENABLE_FRAGMENTATION_FIX("p->encoder_param.enable_fragmentation_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_h265_enc}.
     */
    P_ENCODER_PARAM_ENABLE_H265_ENC("p->encoder_param.enable_h265_enc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_kf_switch_fix}.
     */
    P_ENCODER_PARAM_ENABLE_KF_SWITCH_FIX("p->encoder_param.enable_kf_switch_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_passthrough_frame_dropper}.
     */
    P_ENCODER_PARAM_ENABLE_PASSTHROUGH_FRAME_DROPPER("p->encoder_param.enable_passthrough_frame_dropper", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_refresh_frame}.
     */
    P_ENCODER_PARAM_ENABLE_REFRESH_FRAME("p->encoder_param.enable_refresh_frame", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_roi_encoding}.
     */
    P_ENCODER_PARAM_ENABLE_ROI_ENCODING("p->encoder_param.enable_roi_encoding", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_sampling_dist}.
     */
    P_ENCODER_PARAM_ENABLE_SAMPLING_DIST("p->encoder_param.enable_sampling_dist", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.enable_vid_frame_logging}.
     */
    P_ENCODER_PARAM_ENABLE_VID_FRAME_LOGGING("p->encoder_param.enable_vid_frame_logging", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.equalize_packet_sizes}.
     */
    P_ENCODER_PARAM_EQUALIZE_PACKET_SIZES("p->encoder_param.equalize_packet_sizes", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.force_fallback_to_openh264_enc}.
     */
    P_ENCODER_PARAM_FORCE_FALLBACK_TO_OPENH264_ENC("p->encoder_param.force_fallback_to_openh264_enc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.group_video_encode_height}.
     */
    P_ENCODER_PARAM_GROUP_VIDEO_ENCODE_HEIGHT("p->encoder_param.group_video_encode_height", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.group_video_encode_width}.
     */
    P_ENCODER_PARAM_GROUP_VIDEO_ENCODE_WIDTH("p->encoder_param.group_video_encode_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.h265_bitrate_multiplier}.
     */
    P_ENCODER_PARAM_H265_BITRATE_MULTIPLIER("p->encoder_param.h265_bitrate_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_data_rate_limit_sec}.
     */
    P_ENCODER_PARAM_IOS_DATA_RATE_LIMIT_SEC("p->encoder_param.ios_data_rate_limit_sec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_data_rate_perc}.
     */
    P_ENCODER_PARAM_IOS_DATA_RATE_PERC("p->encoder_param.ios_data_rate_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_enable_hw_frame_converter}.
     */
    P_ENCODER_PARAM_IOS_ENABLE_HW_FRAME_CONVERTER("p->encoder_param.ios_enable_hw_frame_converter", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_enable_hw_frame_converter_h265}.
     */
    P_ENCODER_PARAM_IOS_ENABLE_HW_FRAME_CONVERTER_H265("p->encoder_param.ios_enable_hw_frame_converter_h265", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_enable_low_latency_rc}.
     */
    P_ENCODER_PARAM_IOS_ENABLE_LOW_LATENCY_RC("p->encoder_param.ios_enable_low_latency_rc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_enable_ts}.
     */
    P_ENCODER_PARAM_IOS_ENABLE_TS("p->encoder_param.ios_enable_ts", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_h264_profile_level}.
     */
    P_ENCODER_PARAM_IOS_H264_PROFILE_LEVEL("p->encoder_param.ios_h264_profile_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_h265_profile_level}.
     */
    P_ENCODER_PARAM_IOS_H265_PROFILE_LEVEL("p->encoder_param.ios_h265_profile_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_is_compression_session_realtime}.
     */
    P_ENCODER_PARAM_IOS_IS_COMPRESSION_SESSION_REALTIME("p->encoder_param.ios_is_compression_session_realtime", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_is_encoding_synchronous}.
     */
    P_ENCODER_PARAM_IOS_IS_ENCODING_SYNCHRONOUS("p->encoder_param.ios_is_encoding_synchronous", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_max_allowed_frame_qp_h265}.
     */
    P_ENCODER_PARAM_IOS_MAX_ALLOWED_FRAME_QP_H265("p->encoder_param.ios_max_allowed_frame_qp_h265", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_min_allowed_frame_qp_h265}.
     */
    P_ENCODER_PARAM_IOS_MIN_ALLOWED_FRAME_QP_H265("p->encoder_param.ios_min_allowed_frame_qp_h265", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ios_prioritize_encoding_quality_h265}.
     */
    P_ENCODER_PARAM_IOS_PRIORITIZE_ENCODING_QUALITY_H265("p->encoder_param.ios_prioritize_encoding_quality_h265", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ltr_pool_ring_buf_max_frames}.
     */
    P_ENCODER_PARAM_LTR_POOL_RING_BUF_MAX_FRAMES("p->encoder_param.ltr_pool_ring_buf_max_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ltrp_kf_correction_factor_wt}.
     */
    P_ENCODER_PARAM_LTRP_KF_CORRECTION_FACTOR_WT("p->encoder_param.ltrp_kf_correction_factor_wt", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ltrp_kf_frame_size_wt}.
     */
    P_ENCODER_PARAM_LTRP_KF_FRAME_SIZE_WT("p->encoder_param.ltrp_kf_frame_size_wt", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.ltrp_qp_offset}.
     */
    P_ENCODER_PARAM_LTRP_QP_OFFSET("p->encoder_param.ltrp_qp_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.min_fragmentation_size}.
     */
    P_ENCODER_PARAM_MIN_FRAGMENTATION_SIZE("p->encoder_param.min_fragmentation_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.min_packets_per_frame}.
     */
    P_ENCODER_PARAM_MIN_PACKETS_PER_FRAME("p->encoder_param.min_packets_per_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.allow_key_frame_drop}.
     */
    P_ENCODER_PARAM_OPENH264_ALLOW_KEY_FRAME_DROP("p->encoder_param.openh264.allow_key_frame_drop", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.complexity}.
     */
    P_ENCODER_PARAM_OPENH264_COMPLEXITY("p->encoder_param.openh264.complexity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.enable_cross_me_for_screenshare}.
     */
    P_ENCODER_PARAM_OPENH264_ENABLE_CROSS_ME_FOR_SCREENSHARE("p->encoder_param.openh264.enable_cross_me_for_screenshare", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.enable_frame_skip}.
     */
    P_ENCODER_PARAM_OPENH264_ENABLE_FRAME_SKIP("p->encoder_param.openh264.enable_frame_skip", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.enable_non_5multiple_fps}.
     */
    P_ENCODER_PARAM_OPENH264_ENABLE_NON_5MULTIPLE_FPS("p->encoder_param.openh264.enable_non_5multiple_fps", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.enable_selfside_frame_rotation}.
     */
    P_ENCODER_PARAM_OPENH264_ENABLE_SELFSIDE_FRAME_ROTATION("p->encoder_param.openh264.enable_selfside_frame_rotation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.enable_simd_psnr}.
     */
    P_ENCODER_PARAM_OPENH264_ENABLE_SIMD_PSNR("p->encoder_param.openh264.enable_simd_psnr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.fix_gom_calculation}.
     */
    P_ENCODER_PARAM_OPENH264_FIX_GOM_CALCULATION("p->encoder_param.openh264.fix_gom_calculation", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.fix_preframe_skip}.
     */
    P_ENCODER_PARAM_OPENH264_FIX_PREFRAME_SKIP("p->encoder_param.openh264.fix_preframe_skip", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.idr_bitrate_ratio}.
     */
    P_ENCODER_PARAM_OPENH264_IDR_BITRATE_RATIO("p->encoder_param.openh264.idr_bitrate_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.interfrm_thrd_for_idr_ratio}.
     */
    P_ENCODER_PARAM_OPENH264_INTERFRM_THRD_FOR_IDR_RATIO("p->encoder_param.openh264.interfrm_thrd_for_idr_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.ltr_marking_period_in_frames}.
     */
    P_ENCODER_PARAM_OPENH264_LTR_MARKING_PERIOD_IN_FRAMES("p->encoder_param.openh264.ltr_marking_period_in_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.ltr_ref_list_size}.
     */
    P_ENCODER_PARAM_OPENH264_LTR_REF_LIST_SIZE("p->encoder_param.openh264.ltr_ref_list_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.max_qp}.
     */
    P_ENCODER_PARAM_OPENH264_MAX_QP("p->encoder_param.openh264.max_qp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.min_qp}.
     */
    P_ENCODER_PARAM_OPENH264_MIN_QP("p->encoder_param.openh264.min_qp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.no_periodic_key_frame}.
     */
    P_ENCODER_PARAM_OPENH264_NO_PERIODIC_KEY_FRAME("p->encoder_param.openh264.no_periodic_key_frame", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.num_ltr_frames}.
     */
    P_ENCODER_PARAM_OPENH264_NUM_LTR_FRAMES("p->encoder_param.openh264.num_ltr_frames", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.num_temporal_layers}.
     */
    P_ENCODER_PARAM_OPENH264_NUM_TEMPORAL_LAYERS("p->encoder_param.openh264.num_temporal_layers", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.openh264.openh264_screen_share_enable_qp_reconfig}.
     */
    P_ENCODER_PARAM_OPENH264_OPENH264_SCREEN_SHARE_ENABLE_QP_RECONFIG("p->encoder_param.openh264.openh264_screen_share_enable_qp_reconfig", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.periodic_ltrp_interval}.
     */
    P_ENCODER_PARAM_PERIODIC_LTRP_INTERVAL("p->encoder_param.periodic_ltrp_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.refresh_vp9_key_frame_during_res_change}.
     */
    P_ENCODER_PARAM_REFRESH_VP9_KEY_FRAME_DURING_RES_CHANGE("p->encoder_param.refresh_vp9_key_frame_during_res_change", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.restrict_fmt_change_event}.
     */
    P_ENCODER_PARAM_RESTRICT_FMT_CHANGE_EVENT("p->encoder_param.restrict_fmt_change_event", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.second_kf_after_reset}.
     */
    P_ENCODER_PARAM_SECOND_KF_AFTER_RESET("p->encoder_param.second_kf_after_reset", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.second_kf_interval_ms}.
     */
    P_ENCODER_PARAM_SECOND_KF_INTERVAL_MS("p->encoder_param.second_kf_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.skip_nack_if_ltrp_sent}.
     */
    P_ENCODER_PARAM_SKIP_NACK_IF_LTRP_SENT("p->encoder_param.skip_nack_if_ltrp_sent", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.threshold_fps_pct_for_preset_change}.
     */
    P_ENCODER_PARAM_THRESHOLD_FPS_PCT_FOR_PRESET_CHANGE("p->encoder_param.threshold_fps_pct_for_preset_change", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.unify_enc_cpu_complexity}.
     */
    P_ENCODER_PARAM_UNIFY_ENC_CPU_COMPLEXITY("p->encoder_param.unify_enc_cpu_complexity", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.update_fps_enc_preset_interval}.
     */
    P_ENCODER_PARAM_UPDATE_FPS_ENC_PRESET_INTERVAL("p->encoder_param.update_fps_enc_preset_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.vpx_bg_delta_qp}.
     */
    P_ENCODER_PARAM_VPX_BG_DELTA_QP("p->encoder_param.vpx_bg_delta_qp", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.vpx_cpu}.
     */
    P_ENCODER_PARAM_VPX_CPU("p->encoder_param.vpx_cpu", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;encoder_param.vpx_roi_delta_qp}.
     */
    P_ENCODER_PARAM_VPX_ROI_DELTA_QP("p->encoder_param.vpx_roi_delta_qp", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;exit_forced_probing_after_rbwe_update}.
     */
    P_EXIT_FORCED_PROBING_AFTER_RBWE_UPDATE("p->exit_forced_probing_after_rbwe_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;exit_forced_probing_after_sbwe_update}.
     */
    P_EXIT_FORCED_PROBING_AFTER_SBWE_UPDATE("p->exit_forced_probing_after_sbwe_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fall_back_to_rtt_congestion_upon_delay_error}.
     */
    P_FALL_BACK_TO_RTT_CONGESTION_UPON_DELAY_ERROR("p->fall_back_to_rtt_congestion_upon_delay_error", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fallback_model}.
     */
    P_FALLBACK_MODEL("p->fallback_model", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;fallback_model_type}.
     */
    P_FALLBACK_MODEL_TYPE("p->fallback_model_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_participant_report_interval_ms}.
     */
    P_FAST_PARTICIPANT_REPORT_INTERVAL_MS("p->fast_participant_report_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_pr_peer_downlink_plr_threshold}.
     */
    P_FAST_PR_PEER_DOWNLINK_PLR_THRESHOLD("p->fast_pr_peer_downlink_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_pr_self_uplink_plr_threshold}.
     */
    P_FAST_PR_SELF_UPLINK_PLR_THRESHOLD("p->fast_pr_self_uplink_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.delayed_start}.
     */
    P_FAST_REMB_PARAMS_DELAYED_START("p->fast_remb_params.delayed_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.enable_in_downgraded_1x1_call}.
     */
    P_FAST_REMB_PARAMS_ENABLE_IN_DOWNGRADED_1X1_CALL("p->fast_remb_params.enable_in_downgraded_1x1_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.fast_remb_send_interval_ms}.
     */
    P_FAST_REMB_PARAMS_FAST_REMB_SEND_INTERVAL_MS("p->fast_remb_params.fast_remb_send_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.parallel_remb}.
     */
    P_FAST_REMB_PARAMS_PARALLEL_REMB("p->fast_remb_params.parallel_remb", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.prefer_video_rtp}.
     */
    P_FAST_REMB_PARAMS_PREFER_VIDEO_RTP("p->fast_remb_params.prefer_video_rtp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.use_fast_rr_parser}.
     */
    P_FAST_REMB_PARAMS_USE_FAST_RR_PARSER("p->fast_remb_params.use_fast_rr_parser", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_remb_params.video_remb_start_delay}.
     */
    P_FAST_REMB_PARAMS_VIDEO_REMB_START_DELAY("p->fast_remb_params.video_remb_start_delay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_handling_policy}.
     */
    P_FAST_RR_HANDLING_POLICY("p->fast_rr_handling_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.compute_loss_using_sr}.
     */
    P_FAST_RR_PARAMS_COMPUTE_LOSS_USING_SR("p->fast_rr_params.compute_loss_using_sr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.fast_rr_min_send_interval_ms}.
     */
    P_FAST_RR_PARAMS_FAST_RR_MIN_SEND_INTERVAL_MS("p->fast_rr_params.fast_rr_min_send_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.plr_src}.
     */
    P_FAST_RR_PARAMS_PLR_SRC("p->fast_rr_params.plr_src", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.rtt_src}.
     */
    P_FAST_RR_PARAMS_RTT_SRC("p->fast_rr_params.rtt_src", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.send_fast_rr_on_nack}.
     */
    P_FAST_RR_PARAMS_SEND_FAST_RR_ON_NACK("p->fast_rr_params.send_fast_rr_on_nack", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_params.start_delay_ms}.
     */
    P_FAST_RR_PARAMS_START_DELAY_MS("p->fast_rr_params.start_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_pt}.
     */
    P_FAST_RR_PT("p->fast_rr_pt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fast_rr_send_interval_ms}.
     */
    P_FAST_RR_SEND_INTERVAL_MS("p->fast_rr_send_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_algorithm}.
     */
    P_FEC_ALGORITHM("p->fec_algorithm", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_burst_on_relay_change_duration_ms}.
     */
    P_FEC_BURST_ON_RELAY_CHANGE_DURATION_MS("p->fec_burst_on_relay_change_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_burst_on_relay_change_ratio}.
     */
    P_FEC_BURST_ON_RELAY_CHANGE_RATIO("p->fec_burst_on_relay_change_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_cover_range}.
     */
    P_FEC_COVER_RANGE("p->fec_cover_range", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_exploration_max_duration_ms}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORATION_MAX_DURATION_MS("p->fec_exploration_params.fec_exploration_max_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_exploration_max_ratio_delta}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORATION_MAX_RATIO_DELTA("p->fec_exploration_params.fec_exploration_max_ratio_delta", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_exploration_min_duration_ms}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORATION_MIN_DURATION_MS("p->fec_exploration_params.fec_exploration_min_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_exploration_min_wait_time_ms}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORATION_MIN_WAIT_TIME_MS("p->fec_exploration_params.fec_exploration_min_wait_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_exploration_prob_perc}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORATION_PROB_PERC("p->fec_exploration_params.fec_exploration_prob_perc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_explore_absolute_target}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORE_ABSOLUTE_TARGET("p->fec_exploration_params.fec_explore_absolute_target", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_explore_max_target}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORE_MAX_TARGET("p->fec_exploration_params.fec_explore_max_target", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_exploration_params.fec_explore_min_feasible_bwe_kbps}.
     */
    P_FEC_EXPLORATION_PARAMS_FEC_EXPLORE_MIN_FEASIBLE_BWE_KBPS("p->fec_exploration_params.fec_explore_min_feasible_bwe_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_frames}.
     */
    P_FEC_FRAMES("p->fec_frames", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;fec_high_rtt_ms}.
     */
    P_FEC_HIGH_RTT_MS("p->fec_high_rtt_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_hyst_time_ms}.
     */
    P_FEC_HYST_TIME_MS("p->fec_hyst_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_low_rtt_ms}.
     */
    P_FEC_LOW_RTT_MS("p->fec_low_rtt_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_max_media_frames}.
     */
    P_FEC_MAX_MEDIA_FRAMES("p->fec_max_media_frames", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_max_rtt_decrease_factor}.
     */
    P_FEC_MAX_RTT_DECREASE_FACTOR("p->fec_max_rtt_decrease_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_max_rtt_increase_factor}.
     */
    P_FEC_MAX_RTT_INCREASE_FACTOR("p->fec_max_rtt_increase_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_min_num_media_frame}.
     */
    P_FEC_MIN_NUM_MEDIA_FRAME("p->fec_min_num_media_frame", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_min_num_media_packet}.
     */
    P_FEC_MIN_NUM_MEDIA_PACKET("p->fec_min_num_media_packet", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_mode}.
     */
    P_FEC_MODE("p->fec_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_nack}.
     */
    P_FEC_NACK("p->fec_nack", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_pkts}.
     */
    P_FEC_PKTS("p->fec_pkts", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;fec_quant_step}.
     */
    P_FEC_QUANT_STEP("p->fec_quant_step", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_rs_num_parity_threshold}.
     */
    P_FEC_RS_NUM_PARITY_THRESHOLD("p->fec_rs_num_parity_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_rtt_based_scaling_weight}.
     */
    P_FEC_RTT_BASED_SCALING_WEIGHT("p->fec_rtt_based_scaling_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_timeout}.
     */
    P_FEC_TIMEOUT("p->fec_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_to_packet_loss}.
     */
    P_FEC_TO_PACKET_LOSS("p->fec_to_packet_loss", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;fec_vid_bitrate_adj_factor}.
     */
    P_FEC_VID_BITRATE_ADJ_FACTOR("p->fec_vid_bitrate_adj_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;finish_probing_always}.
     */
    P_FINISH_PROBING_ALWAYS("p->finish_probing_always", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;force_additive_sender_bwe_inc}.
     */
    P_FORCE_ADDITIVE_SENDER_BWE_INC("p->force_additive_sender_bwe_inc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;forced_probing_after_segment_start_ms}.
     */
    P_FORCED_PROBING_AFTER_SEGMENT_START_MS("p->forced_probing_after_segment_start_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;forced_probing_interval_ms}.
     */
    P_FORCED_PROBING_INTERVAL_MS("p->forced_probing_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;forced_probing_scheme}.
     */
    P_FORCED_PROBING_SCHEME("p->forced_probing_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;forced_probing_ts_length}.
     */
    P_FORCED_PROBING_TS_LENGTH("p->forced_probing_ts_length", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;frame_length_ms}.
     */
    P_FRAME_LENGTH_MS("p->frame_length_ms", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;frames_per_packet}.
     */
    P_FRAMES_PER_PACKET("p->frames_per_packet", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;fs_enable_sfu_e2e_tx_pkt_loss}.
     */
    P_FS_ENABLE_SFU_E2E_TX_PKT_LOSS("p->fs_enable_sfu_e2e_tx_pkt_loss", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;get_transport_stats}.
     */
    P_GET_TRANSPORT_STATS("p->get_transport_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;group_call_record_enable}.
     */
    P_GROUP_CALL_RECORD_ENABLE("p->group_call_record_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;h264_vqs}.
     */
    P_H264_VQS("p->h264_vqs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hash_table_size}.
     */
    P_HASH_TABLE_SIZE("p->hash_table_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hbh_fec_rx_algorithm}.
     */
    P_HBH_FEC_RX_ALGORITHM("p->hbh_fec_rx_algorithm", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hbh_fec_tx_algorithm}.
     */
    P_HBH_FEC_TX_ALGORITHM("p->hbh_fec_tx_algorithm", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hd_dyn_max_target_bitrate}.
     */
    P_HD_DYN_MAX_TARGET_BITRATE("p->hd_dyn_max_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hd_dyn_only_increase_cap}.
     */
    P_HD_DYN_ONLY_INCREASE_CAP("p->hd_dyn_only_increase_cap", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_a2a_ml_feature_val}.
     */
    P_HD_TARGETING_A2A_ML_FEATURE_VAL("p->hd_targeting_a2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_a2i_ml_feature_val}.
     */
    P_HD_TARGETING_A2I_ML_FEATURE_VAL("p->hd_targeting_a2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_default_platform_feature_val}.
     */
    P_HD_TARGETING_DEFAULT_PLATFORM_FEATURE_VAL("p->hd_targeting_default_platform_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_i2a_ml_feature_val}.
     */
    P_HD_TARGETING_I2A_ML_FEATURE_VAL("p->hd_targeting_i2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_i2i_ml_feature_val}.
     */
    P_HD_TARGETING_I2I_ML_FEATURE_VAL("p->hd_targeting_i2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_model_name}.
     */
    P_HD_TARGETING_MODEL_NAME("p->hd_targeting_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting_web_ml_feature_val}.
     */
    P_HD_TARGETING_WEB_ML_FEATURE_VAL("p->hd_targeting_web_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;hd_targeting2_model_name}.
     */
    P_HD_TARGETING2_MODEL_NAME("p->hd_targeting2_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;hd_user_setting_bitmap}.
     */
    P_HD_USER_SETTING_BITMAP("p->hd_user_setting_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;high_cellular_bitrate_usage_detection_bitrate_threshold}.
     */
    P_HIGH_CELLULAR_BITRATE_USAGE_DETECTION_BITRATE_THRESHOLD("p->high_cellular_bitrate_usage_detection_bitrate_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;high_cellular_bitrate_usage_detection_duration_threshold}.
     */
    P_HIGH_CELLULAR_BITRATE_USAGE_DETECTION_DURATION_THRESHOLD("p->high_cellular_bitrate_usage_detection_duration_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;high_cellular_bitrate_usage_detection_mode}.
     */
    P_HIGH_CELLULAR_BITRATE_USAGE_DETECTION_MODE("p->high_cellular_bitrate_usage_detection_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_always_use_if_avail}.
     */
    P_HIS_ALWAYS_USE_IF_AVAIL("p->his_always_use_if_avail", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_bitrate_multiplier}.
     */
    P_HIS_BITRATE_MULTIPLIER("p->his_bitrate_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_enable}.
     */
    P_HIS_ENABLE("p->his_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_recent_call_threshold_sec_cell}.
     */
    P_HIS_RECENT_CALL_THRESHOLD_SEC_CELL("p->his_recent_call_threshold_sec_cell", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_recent_call_threshold_sec_wifi}.
     */
    P_HIS_RECENT_CALL_THRESHOLD_SEC_WIFI("p->his_recent_call_threshold_sec_wifi", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;his_use_recent_probing}.
     */
    P_HIS_USE_RECENT_PROBING("p->his_use_recent_probing", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;historical_relay_hash_table_size}.
     */
    P_HISTORICAL_RELAY_HASH_TABLE_SIZE("p->historical_relay_hash_table_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;historical_relay_latency_threshold_ms}.
     */
    P_HISTORICAL_RELAY_LATENCY_THRESHOLD_MS("p->historical_relay_latency_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP("p->history_based_bwe_instant_ramp_up", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up_match_peer}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP_MATCH_PEER("p->history_based_bwe_instant_ramp_up_match_peer", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up_match_self}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP_MATCH_SELF("p->history_based_bwe_instant_ramp_up_match_self", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up_option}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP_OPTION("p->history_based_bwe_instant_ramp_up_option", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up_ratio}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP_RATIO("p->history_based_bwe_instant_ramp_up_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_instant_ramp_up_threshold}.
     */
    P_HISTORY_BASED_BWE_INSTANT_RAMP_UP_THRESHOLD("p->history_based_bwe_instant_ramp_up_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_update_ceiling}.
     */
    P_HISTORY_BASED_BWE_UPDATE_CEILING("p->history_based_bwe_update_ceiling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_update_ceiling_audio_reserve}.
     */
    P_HISTORY_BASED_BWE_UPDATE_CEILING_AUDIO_RESERVE("p->history_based_bwe_update_ceiling_audio_reserve", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_bwe_update_ceiling_forced}.
     */
    P_HISTORY_BASED_BWE_UPDATE_CEILING_FORCED("p->history_based_bwe_update_ceiling_forced", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_sfu_downlink_init_bwe}.
     */
    P_HISTORY_BASED_SFU_DOWNLINK_INIT_BWE("p->history_based_sfu_downlink_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_sfu_downlink_init_bwe_ratio}.
     */
    P_HISTORY_BASED_SFU_DOWNLINK_INIT_BWE_RATIO("p->history_based_sfu_downlink_init_bwe_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_sfu_uplink_init_bwe}.
     */
    P_HISTORY_BASED_SFU_UPLINK_INIT_BWE("p->history_based_sfu_uplink_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_based_sfu_uplink_init_bwe_ratio}.
     */
    P_HISTORY_BASED_SFU_UPLINK_INIT_BWE_RATIO("p->history_based_sfu_uplink_init_bwe_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;history_v2_cond_range_mode}.
     */
    P_HISTORY_V2_COND_RANGE_MODE("p->history_v2_cond_range_mode", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;hmrtt_congestion_threshold_margin_constant}.
     */
    P_HMRTT_CONGESTION_THRESHOLD_MARGIN_CONSTANT("p->hmrtt_congestion_threshold_margin_constant", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hmrtt_congestion_threshold_margin_ratio}.
     */
    P_HMRTT_CONGESTION_THRESHOLD_MARGIN_RATIO("p->hmrtt_congestion_threshold_margin_ratio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_adaptive_leveler_ec_thres}.
     */
    P_HS_ADAPTIVE_LEVELER_EC_THRES("p->hs_adaptive_leveler_ec_thres", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_adaptive_leveler_mode}.
     */
    P_HS_ADAPTIVE_LEVELER_MODE("p->hs_adaptive_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;hs_adaptive_leveler_mode_hi_low}.
     */
    P_HS_ADAPTIVE_LEVELER_MODE_HI_LOW("p->hs_adaptive_leveler_mode_hi_low", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;hs_adaptive_leveler_mode_min_intensity}.
     */
    P_HS_ADAPTIVE_LEVELER_MODE_MIN_INTENSITY("p->hs_adaptive_leveler_mode_min_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_adaptive_leveler_mode_rev}.
     */
    P_HS_ADAPTIVE_LEVELER_MODE_REV("p->hs_adaptive_leveler_mode_rev", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;hs_bipolar_compression_en}.
     */
    P_HS_BIPOLAR_COMPRESSION_EN("p->hs_bipolar_compression_en", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;hs_leveler_intensity}.
     */
    P_HS_LEVELER_INTENSITY("p->hs_leveler_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_min_wait_frames_transitions}.
     */
    P_HS_MIN_WAIT_FRAMES_TRANSITIONS("p->hs_min_wait_frames_transitions", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_smooth_leveler_mode_factor}.
     */
    P_HS_SMOOTH_LEVELER_MODE_FACTOR("p->hs_smooth_leveler_mode_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;hs_use_smooth_leveler_mode}.
     */
    P_HS_USE_SMOOTH_LEVELER_MODE("p->hs_use_smooth_leveler_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;i2a_ml_feature_val}.
     */
    P_I2A_ML_FEATURE_VAL("p->i2a_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;i2i_ml_feature_val}.
     */
    P_I2I_ML_FEATURE_VAL("p->i2i_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;ice_rtt_low_threshold_ms}.
     */
    P_ICE_RTT_LOW_THRESHOLD_MS("p->ice_rtt_low_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ignore_batt_rules}.
     */
    P_IGNORE_BATT_RULES("p->ignore_batt_rules", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ignore_warp_mi_failures}.
     */
    P_IGNORE_WARP_MI_FAILURES("p->ignore_warp_mi_failures", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;imm_send_historical_relay_latencies}.
     */
    P_IMM_SEND_HISTORICAL_RELAY_LATENCIES("p->imm_send_historical_relay_latencies", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;inference_latency_win_size}.
     */
    P_INFERENCE_LATENCY_WIN_SIZE("p->inference_latency_win_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_bitrate}.
     */
    P_INIT_BITRATE("p->init_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_bwe_fr_check_time_ms}.
     */
    P_INIT_BWE_FR_CHECK_TIME_MS("p->init_bwe_fr_check_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_dl_bwe_fr_check_time_ms}.
     */
    P_INIT_DL_BWE_FR_CHECK_TIME_MS("p->init_dl_bwe_fr_check_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_fallback_dl_pp_weight}.
     */
    P_INIT_FALLBACK_DL_PP_WEIGHT("p->init_fallback_dl_pp_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_fallback_pp_weight}.
     */
    P_INIT_FALLBACK_PP_WEIGHT("p->init_fallback_pp_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_high_end_bitrate}.
     */
    P_INIT_HIGH_END_BITRATE("p->init_high_end_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.allow_remote_dec}.
     */
    P_INIT_PP_PROBING_PARAMS_ALLOW_REMOTE_DEC("p->init_pp_probing_params.allow_remote_dec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.enable_stop_clamp}.
     */
    P_INIT_PP_PROBING_PARAMS_ENABLE_STOP_CLAMP("p->init_pp_probing_params.enable_stop_clamp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.momentum_multiplier}.
     */
    P_INIT_PP_PROBING_PARAMS_MOMENTUM_MULTIPLIER("p->init_pp_probing_params.momentum_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_bitrate_ratio}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_BITRATE_RATIO("p->init_pp_probing_params.pp_bitrate_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_hdr}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_HDR("p->init_pp_probing_params.pp_hdr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_min_bps_to_use_momentum}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_MIN_BPS_TO_USE_MOMENTUM("p->init_pp_probing_params.pp_min_bps_to_use_momentum", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_probing_reset_inflection_point}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_PROBING_RESET_INFLECTION_POINT("p->init_pp_probing_params.pp_probing_reset_inflection_point", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_probing_stop_fix}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_PROBING_STOP_FIX("p->init_pp_probing_params.pp_probing_stop_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_probing_stop_on_receive_drop}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_PROBING_STOP_ON_RECEIVE_DROP("p->init_pp_probing_params.pp_probing_stop_on_receive_drop", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_expire_ms}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_EXPIRE_MS("p->init_pp_probing_params.pp_ramp_up_expire_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_stop_min_bps}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_STOP_MIN_BPS("p->init_pp_probing_params.pp_ramp_up_stop_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_target_hd_bps}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_TARGET_HD_BPS("p->init_pp_probing_params.pp_ramp_up_target_hd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_target_hd_min_bps}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_TARGET_HD_MIN_BPS("p->init_pp_probing_params.pp_ramp_up_target_hd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_target_sd_bps}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_TARGET_SD_BPS("p->init_pp_probing_params.pp_ramp_up_target_sd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_target_sd_min_bps}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_TARGET_SD_MIN_BPS("p->init_pp_probing_params.pp_ramp_up_target_sd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.pp_ramp_up_weight}.
     */
    P_INIT_PP_PROBING_PARAMS_PP_RAMP_UP_WEIGHT("p->init_pp_probing_params.pp_ramp_up_weight", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.ramp_up_target_using_pp}.
     */
    P_INIT_PP_PROBING_PARAMS_RAMP_UP_TARGET_USING_PP("p->init_pp_probing_params.ramp_up_target_using_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.ramp_up_using_pp}.
     */
    P_INIT_PP_PROBING_PARAMS_RAMP_UP_USING_PP("p->init_pp_probing_params.ramp_up_using_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.skip_first_n_pp}.
     */
    P_INIT_PP_PROBING_PARAMS_SKIP_FIRST_N_PP("p->init_pp_probing_params.skip_first_n_pp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.stop_momentum_on_low_pp}.
     */
    P_INIT_PP_PROBING_PARAMS_STOP_MOMENTUM_ON_LOW_PP("p->init_pp_probing_params.stop_momentum_on_low_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.update_bitrate_using_pp}.
     */
    P_INIT_PP_PROBING_PARAMS_UPDATE_BITRATE_USING_PP("p->init_pp_probing_params.update_bitrate_using_pp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.use_momentum}.
     */
    P_INIT_PP_PROBING_PARAMS_USE_MOMENTUM("p->init_pp_probing_params.use_momentum", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_pp_probing_params.use_remote_bitrate}.
     */
    P_INIT_PP_PROBING_PARAMS_USE_REMOTE_BITRATE("p->init_pp_probing_params.use_remote_bitrate", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;init_secondary_video_stream_bitrate}.
     */
    P_INIT_SECONDARY_VIDEO_STREAM_BITRATE("p->init_secondary_video_stream_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_stats_window_ms}.
     */
    P_INIT_STATS_WINDOW_MS("p->init_stats_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;init_time_threshold_ms}.
     */
    P_INIT_TIME_THRESHOLD_MS("p->init_time_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;initial_rtt_congestion_threshold}.
     */
    P_INITIAL_RTT_CONGESTION_THRESHOLD("p->initial_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;instant_ramp_up_min_bps}.
     */
    P_INSTANT_RAMP_UP_MIN_BPS("p->instant_ramp_up_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;instant_ramp_up_target_bps}.
     */
    P_INSTANT_RAMP_UP_TARGET_BPS("p->instant_ramp_up_target_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;key_fec_ratio_multiplier}.
     */
    P_KEY_FEC_RATIO_MULTIPLIER("p->key_fec_ratio_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;key_frame_interval}.
     */
    P_KEY_FRAME_INTERVAL("p->key_frame_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;last_udst_rd_factor}.
     */
    P_LAST_UDST_RD_FACTOR("p->last_udst_rd_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe .aimd_rp_pp_aimd_rate_controller_multiplicative_slow_increase_pct}.
     */
    P_LATEST_DBWE_AIMD_RP_PP_AIMD_RATE_CONTROLLER_MULTIPLICATIVE_SLOW_INCREASE_PCT("p->latest_dbwe .aimd_rp_pp_aimd_rate_controller_multiplicative_slow_increase_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_adpt_thresh_exp}.
     */
    P_LATEST_DBWE_AIMD_ADPT_THRESH_EXP("p->latest_dbwe.aimd_adpt_thresh_exp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_enable}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_ENABLE("p->latest_dbwe.aimd_rp_bw_cap_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_low_bitrate_pct}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_LOW_BITRATE_PCT("p->latest_dbwe.aimd_rp_bw_cap_low_bitrate_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_max_delay_ms}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_MAX_DELAY_MS("p->latest_dbwe.aimd_rp_bw_cap_max_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_min_bitrate_kbps}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_MIN_BITRATE_KBPS("p->latest_dbwe.aimd_rp_bw_cap_min_bitrate_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_rtt_threshold_ms}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_RTT_THRESHOLD_MS("p->latest_dbwe.aimd_rp_bw_cap_rtt_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_bw_cap_throughput_pct}.
     */
    P_LATEST_DBWE_AIMD_RP_BW_CAP_THROUGHPUT_PCT("p->latest_dbwe.aimd_rp_bw_cap_throughput_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_min_bitrate_for_pp_mode_kbps}.
     */
    P_LATEST_DBWE_AIMD_RP_MIN_BITRATE_FOR_PP_MODE_KBPS("p->latest_dbwe.aimd_rp_min_bitrate_for_pp_mode_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_pp_aimd_rate_controller_max_estimate_pct}.
     */
    P_LATEST_DBWE_AIMD_RP_PP_AIMD_RATE_CONTROLLER_MAX_ESTIMATE_PCT("p->latest_dbwe.aimd_rp_pp_aimd_rate_controller_max_estimate_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_use_pp_aimd_rate_controller_bwe}.
     */
    P_LATEST_DBWE_AIMD_RP_USE_PP_AIMD_RATE_CONTROLLER_BWE("p->latest_dbwe.aimd_rp_use_pp_aimd_rate_controller_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.aimd_rp_version_of_pp_bwe}.
     */
    P_LATEST_DBWE_AIMD_RP_VERSION_OF_PP_BWE("p->latest_dbwe.aimd_rp_version_of_pp_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.br_est_scale_small}.
     */
    P_LATEST_DBWE_BR_EST_SCALE_SMALL("p->latest_dbwe.br_est_scale_small", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.br_est_small_thresh}.
     */
    P_LATEST_DBWE_BR_EST_SMALL_THRESH("p->latest_dbwe.br_est_small_thresh", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.br_est_window_ms}.
     */
    P_LATEST_DBWE_BR_EST_WINDOW_MS("p->latest_dbwe.br_est_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_audio_only_force_additive_increase}.
     */
    P_LATEST_DBWE_DBWE_AUDIO_ONLY_FORCE_ADDITIVE_INCREASE("p->latest_dbwe.dbwe_audio_only_force_additive_increase", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_audio_remb_clamp_bps}.
     */
    P_LATEST_DBWE_DBWE_AUDIO_REMB_CLAMP_BPS("p->latest_dbwe.dbwe_audio_remb_clamp_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_clamp_estimate_by_max_bitrate}.
     */
    P_LATEST_DBWE_DBWE_CLAMP_ESTIMATE_BY_MAX_BITRATE("p->latest_dbwe.dbwe_clamp_estimate_by_max_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_enable_aimd_rate_control_rp}.
     */
    P_LATEST_DBWE_DBWE_ENABLE_AIMD_RATE_CONTROL_RP("p->latest_dbwe.dbwe_enable_aimd_rate_control_rp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_enable_initial_overuse_detection}.
     */
    P_LATEST_DBWE_DBWE_ENABLE_INITIAL_OVERUSE_DETECTION("p->latest_dbwe.dbwe_enable_initial_overuse_detection", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_enable_separate_audio}.
     */
    P_LATEST_DBWE_DBWE_ENABLE_SEPARATE_AUDIO("p->latest_dbwe.dbwe_enable_separate_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.dbwe_inter_arrival_use_burst_duration}.
     */
    P_LATEST_DBWE_DBWE_INTER_ARRIVAL_USE_BURST_DURATION("p->latest_dbwe.dbwe_inter_arrival_use_burst_duration", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_est_k_down}.
     */
    P_LATEST_DBWE_TRENDLINE_EST_K_DOWN("p->latest_dbwe.trendline_est_k_down", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_est_k_up}.
     */
    P_LATEST_DBWE_TRENDLINE_EST_K_UP("p->latest_dbwe.trendline_est_k_up", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_est_threshold}.
     */
    P_LATEST_DBWE_TRENDLINE_EST_THRESHOLD("p->latest_dbwe.trendline_est_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_overusing_time_threshold}.
     */
    P_LATEST_DBWE_TRENDLINE_OVERUSING_TIME_THRESHOLD("p->latest_dbwe.trendline_overusing_time_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_smoothing_coef}.
     */
    P_LATEST_DBWE_TRENDLINE_SMOOTHING_COEF("p->latest_dbwe.trendline_smoothing_coef", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;latest_dbwe.trendline_window_size}.
     */
    P_LATEST_DBWE_TRENDLINE_WINDOW_SIZE("p->latest_dbwe.trendline_window_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;leveler_decimation_step_size}.
     */
    P_LEVELER_DECIMATION_STEP_SIZE("p->leveler_decimation_step_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;leveler_intensity}.
     */
    P_LEVELER_INTENSITY("p->leveler_intensity", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;leveler_intensity_echo}.
     */
    P_LEVELER_INTENSITY_ECHO("p->leveler_intensity_echo", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;leveler_process_20ms_samples}.
     */
    P_LEVELER_PROCESS_20MS_SAMPLES("p->leveler_process_20ms_samples", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;leveler_smooth_factor}.
     */
    P_LEVELER_SMOOTH_FACTOR("p->leveler_smooth_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;limit_bw_with_ceiling_duration_sec}.
     */
    P_LIMIT_BW_WITH_CEILING_DURATION_SEC("p->limit_bw_with_ceiling_duration_sec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;limit_bw_with_ceiling_multiplier}.
     */
    P_LIMIT_BW_WITH_CEILING_MULTIPLIER("p->limit_bw_with_ceiling_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;limit_bw_with_ceiling_skip_times}.
     */
    P_LIMIT_BW_WITH_CEILING_SKIP_TIMES("p->limit_bw_with_ceiling_skip_times", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;limiter_enable}.
     */
    P_LIMITER_ENABLE("p->limiter_enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;log_mic_mode}.
     */
    P_LOG_MIC_MODE("p->log_mic_mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;loudness_limit}.
     */
    P_LOUDNESS_LIMIT("p->loudness_limit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;low_data_usage_bitrate}.
     */
    P_LOW_DATA_USAGE_BITRATE("p->low_data_usage_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;low_rbwe_threshold}.
     */
    P_LOW_RBWE_THRESHOLD("p->low_rbwe_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_audio_frame_disorder_distance_rc}.
     */
    P_MAX_AUDIO_FRAME_DISORDER_DISTANCE_RC("p->max_audio_frame_disorder_distance_rc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_bitrate}.
     */
    P_MAX_BITRATE("p->max_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_bwe}.
     */
    P_MAX_BWE("p->max_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_bytes}.
     */
    P_MAX_BYTES("p->max_bytes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_capture_fps}.
     */
    P_MAX_CAPTURE_FPS("p->max_capture_fps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_capture_width}.
     */
    P_MAX_CAPTURE_WIDTH("p->max_capture_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_cpu}.
     */
    P_MAX_CPU("p->max_cpu", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_decrease_factor_on_congestion}.
     */
    P_MAX_DECREASE_FACTOR_ON_CONGESTION("p->max_decrease_factor_on_congestion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_encode_height}.
     */
    P_MAX_ENCODE_HEIGHT("p->max_encode_height", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_encode_width}.
     */
    P_MAX_ENCODE_WIDTH("p->max_encode_width", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_fec_ratio}.
     */
    P_MAX_FEC_RATIO("p->max_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_fps}.
     */
    P_MAX_FPS("p->max_fps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_frames_per_packet}.
     */
    P_MAX_FRAMES_PER_PACKET("p->max_frames_per_packet", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;max_frames_per_packet_duration}.
     */
    P_MAX_FRAMES_PER_PACKET_DURATION("p->max_frames_per_packet_duration", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_hbh_fec_ratio}.
     */
    P_MAX_HBH_FEC_RATIO("p->max_hbh_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_hist_init_bitrate}.
     */
    P_MAX_HIST_INIT_BITRATE("p->max_hist_init_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_inference_latency_ms}.
     */
    P_MAX_INFERENCE_LATENCY_MS("p->max_inference_latency_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_init_bwe}.
     */
    P_MAX_INIT_BWE("p->max_init_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_iterations}.
     */
    P_MAX_ITERATIONS("p->max_iterations", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_key_fec_ratio}.
     */
    P_MAX_KEY_FEC_RATIO("p->max_key_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_key_frame_mode_bitrate}.
     */
    P_MAX_KEY_FRAME_MODE_BITRATE("p->max_key_frame_mode_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_num_of_call_participant_record}.
     */
    P_MAX_NUM_OF_CALL_PARTICIPANT_RECORD("p->max_num_of_call_participant_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_num_of_call_record}.
     */
    P_MAX_NUM_OF_CALL_RECORD("p->max_num_of_call_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_num_of_call_transport_record}.
     */
    P_MAX_NUM_OF_CALL_TRANSPORT_RECORD("p->max_num_of_call_transport_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_packets}.
     */
    P_MAX_PACKETS("p->max_packets", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_plr_to_opus}.
     */
    P_MAX_PLR_TO_OPUS("p->max_plr_to_opus", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_redial_interval}.
     */
    P_MAX_REDIAL_INTERVAL("p->max_redial_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_time_ms_to_use_udst_based_rd_target}.
     */
    P_MAX_TIME_MS_TO_USE_UDST_BASED_RD_TARGET("p->max_time_ms_to_use_udst_based_rd_target", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_tx_rott_based_bitrate}.
     */
    P_MAX_TX_ROTT_BASED_BITRATE("p->max_tx_rott_based_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_udp_fallbacks}.
     */
    P_MAX_UDP_FALLBACKS("p->max_udp_fallbacks", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_udp_pongs_for_network_restart}.
     */
    P_MAX_UDP_PONGS_FOR_NETWORK_RESTART("p->max_udp_pongs_for_network_restart", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;max_udp_retries_before_tcp_attempt}.
     */
    P_MAX_UDP_RETRIES_BEFORE_TCP_ATTEMPT("p->max_udp_retries_before_tcp_attempt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mc_ferraris_suppression_level}.
     */
    P_MC_FERRARIS_SUPPRESSION_LEVEL("p->mc_ferraris_suppression_level", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcp_mode}.
     */
    P_MCP_MODE("p->mcp_mode", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_set_inflection_point}.
     */
    P_MCP_SET_INFLECTION_POINT("p->mcp_set_inflection_point", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_skip_additive_ramp_up}.
     */
    P_MCP_SKIP_ADDITIVE_RAMP_UP("p->mcp_skip_additive_ramp_up", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_skip_ml_inference}.
     */
    P_MCP_SKIP_ML_INFERENCE("p->mcp_skip_ml_inference", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_skip_ramp_up_pause}.
     */
    P_MCP_SKIP_RAMP_UP_PAUSE("p->mcp_skip_ramp_up_pause", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_skip_remote_bwe}.
     */
    P_MCP_SKIP_REMOTE_BWE("p->mcp_skip_remote_bwe", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_stop_bitrate_bps}.
     */
    P_MCP_STOP_BITRATE_BPS("p->mcp_stop_bitrate_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_stop_bitrate_inc_pcnt}.
     */
    P_MCP_STOP_BITRATE_INC_PCNT("p->mcp_stop_bitrate_inc_pcnt", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcp_stop_sbwe_to_pp_pcnt}.
     */
    P_MCP_STOP_SBWE_TO_PP_PCNT("p->mcp_stop_sbwe_to_pp_pcnt", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.allow_bwe_configuration}.
     */
    P_MCS_PARAMS_ALLOW_BWE_CONFIGURATION("p->mcs_params.allow_bwe_configuration", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.audio_fec_plr_coeff}.
     */
    P_MCS_PARAMS_AUDIO_FEC_PLR_COEFF("p->mcs_params.audio_fec_plr_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.cc_bwe_slow_ramp_up_ceiling_mode_dl}.
     */
    P_MCS_PARAMS_CC_BWE_SLOW_RAMP_UP_CEILING_MODE_DL("p->mcs_params.cc_bwe_slow_ramp_up_ceiling_mode_dl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.centile_plr_update_interval_ms}.
     */
    P_MCS_PARAMS_CENTILE_PLR_UPDATE_INTERVAL_MS("p->mcs_params.centile_plr_update_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.disable_sml_nadl_override_when_simulcast}.
     */
    P_MCS_PARAMS_DISABLE_SML_NADL_OVERRIDE_WHEN_SIMULCAST("p->mcs_params.disable_sml_nadl_override_when_simulcast", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_init_rbwe_bitrate}.
     */
    P_MCS_PARAMS_DOWNLINK_INIT_RBWE_BITRATE("p->mcs_params.downlink_init_rbwe_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_init_target_bitrate}.
     */
    P_MCS_PARAMS_DOWNLINK_INIT_TARGET_BITRATE("p->mcs_params.downlink_init_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_min_remote_bwe_lower_bound}.
     */
    P_MCS_PARAMS_DOWNLINK_MIN_REMOTE_BWE_LOWER_BOUND("p->mcs_params.downlink_min_remote_bwe_lower_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_min_remote_bwe_upper_bound}.
     */
    P_MCS_PARAMS_DOWNLINK_MIN_REMOTE_BWE_UPPER_BOUND("p->mcs_params.downlink_min_remote_bwe_upper_bound", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_per_peer_min_remote_bwe}.
     */
    P_MCS_PARAMS_DOWNLINK_PER_PEER_MIN_REMOTE_BWE("p->mcs_params.downlink_per_peer_min_remote_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.downlink_remote_bwe_scheme}.
     */
    P_MCS_PARAMS_DOWNLINK_REMOTE_BWE_SCHEME("p->mcs_params.downlink_remote_bwe_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.dynamic_twcc_max_interval_ms}.
     */
    P_MCS_PARAMS_DYNAMIC_TWCC_MAX_INTERVAL_MS("p->mcs_params.dynamic_twcc_max_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.dynamic_twcc_min_interval_ms}.
     */
    P_MCS_PARAMS_DYNAMIC_TWCC_MIN_INTERVAL_MS("p->mcs_params.dynamic_twcc_min_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.e2e_plr_congestion_threshold}.
     */
    P_MCS_PARAMS_E2E_PLR_CONGESTION_THRESHOLD("p->mcs_params.e2e_plr_congestion_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.e2e_rtt_congestion_threshold}.
     */
    P_MCS_PARAMS_E2E_RTT_CONGESTION_THRESHOLD("p->mcs_params.e2e_rtt_congestion_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_bwe_reset}.
     */
    P_MCS_PARAMS_ENABLE_BWE_RESET("p->mcs_params.enable_bwe_reset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_dynamic_twcc_interval}.
     */
    P_MCS_PARAMS_ENABLE_DYNAMIC_TWCC_INTERVAL("p->mcs_params.enable_dynamic_twcc_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_nadl_model}.
     */
    P_MCS_PARAMS_ENABLE_NADL_MODEL("p->mcs_params.enable_nadl_model", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_pause_rampup_dl_sbwe}.
     */
    P_MCS_PARAMS_ENABLE_PAUSE_RAMPUP_DL_SBWE("p->mcs_params.enable_pause_rampup_dl_sbwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_pause_rampup_ul_sbwe}.
     */
    P_MCS_PARAMS_ENABLE_PAUSE_RAMPUP_UL_SBWE("p->mcs_params.enable_pause_rampup_ul_sbwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_pr_on_combined_dl_drop}.
     */
    P_MCS_PARAMS_ENABLE_PR_ON_COMBINED_DL_DROP("p->mcs_params.enable_pr_on_combined_dl_drop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_probation}.
     */
    P_MCS_PARAMS_ENABLE_PROBATION("p->mcs_params.enable_probation", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_sml_nadl_override}.
     */
    P_MCS_PARAMS_ENABLE_SML_NADL_OVERRIDE("p->mcs_params.enable_sml_nadl_override", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_verbose_ceiling_stats}.
     */
    P_MCS_PARAMS_ENABLE_VERBOSE_CEILING_STATS("p->mcs_params.enable_verbose_ceiling_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_pr_bg_flag}.
     */
    P_MCS_PARAMS_ENABLE_WARP_PR_BG_FLAG("p->mcs_params.enable_warp_pr_bg_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_pr_cam_flag}.
     */
    P_MCS_PARAMS_ENABLE_WARP_PR_CAM_FLAG("p->mcs_params.enable_warp_pr_cam_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_pr_dl_bwe_pp_report}.
     */
    P_MCS_PARAMS_ENABLE_WARP_PR_DL_BWE_PP_REPORT("p->mcs_params.enable_warp_pr_dl_bwe_pp_report", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_pr_speaker_view_flag}.
     */
    P_MCS_PARAMS_ENABLE_WARP_PR_SPEAKER_VIEW_FLAG("p->mcs_params.enable_warp_pr_speaker_view_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_pr_ss_flag}.
     */
    P_MCS_PARAMS_ENABLE_WARP_PR_SS_FLAG("p->mcs_params.enable_warp_pr_ss_flag", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_twcc_bw_deduction}.
     */
    P_MCS_PARAMS_ENABLE_WARP_TWCC_BW_DEDUCTION("p->mcs_params.enable_warp_twcc_bw_deduction", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.enable_warp_twsn}.
     */
    P_MCS_PARAMS_ENABLE_WARP_TWSN("p->mcs_params.enable_warp_twsn", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.force_sending_server_bwe_update}.
     */
    P_MCS_PARAMS_FORCE_SENDING_SERVER_BWE_UPDATE("p->mcs_params.force_sending_server_bwe_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.force_worst_e2e_plr_fec_in_multi_pop}.
     */
    P_MCS_PARAMS_FORCE_WORST_E2E_PLR_FEC_IN_MULTI_POP("p->mcs_params.force_worst_e2e_plr_fec_in_multi_pop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.hbh_fec_loss_overhead}.
     */
    P_MCS_PARAMS_HBH_FEC_LOSS_OVERHEAD("p->mcs_params.hbh_fec_loss_overhead", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.hbh_fec_loss_threshold_window_ms}.
     */
    P_MCS_PARAMS_HBH_FEC_LOSS_THRESHOLD_WINDOW_MS("p->mcs_params.hbh_fec_loss_threshold_window_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.imbalanced_plr_rtt_threshold}.
     */
    P_MCS_PARAMS_IMBALANCED_PLR_RTT_THRESHOLD("p->mcs_params.imbalanced_plr_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.loss_threshold_for_hbh_fec}.
     */
    P_MCS_PARAMS_LOSS_THRESHOLD_FOR_HBH_FEC("p->mcs_params.loss_threshold_for_hbh_fec", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_audio_dup_q_thresh}.
     */
    P_MCS_PARAMS_NADL_AUDIO_DUP_Q_THRESH("p->mcs_params.nadl_audio_dup_q_thresh", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_inference_interval_ms}.
     */
    P_MCS_PARAMS_NADL_INFERENCE_INTERVAL_MS("p->mcs_params.nadl_inference_interval_ms", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_model_load_max_retry}.
     */
    P_MCS_PARAMS_NADL_MODEL_LOAD_MAX_RETRY("p->mcs_params.nadl_model_load_max_retry", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_model_load_retry_interval}.
     */
    P_MCS_PARAMS_NADL_MODEL_LOAD_RETRY_INTERVAL("p->mcs_params.nadl_model_load_retry_interval", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_model_load_trigger_point}.
     */
    P_MCS_PARAMS_NADL_MODEL_LOAD_TRIGGER_POINT("p->mcs_params.nadl_model_load_trigger_point", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_model_name}.
     */
    P_MCS_PARAMS_NADL_MODEL_NAME("p->mcs_params.nadl_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.nadl_model_only_load_max_retry}.
     */
    P_MCS_PARAMS_NADL_MODEL_ONLY_LOAD_MAX_RETRY("p->mcs_params.nadl_model_only_load_max_retry", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.on_relay_change_detect}.
     */
    P_MCS_PARAMS_ON_RELAY_CHANGE_DETECT("p->mcs_params.on_relay_change_detect", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.peer_dl_plr_centile}.
     */
    P_MCS_PARAMS_PEER_DL_PLR_CENTILE("p->mcs_params.peer_dl_plr_centile", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.plr_aggregation_scheme}.
     */
    P_MCS_PARAMS_PLR_AGGREGATION_SCHEME("p->mcs_params.plr_aggregation_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.plr_wt}.
     */
    P_MCS_PARAMS_PLR_WT("p->mcs_params.plr_wt", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.report_sml_nadl_fs}.
     */
    P_MCS_PARAMS_REPORT_SML_NADL_FS("p->mcs_params.report_sml_nadl_fs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.rtt_min_ema_alpha}.
     */
    P_MCS_PARAMS_RTT_MIN_EMA_ALPHA("p->mcs_params.rtt_min_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.rtt_normalize_factor}.
     */
    P_MCS_PARAMS_RTT_NORMALIZE_FACTOR("p->mcs_params.rtt_normalize_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.send_bwe_configuration_duration_ms}.
     */
    P_MCS_PARAMS_SEND_BWE_CONFIGURATION_DURATION_MS("p->mcs_params.send_bwe_configuration_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.send_bwe_configuration_interval_ms}.
     */
    P_MCS_PARAMS_SEND_BWE_CONFIGURATION_INTERVAL_MS("p->mcs_params.send_bwe_configuration_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sfu_delay_since_last_pr_discard_threshold_ms}.
     */
    P_MCS_PARAMS_SFU_DELAY_SINCE_LAST_PR_DISCARD_THRESHOLD_MS("p->mcs_params.sfu_delay_since_last_pr_discard_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sfu_downlink_init_bwe_on_high_end_android}.
     */
    P_MCS_PARAMS_SFU_DOWNLINK_INIT_BWE_ON_HIGH_END_ANDROID("p->mcs_params.sfu_downlink_init_bwe_on_high_end_android", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sfu_rtt_adjust_threshold_ms}.
     */
    P_MCS_PARAMS_SFU_RTT_ADJUST_THRESHOLD_MS("p->mcs_params.sfu_rtt_adjust_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sfu_rtt_discard_threshold_ms}.
     */
    P_MCS_PARAMS_SFU_RTT_DISCARD_THRESHOLD_MS("p->mcs_params.sfu_rtt_discard_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sml_nadl_stale_behavior}.
     */
    P_MCS_PARAMS_SML_NADL_STALE_BEHAVIOR("p->mcs_params.sml_nadl_stale_behavior", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sml_nadl_staleness_threshold_ms}.
     */
    P_MCS_PARAMS_SML_NADL_STALENESS_THRESHOLD_MS("p->mcs_params.sml_nadl_staleness_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.sync_dl_plr_calc_to_pr_send}.
     */
    P_MCS_PARAMS_SYNC_DL_PLR_CALC_TO_PR_SEND("p->mcs_params.sync_dl_plr_calc_to_pr_send", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.uplink_init_rbwe_bitrate}.
     */
    P_MCS_PARAMS_UPLINK_INIT_RBWE_BITRATE("p->mcs_params.uplink_init_rbwe_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.uplink_init_rbwe_duration_ms}.
     */
    P_MCS_PARAMS_UPLINK_INIT_RBWE_DURATION_MS("p->mcs_params.uplink_init_rbwe_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.uplink_init_target_bitrate}.
     */
    P_MCS_PARAMS_UPLINK_INIT_TARGET_BITRATE("p->mcs_params.uplink_init_target_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.uplink_min_rbwe}.
     */
    P_MCS_PARAMS_UPLINK_MIN_RBWE("p->mcs_params.uplink_min_rbwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_real_e2e_peer_stats}.
     */
    P_MCS_PARAMS_USE_REAL_E2E_PEER_STATS("p->mcs_params.use_real_e2e_peer_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_secondary_rbwe_without_null_check}.
     */
    P_MCS_PARAMS_USE_SECONDARY_RBWE_WITHOUT_NULL_CHECK("p->mcs_params.use_secondary_rbwe_without_null_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_for_aud_rc}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_FOR_AUD_RC("p->mcs_params.use_xpop_e2e_rtt_for_aud_rc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_for_aud_stream}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_FOR_AUD_STREAM("p->mcs_params.use_xpop_e2e_rtt_for_aud_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_for_rtt_congestion}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_FOR_RTT_CONGESTION("p->mcs_params.use_xpop_e2e_rtt_for_rtt_congestion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_for_vid_rc}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_FOR_VID_RC("p->mcs_params.use_xpop_e2e_rtt_for_vid_rc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_for_vid_stream}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_FOR_VID_STREAM("p->mcs_params.use_xpop_e2e_rtt_for_vid_stream", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.use_xpop_e2e_rtt_stats}.
     */
    P_MCS_PARAMS_USE_XPOP_E2E_RTT_STATS("p->mcs_params.use_xpop_e2e_rtt_stats", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_dl_bwe_to_video_stream_adjustment_factor}.
     */
    P_MCS_PARAMS_WARP_DL_BWE_TO_VIDEO_STREAM_ADJUSTMENT_FACTOR("p->mcs_params.warp_dl_bwe_to_video_stream_adjustment_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_early_pr_threshold}.
     */
    P_MCS_PARAMS_WARP_EARLY_PR_THRESHOLD("p->mcs_params.warp_early_pr_threshold", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_mcs_stale_state_fix_enable}.
     */
    P_MCS_PARAMS_WARP_MCS_STALE_STATE_FIX_ENABLE("p->mcs_params.warp_mcs_stale_state_fix_enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_sn_max_disorder}.
     */
    P_MCS_PARAMS_WARP_SN_MAX_DISORDER("p->mcs_params.warp_sn_max_disorder", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_sn_max_dropout}.
     */
    P_MCS_PARAMS_WARP_SN_MAX_DROPOUT("p->mcs_params.warp_sn_max_dropout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_transport_feedback_clear_when_disabled}.
     */
    P_MCS_PARAMS_WARP_TRANSPORT_FEEDBACK_CLEAR_WHEN_DISABLED("p->mcs_params.warp_transport_feedback_clear_when_disabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mcs_params.warp_transport_feedback_send_period_msec}.
     */
    P_MCS_PARAMS_WARP_TRANSPORT_FEEDBACK_SEND_PERIOD_MSEC("p->mcs_params.warp_transport_feedback_send_period_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;metrics_compute_after_agc}.
     */
    P_METRICS_COMPUTE_AFTER_AGC("p->metrics_compute_after_agc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;metrics_compute_after_process}.
     */
    P_METRICS_COMPUTE_AFTER_PROCESS("p->metrics_compute_after_process", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;min_bitrate}.
     */
    P_MIN_BITRATE("p->min_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_bwe}.
     */
    P_MIN_BWE("p->min_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_decrease_factor_on_congestion}.
     */
    P_MIN_DECREASE_FACTOR_ON_CONGESTION("p->min_decrease_factor_on_congestion", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_duration_to_save_call_transport_record}.
     */
    P_MIN_DURATION_TO_SAVE_CALL_TRANSPORT_RECORD("p->min_duration_to_save_call_transport_record", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_elastic_disorder_buf_size_in_frames_rc}.
     */
    P_MIN_ELASTIC_DISORDER_BUF_SIZE_IN_FRAMES_RC("p->min_elastic_disorder_buf_size_in_frames_rc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_elastic_disorder_buf_size_ratio_rc}.
     */
    P_MIN_ELASTIC_DISORDER_BUF_SIZE_RATIO_RC("p->min_elastic_disorder_buf_size_ratio_rc", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_fec_ratio}.
     */
    P_MIN_FEC_RATIO("p->min_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_frames_per_packet}.
     */
    P_MIN_FRAMES_PER_PACKET("p->min_frames_per_packet", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;min_hbh_fec_ratio}.
     */
    P_MIN_HBH_FEC_RATIO("p->min_hbh_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_key_fec_ratio}.
     */
    P_MIN_KEY_FEC_RATIO("p->min_key_fec_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_partition_ratio_to_promote}.
     */
    P_MIN_PARTITION_RATIO_TO_PROMOTE("p->min_partition_ratio_to_promote", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_partition_ratio_to_stay}.
     */
    P_MIN_PARTITION_RATIO_TO_STAY("p->min_partition_ratio_to_stay", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_passive_rx_pkt_to_flip}.
     */
    P_MIN_PASSIVE_RX_PKT_TO_FLIP("p->min_passive_rx_pkt_to_flip", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_segment_duration_to_use_2p_info_ms}.
     */
    P_MIN_SEGMENT_DURATION_TO_USE_2P_INFO_MS("p->min_segment_duration_to_use_2p_info_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_sender_estimate_on_drop}.
     */
    P_MIN_SENDER_ESTIMATE_ON_DROP("p->min_sender_estimate_on_drop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_target_bitrate}.
     */
    P_MIN_TARGET_BITRATE("p->min_target_bitrate", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;min_time_ms_bw_ares_upd}.
     */
    P_MIN_TIME_MS_BW_ARES_UPD("p->min_time_ms_bw_ares_upd", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_time_ms_bw_audio_br_calls}.
     */
    P_MIN_TIME_MS_BW_AUDIO_BR_CALLS("p->min_time_ms_bw_audio_br_calls", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_vid_stream_reserve_bps_receiver}.
     */
    P_MIN_VID_STREAM_RESERVE_BPS_RECEIVER("p->min_vid_stream_reserve_bps_receiver", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_vid_stream_reserve_bps_sender}.
     */
    P_MIN_VID_STREAM_RESERVE_BPS_SENDER("p->min_vid_stream_reserve_bps_sender", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;min_wait_frames_transitions}.
     */
    P_MIN_WAIT_FRAMES_TRANSITIONS("p->min_wait_frames_transitions", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ml_cong_decrease_pcnt}.
     */
    P_ML_CONG_DECREASE_PCNT("p->ml_cong_decrease_pcnt", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_cong_max_decrease_cnt_per_cycle}.
     */
    P_ML_CONG_MAX_DECREASE_CNT_PER_CYCLE("p->ml_cong_max_decrease_cnt_per_cycle", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_cong_time_since_last_ramp_down_ms}.
     */
    P_ML_CONG_TIME_SINCE_LAST_RAMP_DOWN_MS("p->ml_cong_time_since_last_ramp_down_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_ns_debug_asp_load_only}.
     */
    P_ML_NS_DEBUG_ASP_LOAD_ONLY("p->ml_ns_debug_asp_load_only", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;ml_ns_debug_init_only}.
     */
    P_ML_NS_DEBUG_INIT_ONLY("p->ml_ns_debug_init_only", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;ml_ns_log_sys_available_mem}.
     */
    P_ML_NS_LOG_SYS_AVAILABLE_MEM("p->ml_ns_log_sys_available_mem", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_cap_fraction}.
     */
    P_ML_UDST_CAP_FRACTION("p->ml_udst_cap_fraction", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_cap_margin}.
     */
    P_ML_UDST_CAP_MARGIN("p->ml_udst_cap_margin", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_check_pp}.
     */
    P_ML_UDST_CHECK_PP("p->ml_udst_check_pp", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_encode_margin}.
     */
    P_ML_UDST_ENCODE_MARGIN("p->ml_udst_encode_margin", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_max_pp_kbps}.
     */
    P_ML_UDST_MAX_PP_KBPS("p->ml_udst_max_pp_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_min_bitrate_bps}.
     */
    P_ML_UDST_MIN_BITRATE_BPS("p->ml_udst_min_bitrate_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_min_pp_bps}.
     */
    P_ML_UDST_MIN_PP_BPS("p->ml_udst_min_pp_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_model_type}.
     */
    P_ML_UDST_MODEL_TYPE("p->ml_udst_model_type", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_pp_noise_to_mean_ratio}.
     */
    P_ML_UDST_PP_NOISE_TO_MEAN_RATIO("p->ml_udst_pp_noise_to_mean_ratio", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_ratio_calc_min_cnt}.
     */
    P_ML_UDST_RATIO_CALC_MIN_CNT("p->ml_udst_ratio_calc_min_cnt", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_ratio_calc_threshold}.
     */
    P_ML_UDST_RATIO_CALC_THRESHOLD("p->ml_udst_ratio_calc_threshold", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_ratio_calc_window_ms}.
     */
    P_ML_UDST_RATIO_CALC_WINDOW_MS("p->ml_udst_ratio_calc_window_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_skip_if_ml_plc_in_effect}.
     */
    P_ML_UDST_SKIP_IF_ML_PLC_IN_EFFECT("p->ml_udst_skip_if_ml_plc_in_effect", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_tgt_br_fraction}.
     */
    P_ML_UDST_TGT_BR_FRACTION("p->ml_udst_tgt_br_fraction", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_time_since_last_ramp_down_ms}.
     */
    P_ML_UDST_TIME_SINCE_LAST_RAMP_DOWN_MS("p->ml_udst_time_since_last_ramp_down_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst_time_since_last_ramp_up_ms}.
     */
    P_ML_UDST_TIME_SINCE_LAST_RAMP_UP_MS("p->ml_udst_time_since_last_ramp_up_ms", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;ml_udst2_max_pp_kbps}.
     */
    P_ML_UDST2_MAX_PP_KBPS("p->ml_udst2_max_pp_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_dec_cutoff_hz}.
     */
    P_MLOW_DEC_CUTOFF_HZ("p->mlow_dec_cutoff_hz", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_dtx_hangover_ms}.
     */
    P_MLOW_DTX_HANGOVER_MS("p->mlow_dtx_hangover_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_enable_lpc_postfilter}.
     */
    P_MLOW_ENABLE_LPC_POSTFILTER("p->mlow_enable_lpc_postfilter", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_inband_fec_fixed_bitrate}.
     */
    P_MLOW_INBAND_FEC_FIXED_BITRATE("p->mlow_inband_fec_fixed_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_red_redundancy_level}.
     */
    P_MLOW_RED_REDUNDANCY_LEVEL("p->mlow_red_redundancy_level", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_red_secondary_bitrate}.
     */
    P_MLOW_RED_SECONDARY_BITRATE("p->mlow_red_secondary_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_red_secondary_complexity}.
     */
    P_MLOW_RED_SECONDARY_COMPLEXITY("p->mlow_red_secondary_complexity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_reuse_hb_data}.
     */
    P_MLOW_REUSE_HB_DATA("p->mlow_reuse_hb_data", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_sf_imp_factor}.
     */
    P_MLOW_SF_IMP_FACTOR("p->mlow_sf_imp_factor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_use_sp_act_flat}.
     */
    P_MLOW_USE_SP_ACT_FLAT("p->mlow_use_sp_act_flat", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_vad_hp_sharpness}.
     */
    P_MLOW_VAD_HP_SHARPNESS("p->mlow_vad_hp_sharpness", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mlow_vad_non_binary}.
     */
    P_MLOW_VAD_NON_BINARY("p->mlow_vad_non_binary", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;mludst_only_start_bps}.
     */
    P_MLUDST_ONLY_START_BPS("p->mludst_only_start_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;mode}.
     */
    P_MODE("p->mode", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;model_name}.
     */
    P_MODEL_NAME("p->model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;monochrome_ratio}.
     */
    P_MONOCHROME_RATIO("p->monochrome_ratio", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;mtu_aware}.
     */
    P_MTU_AWARE("p->mtu_aware", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;nack}.
     */
    P_NACK("p->nack", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;nack_rtx_pkt_seq_threshold}.
     */
    P_NACK_RTX_PKT_SEQ_THRESHOLD("p->nack_rtx_pkt_seq_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;nack_rtx_pkt_ts_threshold_multiplier}.
     */
    P_NACK_RTX_PKT_TS_THRESHOLD_MULTIPLIER("p->nack_rtx_pkt_ts_threshold_multiplier", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;neteq_rc.delay_offset_ms}.
     */
    P_NETEQ_RC_DELAY_OFFSET_MS("p->neteq_rc.delay_offset_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;nm_pair_c2c_ml_feature_val}.
     */
    P_NM_PAIR_C2C_ML_FEATURE_VAL("p->nm_pair_c2c_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;nm_pair_c2w_ml_feature_val}.
     */
    P_NM_PAIR_C2W_ML_FEATURE_VAL("p->nm_pair_c2w_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;nm_pair_w2c_ml_feature_val}.
     */
    P_NM_PAIR_W2C_ML_FEATURE_VAL("p->nm_pair_w2c_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;nm_pair_w2w_ml_feature_val}.
     */
    P_NM_PAIR_W2W_ML_FEATURE_VAL("p->nm_pair_w2w_ml_feature_val", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code p-&gt;no_data_received_threshold}.
     */
    P_NO_DATA_RECEIVED_THRESHOLD("p->no_data_received_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;no_initial_rtt_threshold}.
     */
    P_NO_INITIAL_RTT_THRESHOLD("p->no_initial_rtt_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;no_process_ignored_rd}.
     */
    P_NO_PROCESS_IGNORED_RD("p->no_process_ignored_rd", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;no_rtcp_received_threshold}.
     */
    P_NO_RTCP_RECEIVED_THRESHOLD("p->no_rtcp_received_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;noise_est_gvad_threshold}.
     */
    P_NOISE_EST_GVAD_THRESHOLD("p->noise_est_gvad_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;noise_est_vad_threshold}.
     */
    P_NOISE_EST_VAD_THRESHOLD("p->noise_est_vad_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;offline_rl_bwe_model_name}.
     */
    P_OFFLINE_RL_BWE_MODEL_NAME("p->offline_rl_bwe_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;offset}.
     */
    P_OFFSET("p->offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;oibwe_slow_polling}.
     */
    P_OIBWE_SLOW_POLLING("p->oibwe_slow_polling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;old_key_valid_threshold}.
     */
    P_OLD_KEY_VALID_THRESHOLD("p->old_key_valid_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;one_way_delay_params.ignore_transient_delay_error}.
     */
    P_ONE_WAY_DELAY_PARAMS_IGNORE_TRANSIENT_DELAY_ERROR("p->one_way_delay_params.ignore_transient_delay_error", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;one_way_delay_params.ts_diff_calculation_scheme}.
     */
    P_ONE_WAY_DELAY_PARAMS_TS_DIFF_CALCULATION_SCHEME("p->one_way_delay_params.ts_diff_calculation_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;opus_max_bandwidth}.
     */
    P_OPUS_MAX_BANDWIDTH("p->opus_max_bandwidth", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;opus_non_speech_bitrate}.
     */
    P_OPUS_NON_SPEECH_BITRATE("p->opus_non_speech_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;opus_vad_threshold}.
     */
    P_OPUS_VAD_THRESHOLD("p->opus_vad_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;opus_version}.
     */
    P_OPUS_VERSION("p->opus_version", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;oscillating_width_fix}.
     */
    P_OSCILLATING_WIDTH_FIX("p->oscillating_width_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;output_noise_loudness_hist_enabled}.
     */
    P_OUTPUT_NOISE_LOUDNESS_HIST_ENABLED("p->output_noise_loudness_hist_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_add_rte_reflexive_addr}.
     */
    P_P2P_ADD_RTE_REFLEXIVE_ADDR("p->p2p_add_rte_reflexive_addr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_keep_alive_force_fixed_cadence}.
     */
    P_P2P_KEEP_ALIVE_FORCE_FIXED_CADENCE("p->p2p_keep_alive_force_fixed_cadence", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_keep_alive_timeout_ms}.
     */
    P_P2P_KEEP_ALIVE_TIMEOUT_MS("p->p2p_keep_alive_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_nego_max_retry_cnt}.
     */
    P_P2P_NEGO_MAX_RETRY_CNT("p->p2p_nego_max_retry_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_request_timeout}.
     */
    P_P2P_REQUEST_TIMEOUT("p->p2p_request_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_retry_mode}.
     */
    P_P2P_RETRY_MODE("p->p2p_retry_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_retry_timeout}.
     */
    P_P2P_RETRY_TIMEOUT("p->p2p_retry_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;p2p_retry_timeout_ms_short}.
     */
    P_P2P_RETRY_TIMEOUT_MS_SHORT("p->p2p_retry_timeout_ms_short", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pacer_min_bwe_kbps_for_packet_pairs}.
     */
    P_PACER_MIN_BWE_KBPS_FOR_PACKET_PAIRS("p->pacer_min_bwe_kbps_for_packet_pairs", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pacer_percent_packet_pairs}.
     */
    P_PACER_PERCENT_PACKET_PAIRS("p->pacer_percent_packet_pairs", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;pacing_factor}.
     */
    P_PACING_FACTOR("p->pacing_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;participant_report_interval_ms}.
     */
    P_PARTICIPANT_REPORT_INTERVAL_MS("p->participant_report_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ping_ice_rtt_diff_threshold_ms}.
     */
    P_PING_ICE_RTT_DIFF_THRESHOLD_MS("p->ping_ice_rtt_diff_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ping_interval}.
     */
    P_PING_INTERVAL("p->ping_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ping_rounds}.
     */
    P_PING_ROUNDS("p->ping_rounds", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pip_mode_bitrate_estimation_action}.
     */
    P_PIP_MODE_BITRATE_ESTIMATION_ACTION("p->pip_mode_bitrate_estimation_action", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pip_sbwe_reset_max_cap}.
     */
    P_PIP_SBWE_RESET_MAX_CAP("p->pip_sbwe_reset_max_cap", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pip_update_fix}.
     */
    P_PIP_UPDATE_FIX("p->pip_update_fix", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pkt_loss_mode}.
     */
    P_PKT_LOSS_MODE("p->pkt_loss_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pkt_loss_threshold_in_milliseconds}.
     */
    P_PKT_LOSS_THRESHOLD_IN_MILLISECONDS("p->pkt_loss_threshold_in_milliseconds", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pkt_loss_threshold_in_packets}.
     */
    P_PKT_LOSS_THRESHOLD_IN_PACKETS("p->pkt_loss_threshold_in_packets", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pkt_size_thresh_bitrate}.
     */
    P_PKT_SIZE_THRESH_BITRATE("p->pkt_size_thresh_bitrate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plc_model_name}.
     */
    P_PLC_MODEL_NAME("p->plc_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;plc_type}.
     */
    P_PLC_TYPE("p->plc_type", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pli_quick_send}.
     */
    P_PLI_QUICK_SEND("p->pli_quick_send", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plr_reset_fix}.
     */
    P_PLR_RESET_FIX("p->plr_reset_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.enable_logs}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_ENABLE_LOGS("p->plrh_config.pl_det_config.enable_logs", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.enabled_mask}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_ENABLED_MASK("p->plrh_config.pl_det_config.enabled_mask", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.max_nack_info}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_MAX_NACK_INFO("p->plrh_config.pl_det_config.max_nack_info", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.algo}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_ALGO("p->plrh_config.pl_det_config.rand_loss_det_config.algo", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.cz_params .dist_adj_factor}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_CZ_PARAMS_DIST_ADJ_FACTOR("p->plrh_config.pl_det_config.rand_loss_det_config.cz_params .dist_adj_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.cz_params .rand_cluster_size}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_CZ_PARAMS_RAND_CLUSTER_SIZE("p->plrh_config.pl_det_config.rand_loss_det_config.cz_params .rand_cluster_size", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.init_eval_size}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_INIT_EVAL_SIZE("p->plrh_config.pl_det_config.rand_loss_det_config.init_eval_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.init_win_size}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_INIT_WIN_SIZE("p->plrh_config.pl_det_config.rand_loss_det_config.init_win_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.max_delta_mean}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_MAX_DELTA_MEAN("p->plrh_config.pl_det_config.rand_loss_det_config.max_delta_mean", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.max_delta_var}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_MAX_DELTA_VAR("p->plrh_config.pl_det_config.rand_loss_det_config.max_delta_var", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.min_plr}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_MIN_PLR("p->plrh_config.pl_det_config.rand_loss_det_config.min_plr", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.wait_time_ms}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_WAIT_TIME_MS("p->plrh_config.pl_det_config.rand_loss_det_config.wait_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rand_loss_det_config.win_size}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RAND_LOSS_DET_CONFIG_WIN_SIZE("p->plrh_config.pl_det_config.rand_loss_det_config.win_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.pl_det_config.rtt_ratio_rexmt}.
     */
    P_PLRH_CONFIG_PL_DET_CONFIG_RTT_RATIO_REXMT("p->plrh_config.pl_det_config.rtt_ratio_rexmt", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.rtt_config.congestion_hold_ms}.
     */
    P_PLRH_CONFIG_RTT_CONFIG_CONGESTION_HOLD_MS("p->plrh_config.rtt_config.congestion_hold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.rtt_config.congestion_rtt_ratio}.
     */
    P_PLRH_CONFIG_RTT_CONFIG_CONGESTION_RTT_RATIO("p->plrh_config.rtt_config.congestion_rtt_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.rtt_config.long_term_ema_size}.
     */
    P_PLRH_CONFIG_RTT_CONFIG_LONG_TERM_EMA_SIZE("p->plrh_config.rtt_config.long_term_ema_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.rtt_config.min_rtt_update_time_ms}.
     */
    P_PLRH_CONFIG_RTT_CONFIG_MIN_RTT_UPDATE_TIME_MS("p->plrh_config.rtt_config.min_rtt_update_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;plrh_config.rtt_config.short_term_ema_size}.
     */
    P_PLRH_CONFIG_RTT_CONFIG_SHORT_TERM_EMA_SIZE("p->plrh_config.rtt_config.short_term_ema_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;polling_ms}.
     */
    P_POLLING_MS("p->polling_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pp_ceiling_stat}.
     */
    P_PP_CEILING_STAT("p->pp_ceiling_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pp_est_max_age}.
     */
    P_PP_EST_MAX_AGE("p->pp_est_max_age", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pp_flip_count_enabled}.
     */
    P_PP_FLIP_COUNT_ENABLED("p->pp_flip_count_enabled", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;pp_flip_count_for_hd_hi_threshold}.
     */
    P_PP_FLIP_COUNT_FOR_HD_HI_THRESHOLD("p->pp_flip_count_for_hd_hi_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pp_flip_count_for_hd_lo_threshold}.
     */
    P_PP_FLIP_COUNT_FOR_HD_LO_THRESHOLD("p->pp_flip_count_for_hd_lo_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;pp_flip_count_skip_sanity_check}.
     */
    P_PP_FLIP_COUNT_SKIP_SANITY_CHECK("p->pp_flip_count_skip_sanity_check", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;pp_noise_to_mean_ratio}.
     */
    P_PP_NOISE_TO_MEAN_RATIO("p->pp_noise_to_mean_ratio", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pp_slide_window_size}.
     */
    P_PP_SLIDE_WINDOW_SIZE("p->pp_slide_window_size", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;pp_udst_min_link_cap_bps}.
     */
    P_PP_UDST_MIN_LINK_CAP_BPS("p->pp_udst_min_link_cap_bps", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pp_udst_peer_vid_rx_to_sbwe_pcnt}.
     */
    P_PP_UDST_PEER_VID_RX_TO_SBWE_PCNT("p->pp_udst_peer_vid_rx_to_sbwe_pcnt", VoipParamType.INTEGER, 4, true),

    /**
     * Native descriptor for {@code p-&gt;pp_udst_sbwe_to_link_cap_ratio}.
     */
    P_PP_UDST_SBWE_TO_LINK_CAP_RATIO("p->pp_udst_sbwe_to_link_cap_ratio", VoipParamType.FLOAT, 4, true),

    /**
     * Native descriptor for {@code p-&gt;prefer_pp_udst_source}.
     */
    P_PREFER_PP_UDST_SOURCE("p->prefer_pp_udst_source", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;probation_min_sequential}.
     */
    P_PROBATION_MIN_SEQUENTIAL("p->probation_min_sequential", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;probing_pkt_size}.
     */
    P_PROBING_PKT_SIZE("p->probing_pkt_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;probing_req_timeout_ms}.
     */
    P_PROBING_REQ_TIMEOUT_MS("p->probing_req_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;probing_res_timeout_ms}.
     */
    P_PROBING_RES_TIMEOUT_MS("p->probing_res_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_model_name}.
     */
    P_QUICKHD_ML_MODEL_NAME("p->quickhd_ml_model_name", VoipParamType.STRING, 64, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_num_features}.
     */
    P_QUICKHD_ML_NUM_FEATURES("p->quickhd_ml_num_features", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_ramp_up_option}.
     */
    P_QUICKHD_ML_RAMP_UP_OPTION("p->quickhd_ml_ramp_up_option", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_ramp_up_ratio}.
     */
    P_QUICKHD_ML_RAMP_UP_RATIO("p->quickhd_ml_ramp_up_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_target_hd_bps}.
     */
    P_QUICKHD_ML_TARGET_HD_BPS("p->quickhd_ml_target_hd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_target_hd_min_bps}.
     */
    P_QUICKHD_ML_TARGET_HD_MIN_BPS("p->quickhd_ml_target_hd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_target_sd_bps}.
     */
    P_QUICKHD_ML_TARGET_SD_BPS("p->quickhd_ml_target_sd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_ml_target_sd_min_bps}.
     */
    P_QUICKHD_ML_TARGET_SD_MIN_BPS("p->quickhd_ml_target_sd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slc_hd_prob_threshold}.
     */
    P_QUICKHD_SLC_HD_PROB_THRESHOLD("p->quickhd_slc_hd_prob_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slc_sd_prob_threshold}.
     */
    P_QUICKHD_SLC_SD_PROB_THRESHOLD("p->quickhd_slc_sd_prob_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slc_target_hd_bps}.
     */
    P_QUICKHD_SLC_TARGET_HD_BPS("p->quickhd_slc_target_hd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slc_target_sd_bps}.
     */
    P_QUICKHD_SLC_TARGET_SD_BPS("p->quickhd_slc_target_sd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_goodput_coeff}.
     */
    P_QUICKHD_SLR_GOODPUT_COEFF("p->quickhd_slr_goodput_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_hist_coeff}.
     */
    P_QUICKHD_SLR_HIST_COEFF("p->quickhd_slr_hist_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_hist_fix}.
     */
    P_QUICKHD_SLR_HIST_FIX("p->quickhd_slr_hist_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_hist_min_bps}.
     */
    P_QUICKHD_SLR_HIST_MIN_BPS("p->quickhd_slr_hist_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_init_bwe_coeff}.
     */
    P_QUICKHD_SLR_INIT_BWE_COEFF("p->quickhd_slr_init_bwe_coeff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_offset}.
     */
    P_QUICKHD_SLR_OFFSET("p->quickhd_slr_offset", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_ramp_up_option}.
     */
    P_QUICKHD_SLR_RAMP_UP_OPTION("p->quickhd_slr_ramp_up_option", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_ramp_up_ratio}.
     */
    P_QUICKHD_SLR_RAMP_UP_RATIO("p->quickhd_slr_ramp_up_ratio", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_target_hd_bps}.
     */
    P_QUICKHD_SLR_TARGET_HD_BPS("p->quickhd_slr_target_hd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_target_hd_min_bps}.
     */
    P_QUICKHD_SLR_TARGET_HD_MIN_BPS("p->quickhd_slr_target_hd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_target_sd_bps}.
     */
    P_QUICKHD_SLR_TARGET_SD_BPS("p->quickhd_slr_target_sd_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;quickhd_slr_target_sd_min_bps}.
     */
    P_QUICKHD_SLR_TARGET_SD_MIN_BPS("p->quickhd_slr_target_sd_min_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_av1_idr}.
     */
    P_RAMP_DOWN_AV1_IDR("p->ramp_down_av1_idr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_av1_maxqp}.
     */
    P_RAMP_DOWN_AV1_MAXQP("p->ramp_down_av1_maxqp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_av1_overshoot}.
     */
    P_RAMP_DOWN_AV1_OVERSHOOT("p->ramp_down_av1_overshoot", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_av1_undershoot}.
     */
    P_RAMP_DOWN_AV1_UNDERSHOOT("p->ramp_down_av1_undershoot", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_openh264_idr}.
     */
    P_RAMP_DOWN_OPENH264_IDR("p->ramp_down_openh264_idr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_openh264_maxqp}.
     */
    P_RAMP_DOWN_OPENH264_MAXQP("p->ramp_down_openh264_maxqp", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code p-&gt;ramp_down_switches_threshold}.
     */
    P_RAMP_DOWN_SWITCHES_THRESHOLD("p->ramp_down_switches_threshold", VoipParamType.INTEGER, 4, false);

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
    VoipParamKeyCall2(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {
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
