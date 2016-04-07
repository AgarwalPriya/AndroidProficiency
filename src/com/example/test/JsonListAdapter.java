package com.example.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JsonListAdapter extends BaseAdapter {
	private ArrayList<RowItem> listData;
	private LayoutInflater layoutInflater;

	public JsonListAdapter(Context context, ArrayList<RowItem> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.desc = (TextView) convertView.findViewById(R.id.description);
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RowItem jsonFeedItem = listData.get(position);
		
		if(jsonFeedItem.getTitle().equalsIgnoreCase("null")) {
			holder.title.setVisibility(View.GONE);
		} else {
			holder.title.setVisibility(View.VISIBLE);
			holder.title.setText(jsonFeedItem.getTitle());
		}
		if(jsonFeedItem.getDesc().equalsIgnoreCase("null")) {
			holder.desc.setVisibility(View.GONE);
		} else {
			holder.desc.setVisibility(View.VISIBLE);
			holder.desc.setText(jsonFeedItem.getDesc());
		}	
		if(jsonFeedItem.getImageHref().equalsIgnoreCase("null")) {
			holder.imageView.setVisibility(View.GONE);
		} else {
			if (holder.imageView != null) {
				holder.imageView.setVisibility(View.VISIBLE);
				new ImageDownloader(holder.imageView).execute(jsonFeedItem.getImageHref());
			}
		}			
		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView desc;
		ImageView imageView;
	}
}
