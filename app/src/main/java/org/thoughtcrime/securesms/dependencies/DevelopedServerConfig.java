package org.thoughtcrime.securesms.dependencies;

/**
 * RED: Server Redirection Module
 *
 * All endpoints are configurable via the USE_TLS flag. For a sovereign air-gapped LAN the
 * default (cleartext) is acceptable; for any non-isolated deployment set USE_TLS = true and
 * complete the TLS workflow described in SECURITY.md.
 */
public class DevelopedServerConfig {

    // ── Configuration ──────────────────────────────────────────────────────
    /** Set true for production (see SECURITY.md for the TLS workflow). */
    private static final boolean USE_TLS = false; // lab default; flip to true for production

    private static final String BASE_HOST = "192.168.1.50";
    private static final int    BASE_PORT = 8080;

    private static String scheme() { return USE_TLS ? "https" : "http"; }

    // ── Derived endpoints ──────────────────────────────────────────────────
    public static final String LOCAL_IP = scheme() + "://" + BASE_HOST + ":" + BASE_PORT;
    public static final String SIGNAL_URL = LOCAL_IP;
    public static final String SIGNAL_CDN_URL = LOCAL_IP + "/cdn";
    public static final String SIGNAL_CONTACT_DISCOVERY_URL = LOCAL_IP + "/directory";
    public static final String SIGNAL_KEY_BACKUP_URL = LOCAL_IP + "/backup";

    // PSTN / Dumin Gateway Endpoint
    public static final String DUMIN_GATEWAY_URL = "http://192.168.1.100:5060";
}
