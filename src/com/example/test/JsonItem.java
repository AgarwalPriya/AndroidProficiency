package com.example.test;

import java.util.ArrayList;

import android.util.Log;

public class JsonItem {

    private String title;
    private ArrayList<RowItem> rows;
     
    public JsonItem() {}

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
    	
        this.title = title;
        Log.e("PRIYANKA","JsonItem - title = "+title);
    }
    
    public ArrayList<RowItem> getRows() {
        return rows;
    }
    public void setRows(ArrayList<RowItem> rows) {
        this.rows = rows;
        Log.e("PRIYANKA","JsonItem -rows size = "+rows.size());
    }

}
