package kr.khub.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import kr.khub.AppConfig;
import kr.khub.CommonUtilities;
import kr.khub.R;
import kr.khub.ServerUtilities;
import kr.khub.bitmapfun.util.ImageFetcher;
import kr.khub.model.AppUser;
import kr.khub.model.GroupInfo;
import kr.khub.model.NewMessage;
import kr.khub.service.ActiveMessageHandler;
import kr.khub.util.BadgeView;
import kr.khub.util.RecycleUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import static kr.khub.CommonUtilities.GCM_SENDER_ID;
import static kr.khub.CommonUtilities.GCM_SERVER_URL;

public class KLoungeActivity extends TabActivity implements OnClickListener{
	final int GOOGLE_PLAY_ERROR = 10002;
	final int CLOSE_MESSAGE = 10003;
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private String TAG = KLoungeActivity.class.getSimpleName();
	AsyncTask<Void, Void, Void> mRegisterTask;
	AsyncTask<Void, Void, Void> mCheckTask;
	private Context context;
	private boolean mFlag = false;
	private ImageView imageView;
	private TextView textView;
	private LinearLayout file_search_layout;
	private ProgressDialog pd;
	long start = 0L;
	SharedPreferences pref;
	List<GroupInfo> group_list;
	GroupInfo view_groupinfo;
	TabHost tabHost;
	Intent intent;
	Dialog dialog;
	GoogleCloudMessaging gcm;
	ImageFetcher mImageFetcher;
//	BadgeView badge;
	int SPLASH_REQUEST_CODE = 10001;
	public static TabActivity tabActivity;
	private int GROUP_LOUNGE_TAB_NUM = 0;
	private int MY_LOUNGE_TAB_NUM = 1;
	private boolean IS_GROUP_MESSAGE = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
		AppUser.user_id = pref.getInt("user_id", 0);
		tabActivity = this;

		Intent intent = getIntent();
		String popup_type = intent.getStringExtra("popup_type");
		String popup_type2 = intent.getStringExtra("popup_type2");

		if(popup_type2 != null){
			if(popup_type2.equals("body") || !(popup_type.equals("reply"))){
				IS_GROUP_MESSAGE = true;
			}else{
				IS_GROUP_MESSAGE = false;
			}
		}
		
		// 저장 되어있는 user_photo 는 경로는 없이 파일명만 있다.
		// 어플리케이션이 실행 되면 내 사진의 전체 경로를 저장 한다.
		String user_photo = pref.getString("user_photo", "");
		AppUser.user_photo = CommonUtilities.SERVICE_URL + "/photo/"+AppUser.user_id+"/"+user_photo;
		//		Log.d(TAG, ""+user_photo);
		Intent splash_intent =new Intent(this, SplashActivity.class);
		startActivityForResult(splash_intent, SPLASH_REQUEST_CODE);

