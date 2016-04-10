package com.example.androidProficiency;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Priyanka
 *
 */
public class NewsListActivity extends Activity {

	private static final String DOWNLOAD_URL = "https://dl.dropboxusercontent.com/u/746330/facts.json";

	private ListView mListView = null;
	private ProgressDialog mProgressDialog = null;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private NewsListAdapter mListAdapter = null;
	private ArrayList<NewsItem> mFeedList = new ArrayList<NewsItem>();
	private String TAG = "NewsListActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(!haveNetworkConnection()){
			AlertDialog alertDialog = new AlertDialog.Builder(NewsListActivity.this).create();
			alertDialog.setTitle("Error");
			alertDialog.setMessage("Please check the network");
			alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					NewsListActivity.this.finish();
				}
			});
			alertDialog.show();
		}else{
			setContentView(R.layout.activity_list);
			mListView = (ListView) findViewById(R.id.list_view);
			mListAdapter = new NewsListAdapter(NewsListActivity.this); 
			mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipetorefresh);

			mListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					int pos = position + 1;
					Toast.makeText(NewsListActivity.this, "Selected :" + " " + pos, Toast.LENGTH_LONG).show();
				}
			});

			mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					refreshContent();
					mSwipeRefreshLayout.setRefreshing(false);
				}
			});

			new DownloadJsonTask().execute(DOWNLOAD_URL);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Handling the event for refresh button.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshContent();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshContent() { 
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				new DownloadJsonTask().execute(DOWNLOAD_URL);
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}, 2000);
	}

	private class DownloadJsonTask extends AsyncTask<String, Integer, News> {

		private static final String JSON_KEY_TITLE = "title";
		private static final String JSON_KEY_ROWS = "rows";
		private static final String JSON_KEY_DESCRIPTION = "description";
		private static final String JSON_KEY_IMAGE_URL = "imageHref";

		@Override
		protected void onProgressUpdate(Integer... values) {}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Show Loading till the data is displayed
			if (mListView != null && mListView.getVisibility() == View.INVISIBLE) {
				mProgressDialog = new ProgressDialog(NewsListActivity.this);
				mProgressDialog.setMessage(getString(R.string.loading));
				mProgressDialog.show();
			}
		}

		@Override
		protected News doInBackground(String... params) {
			String url = params[0];
			// getting JSON string from URL
			JSONObject jsonObj = getJSONFromUrl(url);
			//parsing json data
			if (jsonObj != null)
				return parseJson(jsonObj);
			else 
				return null;
		}


		@Override
		protected void onPostExecute(News result) {
			super.onPostExecute(result);
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
			if (result == null || result.getRows().size() == 0){
				Toast.makeText(getApplicationContext(), "No data fetched", Toast.LENGTH_LONG).show();
			} else {
				//Set the data in the list adapter
				mListAdapter.setListData(result.getRows());
				mListView.setAdapter(mListAdapter);
				//Set title of action bar with the title as parsed in JSON
				getActionBar().setTitle(result.getTitle());
				// setRefresh as false if refresh is in process
				if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(false);
				}
			}
		}	

		/**
		 * @param url from where json is downloaded
		 * @return JSONObject with name/value mappings from the JSON string
		 */
		public JSONObject getJSONFromUrl(String url) {
			InputStream inputStream = null;
			JSONObject jsonObject = null;
			String jsonString = null;
			// Making HTTP request
			try {
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);

				HttpResponse httpResponse = httpClient.execute(httpPost);
				//Check if status code is OK to proceed else dispaly error message
				if (httpResponse.getStatusLine().getStatusCode() == 200) {
					HttpEntity httpEntity = httpResponse.getEntity();
					inputStream = httpEntity.getContent();

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(inputStream, "iso-8859-1"), 8);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
					jsonString = sb.toString();
					if (jsonString != null && jsonString.length()>0) {
						jsonObject = new JSONObject(jsonString);
					}
				} else {
					Log.e("JSON Parser", "HTTP Response code not OK");
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}catch (JSONException e) {
				Log.e("JSON Parser", "Error parsing data " + e.toString());
				e.printStackTrace();
			}finally{
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return jsonObject;
		}


		/**
		 * @param json object which needs to be parsed
		 * @return parsed JSON Object
		 */
		public News parseJson (JSONObject json) {
			try {
				// parsing json object
				//JsonItem will have title and row
				News newsObj = new News();
				newsObj.setTitle(json.getString(JSON_KEY_TITLE));
				JSONArray rows = json.getJSONArray(JSON_KEY_ROWS);
				mFeedList.clear();
				for (int i = 0; i < rows.length(); i++) {
					JSONObject post = (JSONObject) rows.getJSONObject(i);
					WeakReference<JSONObject> wrJsonObj = new WeakReference<JSONObject>(post);
					//Saving the data in the RowItem object
					NewsItem item = new NewsItem();
					item.setTitle(wrJsonObj.get().getString(JSON_KEY_TITLE));
					item.setDesc(wrJsonObj.get().getString(JSON_KEY_DESCRIPTION));
					item.setImageHref(wrJsonObj.get().getString(JSON_KEY_IMAGE_URL));
					mFeedList.add(item);
				} 
				newsObj.setRows(mFeedList);
				return newsObj;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

	}

	private boolean haveNetworkConnection() {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

}
