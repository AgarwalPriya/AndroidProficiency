package com.example.test;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JsonListAdapter extends BaseAdapter {
	private ArrayList<RowItem> listData;
	private LayoutInflater layoutInflater;
	private LazyImageDownloader imageDownLoader = null;
	private Activity mActivity = null;
	
	public JsonListAdapter(Activity activity, ArrayList<RowItem> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(activity.getApplicationContext());
		imageDownLoader = 
				new LazyImageDownloader(activity.getApplicationContext());
		mActivity = activity;
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
				//new ImageDownloader(holder.imageView).execute(jsonFeedItem.getImageHref());
				imageDownLoader.displayImage(jsonFeedItem.getImageHref(), mActivity, holder.imageView);
			}
		}	
		if (holder.title.getVisibility() == View.GONE && holder.desc.getVisibility() == View.GONE && holder.imageView.getVisibility() == View.GONE) {
			convertView.setVisibility(View.GONE);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView desc;
		ImageView imageView;
	}
}
