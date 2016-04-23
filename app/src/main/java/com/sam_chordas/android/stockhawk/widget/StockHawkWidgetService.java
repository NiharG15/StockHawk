package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StockDetailActivity;

/**
 * Created by niharg on 4/21/16 at 6:20 PM.
 */
public class StockHawkWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockHawkRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StockHawkRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor c;

    StockHawkRemoteViewsFactory(Context context, Intent intent) {
        this.mContext = context;
    }

    @Override
    public void onCreate() {
        c = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, QuoteColumns.ISCURRENT + "=?", new String[]{"1"}, null);
        c.moveToFirst();
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        if (c != null) {
            c.close();
        }
    }

    @Override
    public int getCount() {
        return c.getCount();
    }

    @Override
    public RemoteViews getViewAt(int i) {

        Log.d(this.toString(), "getViewAt " + String.valueOf(i));
        c.moveToPosition(i);

        String symbol = c.getString(c.getColumnIndex(QuoteColumns.SYMBOL));
        String perChange = c.getString(c.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_collection_item);
        rv.setTextViewText(R.id.stock_symbol, symbol);
        rv.setTextViewText(R.id.change, perChange);
        Intent intent = new Intent(mContext, StockDetailActivity.class);
        intent.putExtra(StockDetailActivity.ARG_STOCK_SYMBOL, symbol);
        rv.setOnClickFillInIntent(R.id.widget_list_item, intent);
        return rv;

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}