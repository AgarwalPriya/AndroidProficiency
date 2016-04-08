package com.example.androidProficiency;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsListAdapter extends BaseAdapter {
	private ArrayList<NewsItem> listData;
	private LayoutInflater layoutInflater;
	private NewsImageDownloader imageDownLoader = null;
	private Activity mActivity = null;
	
	public NewsListAdapter(Activity activity, ArrayList<NewsItem> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(activity.getApplicationContext());
		imageDownLoader = 
				new NewsImageDownloader(activity.getApplicationContext());
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
		convertView = null;
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
		NewsItem jsonFeedItem = listData.get(position);
		
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
				imageDownLoader.displayImage(jsonFeedItem.getImageHref(), mActivity, holder.imageView);
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
