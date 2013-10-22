/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.khub.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
	private static final String LOG_TAG = "ImageDownloader";

	public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
	private Mode mode = Mode.CORRECT;
	// 
	int IMAGE_MAX_SIZE_W = 600;
	int IMAGE_MAX_SIZE_H = 800;

//	private Handler aniHandler;
	
	public ImageDownloader(Context context){
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();
		IMAGE_MAX_SIZE_W = (displayWidth > IMAGE_MAX_SIZE_W) ? IMAGE_MAX_SIZE_W : displayWidth;
		IMAGE_MAX_SIZE_H = (displayHeight > IMAGE_MAX_SIZE_H) ? IMAGE_MAX_SIZE_H : displayHeight;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	/**
	 * Download the specified image from the Internet and binds it to the provided ImageView. The
	 * binding is immediate if the image is found in the cache and will be done asynchronously
	 * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
	 *
	 * @param url The URL of the image to download.
	 * @param imageView The ImageView to bind the downloaded image to.
	 */
	public Bitmap downloadImageFromURL(String img_url){
		Bitmap toDisk;
		toDisk = downloadBitmap(img_url);
		return toDisk;
	}
	public void download(String url, ImageView imageView) {
		//startLoadImageAniThread(imageView);
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap == null) {
			forceDownload(url, imageView);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}

	/*
	 * Same as download but the image is always downloaded and the cache is not used.
	 * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
	 */

	/**
	 * Same as download but the image is always downloaded and the cache is not used.
	 * Kept private at the moment as its interest is not clear.
	 */
	private void forceDownload(String url, final ImageView imageView) {
		// State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}
		if (cancelPotentialDownload(url, imageView)) {
			switch (mode) {
			case NO_ASYNC_TASK:
				Bitmap bitmap = null;
				String imgext = url.substring(url.lastIndexOf(".")+1).toLowerCase(Locale.ENGLISH);
				if(imgext.equals("bmp")) {
					bitmap = loadImageFromUrl(url);
				} else bitmap = downloadBitmap(url);
				addBitmapToCache(url, bitmap);
				imageView.setImageBitmap(bitmap);
				break;

			case NO_DOWNLOADED_DRAWABLE:
				imageView.setMinimumHeight(156);
				BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
				task.execute(url);
				break;

			case CORRECT:
				task = new BitmapDownloaderTask(imageView);
				DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
				imageView.setImageDrawable(downloadedDrawable);
				task.execute(url);
				break;
			}
		}
	}
	
//	private void startLoadImageAniThread(final ImageView imageView) {  
//		Thread thread = new Thread(new Runnable() {
//
//			public void run() {
//				try {
//					Log.i("Scale Type", imageView.getScaleType().name());
//					//					imageView.setScaleType(ScaleType.CENTER);
//					//					imageView.setBackgroundResource(R.drawable.load_animation);
//					AnimationDrawable animation = (AnimationDrawable)imageView.getBackground();
//					animation.start();
//
//					//aniHandler.sendEmptyMessageDelayed(0, 50);
//				} catch (Exception e) {
//					Log.w("loading_img", e);
//				}
//			}
//		});
//		thread.start();
//	}
	/**
	 * Returns true if the current download has been canceled or if there was no download in
	 * progress on this image view.
	 * Returns false if the download in progress deals with the same url. The download is not
	 * stopped in that case.
	 */
	private static boolean cancelPotentialDownload(String url, ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView Any imageView
	 * @return Retrieve the currently active download task (if any) associated with this imageView.
	 * null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	Bitmap downloadBitmap(String url) {
		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() : AndroidHttpClient.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);

//		Bitmap imgBitmap = null;
		Bitmap imgSrcBitmap = null;

		try {

			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w("ImageDownloader", "Error " + statusCode +
						" while retrieving bitmap from " + url);
				return null;
			}

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();

					BitmapFactory.Options bfo = new BitmapFactory.Options();
					bfo.inJustDecodeBounds = true;
					bfo.inTempStorage = new byte[8*1024];
					bfo.inSampleSize = 1;
					BitmapFactory.decodeStream(new FlushedInputStream(inputStream), null, bfo);

					if (bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE_H * IMAGE_MAX_SIZE_W) {
						bfo.inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE_W / (double) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
					}

					//Log.i("bfo.inSampleSize", String.valueOf(bfo.inSampleSize));
					bfo.inJustDecodeBounds = false;
					bfo.inTempStorage = new byte[8*1024];

					response = client.execute(getRequest);
					final int nRetryStatusCode = response.getStatusLine().getStatusCode();
					if (nRetryStatusCode != HttpStatus.SC_OK) {
						return null;
					}

					final HttpEntity reEntity = response.getEntity();
					if (reEntity != null) {
						InputStream reInputStream = null;
						try {
							reInputStream = reEntity.getContent();
							imgSrcBitmap = BitmapFactory.decodeStream(new FlushedInputStream(reInputStream), null, bfo);

							//imgBitmap = Bitmap.createScaledBitmap(imgSrcBitmap, imgWidth, imgHeight, true);

						} catch (Exception e) {e.printStackTrace();}
						finally {
							if (reInputStream != null) {
								reInputStream.close();
							}
							reEntity.consumeContent();
						}
					} 
				}catch (Exception e) {e.printStackTrace();}
				finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}
			((AndroidHttpClient) client).close();
		} catch (IOException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Incorrect URL: " + url);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
		} finally {
			if ((client instanceof AndroidHttpClient)) {
				((AndroidHttpClient) client).close();
			}
		}

		return imgSrcBitmap;
	}

	public Bitmap loadImageFromUrl(String url) {
		URL m;
		InputStream i = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream out = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
			bis = new BufferedInputStream(i, 1024 * 8);
			out = new ByteArrayOutputStream();
			int len = 0;
			byte[] buffer = new byte[1024];
			while ((len = bis.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
			out.close();
			bis.close();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		// Drawable d = Drawable.createFromStream(i, "src");
		return bitmap;
	}
	/*
	 * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
	 */
	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break;  // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(final ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);

		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			//ImageView imageView = imageViewReference.get();
			//startLoadImageAniThread(imageView);

			url = params[0];
			String imgext = url.substring(url.lastIndexOf(".")+1).toLowerCase(Locale.ENGLISH);
			if(imgext.equals("bmp")) {
				return loadImageFromUrl(url);
			} 
			return downloadBitmap(url);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}

			addBitmapToCache(url, bitmap);

			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with it
				// Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
				if (this == bitmapDownloaderTask) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download is in progress.
	 *
	 * <p>Contains a reference to the actual download task, so that a download task can be stopped
	 * if a new binding is required, and makes sure that only the last started download process can
	 * bind its result, independently of the download finish order.</p>
	 */
	static class DownloadedDrawable extends ColorDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
			//super(Color.BLACK);

			bitmapDownloaderTaskReference =
					new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}
	/*
	 * Cache-related fields and methods.
	 * 
	 * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
	 * Garbage Collector.
	 */

	private static final int HARD_CACHE_CAPACITY = 10;
	private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

	// Hard cache, with a fixed maximum capacity and a life duration
	private final HashMap<String, Bitmap> sHardBitmapCache =
			new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
			if (size() > HARD_CACHE_CAPACITY) {
				// Entries push-out of hard reference cache are transferred to soft reference cache
				sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	};

	// Soft cache for bitmaps kicked out of hard cache
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
			new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

	private final Handler purgeHandler = new Handler();

	private final Runnable purger = new Runnable() {
		public void run() {
			clearCache();
		}
	};
	/**
	 * Adds this bitmap to the cache.
	 * @param bitmap The newly downloaded bitmap.
	 */
	private void addBitmapToCache(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.put(url, bitmap);
			}
		}
	}

	/**
	 * @param url The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	private Bitmap getBitmapFromCache(String url) {
		// First try the hard reference cache
		synchronized (sHardBitmapCache) {
			final Bitmap bitmap = sHardBitmapCache.get(url);
			if (bitmap != null) {
				// Bitmap found in hard cache
				// Move element to first position, so that it is removed last
				sHardBitmapCache.remove(url);
				sHardBitmapCache.put(url, bitmap);
				return bitmap;
			}
		}

		// Then try the soft reference cache
		SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
		if (bitmapReference != null) {
			final Bitmap bitmap = bitmapReference.get();
			if (bitmap != null) {
				// Bitmap found in soft cache
				return bitmap;
			} else {
				// Soft reference has been Garbage Collected
				sSoftBitmapCache.remove(url);
			}
		}

		return null;
	}

	/**
	 * Clears the image cache used internally to improve performance. Note that for memory
	 * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
	 */
	public void clearCache() {
		sHardBitmapCache.clear();
		sSoftBitmapCache.clear();
	}

	/**
	 * Allow a new delay before the automatic cache clear is done.
	 */
	private void resetPurgeTimer() {
		purgeHandler.removeCallbacks(purger);
		purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
	}
}
