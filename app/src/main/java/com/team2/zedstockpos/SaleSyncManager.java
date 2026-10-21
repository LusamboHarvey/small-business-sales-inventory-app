
package com.team2.zedstockpos;

import android.content.Context;
import android.util.Log;

import com.team2.zedstockpos.model.Sale;
import com.team2.zedstockpos.model.SaleItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaleSyncManager {

    private static final String TAG = "SaleSyncManager";

    private static final String API_URL =
            "http://192.168.42.193:8080/api/sales/sync";

    private final DatabaseHelper databaseHelper;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public interface SyncCallback {
        void onComplete(int syncedCount, int failedCount);
    }

    public SaleSyncManager(Context context) {
        databaseHelper = new DatabaseHelper(
                context.getApplicationContext()
        );
    }

    /**
     * Synchronises queued sales in the background.
     * Call this method from your Activity or other app component.
     */
    public void syncPendingSales(SyncCallback callback) {
        executor.execute(() -> {
            int syncedCount = 0;
            int failedCount = 0;

            try {
                for (long saleId : databaseHelper.getPendingSaleIds()) {
                    if (!databaseHelper.markSaleSyncAttempt(saleId)) {
                        failedCount++;
                        continue;
                    }

                    try {
                        Sale sale = databaseHelper.getSaleForSync(saleId);

                        if (sale == null) {
                            databaseHelper.markSaleSyncFailed(
                                    saleId,
                                    "Sale or sale items could not be found."
                            );
                            failedCount++;
                            continue;
                        }

                        String response = sendSale(sale);

                        JSONObject result = new JSONObject(response);
                        String status = result.optString("status", "");

                        if ("SYNCED".equals(status)
                                || "ALREADY_SYNCED".equals(status)) {

                            databaseHelper.markSaleSynced(saleId);
                            syncedCount++;

                            Log.i(TAG, "Sale synced: " + saleId);

                        } else {
                            databaseHelper.markSaleSyncFailed(
                                    saleId,
                                    "Unexpected server response: " + response
                            );
                            failedCount++;
                        }

                    } catch (Exception e) {
                        String message = e.getMessage();

                        databaseHelper.markSaleSyncFailed(
                                saleId,
                                message == null
                                        ? "Unknown synchronisation error."
                                        : message
                        );

                        Log.e(TAG, "Failed to sync sale " + saleId, e);
                        failedCount++;
                    }
                }
            } finally {
                if (callback != null) {
                    int finalSyncedCount = syncedCount;
                    int finalFailedCount = failedCount;

                    new android.os.Handler(
                            android.os.Looper.getMainLooper()
                    ).post(() -> callback.onComplete(
                            finalSyncedCount,
                            finalFailedCount
                    ));
                }
            }
        });
    }

    private String sendSale(Sale sale) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            JSONObject request = new JSONObject();

            request.put("clientSaleId", sale.getClientSaleId());
            request.put("cashierEmail", sale.getCashierEmail());
            request.put("saleDate", sale.getSaleDate());
            request.put("subtotal", sale.getSubtotal());
            request.put("discount", sale.getDiscount());
            request.put("total", sale.getTotal());

            JSONArray items = new JSONArray();

            for (SaleItem item : sale.getItems()) {
                JSONObject jsonItem = new JSONObject();

                // Local SQLite product IDs may not match MySQL IDs.
                jsonItem.put("productId", JSONObject.NULL);

                jsonItem.put("productName", item.getProductName());
                jsonItem.put("unitPrice", item.getUnitPrice());
                jsonItem.put("quantity", item.getQuantity());
                jsonItem.put("lineTotal", item.getLineTotal());

                items.put(jsonItem);
            }

            request.put("items", items);

            byte[] body = request.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (OutputStream output = connection.getOutputStream()) {
                output.write(body);
                output.flush();
            }

            int responseCode = connection.getResponseCode();

            InputStream stream = responseCode >= 200
                    && responseCode < 400
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            String responseBody = readResponse(stream);

            if (responseCode != HttpURLConnection.HTTP_OK
                    && responseCode != HttpURLConnection.HTTP_CREATED) {

                throw new Exception(
                        "HTTP " + responseCode + ": " + responseBody
                );
            }

            return responseBody;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8
                ))) {

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
