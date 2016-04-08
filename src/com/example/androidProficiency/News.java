package com.example.androidProficiency;

import java.util.ArrayList;

import android.util.Log;

public class News {

    private String title;
    private ArrayList<NewsItem> rows;
     
    public News() {}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    public ArrayList<NewsItem> getRows() {
        return rows;
    }
    public void setRows(ArrayList<NewsItem> rows) {
        this.rows = rows;
    }

}
