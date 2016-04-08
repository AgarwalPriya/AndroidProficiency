package com.example.test;

import android.graphics.Bitmap;

public class RowItem {
	private String imageHref = "";
	private String title = "";
	private String desc = "";
	private Bitmap imageBitmap = null;

	public RowItem() {}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getImageHref() {
		return imageHref;
	}
	public void setImageHref(String imageHref) {
		this.imageHref = imageHref;
	}
	
	public void setImageBitmap(Bitmap imageBitmap) {
		this.imageBitmap = imageBitmap;
	}
	
	public Bitmap getImageBitmap() {
		return imageBitmap;
	}

}
