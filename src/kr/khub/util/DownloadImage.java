package kr.khub.util;

import static kr.khub.CommonUtilities.DOWNLOAD_PATH;

import java.io.File;
import java.io.FileNotFoundException;

import kr.khub.R;
import kr.khub.bitmapfun.util.Utils;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadImage extends Activity{
	
	String allURL;
	String file_name;
	AnimationDrawable m_anim;
	private String TAG =DownloadImage.class.getSimpleName();
	private int NOTIFICATION_ID = 9072615;
	Notification notification;
	NotificationManager notificationManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_progress);
		
		allURL = getIntent().getStringExtra("download_url");
		file_name = getIntent().getStringExtra("download_filename");
		
		// configure the notification
		notification = new Notification(R.drawable.notification_level_list, "이미지 다운로드중..", System.currentTimeMillis());
		// notification_level_list.xml 
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.iconLevel = 0;
		
		notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
		notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.download_image_ticker);
		notification.contentView.setTextViewText(R.id.status_text, file_name);
	
		notificationManager = (NotificationManager) getApplicationContext().getSystemService(
				getApplicationContext().NOTIFICATION_SERVICE);
		
		notificationManager.notify(NOTIFICATION_ID, notification);
		new SaveImage().execute(file_name);
		finish();
	}
	class SaveImage extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... params) {
			String filename = params[0];
			String image_uri = "";
			String sdcardState = android.os.Environment.getExternalStorageState();
			if(sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)){
				final String downlodedPath =DOWNLOAD_PATH+"/"+filename;
				FileDownloader fdown = new FileDownloader();
				boolean download_file_result;
				
				try {
					download_file_result = fdown.downloadfile(allURL, filename);
					image_uri = MediaStore.Images.Media.insertImage(getContentResolver(), downlodedPath, filename, "");
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+DOWNLOAD_PATH)));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}else{
				Toast.makeText(getApplicationContext(), "sd card unmounted", Toast.LENGTH_SHORT).show();
			}
			return image_uri;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Intent view_intent = new Intent();
			view_intent.setAction(android.content.Intent.ACTION_VIEW);
			view_intent.setDataAndType(Uri.parse(result), "image/jpg");
			PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, view_intent, 0);
			if(!(result.equals(""))||result.length()!=0){
				notification.iconLevel = 1;
				notification.tickerText ="다운로드 완료";
				notification.icon = R.drawable.content_picture;
				notification.setLatestEventInfo(getApplicationContext(), file_name, "다운로드 완료", contentIntent);
			}else{
				notification.iconLevel = 2;
				notification.tickerText ="다운로드 실패";
				notification.icon = R.drawable.alerts_and_states_warning;
				notification.setLatestEventInfo(getApplicationContext(), file_name, "다운로드 실패", contentIntent);
			}
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}
}
