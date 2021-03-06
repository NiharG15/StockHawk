package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.model.QueryResult;
import com.sam_chordas.android.stockhawk.model.Quote;
import com.sam_chordas.android.stockhawk.service.HistoricalDataService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StockDetailActivity extends AppCompatActivity {

    public static final String ARG_STOCK_SYMBOL = "stock_symbol";
    @Bind(R.id.stock_symbol)
    TextView tvstockSymbol;
    @Bind(R.id.lineChart)
    LineChart lineChart;
    @Bind(R.id.stock_name)
    TextView stockName;
    @Bind(R.id.change)
    TextView change;
    @Bind(R.id.bid_price)
    TextView bidPrice;
    private String stockSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);
        stockSymbol = getIntent().getStringExtra(ARG_STOCK_SYMBOL);
        if (stockSymbol == null) {
            stockSymbol = "YHOO";
        }

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting())) {
            Snackbar.make(findViewById(R.id.detail_parent), R.string.offline_mode, Snackbar.LENGTH_INDEFINITE).show();
        }

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.historical_data_base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        final HistoricalDataService dataService = retrofit.create(HistoricalDataService.class);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date today = Calendar.getInstance().getTime();
            String dateStart = sdf.format(today);
            Calendar calendarInstance = Calendar.getInstance();
            calendarInstance.add(Calendar.MONTH, -1);

            Date aMonthAgo = calendarInstance.getTime();
            String dateEnd = sdf.format(aMonthAgo);

            String query = URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol = \"" + stockSymbol + "\" and startDate = \"" + dateEnd + "\" and endDate = \"" + dateStart + "\"", "UTF-8");
            Log.d("Query: ", query);
            Call<QueryResult> call = dataService.getHistoricalData(query, "json", "http%3A%2F%2Fdatatables.org%2Falltables.env");
            call.enqueue(new Callback<QueryResult>() {
                @Override
                public void onResponse(Call<QueryResult> call, Response<QueryResult> response) {
                    QueryResult qr = response.body();

                    List<Quote> quoteList = qr.getQuery().getResults().getQuote();

                    /** The Chart **/
                    ArrayList<Entry> entryList = new ArrayList<>();
                    ArrayList<String> xVals = new ArrayList<String>();

                    for (int i = quoteList.size() - 1; i >= 0; i--) {

                        entryList.add(new BarEntry(Float.parseFloat(quoteList.get(i).getClose()), i));
                        xVals.add(quoteList.get(i).getDate());

                    }

                    Collections.sort(entryList, new Comparator<Entry>() {
                        @Override
                        public int compare(Entry barEntry, Entry t1) {
                            return barEntry.getXIndex() - t1.getXIndex();
                        }
                    });

                    LineDataSet lineDataSet = new LineDataSet(entryList, getString(R.string.chart_legend_stock_data));
                    lineDataSet.setColor(Color.WHITE);
                    lineDataSet.setValueTextColor(Color.WHITE);
                    lineDataSet.setCircleColor(Color.WHITE);
                    lineChart.getXAxis().setGridColor(Color.WHITE);
                    lineChart.getAxisLeft().setGridColor(Color.WHITE);
                    lineChart.getAxisRight().setGridColor(Color.WHITE);
                    lineChart.getXAxis().setTextColor(Color.WHITE);
                    lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    lineChart.getAxisLeft().setTextColor(Color.WHITE);
                    lineChart.getAxisRight().setTextColor(Color.WHITE);
                    lineChart.setData(new LineData(xVals, lineDataSet));
                    lineChart.getLegend().setTextColor(Color.WHITE);
                    lineChart.setDescription("");
                    lineChart.invalidate();
                }

                @Override
                public void onFailure(Call<QueryResult> call, Throwable t) {

                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String name = "";
        String percentChange = "";
        String bid = "";
        int isUp = 0;
        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, QuoteColumns.SYMBOL + "=?" + " AND " + QuoteColumns.ISCURRENT + "=?", new String[]{stockSymbol, "1"}, null);
        if (c != null && c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(QuoteColumns.NAME));
            bid = c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE));
            percentChange = c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            isUp = c.getInt(c.getColumnIndex(QuoteColumns.ISUP));
        }

        if (c != null) {
            c.close();
        }

        stockName.setText(name);
        stockName.setSelected(true);
        tvstockSymbol.setText(stockSymbol);
        change.setText(percentChange);
        bidPrice.setText(bid);

        if (isUp == 1) {
            change.setBackgroundResource(R.drawable.percent_change_pill_green);
        } else {
            change.setBackgroundResource(R.drawable.percent_change_pill_red);
        }

    }
}
