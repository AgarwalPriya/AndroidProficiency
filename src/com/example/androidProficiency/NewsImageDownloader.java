package com.example.androidProficiency;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Stack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
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
		if (newsImageMap.containsKey(url))
			imageView.setImageBitmap(newsImageMap.get(url).get());
		else {
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
			URLConnection openConnection = new URL(url).openConnection();

			String filename = String.valueOf(url.hashCode());

			File bitmapFile = new File(cacheDirectory, filename);
			Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath());
			// Is the bitmap in our cache?
			if (bitmap != null) {
				return bitmap;
			} else {
				// have to download it
				bitmap = BitmapFactory.decodeStream(openConnection
						.getInputStream());
				// save bitmap to cache for later
				writeFile(bitmap, bitmapFile);
				return bitmap;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void writeFile(Bitmap bmp, File f) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(f);
			bmp.compress(Bitmap.CompressFormat.PNG, 80, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
