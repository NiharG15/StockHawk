package com.sam_chordas.android.stockhawk.model;

/**
 * Created by niharg on 3/8/16 at 6:57 PM.
 */

public class Quote {

    private String Symbol;
    private String Date;
    private String Open;
    private String High;
    private String Low;
    private String Close;

    public Quote() {
    }

    public Quote(String symbol, String date, String open, String high, String low, String close) {
        Symbol = symbol;
        Date = date;
        Open = open;
        High = high;
        Low = low;
        Close = close;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getOpen() {
        return Open;
    }

    public void setOpen(String open) {
        Open = open;
    }

    public String getHigh() {
        return High;
    }

    public void setHigh(String high) {
        High = high;
    }

    public String getLow() {
        return Low;
    }

    public void setLow(String low) {
        Low = low;
    }

    public String getClose() {
        return Close;
    }

    public void setClose(String close) {
        Close = close;
    }
}
