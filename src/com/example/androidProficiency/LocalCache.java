package com.example.androidProficiency;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import android.graphics.Bitmap;

public class LocalCache {

	private static LocalCache mINSTANCE;
	private Map<String,Bitmap> cachemap = Collections
			.synchronizedMap(new WeakHashMap<String,Bitmap>());
	
	
	private LocalCache() {}
	
	public static LocalCache getInstance(){
		if(mINSTANCE == null){
			mINSTANCE = new LocalCache();
		}
		return mINSTANCE;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	
	public Bitmap getImageFromCache(String url){
		//read file cache if needed
		return cachemap.get(url);
	}
	
	public void storeImageInCache(String url,Bitmap bitmap){
		//write file cache if needed
		cachemap.put(url,bitmap);
	}
	
	
}
