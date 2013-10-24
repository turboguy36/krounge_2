package kr.co.ktech.cse.util;

import static kr.co.ktech.cse.CommonUtilities.KLOUNGE_FILE_CACHE_NUMBER;
import static kr.co.ktech.cse.CommonUtilities.KLOUNGE_STORAGE_LOCATION;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import kr.co.ktech.cse.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadImageUtil {
	private static final int DELETE_FILE_NUMBER = 5;
	private static final int IMAGE_MAX_SIZE = 300;
	public String imageFilePath;
	private boolean bUseFileCache = true;
	private String TAG = "LoadImageUtil";
	
	public void loadImage(ImageView imageView, final String url, final int user_id) {
		BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);

		DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
		imageView.setImageDrawable(downloadedDrawable);

		task.execute(url, String.valueOf(user_id));
	}
	
	/**
	 * @return the imageFilePath
	 */
	public String getImageFilePath() {
		return imageFilePath;
	}

	/**
	 * @param imageFilePath the imageFilePath to set
	 */
	public void setImageFilePath(String imageFilePath) {
		this.imageFilePath = imageFilePath;
	}

	private Bitmap downloadImage(String imageUrl, int user_id) {
		String img_url = imageUrl;
		Bitmap bitmap = null;
		if(imageUrl == null) {
//			Log.i("IMAGE LOC", user_id+" - "+ "image url is null");
			return null;
		}
		
		imageFilePath = KLOUNGE_STORAGE_LOCATION + "/" + user_id + "USER_" + imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
//		Log.i("IMAGE LOC", user_id+" / "+ imageFilePath);
		
		bitmap = SafeDecodeBitmapFile(imageFilePath);
		File cfile = new File(KLOUNGE_STORAGE_LOCATION);
		File []arrFile = cfile.listFiles();
		
		if(bitmap == null) {
			// FlieCache 파일 개수 관리
			ManageFileCache(KLOUNGE_STORAGE_LOCATION);
	
			InputStream is = null;
			int response = -1;
	
			try {
				img_url = imageUrl.replace(" ", "%20");
				
				URL url = new URL(img_url);
				URLConnection conn = url.openConnection();
	
				if (!(conn instanceof HttpURLConnection))
					throw new IOException("Not an HTTP connection");
	
				HttpURLConnection httpConn = (HttpURLConnection)conn;
				httpConn.setAllowUserInteraction(false);
				httpConn.setInstanceFollowRedirects(true);
				httpConn.setRequestMethod("GET");
				httpConn.connect();
	
				response = httpConn.getResponseCode();
	
				if (response == HttpURLConnection.HTTP_OK) {
					is = httpConn.getInputStream();
					
					File dir = new File(KLOUNGE_STORAGE_LOCATION);
					if (!dir.exists()) dir.mkdirs();
					
					File file = new File(imageFilePath);
					BufferedInputStream bis = new BufferedInputStream(is);
					FileOutputStream fos = new FileOutputStream(file);
	
					byte[] buffer = new byte[1024];
	
					int len1 = 0;
					while ((len1 = bis.read(buffer)) > 0) {
						fos.write(buffer, 0, len1);
						fos.flush();
					}
					fos.close();
					bis.close();
				}
				try{
					is.close();
				}catch(IOException e){
					Log.e(TAG, "downlaodImage IOException"+e);
				}
				is = null;
				httpConn.disconnect();
			} catch (Exception e) {
				Log.i(this.toString(),e.toString());
			}
			bitmap = SafeDecodeBitmapFile(imageFilePath);
		}
		return bitmap;
	}
	
	private void ManageFileCache(String strFolderPath) {
		long nLastModifiedDate = 0;
		File targetFile = null;

		int count = 0;
		int fileNum = 0;
		try{
			fileNum = new File(strFolderPath).listFiles().length;
		}catch(NullPointerException n){
			Log.e(TAG, "NullPointerException -"+n);
		}
		if (fileNum >= KLOUNGE_FILE_CACHE_NUMBER) {
			while (true) {
				if (count == DELETE_FILE_NUMBER)
					break;

				File[] arrFiles = new File(strFolderPath).listFiles();

				for (File file : arrFiles) {
					if (nLastModifiedDate < file.lastModified()) {
						nLastModifiedDate = file.lastModified();
						targetFile = file;
					}
				}

				if (targetFile != null) {
					targetFile.delete();
					count++;
				}
			}
		}
	}

	// File 에서 이미지를 불러올 때 안전하게 불러오기 위해서 만든 함수
	// bitmap size exceeds VM budget 오류 방지용
	private Bitmap SafeDecodeBitmapFile(String strFilePath) {
		File file = new File(strFilePath);
		if (file.exists() == false) {
			return null;
		}

		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(strFilePath, bfo);

		if (bfo.outHeight * bfo.outWidth >= IMAGE_MAX_SIZE * IMAGE_MAX_SIZE) {
			bfo.inSampleSize = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(bfo.outHeight, bfo.outWidth)) / Math.log(0.5)));
		}
		bfo.inJustDecodeBounds = false;
		bfo.inPurgeable = true;
		bfo.inDither = true;

		final Bitmap bitmap = BitmapFactory.decodeFile(strFilePath, bfo);

		return bitmap;
	}
	
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
			url = params[0];
			String strUser_id = params[1];
			int user_id = 0;
			if(strUser_id != null) user_id = Integer.parseInt(strUser_id);
			return downloadImage(url, user_id);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null) {
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				// Change bitmap only if this process is still associated with it
				// Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
				if (this == bitmapDownloaderTask) {
					if(bitmap != null) {
						imageView.setImageBitmap(bitmap);
					} else {
						imageView.setImageResource(R.drawable.no_photo);
					}
				}
			}
		}
	}
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
			super(Color.BLACK);

			bitmapDownloaderTaskReference =
					new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}
}
