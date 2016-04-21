package com.sam_chordas.android.stockhawk.model;

/**
 * Created by niharg on 3/8/16 at 6:55 PM.
 */
public class Query {

    private int count;
    private Results results;

    public Query() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}
