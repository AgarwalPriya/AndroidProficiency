package com.example.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.res.Configuration;
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
 * @author medha3
 *
 */
public class JsonListActivity extends Activity {
	private ArrayList<RowItem> listData = null;
	private ListView listView = null;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private JsonListAdapter listAdapter = null;



	private ArrayList<RowItem> feedList;
	FileInputStream stream;
	String jsonStr = null;
	JSONObject jsonObj = null;
	public JsonItem mJsonItem;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.list_view);
		mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipetorefresh);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				RowItem feedData = (RowItem) listView.getItemAtPosition(position);
				int pos = position + 1;
				Toast.makeText(JsonListActivity.this, "Selected :" + " " + pos, Toast.LENGTH_LONG).show();
			}
		});

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshContent();
				mSwipeRefreshLayout.setRefreshing(false);
			}
		});
	}

	@Override
	protected void onResume() {
		new DownloadJsonTask().execute("https://dl.dropboxusercontent.com/u/746330/facts.json");
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
				new DownloadJsonTask().execute("https://dl.dropboxusercontent.com/u/746330/facts.json");
				mSwipeRefreshLayout.setRefreshing(false);
			}
		}, 2000);
	}

	private class DownloadJsonTask extends AsyncTask<String, Integer, JsonItem> {

		@Override
		protected void onProgressUpdate(Integer... values) {
		}

		@Override
		protected void onPostExecute(JsonItem result) {
			super.onPostExecute(result);
			if (result.getRows().size() == 0){
				Toast.makeText(getApplicationContext(), "No data fetched", Toast.LENGTH_LONG).show();
			} else {
				listAdapter = new JsonListAdapter(getApplicationContext(), result.getRows());   
				listView.setAdapter(listAdapter);
				//Set title of action bar with the title as parsed in JSON
				getActionBar().setTitle(result.getTitle());

				// setRefresh as false if refresh is in process
				if (mSwipeRefreshLayout.isRefreshing()) {
					mSwipeRefreshLayout.setRefreshing(false);
				}
			}
		}

		@Override
		protected JsonItem doInBackground(String... params) {
			String url = params[0];
			// getting JSON string from URL
			jsonObj = getJSONFromUrl(url);
			//parsing json data
			if (jsonObj != null)
				mJsonItem = parseJson(jsonObj);
			return mJsonItem;
		}
	}

	/**
	 * @param url from where json is downloaded
	 * @return JSONObject with name/value mappings from the JSON string
	 */
	public JSONObject getJSONFromUrl(String url) {
		InputStream is = null;
		JSONObject jObj = null;
		String json = null;
		// Making HTTP request
		try {
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			json = sb.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		// return JSON String
		return jObj;
	}

	
	/**
	 * @param json object which needs to be parsed
	 * @return parsed JSON Object
	 */
	public JsonItem parseJson (JSONObject json) {
		try {
			// parsing json object
			//JsonItem will have title and row
			JsonItem jsonItem = new JsonItem();

			jsonItem.setTitle(json.getString("title"));
			JSONArray rows = json.getJSONArray("rows");

			feedList = new ArrayList();
			for (int i = 0; i < rows.length(); i++) {
				JSONObject post = (JSONObject) rows.getJSONObject(i);
				//Saving the data in the RowItem object
				RowItem item = new RowItem();
				item.setTitle(post.getString("title"));
				item.setDesc(post.getString("description"));
				item.setImageHref(post.getString("imageHref"));
				feedList.add(item);
			} 
			jsonItem.setRows(feedList);
			return jsonItem;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
