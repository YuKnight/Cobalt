/*
 * Aggregator header for jextract: surfaces the SystemConfiguration and
 * CoreFoundation primitives MacosNetworkConnectivityMonitor uses.
 *
 * The monitor creates an SCNetworkReachability reference for the default route
 * (0.0.0.0), registers a callback (an FFM upcall stub) via
 * SCNetworkReachabilitySetCallback, schedules it on the current CFRunLoop, and
 * runs the loop; CFRunLoopStop ends it. SCNetworkReachabilityGetFlags seeds the
 * initial state; online when kSCNetworkReachabilityFlagsReachable is set and
 * ConnectionRequired is not. CFRelease frees the reference.
 */
#include <SystemConfiguration/SCNetworkReachability.h>
#include <CoreFoundation/CoreFoundation.h>
