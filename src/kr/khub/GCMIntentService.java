package kr.khub;

import static kr.khub.CommonUtilities.GCM_SENDER_ID;
import static kr.khub.CommonUtilities.displayMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.khub.BuildConfig;
import kr.khub.R;
import kr.khub.activity.LoginActivity;
import kr.khub.activity.PopupMessage;
import kr.khub.bitmapfun.util.Utils;
import kr.khub.model.AppUser;
import kr.khub.model.GCMInfo;
import kr.khub.model.NewMessage;
import kr.khub.model.SnsAppInfo;
import kr.khub.util.PushWakeLock;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String LOG_TAG = GCMIntentService.class.getSimpleName();
	//	private final int FROM_SERVICE_ON_MESSAGE = 3001;
	private final String FILENAME = "new_message_data.json";
	private static final int NOTIFICATION_ID = 2709072;
	Bitmap bitmap;

	public GCMIntentService() {
		super(GCM_SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		boolean succeed = false;

		displayMessage(context, getString(R.string.gcm_registered));
		if(AppConfig.PUSH){
			succeed = ServerUtilities.register(context, registrationId, AppUser.user_id);
		}
		if(succeed){
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		displayMessage(context, getString(R.string.gcm_unregistered));
		if (GCMRegistrar.isRegisteredOnServer(context)){
			if(AppConfig.PUSH){
				ServerUtilities.unregister(context, registrationId, AppUser.user_id);
			}
		}else{
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		SharedPreferences prefs;
		prefs = context.getSharedPreferences("KLounge", Context.MODE_PRIVATE);

		String type = intent.getStringExtra("type");
		String type2 = intent.getStringExtra("type2");
		int group_id = Integer.parseInt(intent.getStringExtra("group_id"));
		String group_name = intent.getStringExtra("group_name");
		int post_super_id = Integer.parseInt(intent.getStringExtra("post_super_id"));
		int user_id = Integer.parseInt(intent.getStringExtra("user_id"));
		String user_name = intent.getStringExtra("user_name");
		String user_photo = intent.getStringExtra("user_photo");
		String photo_video = intent.getStringExtra("photo_video");
		String message = intent.getStringExtra("message");		

		SnsAppInfo snsinfo = new SnsAppInfo();
					snsinfo.setGroupId(group_id);
					snsinfo.setGroup_name(group_name);
					snsinfo.setSuperId(post_super_id);
					snsinfo.setUserId(user_id);
					snsinfo.setUserName(user_name);
					snsinfo.setPhoto(user_photo);
					snsinfo.setBody(message);

		if(photo_video!=null){
			snsinfo.setPhotoVideo(photo_video);
		}

		//with declaring broadcast receivers
//		displayMessage(context, message);

		// 상태 파악
		if(isRunningProcess(context, context.getPackageName())){
			// 앱이 사용중이라면 List 에 정보를 Add 
			NewMessage nMessage = new NewMessage();
						nMessage.setGroup_id(group_id);
						nMessage.setGroup_name(group_name);
						nMessage.setSuper_id(post_super_id);
						nMessage.setUser_id(user_id);
						nMessage.setUser_name(user_name);
						nMessage.setUser_photo(user_photo);
						nMessage.setMessage(message);
						nMessage.setType1(type);
						nMessage.setType2(type2);

			AppUser.NEW_MESSAGE.add(nMessage);
		}else{
			PushWakeLock.acquireCpuWakeLock(context);
			
//			if (prefs.getBoolean(CommonUtilities.POPUP_PREVIEW_SETTING, false)) {
//				boolean group_sns = prefs.getBoolean(CommonUtilities.POPUP_GROUP_SNS_SETTING, false);
//
//				if(type2.equalsIgnoreCase("group") && !(group_sns)){
//					// Group SNS 글은 받아보지 원치 않는다면(기본)
//					return;
//				}
//				// 팝업으로 사용할 액티비티를 호출할 인텐트를 작성한다.
//				Intent popupIntent = new Intent(context, PopupMessage.class).setFlags(
//						Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//				// FLAG_ACTIVITY_MULTIPLE_TASK  ->  다른 어플리케이션 실행 중일 때 투명한 다이얼로그 보이게한다.
//
//				StringBuffer title = new StringBuffer();
//				title.append(snsinfo.getUserName());
//				title.append(" 님 의");
//				title.append(type.equals(CommonUtilities.FLAG_BODY_POST)? " 새글" : " 댓글");
//
//				snsinfo.setTitle(title.toString());
//
//				popupIntent.putExtra("popup_type", type);
//				popupIntent.putExtra("popup_type2", type2);
//				popupIntent.putExtra("popup_group_id", group_id);
//				popupIntent.putExtra("popup_snsAppInfo", snsinfo);
//				// 그리고 호출한다.
//				context.startActivity(popupIntent);
//			}
			try{
				if(prefs.getBoolean(CommonUtilities.POPUP_SETTING, true)){
					generateNotification(context, type, type2, snsinfo);
				}
			}catch (Exception e) {
				Log.e(LOG_TAG, "[onMessage] Exception : " + e.getMessage());
			}
		}
		storeAtInternalFile(snsinfo,type,type2);

		Intent broadcast_intent = new Intent("GCMMessageReceived");
		sendOrderedBroadcast(broadcast_intent, null);

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// WakeLock 해제.
				PushWakeLock.releaseCpuLock();
			}
		},3000);
	}

	public boolean storeAtInternalFile(SnsAppInfo snsinfo, String type, String type2){
		boolean result = false;

		StringBuilder sb = new StringBuilder();
		sb.append("{ \"new_message\" : [");

		try {
			FileInputStream fis = openFileInput(FILENAME);
			byte in[] = new byte[fis.available()];
			fis.read(in);

			JSONObject existFileObj = new JSONObject(new String(in));
			JSONArray existJsonArray = existFileObj.getJSONArray("new_message");

			for(int i=0; i<existJsonArray.length();i++){
				JSONObject existJsonObject = existJsonArray.getJSONObject(i);
				sb.append(existJsonObject.toString());
				sb.append(",");
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = new File(getApplication().getFilesDir(), FILENAME);
		file.delete();

		if(snsinfo != null){
			try{
				JSONObject obj = new JSONObject();
				obj.put("group_id", snsinfo.getGroupId());
				obj.put("group_name", snsinfo.getGroup_name());
				obj.put("super_id", snsinfo.getSuperId());
				obj.put("user_id", snsinfo.getUserId());
				obj.put("user_name", snsinfo.getUserName());
				obj.put("user_photo", snsinfo.getPhoto());
				obj.put("message", snsinfo.getBody());
				obj.put("type_1", type);
				obj.put("type_2", type2);

				sb.append(obj.toString()).append("]}");

				byte[] outByte = sb.toString().getBytes();

				FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_APPEND);				
				fos.write(outByte);
				fos.close();

			}catch(FileNotFoundException fe){
				Log.e(LOG_TAG,"FileNotFoundException - "+fe);
			}catch(JSONException je){
				Log.e(LOG_TAG,"JSONException - "+je);
			}catch(IOException ie){
				Log.e(LOG_TAG,"IOException - "+ie);
			}
		}
		return result;
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onDeletedMessages");
		// 메세지 삭제
		/*String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);*/
	}
	@Override
	protected void onError(Context context, String errorId) {
		if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onError: " + errorId);
		// 오류 발생 시 처리
		Log.i(LOG_TAG, "onError: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.i(LOG_TAG, "Received recoverable error: " + errorId);
		//displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}
	private Bitmap getUserBitmap(URL url){
		Bitmap bm = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			bm = BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException ne){
			ne.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return bm;
	}
	private Bitmap getAttachBitmap(URL url){
		Bitmap bm = null;
		try{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			bm= BitmapFactory.decodeStream(input);
		} catch(IOException e) {
			e.printStackTrace();
		} catch (NullPointerException ne){
			ne.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		return bm;
	}
	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private void generateNotification(Context context, 
			String type, String type2, SnsAppInfo snsinfo) {
		SharedPreferences prefs = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);

		/*
		 * getting Post User Photo
		 * Main thread 에서 일어나는 일이 아니기 때문에 AsyncTask 쓰지 않아도 됨
		 */
		URL url = null;
		try {
			url = new URL(snsinfo.getPhoto());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bitmap = getUserBitmap(url);

		/*
		 * getting attachd photo
		 */
		Bitmap photo_video_bitmap = null;
		URL attach_url = null;
		try {
			attach_url = new URL(snsinfo.getPhotoVideo());
			if(attach_url !=null){
				photo_video_bitmap = getAttachBitmap(attach_url);
			}
		} catch (MalformedURLException me) {
			// TODO Auto-generated catch block
			me.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(Utils.hasJellyBean()){
			makeNotification_jellybean(context, snsinfo, type, type2, prefs, photo_video_bitmap);
		}else if(Utils.hasHoneycomb()){
			makeNotification_honeyComb(context, snsinfo, type, type2, prefs, bitmap);
		}else{
			makeNotification(context, snsinfo, type, type2, prefs);
		}
	}

	private boolean isRunningProcess(Context context, String packagename) {
		if(context == null || packagename.length() < 0 ) 
			return false;

		ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runList = am.getRunningTasks(1);
		ComponentName topActivity = runList.get(0).topActivity;
		if(packagename.startsWith(topActivity.getPackageName())) {
			return true;
		} 
		return false;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void makeNotification_jellybean(Context context, SnsAppInfo snsinfo, String type, String type2, SharedPreferences prefs, Bitmap photo_video_bitmap){
		String title = snsinfo.getUserName();

		String big_text = snsinfo.getBody();
		String small_text = "";
		if(big_text.length() > 30){
			small_text = big_text.substring(0, 30);
		}
		
		try{
			Intent noti_intent = getNotificationIntent(context, type, type2, snsinfo);
			PendingIntent pi = PendingIntent.getActivity(context, 0, noti_intent, 0);
			Notification.Builder builder = getBuilder(context, title, big_text, prefs, pi);
			Notification noti;
			if(!(snsinfo.getPhotoVideo() == null || snsinfo.getPhotoVideo().equalsIgnoreCase("null"))){ 
				//그림이 첨부된 글이라면 Big Image Notification 
				noti = new Notification.BigPictureStyle(builder)
				.bigPicture(photo_video_bitmap)
				.setBigContentTitle(title)
				.setSummaryText(getResources().getString(R.string.krounge_message) + " with attached")
				.build();
			}else if(!(small_text.equals("") || small_text.length() == 0)){
				// 글이 매우 긴 게시물 이라면 Big Text Notification
				noti = new Notification.BigTextStyle(builder)
				.bigText(big_text)
				.setSummaryText(getResources().getString(R.string.krounge_message))
				.build();
			}else{
				noti = builder.build();
			}
			
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, noti);
		}catch(Exception e){
			Log.e(LOG_TAG, "[setNotification] Exception : " + e.getMessage());
		}
	}
	private Intent getNotificationIntent(Context context, String type, String type2, SnsAppInfo snsinfo){
		Intent notificationIntent = new Intent(context, LoginActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificationIntent.putExtra("popup_type", type);
		notificationIntent.putExtra("popup_type2", type2);
		notificationIntent.putExtra("popup_group_id", snsinfo.getGroupId());
		notificationIntent.putExtra("snsAppInfo", snsinfo);
		notificationIntent.putExtra("from_notification", true);
		
		return notificationIntent;
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private Notification.Builder getBuilder(Context context, String title, String big_text, SharedPreferences prefs, PendingIntent pending){
		Uri sound_uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		int pendingNotificationsCount = KloungeApplication.getPendingNotificationsCount()+ 1;
		KloungeApplication.setPendingNotificationsCount(pendingNotificationsCount);
		
		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentTitle(title)
				.setContentText(big_text)
				.setContentIntent(pending)
				.setSmallIcon(R.drawable.ic_stat_gcm)
				.setLargeIcon(bitmap)
				.setTicker(getResources().getString(R.string.krounge_message))
				.setLights(Color.BLUE, 500, 500)
				.setNumber(pendingNotificationsCount)
				.setWhen(System.currentTimeMillis());
		if(prefs.getBoolean(CommonUtilities.POPUP_SOUND_SETTING, true)){
			builder.setSound(sound_uri);
		}
		if(prefs.getBoolean(CommonUtilities.POPUP_VIBRATE_SETTING, true)){
			builder.setVibrate(new long[] {300, 500, 300, 500});
		}
		return builder;
	}
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void makeNotification_honeyComb(Context context, SnsAppInfo snsinfo, String type, String type2, SharedPreferences prefs, Bitmap user_photo_bitmap){
		String title = snsinfo.getUserName();
		String message = snsinfo.getBody();
		try{
			Uri sound_uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Intent notificationIntent = new Intent(context, LoginActivity.class);
			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			notificationIntent.putExtra("popup_type", type);
			notificationIntent.putExtra("popup_type2", type2);
			notificationIntent.putExtra("popup_group_id", snsinfo.getGroupId());
			notificationIntent.putExtra("snsAppInfo", snsinfo);
			notificationIntent.putExtra("from_notification", true);
			PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			int pendingNotificationsCount = KloungeApplication.getPendingNotificationsCount()+ 1;
			KloungeApplication.setPendingNotificationsCount(pendingNotificationsCount);

			Notification.Builder noti_builder = new Notification.Builder(context);
			noti_builder
					.setContentTitle(title)
					.setContentText(message)
					.setContentIntent(pi)
					.setSmallIcon(R.drawable.ic_stat_gcm)
					.setLargeIcon(user_photo_bitmap)
					.setTicker(getResources().getString(R.string.krounge_message))
					.setNumber(pendingNotificationsCount)
					.setLights(Color.BLUE, 500, 500)
					.setWhen(System.currentTimeMillis());

			if(prefs.getBoolean(CommonUtilities.POPUP_SOUND_SETTING, true)){
				noti_builder.setSound(sound_uri);
			}
			if(prefs.getBoolean(CommonUtilities.POPUP_VIBRATE_SETTING, true)){
				noti_builder.setVibrate(new long[] {300, 500, 300, 500});
			}

			Notification noti = noti_builder.getNotification();
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, noti);

		}catch(Exception e){
			Log.e(LOG_TAG, "[setNotification] Exception : " + e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	private void makeNotification(Context context, SnsAppInfo snsinfo, String type, String type2, SharedPreferences prefs){
		NotificationManager notificationManager = null;
		Notification notification = null;
		String message = snsinfo.getBody();
		try {
			notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notification = new Notification(R.drawable.ic_stat_gcm, message, System.currentTimeMillis());
			String title = snsinfo.getUserName();
			//			title += "님 의 메세지";

			Intent notificationIntent = new Intent(context, LoginActivity.class);

			// set intent so it does not start a new activity
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			notificationIntent.putExtra("popup_type", type);
			notificationIntent.putExtra("popup_type2", type2);
			notificationIntent.putExtra("popup_group_id", snsinfo.getGroupId());
			notificationIntent.putExtra("snsAppInfo", snsinfo);
			notificationIntent.putExtra("from_notification", true);
			PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

			notification.setLatestEventInfo(context, title, message, pi);

			int pendingNotificationsCount = KloungeApplication.getPendingNotificationsCount()+ 1;
			KloungeApplication.setPendingNotificationsCount(pendingNotificationsCount);
			notification.number = pendingNotificationsCount;

			if(prefs.getBoolean("POPUP_SOUND_SETTING", true)){
				notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			}if(prefs.getBoolean("POPUP_VIBRATE_SETTING", true)){
				notification.defaults |= Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
			}
			notification.flags |= Notification.FLAG_AUTO_CANCEL;

			notificationManager.notify(NOTIFICATION_ID, notification);
		} catch (Exception e) {
			Log.e(LOG_TAG, "[setNotification] Exception : " + e.getMessage());
		}
	}
}
