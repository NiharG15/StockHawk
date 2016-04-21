package com.sam_chordas.android.stockhawk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by niharg on 3/8/16 at 6:57 PM.
 */
public class Results {


    private List<Quote> quote = new ArrayList<>();

    public Results() {
    }

    public List<Quote> getQuote() {
        return quote;
    }

    public void setQuote(List<Quote> quote) {
        this.quote = quote;
    }

}
