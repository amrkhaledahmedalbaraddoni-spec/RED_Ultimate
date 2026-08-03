package org.thoughtcrime.securesms.dependencies;

import org.thoughtcrime.securesms.BuildConfig;

/**
 * RED endpoint configuration.
 *
 * The Android client and the merged RED feature surface use the generated BuildConfig values as
 * their single source of truth. Override them at build time with the `red.server.url`,
 * `red.dumin.ip`, and `red.dumin.gateway.url` Gradle properties instead of editing source code.
 */
public final class DevelopedServerConfig {

    private DevelopedServerConfig() {
    }

    public static final String SIGNAL_URL = BuildConfig.RED_SERVER_URL;
    public static final String SIGNAL_CDN_URL = BuildConfig.SIGNAL_CDN_URL;
    public static final String SIGNAL_CONTACT_DISCOVERY_URL = SIGNAL_URL + "/directory";
    public static final String SIGNAL_KEY_BACKUP_URL = SIGNAL_URL + "/backup";
    public static final String DUMIN_GATEWAY_URL = BuildConfig.RED_DUMIN_GATEWAY_URL;
}
