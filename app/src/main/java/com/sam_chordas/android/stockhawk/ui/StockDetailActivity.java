package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
    BarChart lineChart;
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

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okhttp3.OkHttpClient httpClient = new okhttp3.OkHttpClient.Builder()
//                .addInterceptor(httpLoggingInterceptor)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://query.yahooapis.com/v1/public/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        HistoricalDataService dataService = retrofit.create(HistoricalDataService.class);

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
            String path = "https://query.yahooapis.com/v1/public/yql?format=json&env=http%3A%2F%2Fdatatables.org%2Falltables.env" + "&q=" + query;
            Log.d("Path: ", path);
            Call<QueryResult> call = dataService.getHistoricalData(query, "json", "http%3A%2F%2Fdatatables.org%2Falltables.env");
            call.enqueue(new Callback<QueryResult>() {
                @Override
                public void onResponse(Call<QueryResult> call, Response<QueryResult> response) {
                    Log.d("Result Code:", String.valueOf(response.code()));
                    QueryResult qr = response.body();

                    List<Quote> quoteList = qr.getQuery().getResults().getQuote();

                    /** The Chart **/
                    ArrayList<BarEntry> entryList = new ArrayList<BarEntry>();
                    ArrayList<String> xVals = new ArrayList<String>();

                    for (int i = quoteList.size() - 1; i >= 0; i--) {

                        entryList.add(new BarEntry(Float.parseFloat(quoteList.get(i).getClose()), i));
                        xVals.add(quoteList.get(i).getDate());

                    }

                    BarDataSet lineDataSet = new BarDataSet(entryList, "Stock Data");
                    lineDataSet.setColor(ContextCompat.getColor(StockDetailActivity.this, R.color.material_blue_500));
                    lineDataSet.setValueTextColor(Color.WHITE);
                    lineChart.getXAxis().setTextColor(Color.WHITE);
                    lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    lineChart.getAxisLeft().setTextColor(Color.WHITE);
                    lineChart.getAxisRight().setTextColor(Color.WHITE);
                    lineChart.setData(new BarData(xVals, lineDataSet));
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
        Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, QuoteColumns.SYMBOL + "=?", new String[]{stockSymbol}, null);
        if (c != null && c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(QuoteColumns.NAME));
            bid = c.getString(c.getColumnIndex(QuoteColumns.BIDPRICE));
            percentChange = c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
        }

        if (c != null) {
            c.close();
        }

        stockName.setText(name);
        tvstockSymbol.setText(stockSymbol);
        change.setText(percentChange);
        bidPrice.setText(bid);

    }
}
