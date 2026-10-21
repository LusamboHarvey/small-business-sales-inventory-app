
package com.team2.zedstockpos;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class SaleSyncHelper {

    private static final String TAG = "SaleSyncHelper";

    private SaleSyncHelper() {
        // Utility class.
    }

    public static void syncPendingSales(Context context) {
        Context appContext = context.getApplicationContext();

        SaleSyncManager manager =
                new SaleSyncManager(appContext);

        manager.syncPendingSales((syncedCount, failedCount) -> {
            if (syncedCount > 0) {
                Toast.makeText(
                        appContext,
                        syncedCount + " sale(s) synchronised.",
                        Toast.LENGTH_SHORT
                ).show();

                Log.i(TAG, "Synchronised sales: " + syncedCount);
            }

            if (failedCount > 0) {
                Log.w(TAG, "Sales still awaiting synchronisation: "
                        + failedCount);
            }

            manager.shutdown();
        });
    }
}
