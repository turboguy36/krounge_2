package kr.co.ktech.cse;

import static kr.co.ktech.cse.CommonUtilities.GCM_SENDER_ID;
import static kr.co.ktech.cse.CommonUtilities.displayMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import kr.co.ktech.cse.BuildConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.LoginActivity;
import kr.co.ktech.cse.activity.PopupMessage;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GCMInfo;
import kr.co.ktech.cse.model.NewMessage;
import kr.co.ktech.cse.model.SnsAppInfo;
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
import android.media.RingtoneManager;
import android.os.Build;
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
		
//		Log.i(LOG_TAG, "onRegistered-registrationId = " + registrationId + " / " +AppUser.user_id);
		
		displayMessage(context, getString(R.string.gcm_registered));
		if(AppConfig.PUSH){
			succeed = ServerUtilities.register(context, registrationId, AppUser.user_id);
		}
		if(succeed){
//			Log.d(LOG_TAG, "Registration is succeeded");
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		//if (BuildConfig.DEBUG) Log.d(LOG_TAG, "onUnregistered-registrationId = " + registrationId);
//		Log.i(LOG_TAG, "onUnregistered-registrationId = " + registrationId);
		displayMessage(context, getString(R.string.gcm_unregistered));
		if (GCMRegistrar.isRegisteredOnServer(context)){
			if(AppConfig.PUSH){
				ServerUtilities.unregister(context, registrationId, AppUser.user_id);
			}
		}else{
			// This callback results from the call to unregister made on
			// ServerUtilities when the registration to the server failed.
//			Log.i(LOG_TAG, "Ignoring unregister callback");
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
		displayMessage(context, message);

		// if ConversationActivity is active, 
		// send a message to handler to refresh the conversation
		GCMInfo gcmInfo = new GCMInfo();
			gcmInfo.setGroupId(group_id);
			gcmInfo.setGroup_name(group_name);
			gcmInfo.setSuperId(post_super_id);
			gcmInfo.setUserId(user_id);
			gcmInfo.setUserName(user_name);
			gcmInfo.setPhoto(user_photo);
			gcmInfo.setBody(message);
			gcmInfo.setType(type);
			gcmInfo.setType2(type2);
			gcmInfo.setPhotoVideo(photo_video);

		// while focused on this proccess
			/*
		if(ActiveMessageHandler.instance().getActivity() != null)
		{
			Message msg = Message.obtain(ActiveMessageHandler.instance());

			msg.what = FROM_SERVICE_ON_MESSAGE;
			msg.obj = gcmInfo;

			ActiveMessageHandler.instance().sendMessage(msg);
		}
*/
		gcmInfo = null;

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
			// 만약 팝업 알림 설정이 켜져있으면 실행한다.
			if (prefs.getBoolean(CommonUtilities.POPUP_PREVIEW_SETTING, false)) {
				boolean group_sns = prefs.getBoolean(CommonUtilities.POPUP_GROUP_SNS_SETTING, false);
				Log.d(TAG, "type2: "+type2 +" /"+group_sns);
				if(type2.equalsIgnoreCase("group") && !(group_sns)){
					// Group SNS 글은 받아보지 원치 않는다면(기본)
					return;
				}
				// 팝업으로 사용할 액티비티를 호출할 인텐트를 작성한다.
				Intent popupIntent = new Intent(context, PopupMessage.class).setFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				// FLAG_ACTIVITY_MULTIPLE_TASK  ->  다른 어플리케이션 실행 중일 때 투명한 다이얼로그 보이게한다.

				StringBuffer title = new StringBuffer();
				title.append(snsinfo.getUserName());
				title.append(" 님 의");
				title.append(type.equals(CommonUtilities.FLAG_BODY_POST)? " 새글" : " 댓글");

				snsinfo.setTitle(title.toString());

				popupIntent.putExtra("popup_type", type);
				popupIntent.putExtra("popup_type2", type2);
				popupIntent.putExtra("popup_group_id", group_id);
				popupIntent.putExtra("popup_snsAppInfo", snsinfo);
				// 그리고 호출한다.
				context.startActivity(popupIntent);
			}
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
		try {
			URL url = new URL(snsinfo.getPhoto());
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			bitmap = myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException ne){
			ne.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		/*
		 * getting attachd photo
		 */
		Bitmap photo_video_bitmap = null;
		if(snsinfo.getPhotoVideo() !=null){
			try{
				URL url = new URL(snsinfo.getPhotoVideo());

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap= BitmapFactory.decodeStream(input);
				photo_video_bitmap = myBitmap;
			} catch(IOException e) {
				e.printStackTrace();
			} catch (NullPointerException ne){
				ne.printStackTrace();
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
//		
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
//		Log.i("current package name", topActivity.getPackageName());
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
			int notification_default = Notification.DEFAULT_LIGHTS;
			
			if(prefs.getBoolean(CommonUtilities.POPUP_SOUND_SETTING, false)){
				notification_default |= Notification.DEFAULT_SOUND;
			}
			if(prefs.getBoolean(CommonUtilities.POPUP_VIBRATE_SETTING, false)){
				notification_default |= Notification.DEFAULT_VIBRATE;
			}
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
			
			if(!(snsinfo.getPhotoVideo() == null || snsinfo.getPhotoVideo().equalsIgnoreCase("null"))){ 
//			 그림이 첨부된 글이라면 Big Image Notification 
				Notification noti = new Notification.BigPictureStyle(
						new Notification.Builder(context)
						.setContentTitle(title)
						.setContentText(big_text)
						.setContentIntent(pi)
						.setSmallIcon(R.drawable.ic_stat_gcm)
						.setLargeIcon(bitmap)
						.setTicker(getResources().getString(R.string.krounge_message))
						.setDefaults(notification_default)
						.setNumber(pendingNotificationsCount)
						.setWhen(System.currentTimeMillis())
							).bigPicture(photo_video_bitmap)
							.setBigContentTitle(title)
							.setSummaryText(getResources().getString(R.string.krounge_message) + " with attached")
							.build();

				noti.flags |= Notification.FLAG_AUTO_CANCEL;
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				notificationManager.notify(NOTIFICATION_ID, noti);
			}else if(!(small_text.equals("") || small_text.length() == 0)){
//				 글이 매우 긴 게시물 이라면 Big Text Notification
				Notification noti = new Notification.BigTextStyle(
						new Notification.Builder(context)
						.setContentTitle(title)
						.setContentText(small_text)
						.setContentIntent(pi)
						.setSmallIcon(R.drawable.ic_stat_gcm)
						.setLargeIcon(bitmap)
						.setTicker(getResources().getString(R.string.krounge_message))
						.setDefaults(notification_default)
						.setNumber(pendingNotificationsCount)
						.setWhen(System.currentTimeMillis())
							).bigText(big_text)
							.setSummaryText(getResources().getString(R.string.krounge_message))
							.build();

				noti.flags |= Notification.FLAG_AUTO_CANCEL;
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

				notificationManager.notify(NOTIFICATION_ID, noti);
			}else{
				Notification.Builder noti_builder = new Notification.Builder(context);
				noti_builder
				.setContentTitle(title)
				.setContentText(big_text)
				.setContentIntent(pi)
				.setSmallIcon(R.drawable.ic_stat_gcm)
				.setLargeIcon(bitmap)
				.setTicker(getResources().getString(R.string.krounge_message))
				.setDefaults(notification_default)
				.setNumber(pendingNotificationsCount)
				.setWhen(System.currentTimeMillis());
				
				Notification noti = noti_builder.getNotification();
				noti.flags |= Notification.FLAG_AUTO_CANCEL;
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.notify(NOTIFICATION_ID, noti);
			}
		}catch(Exception e){
			Log.e(LOG_TAG, "[setNotification] Exception : " + e.getMessage());
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void makeNotification_honeyComb(Context context, SnsAppInfo snsinfo, String type, String type2, SharedPreferences prefs, Bitmap user_photo_bitmap){
		String title = snsinfo.getUserName();
//		title += "님 의 메세지";
		String message = snsinfo.getBody();
		try{
			int notification_default = Notification.DEFAULT_LIGHTS;
			
			if(prefs.getBoolean(CommonUtilities.POPUP_SOUND_SETTING, true)){
				notification_default |= Notification.DEFAULT_SOUND;
			}
			if(prefs.getBoolean(CommonUtilities.POPUP_VIBRATE_SETTING, true)){
				notification_default |= Notification.DEFAULT_VIBRATE;
			}
			
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
			.setDefaults(notification_default)
			.setNumber(pendingNotificationsCount)
			.setWhen(System.currentTimeMillis());
			
			Notification noti = noti_builder.getNotification();
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(NOTIFICATION_ID, noti);
			
		}catch(Exception e){
			Log.e(LOG_TAG, "[setNotification] Exception : " + e.getMessage());
		}
	}
	
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