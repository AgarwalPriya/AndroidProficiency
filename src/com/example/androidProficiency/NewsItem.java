package com.example.androidProficiency;

public class NewsItem {
	private String imageHref = "";
	private String title = "";
	private String desc = "";

	public NewsItem() {}

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

}
