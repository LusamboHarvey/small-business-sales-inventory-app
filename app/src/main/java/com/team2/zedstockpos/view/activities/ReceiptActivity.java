
package com.team2.zedstockpos.view.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.team2.zedstockpos.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceiptActivity extends AppCompatActivity {

    private final int green = Color.rgb(30, 99, 48);
    private final int darkGreen = Color.rgb(24, 85, 40);
    private final int grey = Color.rgb(105, 112, 122);

    private String receiptNumber;
    private String cashier;
    private String dateTime;
    private String[] itemNames;
    private int[] quantities;
    private double[] prices;
    private double subtotal;
    private double discount;
    private double total;
    private String paymentMethod = "Cash";

    private LinearLayout receiptContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readReceiptData();
        buildReceiptScreen();
    }

    private void readReceiptData() {
        Intent intent = getIntent();

        receiptNumber = intent.getStringExtra("receipt_number");
        cashier = intent.getStringExtra("cashier");
        dateTime = intent.getStringExtra("date_time");
        itemNames = intent.getStringArrayExtra("item_names");
        quantities = intent.getIntArrayExtra("quantities");
        prices = intent.getDoubleArrayExtra("prices");

        subtotal = intent.getDoubleExtra("subtotal", 0);
        discount = intent.getDoubleExtra("discount", 0);
        total = intent.getDoubleExtra("total", 0);
        paymentMethod = intent.getStringExtra("payment_method");

        if (receiptNumber == null) receiptNumber = "N/A";
        if (cashier == null || cashier.trim().isEmpty()) {
            cashier = "Cashier";
        }
        if (dateTime == null) {
            dateTime = new SimpleDateFormat(
                    "dd MMM yyyy • hh:mm a",
                    Locale.getDefault()
            ).format(new Date());
        }
        if (itemNames == null) itemNames = new String[0];
        if (quantities == null) quantities = new int[0];
        if (prices == null) prices = new double[0];
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "Cash";
        }
    }

    private void buildReceiptScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(247, 248, 245));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(12), dp(16), dp(20));
        scrollView.addView(root);

        TextView heading = text("Receipt", 25, Color.BLACK, true);
        root.addView(heading);

        TextView subtitle = text(
                "Completed Sale",
                14,
                grey,
                false
        );
        root.addView(subtitle);

        addSpace(root, 18);

        // Green success banner
        LinearLayout successBanner = new LinearLayout(this);
        successBanner.setOrientation(LinearLayout.VERTICAL);
        successBanner.setPadding(dp(18), dp(20), dp(18), dp(20));
        successBanner.setBackgroundColor(green);

        successBanner.addView(
                text("✓  Sale Completed", 23, Color.WHITE, true)
        );
        successBanner.addView(
                text(
                        "Thank you for your business!",
                        15,
                        Color.WHITE,
                        false
                )
        );

        root.addView(successBanner);

        // Receipt body
        receiptContent = new LinearLayout(this);
        receiptContent.setOrientation(LinearLayout.VERTICAL);
        receiptContent.setPadding(dp(16), dp(18), dp(16), dp(18));
        receiptContent.setBackgroundColor(Color.WHITE);

        root.addView(receiptContent);

        receiptContent.addView(
                text("YOUR BUSINESS NAME", 20, darkGreen, true)
        );
        receiptContent.addView(
                text(
                        "Supplies • Groceries • Lusaka, Zambia",
                        13,
                        grey,
                        false
                )
        );

        addDivider(receiptContent);

        receiptContent.addView(
                text("Date & Time", 13, grey, false)
        );
        receiptContent.addView(
                text(dateTime, 15, Color.DKGRAY, true)
        );

        addSpace(receiptContent, 10);

        receiptContent.addView(
                text("Cashier", 13, grey, false)
        );
        receiptContent.addView(
                text(cashier, 15, Color.DKGRAY, true)
        );

        addSpace(receiptContent, 10);

        receiptContent.addView(
                text("Receipt No.", 13, grey, false)
        );
        receiptContent.addView(
                text(receiptNumber, 15, Color.DKGRAY, true)
        );

        addDivider(receiptContent);

        // Item table header
        LinearLayout tableHeader = new LinearLayout(this);
        tableHeader.setOrientation(LinearLayout.HORIZONTAL);

        addTableCell(tableHeader, "Item", 2, Gravity.START, true);
        addTableCell(tableHeader, "Qty", 1, Gravity.CENTER, true);
        addTableCell(tableHeader, "Price", 2, Gravity.END, true);
        addTableCell(tableHeader, "Total", 2, Gravity.END, true);

        receiptContent.addView(tableHeader);
        addSpace(receiptContent, 8);

        int count = Math.min(
                itemNames.length,
                Math.min(quantities.length, prices.length)
        );

        for (int i = 0; i < count; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(7), 0, dp(7));

            addTableCell(row, itemNames[i], 2, Gravity.START, false);
            addTableCell(
                    row,
                    String.valueOf(quantities[i]),
                    1,
                    Gravity.CENTER,
                    false
            );
            addTableCell(
                    row,
                    money(prices[i]),
                    2,
                    Gravity.END,
                    false
            );
            addTableCell(
                    row,
                    money(prices[i] * quantities[i]),
                    2,
                    Gravity.END,
                    false
            );

            receiptContent.addView(row);
        }

        addDivider(receiptContent);

        addSummaryRow("Subtotal", money(subtotal), false);
        addSummaryRow("Discount", "- " + money(discount), false);
        addSummaryRow("Total", money(total), true);

        addDivider(receiptContent);

        addSummaryRow("Payment Method", paymentMethod, false);
        addSummaryRow("Amount Paid", money(total), true);

        addSpace(root, 14);

        TextView shareHint = text(
                "You can share this receipt with your customer.",
                14,
                darkGreen,
                false
        );
        shareHint.setGravity(Gravity.CENTER);
        shareHint.setPadding(dp(10), dp(14), dp(10), dp(14));
        shareHint.setBackgroundColor(Color.rgb(229, 239, 228));
        root.addView(shareHint);

        addSpace(root, 14);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button shareButton = new Button(this);
        shareButton.setText("Share PDF");
        shareButton.setOnClickListener(v -> sharePdf());

        Button printButton = new Button(this);
        printButton.setText("Print / Save");
        printButton.setOnClickListener(v -> printPdf());

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(0, dp(54), 1);

        actionParams.setMargins(0, 0, dp(6), 0);
        actions.addView(shareButton, actionParams);

        LinearLayout.LayoutParams printParams =
                new LinearLayout.LayoutParams(0, dp(54), 1);

        printParams.setMargins(dp(6), 0, 0, 0);
        actions.addView(printButton, printParams);

        root.addView(actions);

        addSpace(root, 12);

        Button newSaleButton = new Button(this);
        newSaleButton.setText("🛒  New Sale");
        newSaleButton.setTextColor(Color.WHITE);
        newSaleButton.setTextSize(17);
        newSaleButton.setAllCaps(false);
        newSaleButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(green)
        );

        newSaleButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    ReceiptActivity.this,
                    CashierSalesActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            );

            startActivity(intent);
            finish();
        });

        root.addView(
                newSaleButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                )
        );

        setContentView(scrollView);
    }

    private void addSummaryRow(
            String label,
            String value,
            boolean emphasize
    ) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(7), 0, dp(7));

        TextView left = text(
                label,
                emphasize ? 17 : 14,
                emphasize ? darkGreen : grey,
                emphasize
        );

        TextView right = text(
                value,
                emphasize ? 18 : 14,
                emphasize ? darkGreen : Color.DKGRAY,
                emphasize
        );

        row.addView(
                left,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        right.setGravity(Gravity.END);

        row.addView(
                right,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        receiptContent.addView(row);
    }

    private void addTableCell(
            LinearLayout row,
            String value,
            int weight,
            int gravity,
            boolean bold
    ) {
        TextView cell = text(
                value,
                11,
                bold ? Color.DKGRAY : Color.BLACK,
                bold
        );

        cell.setGravity(gravity | Gravity.CENTER_VERTICAL);
        cell.setMaxLines(3);

        row.addView(
                cell,
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        weight
                )
        );
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold
    ) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    private void addSpace(LinearLayout parent, int dp) {
        View space = new View(this);
        parent.addView(
                space,
                new LinearLayout.LayoutParams(
                        1,
                        this.dp(dp)
                )
        );
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(225, 228, 225));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        params.setMargins(0, dp(12), 0, dp(12));
        parent.addView(divider, params);
    }

    private String money(double amount) {
        return String.format(
                Locale.getDefault(),
                "ZMW %.2f",
                amount
        );
    }

    private int dp(int value) {
        return (int) (
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    // Creates a PDF containing the receipt text.
    private File createReceiptPdf() throws Exception {
        PdfDocument document = new PdfDocument();

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        595,
                        842,
                        1
                ).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        android.graphics.Canvas canvas = page.getCanvas();
        android.graphics.Paint paint =
                new android.graphics.Paint(
                        android.graphics.Paint.ANTI_ALIAS_FLAG
                );

        int x = 36;
        int y = 48;

        paint.setColor(green);
        paint.setTextSize(22);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("SALE COMPLETED", x, y, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(12);
        paint.setTypeface(Typeface.DEFAULT);

        y += 30;
        canvas.drawText("YOUR BUSINESS NAME", x, y, paint);
        y += 20;
        canvas.drawText("Receipt: " + receiptNumber, x, y, paint);
        y += 18;
        canvas.drawText("Date: " + dateTime, x, y, paint);
        y += 18;
        canvas.drawText("Cashier: " + cashier, x, y, paint);

        y += 32;
        canvas.drawText("ITEM", x, y, paint);
        canvas.drawText("QTY", 300, y, paint);
        canvas.drawText("PRICE", 360, y, paint);
        canvas.drawText("TOTAL", 465, y, paint);

        int count = Math.min(
                itemNames.length,
                Math.min(quantities.length, prices.length)
        );

        y += 20;

        for (int i = 0; i < count; i++) {
            if (y > 750) break;

            String name = itemNames[i];

            if (name.length() > 30) {
                name = name.substring(0, 27) + "...";
            }

            canvas.drawText(name, x, y, paint);
            canvas.drawText(
                    String.valueOf(quantities[i]),
                    300,
                    y,
                    paint
            );
            canvas.drawText(
                    String.format(
                            Locale.US,
                            "%.2f",
                            prices[i]
                    ),
                    360,
                    y,
                    paint
            );
            canvas.drawText(
                    String.format(
                            Locale.US,
                            "%.2f",
                            prices[i] * quantities[i]
                    ),
                    465,
                    y,
                    paint
            );

            y += 22;
        }

        y += 24;
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("Subtotal: " + money(subtotal), x, y, paint);
        y += 22;
        canvas.drawText("Discount: " + money(discount), x, y, paint);
        y += 25;
        canvas.drawText("TOTAL: " + money(total), x, y, paint);
        y += 25;
        canvas.drawText("Payment: " + paymentMethod, x, y, paint);

        document.finishPage(page);

        File directory = new File(
                getCacheDir(),
                "receipts"
        );

        if (!directory.exists() && !directory.mkdirs()) {
            document.close();
            throw new Exception("Could not create receipt folder.");
        }

        File file = new File(
                directory,
                "Receipt_" + receiptNumber.replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                ) + ".pdf"
        );

        try (FileOutputStream output =
                     new FileOutputStream(file)) {
            document.writeTo(output);
        } finally {
            document.close();
        }

        return file;
    }

    private void sharePdf() {
        try {
            File file = createReceiptPdf();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Receipt " + receiptNumber
            );
            shareIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            startActivity(
                    Intent.createChooser(
                            shareIntent,
                            "Share Receipt"
                    )
            );

        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "Could not create the receipt PDF.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void printPdf() {
        try {
            File file = createReceiptPdf();

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    file
            );

            Intent printIntent = new Intent(Intent.ACTION_VIEW);
            printIntent.setDataAndType(uri, "application/pdf");
            printIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );

            try {
                startActivity(
                        Intent.createChooser(
                                printIntent,
                                "Print or Save Receipt"
                        )
                );
            } catch (Exception exception) {
                Toast.makeText(
                        this,
                        "Install a PDF viewer to print or save the receipt.",
                        Toast.LENGTH_LONG
                ).show();
            }

        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "Could not generate the receipt PDF.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
