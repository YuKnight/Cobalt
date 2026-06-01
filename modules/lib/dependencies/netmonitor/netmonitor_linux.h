/*
 * Aggregator header for jextract: surfaces the libc primitives
 * LinuxNetworkConnectivityMonitor uses.
 *
 * Event source: an AF_NETLINK/NETLINK_ROUTE socket bound to
 * RTMGRP_LINK | RTMGRP_IPV4_IFADDR | RTMGRP_IPV6_IFADDR, then a blocking recv()
 * per change. Current state: getifaddrs(), online when a non-loopback interface
 * is IFF_UP|IFF_RUNNING with a non-link-local AF_INET/AF_INET6 address.
 */
#include <sys/socket.h>
#include <linux/netlink.h>
#include <linux/rtnetlink.h>
#include <ifaddrs.h>
#include <net/if.h>
