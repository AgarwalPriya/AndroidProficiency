package com.example.androidProficiency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

public class NewsImageDownloader {

	private HashMap<String, SoftReference<Bitmap>> newsImageMap = new HashMap<String, SoftReference<Bitmap>>();

	private File cacheDirectory;
	private ImageQueue imgQueue = new ImageQueue();
	private Thread imgLoaderThread = new Thread(new ImageQueueManager());

	public NewsImageDownloader(Context context) {

		// Thread set to low priority, to avoid UI performance from being hit
		imgLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

		// Find the dir to save cached images
		String sdState = Environment.getExternalStorageState();
		if (sdState.equals(Environment.MEDIA_MOUNTED)) {
			File sdDir = Environment.getExternalStorageDirectory();
			cacheDirectory = new File(sdDir, "androidproficiency/images");
			if (!cacheDirectory.exists()) {
				cacheDirectory.mkdirs();
			}
		} else {
			cacheDirectory = context.getCacheDir();
		}

	}

	public void displayImage(String url, Activity activity, ImageView imageView) {
		if (newsImageMap.containsKey(url)) {
			Bitmap bmp = newsImageMap.get(url).get();
			if(bmp != null) {
				imageView.setImageBitmap(bmp);
				float density = activity.getApplicationContext().getResources().getDisplayMetrics().density;
				Log.e("PRIYANKA","density ="+density);
				Log.e("PRIYANKA","bmp.getHeight() ="+bmp.getHeight());
				Log.e("PRIYANKA","bmp.getWidth() ="+bmp.getWidth());
				
				int modifiedHT = (int) (bmp.getHeight()*density);
				int modifiedWidth = (int) (bmp.getWidth()*density);
				
				if(modifiedHT > 400 || modifiedWidth > 400 ){
					modifiedHT = 350;
					modifiedWidth = 350;
				}
				
				imageView.getLayoutParams().height = (int) modifiedHT;
				imageView.getLayoutParams().width = (int) modifiedWidth;
			
				
			}
		} else {
			queueImage(url, imageView);
			imageView.setImageResource(R.drawable.imageholder);
		}
	}

	private void queueImage(String url, ImageView imageView) {
		// This ImageView might have been used for other images, so we clear
		// the queue of old tasks before starting.
		imgQueue.clean(imageView);
		ImageReference p = new ImageReference(url, imageView);

		synchronized (imgQueue.imageReference) {
			imgQueue.imageReference.push(p);
			imgQueue.imageReference.notifyAll();
		}
		// Start thread if it's not started yet
		if (imgLoaderThread.getState() == Thread.State.NEW) {
			imgLoaderThread.start();
		}
	}

	private Bitmap getBitmap(String url) {

		try {
			Log.e("PRI","url ="+url);

			String filename = String.valueOf(url.hashCode());
			Log.e("PRI","filename ="+filename);
			File bitmapFile = new File(cacheDirectory, filename);
			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapFile));
			// Is the bitmap in our cache?
			if (bitmap != null) {
				return bitmap;
			} else {
				// have to download it
				Bitmap bmp = downloadBitmap(url);
				// save bitmap to cache for later use
				writeFile(bmp, bitmapFile);
				return bitmap;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			Log.e("EXCEPTION","bitmap not decoded");
			return null;
		}
	}
	
	private Bitmap downloadBitmap(String url) {
		HttpURLConnection urlConnection = null;
		try {
			URL uri = new URL(url);
			urlConnection = (HttpURLConnection) uri.openConnection();
			urlConnection.connect();
			InputStream inputStream = urlConnection.getInputStream();
			if (inputStream != null) {
				Log.e("PRI","inputStream not null");
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.reset();
				inputStream.close();
				if(bitmap != null){
					Log.e("PRI","bitmap not null");
				}
				return bitmap;
			}
		} catch (Exception e) {
			if(urlConnection != null) {
				urlConnection.disconnect();
				Log.w("ImageDownloader", "Error downloading image from " + url);
			}
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
		return null;
	}

	/**
	 * Write bitmap associated with a url to cache
	 */
	private void writeFile(Bitmap bmp, File file) {
		Log.e("PRI","writeFile ="+file);
		FileOutputStream out = null;
		try {
			// Create a file at the file path, and open it for writing obtaining the output stream
			file.createNewFile();
			out = new FileOutputStream(file);
			// Write the bitmap to the output stream (and thus the file) in PNG format 
			bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
			Log.e("Success", "saving image to cache. ");  
			// Flush and close the output stream  
			out.flush();       
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Error", "Error when saving image to cache. "+ e);   
		} 
	}

	/** Classes required for caching and displaying the images **/

	private class ImageReference {
		public String url;
		public ImageView imageView;

		public ImageReference(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	/**
	 * save list of images to download
	 * 
	 */
	private class ImageQueue {
		private Stack<ImageReference> imageReference = new Stack<ImageReference>();

		// clear all instances of this ImageView
		public void clean(ImageView view) {

			for (int i = 0; i < imageReference.size();) {
				if (imageReference.get(i).imageView == view)
					imageReference.remove(i);
				else
					++i;
			}
		}
	}

	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					/*
					 * call wait on the Thread till there are images in the
					 * queue to be retrieved
					 */
					if (imgQueue.imageReference.size() == 0) {
						synchronized (imgQueue.imageReference) {
							imgQueue.imageReference.wait();
						}
					}
					// When we have images to load, pop it from the stack
					if (imgQueue.imageReference.size() != 0) {
						ImageReference imageToLoad;

						synchronized (imgQueue.imageReference) {
							imageToLoad = imgQueue.imageReference.pop();
						}

						Bitmap bmp = getBitmap(imageToLoad.url);
						newsImageMap.put(imageToLoad.url,
								new SoftReference<Bitmap>(bmp));
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety
						if (tag != null
								&& ((String) tag).equals(imageToLoad.url)) {
							BitmapDisplayer bitmapDisplayer = new BitmapDisplayer(
									bmp, imageToLoad.imageView);

							Activity activity = (Activity) imageToLoad.imageView.getContext();
							activity.runOnUiThread(bitmapDisplayer);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
			}
		}
	}

	// Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

		public void run() {
			if (bitmap != null)
				imageView.setImageBitmap(bitmap);
			else
				imageView.setImageResource(R.drawable.imageholder);
		}
	}
}
