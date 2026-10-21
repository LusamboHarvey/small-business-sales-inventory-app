
package com.team2.zedstockpos.view.activities;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.team2.zedstockpos.DatabaseHelper;
import com.team2.zedstockpos.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SalesReportsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private TextView txtReportRevenue;
    private TextView txtReportTransactions;
    private TextView txtStockTurnover;
    private TextView txtRevenueChange;
    private TextView txtTransactionsChange;
    private TextView txtRevenueComparison;
    private TextView txtRevenuePercentage;
    private TextView txtReportDateRange;
    private TextView txtNoBestSellingProducts;

    private LinearLayout bestSellingProductsContainer;
    private LineChart revenueChart;

    private final Calendar selectedDate = Calendar.getInstance();
    private String selectedPeriod = "Weekly";

    private final int green = Color.rgb(36, 107, 59);
    private final int grey = Color.rgb(135, 145, 137);
    private final int dark = Color.rgb(32, 41, 35);
    private final int orange = Color.rgb(239, 145, 45);

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private final SimpleDateFormat dayFormat =
            new SimpleDateFormat("dd MMM", Locale.getDefault());

    private final SimpleDateFormat hourFormat =
            new SimpleDateFormat("ha", Locale.getDefault());

    private static class Period {
        final long start;
        final long end;

        Period(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }

    private static class Summary {
        double revenue;
        int transactions;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_reports);

        databaseHelper = new DatabaseHelper(this);

        bindViews();
        setupChart();
        setupPeriodButtons();
        setupDatePicker();

        View menuButton = findViewById(R.id.btnReportsMenu);
        if (menuButton != null) {
            menuButton.setOnClickListener(v ->
                    Toast.makeText(this, "Reports menu",
                            Toast.LENGTH_SHORT).show());
        }

        View allProductsButton = findViewById(R.id.btnViewAllProducts);
        if (allProductsButton != null) {
            allProductsButton.setOnClickListener(v ->
                    Toast.makeText(this,
                            "Showing the best-selling products for this period.",
                            Toast.LENGTH_SHORT).show());
        }

        selectPeriodButton(R.id.btnDaily, false);
        selectPeriodButton(R.id.btnWeekly, true);
        selectPeriodButton(R.id.btnMonthly, false);

        refreshReports();
    }

    private void bindViews() {
        txtReportRevenue = findViewById(R.id.txtReportRevenue);
        txtReportTransactions = findViewById(R.id.txtReportTransactions);
        txtStockTurnover = findViewById(R.id.txtStockTurnover);
        txtRevenueChange = findViewById(R.id.txtRevenueChange);
        txtTransactionsChange = findViewById(R.id.txtTransactionsChange);
        txtRevenueComparison = findViewById(R.id.txtRevenueComparison);
        txtRevenuePercentage = findViewById(R.id.txtRevenuePercentage);
        txtReportDateRange = findViewById(R.id.txtReportDateRange);

        bestSellingProductsContainer =
                findViewById(R.id.bestSellingProductsContainer);

        txtNoBestSellingProducts =
                findViewById(R.id.txtNoBestSellingProducts);

        revenueChart = findViewById(R.id.revenueChart);
    }

    private void setupChart() {
        revenueChart.setNoDataText("No sales recorded for this period.");
        revenueChart.setNoDataTextColor(grey);
        revenueChart.setDrawGridBackground(false);
        revenueChart.setDrawBorders(false);
        revenueChart.setTouchEnabled(true);
        revenueChart.setDragEnabled(true);
        revenueChart.setScaleEnabled(true);
        revenueChart.setPinchZoom(true);

        Description description = new Description();
        description.setText("");
        revenueChart.setDescription(description);

        revenueChart.getAxisRight().setEnabled(false);
        revenueChart.getAxisLeft().setTextColor(grey);
        revenueChart.getAxisLeft().setGridColor(
                Color.rgb(232, 235, 231));
        revenueChart.getAxisLeft().setAxisMinimum(0f);
        revenueChart.getLegend().setEnabled(true);
        revenueChart.getLegend().setTextColor(dark);

        XAxis xAxis = revenueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(grey);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-30f);
    }

    private void setupPeriodButtons() {
        findViewById(R.id.btnDaily).setOnClickListener(v ->
                changePeriod("Daily"));

        findViewById(R.id.btnWeekly).setOnClickListener(v ->
                changePeriod("Weekly"));

        findViewById(R.id.btnMonthly).setOnClickListener(v ->
                changePeriod("Monthly"));
    }

    private void changePeriod(String period) {
        selectedPeriod = period;

        selectPeriodButton(R.id.btnDaily, period.equals("Daily"));
        selectPeriodButton(R.id.btnWeekly, period.equals("Weekly"));
        selectPeriodButton(R.id.btnMonthly, period.equals("Monthly"));

        refreshReports();
    }

    private void selectPeriodButton(int id, boolean selected) {
        TextView button = findViewById(id);
        if (button == null) return;

        button.setBackgroundResource(selected
                ? R.drawable.bg_report_selected
                : android.R.color.transparent);

        button.setTextColor(selected ? Color.WHITE : grey);
        button.setTypeface(null, selected
                ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);
    }

    private void setupDatePicker() {
        View.OnClickListener openPicker = v -> {
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, day);
                        selectedDate.set(Calendar.HOUR_OF_DAY, 12);
                        selectedDate.set(Calendar.MINUTE, 0);
                        selectedDate.set(Calendar.SECOND, 0);
                        selectedDate.set(Calendar.MILLISECOND, 0);
                        refreshReports();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        };

        txtReportDateRange.setOnClickListener(openPicker);

        View filterButton = findViewById(R.id.btnReportFilter);
        if (filterButton != null) {
            filterButton.setOnClickListener(openPicker);
        }
    }

    private Period getCurrentPeriod() {
        Calendar start = (Calendar) selectedDate.clone();
        Calendar end;

        if ("Daily".equals(selectedPeriod)) {
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 1);

        } else if ("Weekly".equals(selectedPeriod)) {
            start.setFirstDayOfWeek(Calendar.MONDAY);

            int dayOfWeek = start.get(Calendar.DAY_OF_WEEK);
            int daysFromMonday =
                    (dayOfWeek + 5) % 7;

            start.add(Calendar.DAY_OF_MONTH, -daysFromMonday);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            end = (Calendar) start.clone();
            end.add(Calendar.DAY_OF_MONTH, 7);

        } else {
            start.set(Calendar.DAY_OF_MONTH, 1);
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);
            start.set(Calendar.MILLISECOND, 0);

            end = (Calendar) start.clone();
            end.add(Calendar.MONTH, 1);
        }

        return new Period(
                start.getTimeInMillis(),
                end.getTimeInMillis());
    }

    private Period getPreviousPeriod(Period current) {
        long duration = current.end - current.start;

        return new Period(
                current.start - duration,
                current.start);
    }

    private void refreshReports() {
        Period current = getCurrentPeriod();
        Period previous = getPreviousPeriod(current);

        txtReportDateRange.setText(
                "▦   " + dateFormat.format(new Date(current.start))
                        + " – "
                        + dateFormat.format(new Date(current.end - 1))
                        + "   ⌄"
        );

        Summary now = getSummary(current);
        Summary before = getSummary(previous);

        txtReportRevenue.setText(formatMoney(now.revenue));
        txtReportTransactions.setText(
                String.valueOf(now.transactions));

        // Historical inventory data is required to calculate
        // a genuine stock-turnover metric.
        txtStockTurnover.setText("N/A");

        txtRevenueChange.setText(
                getChangeText(now.revenue, before.revenue));

        txtTransactionsChange.setText(
                getChangeText(now.transactions, before.transactions));

        updateComparison(now, before);
        updateChart(current, previous);
        loadBestSellingProducts(current);
    }

    private Summary getSummary(Period period) {
        Summary summary = new Summary();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT COUNT(*), COALESCE(SUM(total), 0) " +
                        "FROM sales WHERE sale_date >= ? AND sale_date < ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(period.start),
                String.valueOf(period.end)
        })) {
            if (cursor.moveToFirst()) {
                summary.transactions = cursor.getInt(0);
                summary.revenue = cursor.getDouble(1);
            }
        }

        return summary;
    }

    private String formatMoney(double amount) {
        return String.format(
                Locale.getDefault(),
                "ZMW %,.2f",
                amount);
    }

    private String getChangeText(double current, double previous) {
        if (previous == 0) {
            return current == 0
                    ? "No change"
                    : "No previous-period data";
        }

        double percentage =
                ((current - previous) / previous) * 100.0;

        return String.format(
                Locale.getDefault(),
                "%+.1f%% vs previous",
                percentage);
    }

    private void updateComparison(
            Summary current, Summary previous) {

        txtRevenueComparison.setText(
                "Revenue: " + formatMoney(current.revenue)
                        + " vs " + formatMoney(previous.revenue));

        if (previous.revenue == 0) {
            txtRevenuePercentage.setText(
                    current.revenue == 0 ? "0%" : "NEW");
        } else {
            double percentage =
                    ((current.revenue - previous.revenue)
                            / previous.revenue) * 100.0;

            txtRevenuePercentage.setText(
                    String.format(
                            Locale.getDefault(),
                            "%+.1f%%",
                            percentage));
        }
    }

    private void updateChart(Period current, Period previous) {
        Map<String, Float> currentValues = new HashMap<>();
        Map<String, Float> previousValues = new HashMap<>();

        loadChartValues(current, currentValues);
        loadChartValues(previous, previousValues);

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Entry> currentEntries = new ArrayList<>();
        ArrayList<Entry> previousEntries = new ArrayList<>();

        Calendar cursor = Calendar.getInstance();
        cursor.setTimeInMillis(current.start);

        int pointCount;

        if ("Daily".equals(selectedPeriod)) {
            pointCount = 24;
        } else if ("Weekly".equals(selectedPeriod)) {
            pointCount = 7;
        } else {
            pointCount = cursor.getActualMaximum(
                    Calendar.DAY_OF_MONTH);
        }

        Calendar previousStart = Calendar.getInstance();
        previousStart.setTimeInMillis(previous.start);

        for (int i = 0; i < pointCount; i++) {
            Calendar point = (Calendar) cursor.clone();

            if ("Daily".equals(selectedPeriod)) {
                point.add(Calendar.HOUR_OF_DAY, i);
                labels.add(hourFormat.format(point.getTime())
                        .toLowerCase(Locale.getDefault()));
            } else {
                point.add(Calendar.DAY_OF_MONTH, i);
                labels.add(dayFormat.format(point.getTime()));
            }

            String key = makeBucketKey(point);

            float currentRevenue =
                    currentValues.containsKey(key)
                            ? currentValues.get(key) : 0f;

            currentEntries.add(new Entry(i, currentRevenue));

            Calendar previousPoint =
                    (Calendar) previousStart.clone();

            if ("Daily".equals(selectedPeriod)) {
                previousPoint.add(Calendar.HOUR_OF_DAY, i);
            } else {
                previousPoint.add(Calendar.DAY_OF_MONTH, i);
            }

            String previousKey = makeBucketKey(previousPoint);

            float previousRevenue =
                    previousValues.containsKey(previousKey)
                            ? previousValues.get(previousKey) : 0f;

            previousEntries.add(new Entry(i, previousRevenue));
        }

        LineDataSet currentSet =
                new LineDataSet(currentEntries, "This period");

        currentSet.setColor(green);
        currentSet.setCircleColor(green);
        currentSet.setLineWidth(2.5f);
        currentSet.setCircleRadius(3f);
        currentSet.setDrawValues(false);
        currentSet.setDrawFilled(true);
        currentSet.setFillColor(green);
        currentSet.setFillAlpha(35);
        currentSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet previousSet =
                new LineDataSet(previousEntries, "Previous period");

        previousSet.setColor(orange);
        previousSet.setCircleColor(orange);
        previousSet.setLineWidth(1.5f);
        previousSet.setCircleRadius(2f);
        previousSet.setDrawValues(false);

        revenueChart.getXAxis().setValueFormatter(
                new IndexAxisValueFormatter(labels));

        revenueChart.getXAxis().setLabelCount(
                Math.min(labels.size(), 7));

        revenueChart.setData(
                new LineData(currentSet, previousSet));

        revenueChart.invalidate();
    }

    private void loadChartValues(
            Period period, Map<String, Float> values) {

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT sale_date, COALESCE(SUM(total), 0) " +
                        "FROM sales " +
                        "WHERE sale_date >= ? AND sale_date < ? " +
                        "GROUP BY sale_date";

        try (Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(period.start),
                String.valueOf(period.end)
        })) {
            while (cursor.moveToNext()) {
                long timestamp = cursor.getLong(0);
                float revenue = cursor.getFloat(1);

                Calendar point = Calendar.getInstance();
                point.setTimeInMillis(timestamp);

                String key = makeBucketKey(point);

                float existing = values.containsKey(key)
                        ? values.get(key) : 0f;

                values.put(key, existing + revenue);
            }
        }
    }

    private String makeBucketKey(Calendar calendar) {
        if ("Daily".equals(selectedPeriod)) {
            return String.format(
                    Locale.US,
                    "%04d-%03d-%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.DAY_OF_YEAR),
                    calendar.get(Calendar.HOUR_OF_DAY));
        }

        return String.format(
                Locale.US,
                "%04d-%03d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.DAY_OF_YEAR));
    }

    private void loadBestSellingProducts(Period period) {
        bestSellingProductsContainer.removeAllViews();

        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String sql =
                "SELECT product_name, " +
                        "SUM(quantity) AS units_sold, " +
                        "SUM(line_total) AS revenue " +
                        "FROM sale_items " +
                        "INNER JOIN sales ON sales.id = sale_items.sale_id " +
                        "WHERE sales.sale_date >= ? AND sales.sale_date < ? " +
                        "GROUP BY product_name " +
                        "ORDER BY units_sold DESC, revenue DESC " +
                        "LIMIT 5";

        int count = 0;

        try (Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(period.start),
                String.valueOf(period.end)
        })) {
            while (cursor.moveToNext()) {
                String productName = cursor.getString(0);
                int quantity = cursor.getInt(1);
                double revenue = cursor.getDouble(2);

                addBestSellerRow(
                        productName, quantity, revenue);

                count++;
            }
        }

        if (txtNoBestSellingProducts != null) {
            txtNoBestSellingProducts.setVisibility(
                    count == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void addBestSellerRow(
            String name, int quantity, double revenue) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(12, 14, 12, 14);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextColor(dark);
        nameView.setTextSize(14);
        nameView.setTypeface(null,
                android.graphics.Typeface.BOLD);

        TextView quantityView = new TextView(this);
        quantityView.setText(quantity + " units sold");
        quantityView.setTextColor(grey);
        quantityView.setTextSize(12);

        details.addView(nameView);
        details.addView(quantityView);

        TextView revenueView = new TextView(this);
        revenueView.setText(formatMoney(revenue));
        revenueView.setTextColor(green);
        revenueView.setTextSize(13);
        revenueView.setTypeface(null,
                android.graphics.Typeface.BOLD);
        revenueView.setGravity(Gravity.END);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);

        row.addView(details, detailsParams);
        row.addView(revenueView);

        bestSellingProductsContainer.addView(row);

        View divider = new View(this);
        divider.setBackgroundColor(
                Color.rgb(232, 235, 231));

        bestSellingProductsContainer.addView(
                divider,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1));
    }
}
