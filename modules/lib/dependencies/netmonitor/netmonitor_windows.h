/*
 * Aggregator header for jextract: surfaces the Windows IP Helper connectivity
 * notification API used by the network-resilience connectivity monitor.
 *
 * Pulls in only the system headers that declare:
 *   - NotifyAddrChange            (iphlpapi.h) - blocks until the address table changes
 *   - GetNetworkConnectivityHint  (netioapi.h) - current connectivity level
 *   - NL_NETWORK_CONNECTIVITY_HINT (nldef.h via netioapi.h)
 *
 * winsock2.h must precede windows.h so the legacy winsock.h is not pulled in.
 */
#include <winsock2.h>
#include <windows.h>
#include <iphlpapi.h>
#include <netioapi.h>
