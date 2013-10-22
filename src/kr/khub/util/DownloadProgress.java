package kr.khub.util;

import static kr.khub.CommonUtilities.DOWNLOAD_PATH;

import java.io.FileNotFoundException;

import kr.khub.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * Simulates a download and updates the notification bar with a Progress
 * 
 * @author Nico Heid
 * 
 */
public class DownloadProgress extends Activity {

	ProgressBar progressBar;
	private int progress = 10;
	String whole_url;
	String file_name;
	AnimationDrawable m_anim;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get the layout
		setContentView(R.layout.download_progress);
		
		String allURL = getIntent().getStringExtra("download_url");
		String filename = getIntent().getStringExtra("download_filename");
		
		// configure the intent
		Intent intent = new Intent(this, DownloadProgress.class);
		final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		// configure the notification
		final Notification notification = new Notification(R.drawable.download_animation, "image downloading", System
				.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
		notification.contentIntent = pendingIntent;
		notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.download_animation);
		notification.contentView.setTextViewText(R.id.status_text, filename);
//		notification.contentView.setProgressBar(R.id.status_progress, 100, progress, false);

		final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(
				getApplicationContext().NOTIFICATION_SERVICE);

		notificationManager.notify(42, notification);
		
		SaveImage saveImageRunnable = new SaveImage(allURL, filename, notification, notificationManager);
		Thread imgDown_thread = new Thread(saveImageRunnable);
		imgDown_thread.start();
		
		finish();

	}
	private class SaveImage implements Runnable{
		String allURL;
		String filename;
		Notification mNotification;
		NotificationManager mNotificationManager;
		SaveImage(String Image_url, String fileName, Notification notification, NotificationManager notificationManager){
			allURL = Image_url;
			filename = fileName;
			mNotification = notification;
			mNotificationManager = notificationManager;
		}

		@Override
		public void run() {
//			mNotification.contentView.setProgressBar(R.id.status_progress, 100, progress, false);
			
			String sdcardState = android.os.Environment.getExternalStorageState();
			if(sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)){
				
				mNotificationManager.notify(42, mNotification);
				
				final String downlodedPath =DOWNLOAD_PATH+"/"+filename;
				FileDownloader fdown = new FileDownloader();
				try {
					fdown.downloadfile(allURL, filename);
					MediaStore.Images.Media.insertImage(getContentResolver(), downlodedPath, filename, "");
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+DOWNLOAD_PATH)));
//					imgDownload_handler.sendEmptyMessage(0);
				} catch (FileNotFoundException e) {
//					imgDownload_handler.sendEmptyMessage(1);
				}
			}else{
				Toast.makeText(getApplicationContext(), "sd card unmounted", Toast.LENGTH_SHORT).show();
			}
			
//			m_anim.stop();
		}
	}
}