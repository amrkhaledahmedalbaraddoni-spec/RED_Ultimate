package org.thoughtcrime.securesms.push;

import android.content.Context;
import org.signal.network.config.TrustStore;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * Sovereign trust store for the local RED server.
 *
 * <p>Reads a BKS-formatted CA certificate from {@code assets/red-local-ca.bks}. This
 * is the CA that the sovereign operator generates (see scripts/generate-local-cert.sh)
 * and which nginx uses for TLS termination.</p>
 *
 * <p><b>Wiring:</b> To activate, replace the {@code SignalServiceTrustStore} in
 * {@code NetworkDependenciesModule.signalOkHttpClient()} with an instance of this class
 * so the app pins the sovereign CA instead of Signal's cloud CA.</p>
 */
public class REDLocalTrustStore implements TrustStore {

    private static final String ASSET_NAME = "red-local-ca.bks";
    private static final String KEYSTORE_PASSWORD = "red_sovereign_2024";

    private final Context context;

    public REDLocalTrustStore(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public InputStream getKeyStoreInputStream() {
        try {
            return context.getAssets().open(ASSET_NAME);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Sovereign CA not found in assets/" + ASSET_NAME + ". "
                + "Run scripts/generate-local-cert.sh and copy the CA to app/src/main/assets/.",
                e
            );
        }
    }

    @Override
    public String getKeyStorePassword() {
        return KEYSTORE_PASSWORD;
    }
}
