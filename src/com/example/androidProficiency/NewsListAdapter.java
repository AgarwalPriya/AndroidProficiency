package com.example.androidProficiency;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * @author Priyanka
 *
 */
public class NewsListAdapter extends BaseAdapter {
	private ArrayList<NewsItem> mListData;
	private LayoutInflater mLayoutInflater;
	private String TAG = "NewsListAdapter";
	private Context mContext = null;

	public NewsListAdapter(Activity activity) {
		//this.mListData = listData;
		mLayoutInflater = LayoutInflater.from(activity.getApplicationContext());
		mContext = activity.getApplicationContext();
	}

	@Override
	public int getCount() {
		return mListData.size();
	}

	@Override
	public Object getItem(int position) {
		return mListData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		convertView = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.list_row_layout, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.desc = (TextView) convertView.findViewById(R.id.description);
			holder.imageView = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		//cleanup
		holder.title.setText("");
		holder.desc.setText("");
		holder.imageView.setImageResource(R.drawable.imageholder);

		NewsItem jsonFeedItem = mListData.get(position);

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
				new ImageDownloaderTask(jsonFeedItem.getImageHref(),holder.imageView).execute(jsonFeedItem.getImageHref());
				//mImageDownLoader.displayImage(jsonFeedItem.getImageHref(),holder.imageView);
			}
		}	
		return convertView;
	}

	static class ViewHolder {
		TextView title;
		TextView desc;
		ImageView imageView;
	}

	public void setListData(ArrayList<NewsItem> listData) {
		this.mListData = listData;
	}

	private class ImageDownloaderTask extends AsyncTask<String, Integer, Bitmap> {

		private int MAX_RETRY_COUNT = 5;

		private ImageView imageView;
		public ImageDownloaderTask(String url,ImageView view){
			imageView = view;	
		}


		@Override
		protected void onProgressUpdate(Integer... values) {}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Bitmap doInBackground(String... params) {
			//int retryCount = 0;
			String url = params[0];
			Log.e(TAG,"url ="+url);
			Bitmap bitmap = LocalCache.getInstance().getImageFromCache(url);
			if(bitmap == null) {
				//while(retryCount < MAX_RETRY_COUNT){
				bitmap = getBitmapFromURL(url);
				if(bitmap != null){
					LocalCache.getInstance().storeImageInCache(url, bitmap);
					//break;
				} else {
					return null;
				}
				//retryCount = retryCount + 1;
				//}
			}
			WeakReference<Bitmap> wrb = new WeakReference<Bitmap>(bitmap);
			return wrb.get();
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			if (bitmap != null) {
				imageView.setImageBitmap(bitmap);
				float density = mContext.getResources()
						.getDisplayMetrics().density;

				int modifiedHT = (int) (bitmap.getHeight() * density);
				int modifiedWidth = (int) (bitmap.getWidth() * density);
				if (modifiedHT > 400 || modifiedWidth > 400) {
					modifiedHT = 350;
					modifiedWidth = 350;
				}

				imageView.getLayoutParams().height = (int) modifiedHT;
				imageView.getLayoutParams().width = (int) modifiedWidth;
			} else {
				imageView.setImageResource(R.drawable.imageholder);
			}
		}	

		private Bitmap getBitmapFromURL(String url) {
			final DefaultHttpClient client = new DefaultHttpClient();
			final HttpGet getRequest = new HttpGet(url);
			try {
				HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					Log.e(TAG,"Error " + statusCode + "for url = " + url);
					return null;
				}
				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						inputStream = entity.getContent();
						final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
						WeakReference<Bitmap> wrb = new WeakReference<Bitmap>(bitmap);
						return wrb.get();
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			} catch (Exception e) {
				getRequest.abort();
				Log.e(TAG, "Exception " + url +" "+ e.toString());
			} 
			return null;
		}

	}



}
