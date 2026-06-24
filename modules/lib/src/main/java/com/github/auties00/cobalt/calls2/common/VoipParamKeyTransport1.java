package com.github.auties00.cobalt.calls2.common;

/**
 * A partition of the {@code tp-&gt;} voip-param registry keys.
 *
 * <p>This enum exists only to keep its generated static initializer within the JVM 64KB
 * method-size limit; callers iterate the full key set through {@link VoipParamKey#values()}
 * rather than this partition directly.
 */
enum VoipParamKeyTransport1 implements VoipParamKey {
    /**
     * Native descriptor for {@code tp-&gt;alloc_error_handling.alloc_err_bitmap}.
     */
    TP_ALLOC_ERROR_HANDLING_ALLOC_ERR_BITMAP("tp->alloc_error_handling.alloc_err_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alloc_error_handling.enable}.
     */
    TP_ALLOC_ERROR_HANDLING_ENABLE("tp->alloc_error_handling.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alloc_error_handling.enable_relay_latency_cnt_fix}.
     */
    TP_ALLOC_ERROR_HANDLING_ENABLE_RELAY_LATENCY_CNT_FIX("tp->alloc_error_handling.enable_relay_latency_cnt_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alloc_error_handling.unauth_src_addr_timeout_ms}.
     */
    TP_ALLOC_ERROR_HANDLING_UNAUTH_SRC_ADDR_TIMEOUT_MS("tp->alloc_error_handling.unauth_src_addr_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_af_check_max_default_af_relay_bind_time_ms}.
     */
    TP_ALT_AF_CHECK_MAX_DEFAULT_AF_RELAY_BIND_TIME_MS("tp->alt_af_check_max_default_af_relay_bind_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_af_ip_retrieval.enable}.
     */
    TP_ALT_AF_IP_RETRIEVAL_ENABLE("tp->alt_af_ip_retrieval.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_af_ip_retrieval.max_probe_alloc_retries}.
     */
    TP_ALT_AF_IP_RETRIEVAL_MAX_PROBE_ALLOC_RETRIES("tp->alt_af_ip_retrieval.max_probe_alloc_retries", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_af_ip_retrieval.probe_alloc_interval_ms}.
     */
    TP_ALT_AF_IP_RETRIEVAL_PROBE_ALLOC_INTERVAL_MS("tp->alt_af_ip_retrieval.probe_alloc_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.allow_alt_net_strategy}.
     */
    TP_ALT_NET_ALLOW_ALT_NET_STRATEGY("tp->alt_net.allow_alt_net_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.alt_net_ping_burst}.
     */
    TP_ALT_NET_ALT_NET_PING_BURST("tp->alt_net.alt_net_ping_burst", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.alt_net_switch_opt_bitmap}.
     */
    TP_ALT_NET_ALT_NET_SWITCH_OPT_BITMAP("tp->alt_net.alt_net_switch_opt_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.alt_net_switch_opt_min_sample}.
     */
    TP_ALT_NET_ALT_NET_SWITCH_OPT_MIN_SAMPLE("tp->alt_net.alt_net_switch_opt_min_sample", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.bandwidth_min_sample}.
     */
    TP_ALT_NET_BANDWIDTH_MIN_SAMPLE("tp->alt_net.bandwidth_min_sample", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.bandwidth_threshold_kbps}.
     */
    TP_ALT_NET_BANDWIDTH_THRESHOLD_KBPS("tp->alt_net.bandwidth_threshold_kbps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.check_active_net_interface}.
     */
    TP_ALT_NET_CHECK_ACTIVE_NET_INTERFACE("tp->alt_net.check_active_net_interface", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.check_active_net_interval_in_msec}.
     */
    TP_ALT_NET_CHECK_ACTIVE_NET_INTERVAL_IN_MSEC("tp->alt_net.check_active_net_interval_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.check_alt_net_state_bitmap}.
     */
    TP_ALT_NET_CHECK_ALT_NET_STATE_BITMAP("tp->alt_net.check_alt_net_state_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.connected_fix_bitmap}.
     */
    TP_ALT_NET_CONNECTED_FIX_BITMAP("tp->alt_net.connected_fix_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.elapsed_msec_since_last_pong_threshold}.
     */
    TP_ALT_NET_ELAPSED_MSEC_SINCE_LAST_PONG_THRESHOLD("tp->alt_net.elapsed_msec_since_last_pong_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.elapsed_msec_since_last_rtp_threshold}.
     */
    TP_ALT_NET_ELAPSED_MSEC_SINCE_LAST_RTP_THRESHOLD("tp->alt_net.elapsed_msec_since_last_rtp_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ignore_hostile_network}.
     */
    TP_ALT_NET_IGNORE_HOSTILE_NETWORK("tp->alt_net.ignore_hostile_network", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.loss_period_consecutive_trigger_threshold}.
     */
    TP_ALT_NET_LOSS_PERIOD_CONSECUTIVE_TRIGGER_THRESHOLD("tp->alt_net.loss_period_consecutive_trigger_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.non_def_cell_data_limit_in_kbytes}.
     */
    TP_ALT_NET_NON_DEF_CELL_DATA_LIMIT_IN_KBYTES("tp->alt_net.non_def_cell_data_limit_in_kbytes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_fast_check_interval_ms}.
     */
    TP_ALT_NET_PING_ALT_FAST_CHECK_INTERVAL_MS("tp->alt_net.ping_alt_fast_check_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_fast_check_start_ms}.
     */
    TP_ALT_NET_PING_ALT_FAST_CHECK_START_MS("tp->alt_net.ping_alt_fast_check_start_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_payload_size}.
     */
    TP_ALT_NET_PING_ALT_PAYLOAD_SIZE("tp->alt_net.ping_alt_payload_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_round_total}.
     */
    TP_ALT_NET_PING_ALT_ROUND_TOTAL("tp->alt_net.ping_alt_round_total", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_threshold_10x_mos}.
     */
    TP_ALT_NET_PING_ALT_THRESHOLD_10X_MOS("tp->alt_net.ping_alt_threshold_10x_mos", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_threshold_loss}.
     */
    TP_ALT_NET_PING_ALT_THRESHOLD_LOSS("tp->alt_net.ping_alt_threshold_loss", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_time_between_rounds_in_msec}.
     */
    TP_ALT_NET_PING_ALT_TIME_BETWEEN_ROUNDS_IN_MSEC("tp->alt_net.ping_alt_time_between_rounds_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_alt_timeout_in_msec}.
     */
    TP_ALT_NET_PING_ALT_TIMEOUT_IN_MSEC("tp->alt_net.ping_alt_timeout_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_def_payload_size}.
     */
    TP_ALT_NET_PING_DEF_PAYLOAD_SIZE("tp->alt_net.ping_def_payload_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_def_threshold_10x_mos}.
     */
    TP_ALT_NET_PING_DEF_THRESHOLD_10X_MOS("tp->alt_net.ping_def_threshold_10x_mos", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_def_threshold_loss}.
     */
    TP_ALT_NET_PING_DEF_THRESHOLD_LOSS("tp->alt_net.ping_def_threshold_loss", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.ping_loss_diff_threshold}.
     */
    TP_ALT_NET_PING_LOSS_DIFF_THRESHOLD("tp->alt_net.ping_loss_diff_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.recreate_socket_on_active_addr}.
     */
    TP_ALT_NET_RECREATE_SOCKET_ON_ACTIVE_ADDR("tp->alt_net.recreate_socket_on_active_addr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.request_net_opt_bitmap}.
     */
    TP_ALT_NET_REQUEST_NET_OPT_BITMAP("tp->alt_net.request_net_opt_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.reset_protocol_on_switch_connection}.
     */
    TP_ALT_NET_RESET_PROTOCOL_ON_SWITCH_CONNECTION("tp->alt_net.reset_protocol_on_switch_connection", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.skip_alt_net_last_pong_internal_ms}.
     */
    TP_ALT_NET_SKIP_ALT_NET_LAST_PONG_INTERNAL_MS("tp->alt_net.skip_alt_net_last_pong_internal_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.skip_local_ip}.
     */
    TP_ALT_NET_SKIP_LOCAL_IP("tp->alt_net.skip_local_ip", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.skip_stay_on_alt_net_traffic_gap_ms}.
     */
    TP_ALT_NET_SKIP_STAY_ON_ALT_NET_TRAFFIC_GAP_MS("tp->alt_net.skip_stay_on_alt_net_traffic_gap_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.stay_on_alt_net_fix_bitmap}.
     */
    TP_ALT_NET_STAY_ON_ALT_NET_FIX_BITMAP("tp->alt_net.stay_on_alt_net_fix_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.stay_on_alt_net_interface}.
     */
    TP_ALT_NET_STAY_ON_ALT_NET_INTERFACE("tp->alt_net.stay_on_alt_net_interface", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.support_ipv6_bitmap}.
     */
    TP_ALT_NET_SUPPORT_IPV6_BITMAP("tp->alt_net.support_ipv6_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.switch_network_jitter_threshold}.
     */
    TP_ALT_NET_SWITCH_NETWORK_JITTER_THRESHOLD("tp->alt_net.switch_network_jitter_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.switch_network_plr_threshold}.
     */
    TP_ALT_NET_SWITCH_NETWORK_PLR_THRESHOLD("tp->alt_net.switch_network_plr_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_alt_net_interval_in_msec}.
     */
    TP_ALT_NET_TEST_ALT_NET_INTERVAL_IN_MSEC("tp->alt_net.test_alt_net_interval_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_block_strategy}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_BLOCK_STRATEGY("tp->alt_net.test_default_net_block_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_block_switch_count}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_BLOCK_SWITCH_COUNT("tp->alt_net.test_default_net_block_switch_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_block_time_ms}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_BLOCK_TIME_MS("tp->alt_net.test_default_net_block_time_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_check_gap_after_first_media}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_CHECK_GAP_AFTER_FIRST_MEDIA("tp->alt_net.test_default_net_check_gap_after_first_media", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_immed_traffic_gap_ms}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_IMMED_TRAFFIC_GAP_MS("tp->alt_net.test_default_net_immed_traffic_gap_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.test_default_net_interval_in_msec}.
     */
    TP_ALT_NET_TEST_DEFAULT_NET_INTERVAL_IN_MSEC("tp->alt_net.test_default_net_interval_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.trigger_weak_wifi_on_tcp_delay_ms}.
     */
    TP_ALT_NET_TRIGGER_WEAK_WIFI_ON_TCP_DELAY_MS("tp->alt_net.trigger_weak_wifi_on_tcp_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.use_alt_net_interface}.
     */
    TP_ALT_NET_USE_ALT_NET_INTERFACE("tp->alt_net.use_alt_net_interface", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;alt_net.use_ping_loss_diff}.
     */
    TP_ALT_NET_USE_PING_LOSS_DIFF("tp->alt_net.use_ping_loss_diff", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;app_data_add_ssrc_to_stream_descriptor}.
     */
    TP_APP_DATA_ADD_SSRC_TO_STREAM_DESCRIPTOR("tp->app_data_add_ssrc_to_stream_descriptor", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;assert_on_mutex_timeout}.
     */
    TP_ASSERT_ON_MUTEX_TIMEOUT("tp->assert_on_mutex_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;bot_call_delay_caller_relay_lat}.
     */
    TP_BOT_CALL_DELAY_CALLER_RELAY_LAT("tp->bot_call_delay_caller_relay_lat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;bot_call_send_relay_lat_trigger_bitmap}.
     */
    TP_BOT_CALL_SEND_RELAY_LAT_TRIGGER_BITMAP("tp->bot_call_send_relay_lat_trigger_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;call_connect_stat_type_bitmap}.
     */
    TP_CALL_CONNECT_STAT_TYPE_BITMAP("tp->call_connect_stat_type_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;client_relay_election_latency_threshold_ms}.
     */
    TP_CLIENT_RELAY_ELECTION_LATENCY_THRESHOLD_MS("tp->client_relay_election_latency_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;client_relay_election_strategy}.
     */
    TP_CLIENT_RELAY_ELECTION_STRATEGY("tp->client_relay_election_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;cross_network_medium_p2p_policy.enable}.
     */
    TP_CROSS_NETWORK_MEDIUM_P2P_POLICY_ENABLE("tp->cross_network_medium_p2p_policy.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;cross_network_medium_p2p_policy.max_ms_since_last_rx}.
     */
    TP_CROSS_NETWORK_MEDIUM_P2P_POLICY_MAX_MS_SINCE_LAST_RX("tp->cross_network_medium_p2p_policy.max_ms_since_last_rx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;deprecate_conn_local_cand}.
     */
    TP_DEPRECATE_CONN_LOCAL_CAND("tp->deprecate_conn_local_cand", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;destroy_warp_mcs_rbwe}.
     */
    TP_DESTROY_WARP_MCS_RBWE("tp->destroy_warp_mcs_rbwe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;disable_cre_bias_strategy}.
     */
    TP_DISABLE_CRE_BIAS_STRATEGY("tp->disable_cre_bias_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;disable_p2p_on_local_net_mode}.
     */
    TP_DISABLE_P2P_ON_LOCAL_NET_MODE("tp->disable_p2p_on_local_net_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;disable_p2p_only_on_same_subnet}.
     */
    TP_DISABLE_P2P_ONLY_ON_SAME_SUBNET("tp->disable_p2p_only_on_same_subnet", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;disable_p2p_transport}.
     */
    TP_DISABLE_P2P_TRANSPORT("tp->disable_p2p_transport", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;disable_ssrc_subscription}.
     */
    TP_DISABLE_SSRC_SUBSCRIPTION("tp->disable_ssrc_subscription", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.bwe_check.bwe_check_interval_ms}.
     */
    TP_DT_BWE_CHECK_BWE_CHECK_INTERVAL_MS("tp->dt.bwe_check.bwe_check_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.bwe_check.bwe_ema_min_sample_cnt}.
     */
    TP_DT_BWE_CHECK_BWE_EMA_MIN_SAMPLE_CNT("tp->dt.bwe_check.bwe_ema_min_sample_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.bwe_check.check_remote_bwe}.
     */
    TP_DT_BWE_CHECK_CHECK_REMOTE_BWE("tp->dt.bwe_check.check_remote_bwe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.bwe_check.min_audio_bwe_bps}.
     */
    TP_DT_BWE_CHECK_MIN_AUDIO_BWE_BPS("tp->dt.bwe_check.min_audio_bwe_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.bwe_check.min_video_bwe_bps}.
     */
    TP_DT_BWE_CHECK_MIN_VIDEO_BWE_BPS("tp->dt.bwe_check.min_video_bwe_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.clear_history_on_running}.
     */
    TP_DT_CLEAR_HISTORY_ON_RUNNING("tp->dt.clear_history_on_running", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.disable_prefer_relay_when_sampled}.
     */
    TP_DT_DISABLE_PREFER_RELAY_WHEN_SAMPLED("tp->dt.disable_prefer_relay_when_sampled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.disable_sts_switch_when_sampled}.
     */
    TP_DT_DISABLE_STS_SWITCH_WHEN_SAMPLED("tp->dt.disable_sts_switch_when_sampled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.enable_light_weight_p2p_switch}.
     */
    TP_DT_ENABLE_LIGHT_WEIGHT_P2P_SWITCH("tp->dt.enable_light_weight_p2p_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.enable_logging_avg_stat}.
     */
    TP_DT_ENABLE_LOGGING_AVG_STAT("tp->dt.enable_logging_avg_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.enable_prefer_relay_for_snadl}.
     */
    TP_DT_ENABLE_PREFER_RELAY_FOR_SNADL("tp->dt.enable_prefer_relay_for_snadl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.af_check_period_ms}.
     */
    TP_DT_IP_VER_OPTIMIZATION_AF_CHECK_PERIOD_MS("tp->dt.ip_ver_optimization.af_check_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.disable_af_switch}.
     */
    TP_DT_IP_VER_OPTIMIZATION_DISABLE_AF_SWITCH("tp->dt.ip_ver_optimization.disable_af_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.enable}.
     */
    TP_DT_IP_VER_OPTIMIZATION_ENABLE("tp->dt.ip_ver_optimization.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.max_af_check_period_ms}.
     */
    TP_DT_IP_VER_OPTIMIZATION_MAX_AF_CHECK_PERIOD_MS("tp->dt.ip_ver_optimization.max_af_check_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.max_af_check_stop_period_ms}.
     */
    TP_DT_IP_VER_OPTIMIZATION_MAX_AF_CHECK_STOP_PERIOD_MS("tp->dt.ip_ver_optimization.max_af_check_stop_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.max_af_switch_cnt}.
     */
    TP_DT_IP_VER_OPTIMIZATION_MAX_AF_SWITCH_CNT("tp->dt.ip_ver_optimization.max_af_switch_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.min_mos_diff}.
     */
    TP_DT_IP_VER_OPTIMIZATION_MIN_MOS_DIFF("tp->dt.ip_ver_optimization.min_mos_diff", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.prefer_v4}.
     */
    TP_DT_IP_VER_OPTIMIZATION_PREFER_V4("tp->dt.ip_ver_optimization.prefer_v4", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.reset_relay_when_switch}.
     */
    TP_DT_IP_VER_OPTIMIZATION_RESET_RELAY_WHEN_SWITCH("tp->dt.ip_ver_optimization.reset_relay_when_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.supported_net_medium_bitmap}.
     */
    TP_DT_IP_VER_OPTIMIZATION_SUPPORTED_NET_MEDIUM_BITMAP("tp->dt.ip_ver_optimization.supported_net_medium_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.ip_ver_optimization.switch_af_only_when_using_relay}.
     */
    TP_DT_IP_VER_OPTIMIZATION_SWITCH_AF_ONLY_WHEN_USING_RELAY("tp->dt.ip_ver_optimization.switch_af_only_when_using_relay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.min_rtt_diff_for_nadl}.
     */
    TP_DT_MIN_RTT_DIFF_FOR_NADL("tp->dt.min_rtt_diff_for_nadl", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_enable_transport_switch}.
     */
    TP_DT_NETWORK_OPTIMIZER_ENABLE_TRANSPORT_SWITCH("tp->dt.network_optimizer_enable_transport_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_max_transport_switch_cnt}.
     */
    TP_DT_NETWORK_OPTIMIZER_MAX_TRANSPORT_SWITCH_CNT("tp->dt.network_optimizer_max_transport_switch_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_min_mos_diff}.
     */
    TP_DT_NETWORK_OPTIMIZER_MIN_MOS_DIFF("tp->dt.network_optimizer_min_mos_diff", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_min_plr_diff}.
     */
    TP_DT_NETWORK_OPTIMIZER_MIN_PLR_DIFF("tp->dt.network_optimizer_min_plr_diff", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_transport_switch_sample_size}.
     */
    TP_DT_NETWORK_OPTIMIZER_TRANSPORT_SWITCH_SAMPLE_SIZE("tp->dt.network_optimizer_transport_switch_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_optimizer_transport_switch_threshold}.
     */
    TP_DT_NETWORK_OPTIMIZER_TRANSPORT_SWITCH_THRESHOLD("tp->dt.network_optimizer_transport_switch_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_active_transport_rx_sample_period_ms}.
     */
    TP_DT_NETWORK_QUALITY_ACTIVE_TRANSPORT_RX_SAMPLE_PERIOD_MS("tp->dt.network_quality_active_transport_rx_sample_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_bind_loss_sample_size}.
     */
    TP_DT_NETWORK_QUALITY_BIND_LOSS_SAMPLE_SIZE("tp->dt.network_quality_bind_loss_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_bind_loss_timeout_ms}.
     */
    TP_DT_NETWORK_QUALITY_BIND_LOSS_TIMEOUT_MS("tp->dt.network_quality_bind_loss_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_check_bind_period_ms}.
     */
    TP_DT_NETWORK_QUALITY_CHECK_BIND_PERIOD_MS("tp->dt.network_quality_check_bind_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_check_bind_period_ms_sampled}.
     */
    TP_DT_NETWORK_QUALITY_CHECK_BIND_PERIOD_MS_SAMPLED("tp->dt.network_quality_check_bind_period_ms_sampled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_check_mode}.
     */
    TP_DT_NETWORK_QUALITY_CHECK_MODE("tp->dt.network_quality_check_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_metric_log_period_ms}.
     */
    TP_DT_NETWORK_QUALITY_METRIC_LOG_PERIOD_MS("tp->dt.network_quality_metric_log_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_min_sampling_period_ms}.
     */
    TP_DT_NETWORK_QUALITY_MIN_SAMPLING_PERIOD_MS("tp->dt.network_quality_min_sampling_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_plr_stat_ema_alpha}.
     */
    TP_DT_NETWORK_QUALITY_PLR_STAT_EMA_ALPHA("tp->dt.network_quality_plr_stat_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.network_quality_stat_ema_alpha}.
     */
    TP_DT_NETWORK_QUALITY_STAT_EMA_ALPHA("tp->dt.network_quality_stat_ema_alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.optimizer_call_side}.
     */
    TP_DT_OPTIMIZER_CALL_SIDE("tp->dt.optimizer_call_side", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.probe_sample_overrides.p2p_retry_mode}.
     */
    TP_DT_PROBE_SAMPLE_OVERRIDES_P2P_RETRY_MODE("tp->dt.probe_sample_overrides.p2p_retry_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.probe_sample_overrides.p2p_retry_timeout}.
     */
    TP_DT_PROBE_SAMPLE_OVERRIDES_P2P_RETRY_TIMEOUT("tp->dt.probe_sample_overrides.p2p_retry_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.probing_call_side}.
     */
    TP_DT_PROBING_CALL_SIDE("tp->dt.probing_call_side", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.random_probe_sample_rate}.
     */
    TP_DT_RANDOM_PROBE_SAMPLE_RATE("tp->dt.random_probe_sample_rate", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.relay_plr_discount_factor_for_nadl}.
     */
    TP_DT_RELAY_PLR_DISCOUNT_FACTOR_FOR_NADL("tp->dt.relay_plr_discount_factor_for_nadl", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.supported_protocol_bitmap}.
     */
    TP_DT_SUPPORTED_PROTOCOL_BITMAP("tp->dt.supported_protocol_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;dt.transport_comparison_mode}.
     */
    TP_DT_TRANSPORT_COMPARISON_MODE("tp->dt.transport_comparison_mode", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_alloc_err_mi_chk}.
     */
    TP_ENABLE_ALLOC_ERR_MI_CHK("tp->enable_alloc_err_mi_chk", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_alt_af_other_addr_valid_check}.
     */
    TP_ENABLE_ALT_AF_OTHER_ADDR_VALID_CHECK("tp->enable_alt_af_other_addr_valid_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_always_handle_p2p_probe_only}.
     */
    TP_ENABLE_ALWAYS_HANDLE_P2P_PROBE_ONLY("tp->enable_always_handle_p2p_probe_only", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_always_handle_p2p_stun}.
     */
    TP_ENABLE_ALWAYS_HANDLE_P2P_STUN("tp->enable_always_handle_p2p_stun", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_aud_over_p2p_in_vid_call}.
     */
    TP_ENABLE_AUD_OVER_P2P_IN_VID_CALL("tp->enable_aud_over_p2p_in_vid_call", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_aud_tx_traffic_started_event}.
     */
    TP_ENABLE_AUD_TX_TRAFFIC_STARTED_EVENT("tp->enable_aud_tx_traffic_started_event", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_cell_signal_optimizations}.
     */
    TP_ENABLE_CELL_SIGNAL_OPTIMIZATIONS("tp->enable_cell_signal_optimizations", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_crash_fix_bitmap}.
     */
    TP_ENABLE_CRASH_FIX_BITMAP("tp->enable_crash_fix_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_destroy_init_bwe_fix}.
     */
    TP_ENABLE_DESTROY_INIT_BWE_FIX("tp->enable_destroy_init_bwe_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_e2e_bind_probe_bitmap}.
     */
    TP_ENABLE_E2E_BIND_PROBE_BITMAP("tp->enable_e2e_bind_probe_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_early_impl_accept_optimization}.
     */
    TP_ENABLE_EARLY_IMPL_ACCEPT_OPTIMIZATION("tp->enable_early_impl_accept_optimization", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_get_host_cand_v2}.
     */
    TP_ENABLE_GET_HOST_CAND_V2("tp->enable_get_host_cand_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_hbh_pli_cer_fix}.
     */
    TP_ENABLE_HBH_PLI_CER_FIX("tp->enable_hbh_pli_cer_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_hbh_rtcp_is_video_override}.
     */
    TP_ENABLE_HBH_RTCP_IS_VIDEO_OVERRIDE("tp->enable_hbh_rtcp_is_video_override", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_hbh_srtp_rtp_index_signaling_req_bitmap}.
     */
    TP_ENABLE_HBH_SRTP_RTP_INDEX_SIGNALING_REQ_BITMAP("tp->enable_hbh_srtp_rtp_index_signaling_req_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_hbh_warp_mi_req_bitmap}.
     */
    TP_ENABLE_HBH_WARP_MI_REQ_BITMAP("tp->enable_hbh_warp_mi_req_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_immediate_subs_bind_inc_retry}.
     */
    TP_ENABLE_IMMEDIATE_SUBS_BIND_INC_RETRY("tp->enable_immediate_subs_bind_inc_retry", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_implicit_relay_election_on_rx_dtls}.
     */
    TP_ENABLE_IMPLICIT_RELAY_ELECTION_ON_RX_DTLS("tp->enable_implicit_relay_election_on_rx_dtls", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_implicit_relay_election_on_rx_pkts}.
     */
    TP_ENABLE_IMPLICIT_RELAY_ELECTION_ON_RX_PKTS("tp->enable_implicit_relay_election_on_rx_pkts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_l4s_scalable_cc}.
     */
    TP_ENABLE_L4S_SCALABLE_CC("tp->enable_l4s_scalable_cc", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_network_medium_attr}.
     */
    TP_ENABLE_NETWORK_MEDIUM_ATTR("tp->enable_network_medium_attr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_new_p2p_priority}.
     */
    TP_ENABLE_NEW_P2P_PRIORITY("tp->enable_new_p2p_priority", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_p2p_after_relay_fail}.
     */
    TP_ENABLE_P2P_AFTER_RELAY_FAIL("tp->enable_p2p_after_relay_fail", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_parallel_af_check}.
     */
    TP_ENABLE_PARALLEL_AF_CHECK("tp->enable_parallel_af_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_peer_local_ip_prefix_fs_L1410938PRV}.
     */
    TP_ENABLE_PEER_LOCAL_IP_PREFIX_FS_L1410938_PRV("tp->enable_peer_local_ip_prefix_fs_L1410938PRV", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_process_server_prefer_ipv6}.
     */
    TP_ENABLE_PROCESS_SERVER_PREFER_IPV6("tp->enable_process_server_prefer_ipv6", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_reason_code_bitmap}.
     */
    TP_ENABLE_REASON_CODE_BITMAP("tp->enable_reason_code_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_rebind_all_bitmap}.
     */
    TP_ENABLE_REBIND_ALL_BITMAP("tp->enable_rebind_all_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_refactored_integrity_for_ping}.
     */
    TP_ENABLE_REFACTORED_INTEGRITY_FOR_PING("tp->enable_refactored_integrity_for_ping", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_refl_addr_signaling}.
     */
    TP_ENABLE_REFL_ADDR_SIGNALING("tp->enable_refl_addr_signaling", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_relay_idx_from_name}.
     */
    TP_ENABLE_RELAY_IDX_FROM_NAME("tp->enable_relay_idx_from_name", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_save_remote_cand_private_addr}.
     */
    TP_ENABLE_SAVE_REMOTE_CAND_PRIVATE_ADDR("tp->enable_save_remote_cand_private_addr", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_send_call_connect_stat}.
     */
    TP_ENABLE_SEND_CALL_CONNECT_STAT("tp->enable_send_call_connect_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_send_rtp_p2p_crash_fix}.
     */
    TP_ENABLE_SEND_RTP_P2P_CRASH_FIX("tp->enable_send_rtp_p2p_crash_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_signaling_network_probe}.
     */
    TP_ENABLE_SIGNALING_NETWORK_PROBE("tp->enable_signaling_network_probe", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_signaling_probe_response}.
     */
    TP_ENABLE_SIGNALING_PROBE_RESPONSE("tp->enable_signaling_probe_response", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_single_ip_relay_fix}.
     */
    TP_ENABLE_SINGLE_IP_RELAY_FIX("tp->enable_single_ip_relay_fix", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_sl_optimizations}.
     */
    TP_ENABLE_SL_OPTIMIZATIONS("tp->enable_sl_optimizations", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_standalone_warp_pr_to_reset_cer_ts}.
     */
    TP_ENABLE_STANDALONE_WARP_PR_TO_RESET_CER_TS("tp->enable_standalone_warp_pr_to_reset_cer_ts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_standalone_warp_pr_to_reset_rx_media_ts}.
     */
    TP_ENABLE_STANDALONE_WARP_PR_TO_RESET_RX_MEDIA_TS("tp->enable_standalone_warp_pr_to_reset_rx_media_ts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_strict_stun_tid_check}.
     */
    TP_ENABLE_STRICT_STUN_TID_CHECK("tp->enable_strict_stun_tid_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_stun_mapped_addr_af_fix}.
     */
    TP_ENABLE_STUN_MAPPED_ADDR_AF_FIX("tp->enable_stun_mapped_addr_af_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_sym_nat_bug_fix}.
     */
    TP_ENABLE_SYM_NAT_BUG_FIX("tp->enable_sym_nat_bug_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_transport_fs_L1320265PRV}.
     */
    TP_ENABLE_TRANSPORT_FS_L1320265_PRV("tp->enable_transport_fs_L1320265PRV", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_ts_logger_mutex}.
     */
    TP_ENABLE_TS_LOGGER_MUTEX("tp->enable_ts_logger_mutex", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_tx_packet_cache_result_cache}.
     */
    TP_ENABLE_TX_PACKET_CACHE_RESULT_CACHE("tp->enable_tx_packet_cache_result_cache", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_tx_packet_cache_ssrc}.
     */
    TP_ENABLE_TX_PACKET_CACHE_SSRC("tp->enable_tx_packet_cache_ssrc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_tx_traffic_enc_check}.
     */
    TP_ENABLE_TX_TRAFFIC_ENC_CHECK("tp->enable_tx_traffic_enc_check", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_type_only_signaling_probe}.
     */
    TP_ENABLE_TYPE_ONLY_SIGNALING_PROBE("tp->enable_type_only_signaling_probe", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_uic_coex_crash_fix}.
     */
    TP_ENABLE_UIC_COEX_CRASH_FIX("tp->enable_uic_coex_crash_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_update_min_latency_relay_idx_fix}.
     */
    TP_ENABLE_UPDATE_MIN_LATENCY_RELAY_IDX_FIX("tp->enable_update_min_latency_relay_idx_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_vpn_interface_used_field_stat}.
     */
    TP_ENABLE_VPN_INTERFACE_USED_FIELD_STAT("tp->enable_vpn_interface_used_field_stat", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_wa_asock_cfg_create_external_sock}.
     */
    TP_ENABLE_WA_ASOCK_CFG_CREATE_EXTERNAL_SOCK("tp->enable_wa_asock_cfg_create_external_sock", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_weak_wifi_in_tcp}.
     */
    TP_ENABLE_WEAK_WIFI_IN_TCP("tp->enable_weak_wifi_in_tcp", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;enable_web_compatible_p2p}.
     */
    TP_ENABLE_WEB_COMPATIBLE_P2P("tp->enable_web_compatible_p2p", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;endpt_polling_timeout_msec}.
     */
    TP_ENDPT_POLLING_TIMEOUT_MSEC("tp->endpt_polling_timeout_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;explore_only_p2p_first_negotiation_delay_ms}.
     */
    TP_EXPLORE_ONLY_P2P_FIRST_NEGOTIATION_DELAY_MS("tp->explore_only_p2p_first_negotiation_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;explore_only_p2p_tx_pct}.
     */
    TP_EXPLORE_ONLY_P2P_TX_PCT("tp->explore_only_p2p_tx_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;fast_call_setup_callee_v2.enable}.
     */
    TP_FAST_CALL_SETUP_CALLEE_V2_ENABLE("tp->fast_call_setup_callee_v2.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;fast_call_setup_callee_v2.max_e2e_bind_retries}.
     */
    TP_FAST_CALL_SETUP_CALLEE_V2_MAX_E2E_BIND_RETRIES("tp->fast_call_setup_callee_v2.max_e2e_bind_retries", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;fast_call_setup_callee_v2.max_e2e_rtt_ms}.
     */
    TP_FAST_CALL_SETUP_CALLEE_V2_MAX_E2E_RTT_MS("tp->fast_call_setup_callee_v2.max_e2e_rtt_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;fast_call_setup_callee_v2.precall_e2e_bind_interval_ms}.
     */
    TP_FAST_CALL_SETUP_CALLEE_V2_PRECALL_E2E_BIND_INTERVAL_MS("tp->fast_call_setup_callee_v2.precall_e2e_bind_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;fast_call_setup_callee_v2.skip_after_elected}.
     */
    TP_FAST_CALL_SETUP_CALLEE_V2_SKIP_AFTER_ELECTED("tp->fast_call_setup_callee_v2.skip_after_elected", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;force_call_failure}.
     */
    TP_FORCE_CALL_FAILURE("tp->force_call_failure", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;force_rebind_on_relay_election}.
     */
    TP_FORCE_REBIND_ON_RELAY_ELECTION("tp->force_rebind_on_relay_election", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;get_refl_ip_from_alloc_err}.
     */
    TP_GET_REFL_IP_FROM_ALLOC_ERR("tp->get_refl_ip_from_alloc_err", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;gethostip_disable_local_resolution}.
     */
    TP_GETHOSTIP_DISABLE_LOCAL_RESOLUTION("tp->gethostip_disable_local_resolution", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;ip_correctness.allow_disable_prefer_relay}.
     */
    TP_IP_CORRECTNESS_ALLOW_DISABLE_PREFER_RELAY("tp->ip_correctness.allow_disable_prefer_relay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;ip_correctness.enable}.
     */
    TP_IP_CORRECTNESS_ENABLE("tp->ip_correctness.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;ip_correctness.max_num_relays_per_update}.
     */
    TP_IP_CORRECTNESS_MAX_NUM_RELAYS_PER_UPDATE("tp->ip_correctness.max_num_relays_per_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;ip_correctness.max_relay_info_update}.
     */
    TP_IP_CORRECTNESS_MAX_RELAY_INFO_UPDATE("tp->ip_correctness.max_relay_info_update", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;low_fd_setsize_factor}.
     */
    TP_LOW_FD_SETSIZE_FACTOR("tp->low_fd_setsize_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_relay_bind_retry_count_strategy}.
     */
    TP_MAX_RELAY_BIND_RETRY_COUNT_STRATEGY("tp->max_relay_bind_retry_count_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_rtp_audio_packet_resends}.
     */
    TP_MAX_RTP_AUDIO_PACKET_RESENDS("tp->max_rtp_audio_packet_resends", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_rtp_video_packet_resends}.
     */
    TP_MAX_RTP_VIDEO_PACKET_RESENDS("tp->max_rtp_video_packet_resends", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_rtx_bitrate_pct}.
     */
    TP_MAX_RTX_BITRATE_PCT("tp->max_rtx_bitrate_pct", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_rtx_window_size_ms}.
     */
    TP_MAX_RTX_WINDOW_SIZE_MS("tp->max_rtx_window_size_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;max_simultaneous_sends}.
     */
    TP_MAX_SIMULTANEOUS_SENDS("tp->max_simultaneous_sends", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;min_ecn_feedback_interval_ms}.
     */
    TP_MIN_ECN_FEEDBACK_INTERVAL_MS("tp->min_ecn_feedback_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;mp.send_dup_bitmap}.
     */
    TP_MP_SEND_DUP_BITMAP("tp->mp.send_dup_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;mp.send_secondary_bind}.
     */
    TP_MP_SEND_SECONDARY_BIND("tp->mp.send_secondary_bind", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;multipop_check_duration_ms}.
     */
    TP_MULTIPOP_CHECK_DURATION_MS("tp->multipop_check_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_banner_sub_message}.
     */
    TP_NET_HEALTH_ENABLE_BANNER_SUB_MESSAGE("tp->net_health.enable_banner_sub_message", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_network_health_monitor}.
     */
    TP_NET_HEALTH_ENABLE_NETWORK_HEALTH_MONITOR("tp->net_health.enable_network_health_monitor", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_post_net_health_status_to_cb_queue}.
     */
    TP_NET_HEALTH_ENABLE_POST_NET_HEALTH_STATUS_TO_CB_QUEUE("tp->net_health.enable_post_net_health_status_to_cb_queue", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_post_net_health_status_to_cb_queue_v2}.
     */
    TP_NET_HEALTH_ENABLE_POST_NET_HEALTH_STATUS_TO_CB_QUEUE_V2("tp->net_health.enable_post_net_health_status_to_cb_queue_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_send_peer_status}.
     */
    TP_NET_HEALTH_ENABLE_SEND_PEER_STATUS("tp->net_health.enable_send_peer_status", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.enable_set_peer_no_network_status}.
     */
    TP_NET_HEALTH_ENABLE_SET_PEER_NO_NETWORK_STATUS("tp->net_health.enable_set_peer_no_network_status", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.max_peer_count_to_send_peer_status}.
     */
    TP_NET_HEALTH_MAX_PEER_COUNT_TO_SEND_PEER_STATUS("tp->net_health.max_peer_count_to_send_peer_status", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.max_sent_banner}.
     */
    TP_NET_HEALTH_MAX_SENT_BANNER("tp->net_health.max_sent_banner", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.max_sent_poor_peer_status_count}.
     */
    TP_NET_HEALTH_MAX_SENT_POOR_PEER_STATUS_COUNT("tp->net_health.max_sent_poor_peer_status_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.max_sound_alert_audio}.
     */
    TP_NET_HEALTH_MAX_SOUND_ALERT_AUDIO("tp->net_health.max_sound_alert_audio", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.max_sound_alert_video}.
     */
    TP_NET_HEALTH_MAX_SOUND_ALERT_VIDEO("tp->net_health.max_sound_alert_video", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.no_network_rx_traffic_timeout_ms}.
     */
    TP_NET_HEALTH_NO_NETWORK_RX_TRAFFIC_TIMEOUT_MS("tp->net_health.no_network_rx_traffic_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.p2p.check_destroying_before_rtcp_stat}.
     */
    TP_NET_HEALTH_P2P_CHECK_DESTROYING_BEFORE_RTCP_STAT("tp->net_health.p2p.check_destroying_before_rtcp_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.p2p.enable_p2p_connection_check}.
     */
    TP_NET_HEALTH_P2P_ENABLE_P2P_CONNECTION_CHECK("tp->net_health.p2p.enable_p2p_connection_check", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.p2p.max_p2p_plr}.
     */
    TP_NET_HEALTH_P2P_MAX_P2P_PLR("tp->net_health.p2p.max_p2p_plr", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.p2p.max_p2p_rtt}.
     */
    TP_NET_HEALTH_P2P_MAX_P2P_RTT("tp->net_health.p2p.max_p2p_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.peer_status_normal_net_rx_traffic_timeout_ms}.
     */
    TP_NET_HEALTH_PEER_STATUS_NORMAL_NET_RX_TRAFFIC_TIMEOUT_MS("tp->net_health.peer_status_normal_net_rx_traffic_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.peer_status_poor_net_rx_traffic_timeout_ms}.
     */
    TP_NET_HEALTH_PEER_STATUS_POOR_NET_RX_TRAFFIC_TIMEOUT_MS("tp->net_health.peer_status_poor_net_rx_traffic_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.ping_loss_sample_size}.
     */
    TP_NET_HEALTH_PING_LOSS_SAMPLE_SIZE("tp->net_health.ping_loss_sample_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.poor_peer_status_interval_ms}.
     */
    TP_NET_HEALTH_POOR_PEER_STATUS_INTERVAL_MS("tp->net_health.poor_peer_status_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.sent_banner_interval_ms}.
     */
    TP_NET_HEALTH_SENT_BANNER_INTERVAL_MS("tp->net_health.sent_banner_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;net_health.sound_alert_interval_ms}.
     */
    TP_NET_HEALTH_SOUND_ALERT_INTERVAL_MS("tp->net_health.sound_alert_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_enable_delay_after_net_switch_ms}.
     */
    TP_P2P_ENABLE_DELAY_AFTER_NET_SWITCH_MS("tp->p2p_enable_delay_after_net_switch_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_optimization_strategy_2}.
     */
    TP_P2P_OPTIMIZATION_STRATEGY_2("tp->p2p_optimization_strategy_2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_optimization_strategy_locked}.
     */
    TP_P2P_OPTIMIZATION_STRATEGY_LOCKED("tp->p2p_optimization_strategy_locked", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_preferred_af}.
     */
    TP_P2P_PREFERRED_AF("tp->p2p_preferred_af", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_to_relay_fallback_timeout}.
     */
    TP_P2P_TO_RELAY_FALLBACK_TIMEOUT("tp->p2p_to_relay_fallback_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_to_relay_on_rx_relay_frame_timeout}.
     */
    TP_P2P_TO_RELAY_ON_RX_RELAY_FRAME_TIMEOUT("tp->p2p_to_relay_on_rx_relay_frame_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;p2p_worker_thread_init_sleep_ms}.
     */
    TP_P2P_WORKER_THREAD_INIT_SLEEP_MS("tp->p2p_worker_thread_init_sleep_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;ping_summary_interval_ms}.
     */
    TP_PING_SUMMARY_INTERVAL_MS("tp->ping_summary_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;prefer_relay_enable_p2p_timeout_ms}.
     */
    TP_PREFER_RELAY_ENABLE_P2P_TIMEOUT_MS("tp->prefer_relay_enable_p2p_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;prefer_relay_fallback_threshold}.
     */
    TP_PREFER_RELAY_FALLBACK_THRESHOLD("tp->prefer_relay_fallback_threshold", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;prefer_relay_with_active_p2p}.
     */
    TP_PREFER_RELAY_WITH_ACTIVE_P2P("tp->prefer_relay_with_active_p2p", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;preserve_all_events_in_event_queue}.
     */
    TP_PRESERVE_ALL_EVENTS_IN_EVENT_QUEUE("tp->preserve_all_events_in_event_queue", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_bitmap}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_BITMAP("tp->priority_tx_queue_cfg.priority_tx_queue_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_capacity}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_CAPACITY("tp->priority_tx_queue_cfg.priority_tx_queue_capacity", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_critical_burst_limit}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_CRITICAL_BURST_LIMIT("tp->priority_tx_queue_cfg.priority_tx_queue_critical_burst_limit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_drain_byte_limit}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_DRAIN_BYTE_LIMIT("tp->priority_tx_queue_cfg.priority_tx_queue_drain_byte_limit", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_max_pkt_size}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_MAX_PKT_SIZE("tp->priority_tx_queue_cfg.priority_tx_queue_max_pkt_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_stale_thin_every}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_STALE_THIN_EVERY("tp->priority_tx_queue_cfg.priority_tx_queue_stale_thin_every", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_stale_thin_send}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_STALE_THIN_SEND("tp->priority_tx_queue_cfg.priority_tx_queue_stale_thin_send", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_ttl_critical_ms}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_TTL_CRITICAL_MS("tp->priority_tx_queue_cfg.priority_tx_queue_ttl_critical_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;priority_tx_queue_cfg.priority_tx_queue_ttl_normal_ms}.
     */
    TP_PRIORITY_TX_QUEUE_CFG_PRIORITY_TX_QUEUE_TTL_NORMAL_MS("tp->priority_tx_queue_cfg.priority_tx_queue_ttl_normal_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;proxy_enable_bitmap}.
     */
    TP_PROXY_ENABLE_BITMAP("tp->proxy_enable_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;reenable_p2p_on_peer_network_switch}.
     */
    TP_REENABLE_P2P_ON_PEER_NETWORK_SWITCH("tp->reenable_p2p_on_peer_network_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;refactor_sockaddr_available}.
     */
    TP_REFACTOR_SOCKADDR_AVAILABLE("tp->refactor_sockaddr_available", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_data_timeout_ms}.
     */
    TP_RELAY_DATA_TIMEOUT_MS("tp->relay_data_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_e2e_probe_rsp_switch_relay_max_delta_ms}.
     */
    TP_RELAY_E2E_PROBE_RSP_SWITCH_RELAY_MAX_DELTA_MS("tp->relay_e2e_probe_rsp_switch_relay_max_delta_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_election_latency_update_threshold_ms}.
     */
    TP_RELAY_ELECTION_LATENCY_UPDATE_THRESHOLD_MS("tp->relay_election_latency_update_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_latency_batch_params.batch_rl_after_media_start}.
     */
    TP_RELAY_LATENCY_BATCH_PARAMS_BATCH_RL_AFTER_MEDIA_START("tp->relay_latency_batch_params.batch_rl_after_media_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_latency_batch_params.batched_relay_latencies_send_interval_ms}.
     */
    TP_RELAY_LATENCY_BATCH_PARAMS_BATCHED_RELAY_LATENCIES_SEND_INTERVAL_MS("tp->relay_latency_batch_params.batched_relay_latencies_send_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_latency_batch_params.batched_relay_latencies_send_size}.
     */
    TP_RELAY_LATENCY_BATCH_PARAMS_BATCHED_RELAY_LATENCIES_SEND_SIZE("tp->relay_latency_batch_params.batched_relay_latencies_send_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_latency_batch_params.send_batched_relay_latencies}.
     */
    TP_RELAY_LATENCY_BATCH_PARAMS_SEND_BATCHED_RELAY_LATENCIES("tp->relay_latency_batch_params.send_batched_relay_latencies", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_latency_batch_params.send_batched_relay_latencies_mutex_fix}.
     */
    TP_RELAY_LATENCY_BATCH_PARAMS_SEND_BATCHED_RELAY_LATENCIES_MUTEX_FIX("tp->relay_latency_batch_params.send_batched_relay_latencies_mutex_fix", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_before_peer_accept_interval_ms}.
     */
    TP_RELAY_PING_BEFORE_PEER_ACCEPT_INTERVAL_MS("tp->relay_ping_before_peer_accept_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.behavior_tuning_bitmap}.
     */
    TP_RELAY_PING_CHOICE_V2_BEHAVIOR_TUNING_BITMAP("tp->relay_ping_choice_v2.behavior_tuning_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.enable}.
     */
    TP_RELAY_PING_CHOICE_V2_ENABLE("tp->relay_ping_choice_v2.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.enable_nat_hole_stale_check}.
     */
    TP_RELAY_PING_CHOICE_V2_ENABLE_NAT_HOLE_STALE_CHECK("tp->relay_ping_choice_v2.enable_nat_hole_stale_check", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.extended_ping_interval_ms}.
     */
    TP_RELAY_PING_CHOICE_V2_EXTENDED_PING_INTERVAL_MS("tp->relay_ping_choice_v2.extended_ping_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.keep_alive_all_relays}.
     */
    TP_RELAY_PING_CHOICE_V2_KEEP_ALIVE_ALL_RELAYS("tp->relay_ping_choice_v2.keep_alive_all_relays", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.nat_hole_stale_threshold_ms}.
     */
    TP_RELAY_PING_CHOICE_V2_NAT_HOLE_STALE_THRESHOLD_MS("tp->relay_ping_choice_v2.nat_hole_stale_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_choice_v2.ping_peer_used_relays}.
     */
    TP_RELAY_PING_CHOICE_V2_PING_PEER_USED_RELAYS("tp->relay_ping_choice_v2.ping_peer_used_relays", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_interval}.
     */
    TP_RELAY_PING_INTERVAL("tp->relay_ping_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_ping_sample_rate}.
     */
    TP_RELAY_PING_SAMPLE_RATE("tp->relay_ping_sample_rate", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_unresponsive_reset_timeout}.
     */
    TP_RELAY_UNRESPONSIVE_RESET_TIMEOUT("tp->relay_unresponsive_reset_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;relay_unresponsive_timeout}.
     */
    TP_RELAY_UNRESPONSIVE_TIMEOUT("tp->relay_unresponsive_timeout", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;remove_empty_ssrclayer}.
     */
    TP_REMOVE_EMPTY_SSRCLAYER("tp->remove_empty_ssrclayer", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;require_rtp_for_stale_rebind}.
     */
    TP_REQUIRE_RTP_FOR_STALE_REBIND("tp->require_rtp_for_stale_rebind", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;restart_on_net_med_update_delay_ms}.
     */
    TP_RESTART_ON_NET_MED_UPDATE_DELAY_MS("tp->restart_on_net_med_update_delay_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;restart_opt_bitmap}.
     */
    TP_RESTART_OPT_BITMAP("tp->restart_opt_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;restart_p2p_negotiation_upon_group_call_downgrade}.
     */
    TP_RESTART_P2P_NEGOTIATION_UPON_GROUP_CALL_DOWNGRADE("tp->restart_p2p_negotiation_upon_group_call_downgrade", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;retry_p2p_imm_timeout_ms}.
     */
    TP_RETRY_P2P_IMM_TIMEOUT_MS("tp->retry_p2p_imm_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_detection_threshold_ms}.
     */
    TP_RX_GAP_DETECTION_THRESHOLD_MS("tp->rx_gap_detection_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.ema_n_dtx}.
     */
    TP_RX_GAP_MONITOR_EMA_N_DTX("tp->rx_gap_monitor.ema_n_dtx", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.ema_n_voice}.
     */
    TP_RX_GAP_MONITOR_EMA_N_VOICE("tp->rx_gap_monitor.ema_n_voice", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.enable}.
     */
    TP_RX_GAP_MONITOR_ENABLE("tp->rx_gap_monitor.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.gap_event_k_stddev}.
     */
    TP_RX_GAP_MONITOR_GAP_EVENT_K_STDDEV("tp->rx_gap_monitor.gap_event_k_stddev", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.gap_event_min_interval_ms}.
     */
    TP_RX_GAP_MONITOR_GAP_EVENT_MIN_INTERVAL_MS("tp->rx_gap_monitor.gap_event_min_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_gap_monitor.gap_logging_interval_ms}.
     */
    TP_RX_GAP_MONITOR_GAP_LOGGING_INTERVAL_MS("tp->rx_gap_monitor.gap_logging_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_subscription_min_duration_ms}.
     */
    TP_RX_SUBSCRIPTION_MIN_DURATION_MS("tp->rx_subscription_min_duration_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_timeout_alloc_resp_ms}.
     */
    TP_RX_TIMEOUT_ALLOC_RESP_MS("tp->rx_timeout_alloc_resp_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_timeout_for_no_media_signal_ms}.
     */
    TP_RX_TIMEOUT_FOR_NO_MEDIA_SIGNAL_MS("tp->rx_timeout_for_no_media_signal_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_traffic_event_rtp_only_v2}.
     */
    TP_RX_TRAFFIC_EVENT_RTP_ONLY_V2("tp->rx_traffic_event_rtp_only_v2", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;rx_traffic_inactive_threshold_ms}.
     */
    TP_RX_TRAFFIC_INACTIVE_THRESHOLD_MS("tp->rx_traffic_inactive_threshold_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;send_accept_before_stream_start}.
     */
    TP_SEND_ACCEPT_BEFORE_STREAM_START("tp->send_accept_before_stream_start", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;send_connect_stat_timeout_ms}.
     */
    TP_SEND_CONNECT_STAT_TIMEOUT_MS("tp->send_connect_stat_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;send_destination_in_gc_relay_latency}.
     */
    TP_SEND_DESTINATION_IN_GC_RELAY_LATENCY("tp->send_destination_in_gc_relay_latency", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;send_is_xpop_in_connect_stat}.
     */
    TP_SEND_IS_XPOP_IN_CONNECT_STAT("tp->send_is_xpop_in_connect_stat", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;send_net_health_timeout_in_connect_stat_ms}.
     */
    TP_SEND_NET_HEALTH_TIMEOUT_IN_CONNECT_STAT_MS("tp->send_net_health_timeout_in_connect_stat_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;short_call_t_ms}.
     */
    TP_SHORT_CALL_T_MS("tp->short_call_t_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;signaling_handler_strategy_interval_ms}.
     */
    TP_SIGNALING_HANDLER_STRATEGY_INTERVAL_MS("tp->signaling_handler_strategy_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;signaling_network_probe_end_ms}.
     */
    TP_SIGNALING_NETWORK_PROBE_END_MS("tp->signaling_network_probe_end_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;signaling_network_probe_interval_ms}.
     */
    TP_SIGNALING_NETWORK_PROBE_INTERVAL_MS("tp->signaling_network_probe_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;signaling_network_probe_start_ms}.
     */
    TP_SIGNALING_NETWORK_PROBE_START_MS("tp->signaling_network_probe_start_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;signaling_probe_response_timeout_ms}.
     */
    TP_SIGNALING_PROBE_RESPONSE_TIMEOUT_MS("tp->signaling_probe_response_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;skip_choose_ip_version_on_same_config}.
     */
    TP_SKIP_CHOOSE_IP_VERSION_ON_SAME_CONFIG("tp->skip_choose_ip_version_on_same_config", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;skip_xor_relayed_addr_in_alloc}.
     */
    TP_SKIP_XOR_RELAYED_ADDR_IN_ALLOC("tp->skip_xor_relayed_addr_in_alloc", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;smart_transport_signal_client_switch}.
     */
    TP_SMART_TRANSPORT_SIGNAL_CLIENT_SWITCH("tp->smart_transport_signal_client_switch", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.conn_id_first_byte}.
     */
    TP_STABLE_ROUTING_CONN_ID_FIRST_BYTE("tp->stable_routing.conn_id_first_byte", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.debug_bitmap}.
     */
    TP_STABLE_ROUTING_DEBUG_BITMAP("tp->stable_routing.debug_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.enable}.
     */
    TP_STABLE_ROUTING_ENABLE("tp->stable_routing.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.enable_share_conn_id}.
     */
    TP_STABLE_ROUTING_ENABLE_SHARE_CONN_ID("tp->stable_routing.enable_share_conn_id", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.enforce_conn_id_presence}.
     */
    TP_STABLE_ROUTING_ENFORCE_CONN_ID_PRESENCE("tp->stable_routing.enforce_conn_id_presence", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.min_audio_bandwidth_bps}.
     */
    TP_STABLE_ROUTING_MIN_AUDIO_BANDWIDTH_BPS("tp->stable_routing.min_audio_bandwidth_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.min_video_bandwidth_bps}.
     */
    TP_STABLE_ROUTING_MIN_VIDEO_BANDWIDTH_BPS("tp->stable_routing.min_video_bandwidth_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.reset_conn_id_on_relay_reset}.
     */
    TP_STABLE_ROUTING_RESET_CONN_ID_ON_RELAY_RESET("tp->stable_routing.reset_conn_id_on_relay_reset", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;stable_routing.tcp_support}.
     */
    TP_STABLE_ROUTING_TCP_SUPPORT("tp->stable_routing.tcp_support", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;sts_recommended_goodput_downlink_enabled}.
     */
    TP_STS_RECOMMENDED_GOODPUT_DOWNLINK_ENABLED("tp->sts_recommended_goodput_downlink_enabled", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;switch_relay_strategy}.
     */
    TP_SWITCH_RELAY_STRATEGY("tp->switch_relay_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;symmetric_nat_detection.enable}.
     */
    TP_SYMMETRIC_NAT_DETECTION_ENABLE("tp->symmetric_nat_detection.enable", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;symmetric_nat_detection.max_p2p_retry}.
     */
    TP_SYMMETRIC_NAT_DETECTION_MAX_P2P_RETRY("tp->symmetric_nat_detection.max_p2p_retry", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;symmetric_nat_detection.min_distinct_reflex_ip_cnt}.
     */
    TP_SYMMETRIC_NAT_DETECTION_MIN_DISTINCT_REFLEX_IP_CNT("tp->symmetric_nat_detection.min_distinct_reflex_ip_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;symmetric_nat_detection.sym_nat_handling_strategy}.
     */
    TP_SYMMETRIC_NAT_DETECTION_SYM_NAT_HANDLING_STRATEGY("tp->symmetric_nat_detection.sym_nat_handling_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tcp_alt_af_bitmap}.
     */
    TP_TCP_ALT_AF_BITMAP("tp->tcp_alt_af_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tcp_alt_af_timeout_hostile_ms}.
     */
    TP_TCP_ALT_AF_TIMEOUT_HOSTILE_MS("tp->tcp_alt_af_timeout_hostile_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tcp_alt_af_timeout_ms}.
     */
    TP_TCP_ALT_AF_TIMEOUT_MS("tp->tcp_alt_af_timeout_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tos_byte_per_net_meidum}.
     */
    TP_TOS_BYTE_PER_NET_MEIDUM("tp->tos_byte_per_net_meidum", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.alpha}.
     */
    TP_TP_OOB_BWE_ALPHA("tp->tp_oob_bwe.alpha", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.enable}.
     */
    TP_TP_OOB_BWE_ENABLE("tp->tp_oob_bwe.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.max_report_bitrate_bps}.
     */
    TP_TP_OOB_BWE_MAX_REPORT_BITRATE_BPS("tp->tp_oob_bwe.max_report_bitrate_bps", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.report_factor}.
     */
    TP_TP_OOB_BWE_REPORT_FACTOR("tp->tp_oob_bwe.report_factor", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.rounding_strategy}.
     */
    TP_TP_OOB_BWE_ROUNDING_STRATEGY("tp->tp_oob_bwe.rounding_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.tx_fixed_bw_bytes}.
     */
    TP_TP_OOB_BWE_TX_FIXED_BW_BYTES("tp->tp_oob_bwe.tx_fixed_bw_bytes", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.tx_type_bitmap}.
     */
    TP_TP_OOB_BWE_TX_TYPE_BITMAP("tp->tp_oob_bwe.tx_type_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tp_oob_bwe.update_interval_ms}.
     */
    TP_TP_OOB_BWE_UPDATE_INTERVAL_MS("tp->tp_oob_bwe.update_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_assert_bitmap}.
     */
    TP_TRANSPORT_ASSERT_BITMAP("tp->transport_assert_bitmap", VoipParamType.INTEGER, 8, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_bwe_monitor.enable}.
     */
    TP_TRANSPORT_BWE_MONITOR_ENABLE("tp->transport_bwe_monitor.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_bwe_monitor.min_sample_cnt}.
     */
    TP_TRANSPORT_BWE_MONITOR_MIN_SAMPLE_CNT("tp->transport_bwe_monitor.min_sample_cnt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_bwe_monitor.update_interval_ms}.
     */
    TP_TRANSPORT_BWE_MONITOR_UPDATE_INTERVAL_MS("tp->transport_bwe_monitor.update_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_debug_bitmap}.
     */
    TP_TRANSPORT_DEBUG_BITMAP("tp->transport_debug_bitmap", VoipParamType.INTEGER, 8, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_debug_log_bind_payload}.
     */
    TP_TRANSPORT_DEBUG_LOG_BIND_PAYLOAD("tp->transport_debug_log_bind_payload", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_protocol_policy}.
     */
    TP_TRANSPORT_PROTOCOL_POLICY("tp->transport_protocol_policy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;transport_stats_p2p_threshold}.
     */
    TP_TRANSPORT_STATS_P2P_THRESHOLD("tp->transport_stats_p2p_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tx_bitrate_logging_interval_ms}.
     */
    TP_TX_BITRATE_LOGGING_INTERVAL_MS("tp->tx_bitrate_logging_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tx_bitrate_tracking}.
     */
    TP_TX_BITRATE_TRACKING("tp->tx_bitrate_tracking", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;tx_cache_size_pkts}.
     */
    TP_TX_CACHE_SIZE_PKTS("tp->tx_cache_size_pkts", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;unusable_relay.check_e2e_connectivity}.
     */
    TP_UNUSABLE_RELAY_CHECK_E2E_CONNECTIVITY("tp->unusable_relay.check_e2e_connectivity", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;use_device_jid_for_relay_latency_peer_lookup}.
     */
    TP_USE_DEVICE_JID_FOR_RELAY_LATENCY_PEER_LOOKUP("tp->use_device_jid_for_relay_latency_peer_lookup", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;use_server_prefer_relay}.
     */
    TP_USE_SERVER_PREFER_RELAY("tp->use_server_prefer_relay", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.allow_tcp_concurrent_asock_ioqueue_key}.
     */
    TP_WA_ASOCK_CFG_ALLOW_TCP_CONCURRENT_ASOCK_IOQUEUE_KEY("tp->wa_asock_cfg.allow_tcp_concurrent_asock_ioqueue_key", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.asock_max_packets_per_loop}.
     */
    TP_WA_ASOCK_CFG_ASOCK_MAX_PACKETS_PER_LOOP("tp->wa_asock_cfg.asock_max_packets_per_loop", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.assert_host_addr_zero_port}.
     */
    TP_WA_ASOCK_CFG_ASSERT_HOST_ADDR_ZERO_PORT("tp->wa_asock_cfg.assert_host_addr_zero_port", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.dscp_overwrite}.
     */
    TP_WA_ASOCK_CFG_DSCP_OVERWRITE("tp->wa_asock_cfg.dscp_overwrite", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.ecn_overwrite}.
     */
    TP_WA_ASOCK_CFG_ECN_OVERWRITE("tp->wa_asock_cfg.ecn_overwrite", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.enable_list_open_fd}.
     */
    TP_WA_ASOCK_CFG_ENABLE_LIST_OPEN_FD("tp->wa_asock_cfg.enable_list_open_fd", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.force_snd_size}.
     */
    TP_WA_ASOCK_CFG_FORCE_SND_SIZE("tp->wa_asock_cfg.force_snd_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.num_file_limit_leeway}.
     */
    TP_WA_ASOCK_CFG_NUM_FILE_LIMIT_LEEWAY("tp->wa_asock_cfg.num_file_limit_leeway", VoipParamType.INTEGER, 2, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.num_max_tcp_connection_retries}.
     */
    TP_WA_ASOCK_CFG_NUM_MAX_TCP_CONNECTION_RETRIES("tp->wa_asock_cfg.num_max_tcp_connection_retries", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.num_tcp_tunnel_buffer_slots}.
     */
    TP_WA_ASOCK_CFG_NUM_TCP_TUNNEL_BUFFER_SLOTS("tp->wa_asock_cfg.num_tcp_tunnel_buffer_slots", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.prefer_host_addr_for_active_addr}.
     */
    TP_WA_ASOCK_CFG_PREFER_HOST_ADDR_FOR_ACTIVE_ADDR("tp->wa_asock_cfg.prefer_host_addr_for_active_addr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.set_port_in_host_addr}.
     */
    TP_WA_ASOCK_CFG_SET_PORT_IN_HOST_ADDR("tp->wa_asock_cfg.set_port_in_host_addr", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.tcp_reconnect_interval_in_msec}.
     */
    TP_WA_ASOCK_CFG_TCP_RECONNECT_INTERVAL_IN_MSEC("tp->wa_asock_cfg.tcp_reconnect_interval_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.tcp_reconnect_reset_window_in_msec}.
     */
    TP_WA_ASOCK_CFG_TCP_RECONNECT_RESET_WINDOW_IN_MSEC("tp->wa_asock_cfg.tcp_reconnect_reset_window_in_msec", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.udp_sobuf_rcv_size}.
     */
    TP_WA_ASOCK_CFG_UDP_SOBUF_RCV_SIZE("tp->wa_asock_cfg.udp_sobuf_rcv_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wa_asock_cfg.udp_sobuf_snd_size}.
     */
    TP_WA_ASOCK_CFG_UDP_SOBUF_SND_SIZE("tp->wa_asock_cfg.udp_sobuf_snd_size", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.connect_bind_success_rate_fail_threshold}.
     */
    TP_WD_CONNECT_BIND_SUCCESS_RATE_FAIL_THRESHOLD("tp->wd.connect_bind_success_rate_fail_threshold", VoipParamType.FLOAT, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.fatal_send_error_bitmap}.
     */
    TP_WD_FATAL_SEND_ERROR_BITMAP("tp->wd.fatal_send_error_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.ip_changed_medium_update_interval_ms}.
     */
    TP_WD_IP_CHANGED_MEDIUM_UPDATE_INTERVAL_MS("tp->wd.ip_changed_medium_update_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.ip_changed_socket_event_reset_interval_ms}.
     */
    TP_WD_IP_CHANGED_SOCKET_EVENT_RESET_INTERVAL_MS("tp->wd.ip_changed_socket_event_reset_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.low_connection_bind_success_rate_grace_period_ms}.
     */
    TP_WD_LOW_CONNECTION_BIND_SUCCESS_RATE_GRACE_PERIOD_MS("tp->wd.low_connection_bind_success_rate_grace_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.max_matching_bound_socket}.
     */
    TP_WD_MAX_MATCHING_BOUND_SOCKET("tp->wd.max_matching_bound_socket", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.no_usable_socket_event_reset_interval_ms}.
     */
    TP_WD_NO_USABLE_SOCKET_EVENT_RESET_INTERVAL_MS("tp->wd.no_usable_socket_event_reset_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.public_ip_change_detection.confirm_period_ms}.
     */
    TP_WD_PUBLIC_IP_CHANGE_DETECTION_CONFIRM_PERIOD_MS("tp->wd.public_ip_change_detection.confirm_period_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.public_ip_change_detection.enable}.
     */
    TP_WD_PUBLIC_IP_CHANGE_DETECTION_ENABLE("tp->wd.public_ip_change_detection.enable", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.public_ip_change_detection.ip_change_strategy}.
     */
    TP_WD_PUBLIC_IP_CHANGE_DETECTION_IP_CHANGE_STRATEGY("tp->wd.public_ip_change_detection.ip_change_strategy", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.public_ip_change_detection.ping_interval}.
     */
    TP_WD_PUBLIC_IP_CHANGE_DETECTION_PING_INTERVAL("tp->wd.public_ip_change_detection.ping_interval", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.public_ip_change_detection.send_ip_req_ping}.
     */
    TP_WD_PUBLIC_IP_CHANGE_DETECTION_SEND_IP_REQ_PING("tp->wd.public_ip_change_detection.send_ip_req_ping", VoipParamType.INTEGER, 1, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.skip_restart_when_restart_pending}.
     */
    TP_WD_SKIP_RESTART_WHEN_RESTART_PENDING("tp->wd.skip_restart_when_restart_pending", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.transport_restart_on_bound_socket_error_max_count}.
     */
    TP_WD_TRANSPORT_RESTART_ON_BOUND_SOCKET_ERROR_MAX_COUNT("tp->wd.transport_restart_on_bound_socket_error_max_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.transport_restart_on_bound_socket_error_ms}.
     */
    TP_WD_TRANSPORT_RESTART_ON_BOUND_SOCKET_ERROR_MS("tp->wd.transport_restart_on_bound_socket_error_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.transport_restart_on_ip_changed_check_interval_ms}.
     */
    TP_WD_TRANSPORT_RESTART_ON_IP_CHANGED_CHECK_INTERVAL_MS("tp->wd.transport_restart_on_ip_changed_check_interval_ms", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.transport_restart_on_ip_changed_max_count}.
     */
    TP_WD_TRANSPORT_RESTART_ON_IP_CHANGED_MAX_COUNT("tp->wd.transport_restart_on_ip_changed_max_count", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;wd.tune_watchdog_bitmap}.
     */
    TP_WD_TUNE_WATCHDOG_BITMAP("tp->wd.tune_watchdog_bitmap", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;xpop.enable_relay_election_with_rtt}.
     */
    TP_XPOP_ENABLE_RELAY_ELECTION_WITH_RTT("tp->xpop.enable_relay_election_with_rtt", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;xpop.enable_xpop_for_group}.
     */
    TP_XPOP_ENABLE_XPOP_FOR_GROUP("tp->xpop.enable_xpop_for_group", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;xpop.relay_election_scheme}.
     */
    TP_XPOP_RELAY_ELECTION_SCHEME("tp->xpop.relay_election_scheme", VoipParamType.INTEGER, 4, false),

    /**
     * Native descriptor for {@code tp-&gt;xpop.relay_latency_discount_factor}.
     */
    TP_XPOP_RELAY_LATENCY_DISCOUNT_FACTOR("tp->xpop.relay_latency_discount_factor", VoipParamType.FLOAT, 4, false);

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
    VoipParamKeyTransport1(String dottedPath, VoipParamType type, int byteWidth, boolean bweParam) {
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