		context = getApplicationContext();

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.klounge_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		file_search_layout= (LinearLayout)findViewById(R.id.shearch_file_view);
		file_search_layout.setVisibility(View.VISIBLE);
		file_search_layout.setOnClickListener(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mImageFetcher = AppUser.mImageFetcher;
		
	}

	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(getString(R.string.error_config, name));
		}
	}

	void registGCM(){
		checkNotNull(GCM_SERVER_URL, "SERVER_URL");
		checkNotNull(GCM_SENDER_ID, "SENDER_ID");
		// Make sure the device has the proper dependencies.

		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.

		GCMRegistrar.checkManifest(this);

		intent = getIntent();
		gcm = GoogleCloudMessaging.getInstance(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		//		if(AppConfig.DEBUG)Log.d(TAG, "regID: "+regId+" /SENDER_ID:"+GCM_SENDER_ID);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GCM_SENDER_ID);
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				// 3rd 
				mCheckTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						ServerUtilities.checkDeviceRegId(regId, AppUser.user_id);
						return null;
					}
					@Override
					protected void onPostExecute(Void result) {
						mCheckTask = null;
					}
				};
				mCheckTask.execute(null, null, null);
			} else {
				Log.i(TAG, "Device is not registered on server.");
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						boolean registered = ServerUtilities.register(context, regId, AppUser.user_id);
						// At this point all attempts to register with the app
						// server failed, so we need to unregister the device
						// from GCM - the app will try to register again when
						// it is restarted. Note that GCM will send an
						// unregistered callback upon completion, but
						// GCMIntentService.onUnregistered() will ignore it.
						if (!registered) {
							GCMRegistrar.unregister(context);
						}
						return null;
					}
					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}
				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == CLOSE_MESSAGE) {
				mFlag = false;
			}
		}
	};

	void createThreadAndDialog() {
		group_list =  new ArrayList<GroupInfo>();

		AppUser.user_name = pref.getString("user_name", "");
		AppUser.CURRENT_TAB = pref.getInt("current_tab", 0);
		String strGroupList = pref.getString("group_list", "");

		//hard_coding by hglee It has to be retrieved
		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
			String[] arrGroup = strGroupList.split(CommonUtilities.SPLIT_SIGN_PARENT);

			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				String[] arrGinfo = strGroup.split(CommonUtilities.SPLIT_SIGN_CHILD);
				try{
					gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
					gInfo.setGroup_name(arrGinfo[1]);
					gInfo.setGroup_total_number(Integer.parseInt(arrGinfo[2]));
				}catch(NumberFormatException n){
					Log.e(TAG, "NumberFormatException - "+n);
				}
				group_list.add(gInfo);
			}
		}

		// 그룹 리스트 중 제일 첫번째 그룹을 보여준다. 
		if(group_list.size() > 0) view_groupinfo = group_list.get(0);
		else {
			view_groupinfo = new GroupInfo();
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		AppUser.GROUP_LIST = group_list;

		tabHost = getTabHost();

		LayoutInflater twGroupLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View twGroupView = (View)twGroupLayout.inflate(R.layout.tabwidget_group, null);

		LayoutInflater twKLoungeLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View twKLoungeView = (View)twKLoungeLayout.inflate(R.layout.tabwidget_klounge, null);

		LayoutInflater twMyLoungeLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View twMyLoungeView = (View)twMyLoungeLayout.inflate(R.layout.tabwidget_mylounge, null);

		LayoutInflater twMoreLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View twMoreView = (View)twMoreLayout.inflate(R.layout.tabwidget_more, null);
		
		int popup_group_id = getIntent().getIntExtra("popup_group_id", -1);
		Intent kloungeMsgIntent = new Intent(context, KLoungeMsg.class);
		Intent myLoungeIntent = new Intent(context, MyLounge.class);
		if(popup_group_id > 0){
			kloungeMsgIntent.putExtra("popup_group_id", popup_group_id);
			myLoungeIntent.putExtra("popup_group_id", popup_group_id);
		}
		
		tabHost.addTab(tabHost.newTabSpec("klounge").setIndicator(twKLoungeView).setContent(kloungeMsgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP)));
		tabHost.addTab(tabHost.newTabSpec("mylonge").setIndicator(twMyLoungeView).setContent(myLoungeIntent));
		tabHost.addTab(tabHost.newTabSpec("group").setIndicator(twGroupView).setContent(new Intent(context, KLoungeGroupList.class)));
		tabHost.addTab(tabHost.newTabSpec("more").setIndicator(twMoreView).setContent(new Intent(context, MoreTabActivityStack.class)));

		for(int tab=0; tab<tabHost.getTabWidget().getChildCount(); ++tab){
			tabHost.getTabWidget().getChildAt(tab).getLayoutParams().height = CommonUtilities.DPFromPixel(context, 95);
			tabHost.getTabWidget().getChildAt(tab).setBackgroundResource(R.drawable.tab_bg);
		}
		
		if(IS_GROUP_MESSAGE){
			// push 로 들어온 popup 창에서 눌러서 들어 왔을 경우에 
			//새로 들어온 메시지에 해당하는 창으로 인도 해 줘야 한다.
			tabHost.setCurrentTab(AppUser.KLOUNGE_TAB);
		}else{
			tabHost.setCurrentTab(AppUser.MYLOUNGE_TAB);
		}
		
		if(pref.getBoolean(CommonUtilities.FIRST_APP_USE, true)){
			// 어플리케이션 처음 설치 했을 때 푸쉬 노티피케이션 사용 할 것인지 선택
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(KLoungeActivity.this);
			alertDialog.setMessage(R.string.ask_user_push_permission);
			alertDialog.setPositiveButton(getResources().getString(R.string.give_permission), positive_listener);
			alertDialog.setNegativeButton(getResources().getString(R.string.text_denied), negative_listener);
			alertDialog.show();
		}
	}

	@Override
	public void onBackPressed() {
		if(!mFlag) {
			Toast.makeText(context, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.", LENGTH_TO_SHOW).show();
			mFlag = true;
			handler.sendEmptyMessageDelayed(CLOSE_MESSAGE, 2000);
		} else {
			finish();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		boolean isKill = intent.getBooleanExtra("KILL_ACT",false);
		if(isKill)finish();
	}

	@Override
	protected void onDestroy() {
		Log.i("Destory", "app is destroyed");
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}

		if(mImageFetcher != null){
			mImageFetcher.closeCache();
		}
		if(AppConfig.PUSH){
			ActiveMessageHandler.instance().setActivity(null);
		}

		//그룹리스트 sharedpreference 에서 삭제
		SharedPreferences.Editor editor = pref.edit();
		editor.remove("group_list");
		editor.commit();

		Log.d(TAG, "on destroy: " + AppUser.NEW_MESSAGE.size());

		storeJsonFile(AppUser.NEW_MESSAGE);

		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
	void storeJsonFile(ArrayList<NewMessage> nList){
		Log.d(TAG, "size(): "+nList.size());
		final String FILENAME = "new_message_data.json";

		File dir = getFilesDir();
		File file = new File(dir, FILENAME);
		file.delete();
		if(nList.size() > 0){
			StringBuilder sb = new StringBuilder();
			sb.append("{ \"new_message\" : [");
			int index = 0;
			for(NewMessage n:nList){
				Log.d(TAG, "index: "+index++);
				JSONObject obj = new JSONObject();
				try {
					obj.put("group_id", n.getGroup_id());
					obj.put("group_name", n.getGroup_name());
					obj.put("super_id", n.getSuper_id());
					obj.put("user_id", n.getUser_id());
					obj.put("user_name", n.getUser_name());
					obj.put("user_photo", n.getUser_photo());
					obj.put("message", n.getMessage());
					obj.put("type_1", n.getType1());
					obj.put("type_2", n.getType2());

					sb.append(obj.toString()).append("]}");
					byte[] outByte = sb.toString().getBytes();

					FileOutputStream fos;

					fos = openFileOutput(FILENAME, Context.MODE_APPEND);
					fos.write(outByte);

					fos.close();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onStop()
	 */
	@Override
	protected void onPause() {

		super.onPause();
		if(mImageFetcher != null){
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
		}
		if(dialog!=null)dialog.dismiss();
	}

	@Override
	public void onResume() {
		super.onResume();
		int googlePlay = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if(!(googlePlay == ConnectionResult.SUCCESS)){
			// google play service 가 디바이스에 설치 되어 있지 않은지 체크 한다.
			// GCM service 를 이용하기 위한 필수 어플리케이션이다.
			dialog = GooglePlayServicesUtil.getErrorDialog(googlePlay, KLoungeActivity.this, GOOGLE_PLAY_ERROR);
			dialog.show();
		}
		if(mImageFetcher != null){
			// image cache process 
			mImageFetcher.setExitTasksEarly(false);
		}
	}
	DialogInterface.OnClickListener positive_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor editor = pref.edit();
			
			String key = CommonUtilities.POPUP_SETTING;
			editor.putBoolean(key, true);
			
			String first_app_use = CommonUtilities.FIRST_APP_USE;
			editor.putBoolean(first_app_use, false);
			
			editor.commit();
			
			dialog.dismiss();
		}
	};
	
	DialogInterface.OnClickListener negative_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			SharedPreferences.Editor editor = pref.edit();
			
			String key = CommonUtilities.POPUP_SETTING;
			editor.putBoolean(key, false);
			
			String first_app_use = CommonUtilities.FIRST_APP_USE;
			editor.putBoolean(first_app_use, false);
			
			editor.commit();
			
			dialog.dismiss();
		}
	};
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		if(AppConfig.PUSH){
			// push message 가 들어 왔을 때 Service 와 Activity 를 이어주기 위한 handler 기본 셋팅
			ActiveMessageHandler.instance().setActivity(this);
		}
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == GOOGLE_PLAY_ERROR){
			if(dialog!=null)dialog.dismiss();
		}else if(requestCode == SPLASH_REQUEST_CODE){
			//splash activity 가 종료 되면 상단 탭과, 하단 KloungeMsg 를 구성한다.
			if(AppConfig.PUSH){
				registGCM();
			}
			createThreadAndDialog();
		}
		
		TabWidget tabs = (TabWidget)findViewById(android.R.id.tabs);
		for(int i=0;i<AppUser.NEW_MESSAGE.size();i++){
			//새로운 메시지 있을 때 Tab 에 표시를 달아준다.
			if(AppUser.NEW_MESSAGE.get(i).getType2().equals("group")
					&&!(AppUser.NEW_MESSAGE.get(i).getUser_id() == AppUser.user_id)){
				BadgeView group_badge = new BadgeView(KLoungeActivity.this, tabs, GROUP_LOUNGE_TAB_NUM);
				group_badge.setText("N");
				group_badge.show();
			}else if(AppUser.NEW_MESSAGE.get(i).getType2().equals("my")
					&&!(AppUser.NEW_MESSAGE.get(i).getUser_id() == AppUser.user_id)){
				BadgeView personal_badge = new BadgeView(KLoungeActivity.this, tabs, MY_LOUNGE_TAB_NUM);
				personal_badge.setText("N");
				personal_badge.show();
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FileSearchListActivity.class);
		startActivity(intent);
	}
}