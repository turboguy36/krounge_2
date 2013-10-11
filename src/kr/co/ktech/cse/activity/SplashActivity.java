package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.KEY_VER_PREFERENCE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.GroupMemberInfo;
import kr.co.ktech.cse.model.NewMessage;
import kr.co.ktech.cse.util.CheckUpdate;
import kr.co.ktech.cse.util.RecycleUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import static kr.co.ktech.cse.CommonUtilities.SHARED_PREFERENCE;

public class SplashActivity extends Activity {
	String TAG = SplashActivity.class.getSimpleName();
	String UPGRADE_DIALOG_STATUS = "dialog_status";
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	int height = 0;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_splash);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		prefs = getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
		
		editor = prefs.edit();

		height = getResources().getDisplayMetrics().heightPixels;

		if(!isOnline()){
			// 3g/ 4g/ wi-fi / 모두 안 될 때
			showDialog_WhenNoDataLink();
		}

		setGroupList(); 
		// async task 를 이용해 어플 활동에서 필요한 GroupList 를 미리 받아 저장 해 둔다.
		setAppConfigValues();
	}
	private void setAppConfigValues(){
		AppConfig.USE_PUSH_MESSAGE = prefs.getBoolean(CommonUtilities.POPUP_SETTING, true);
		AppConfig.PREVIEW_PUSH_MESSAGE = prefs.getBoolean(CommonUtilities.POPUP_PREVIEW_SETTING, true);
		AppConfig.SOUND_PUSH_MESSAGE = prefs.getBoolean(CommonUtilities.POPUP_SOUND_SETTING, true);
		AppConfig.VIBRATE_PUSH_MESSAGE = prefs.getBoolean(CommonUtilities.POPUP_VIBRATE_SETTING, true);
	}
	public void setGroupList() {
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("title");

		String strGroupList = prefs.getString("group_list", "");
		
		//hard_coding by hglee It has to be retrieved
		strGroupList = strGroupList + "|0";
		List<GroupInfo> groupList = new ArrayList<GroupInfo>();

		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name ...
			String[] arrGroup = strGroupList.split(CommonUtilities.SPLIT_SIGN_PARENT);

			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();

				String[] arrGinfo = strGroup.split(CommonUtilities.SPLIT_SIGN_CHILD);
				try{
					gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
					gInfo.setGroup_name(arrGinfo[1]);
					gInfo.setGroup_total_number(Integer.parseInt(arrGinfo[2]));
				}catch(NumberFormatException n){
					Log.e(TAG, "NumberFormatException -"+n);
				}
				groupList.add(gInfo);
			}
		}

		GetDataTask asyncTask = new GetDataTask(groupList);

		if(isOnline()){
			asyncTask.execute();
		}
	}

	public void showDialog_WhenNoDataLink(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this);
		alertDialog.setMessage(
				"K-Rounge 는 데이터 통신을 요구 합니다.\n"+
						"이 기기는 사용 가능한 3G/4G/Wi-Fi 가 없어 " +
						"이용 가능한 서비스가 없습니다.\n"+
						"데이터 설정 후 프로그램을 재실행 해주세요."
				);

		alertDialog.setPositiveButton("설정 하러 가기", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
				// splash activity 종료

				Intent intent = new Intent(SplashActivity.this, KLoungeActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("KILL_ACT", true);
				startActivity(intent);
				// 상위 activity 종료(앱 전체 종료)

				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				// 와이파이 설정 창 가기
			}
		}).setNegativeButton("무시 하기", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertDialog.show();
	}

	private boolean isOnline() {
		boolean connected = false;
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(0).getState(); //mobile
		State wifi = conMan.getNetworkInfo(1).getState(); //wifi
		if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
			connected = true;
		} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
			connected = true;
		}
		return connected;
	}

	@Override
	protected void onDestroy() {
		//		Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}

	private class GetDataTask extends AsyncTask<Void, Integer, String>{
		// group member list 를 저장
		ProgressBar progress;
		TextView tv;
		PackageInfo pi = null;
		SparseArray<List<GroupMemberInfo>> group_member_list;
		private KLoungeGroupRequest kloungeGrouphttp;
		List<GroupInfo> ginfoList = new ArrayList<GroupInfo>();
		String TAG = "GetDataThread";

		private GetDataTask(List<GroupInfo> list){
			group_member_list = new SparseArray<List<GroupMemberInfo>>();
			kloungeGrouphttp = new KLoungeGroupRequest();
			ginfoList = list;
			try {
				pi = getPackageManager().getPackageInfo(getPackageName(), 0);
				// PackageInfo 초기화
			}catch (NameNotFoundException e1) {
				Log.e(TAG, "NameNotFoundException - "+e1);
			}
			progress = (ProgressBar)findViewById(R.id.splash_progress);
			tv = (TextView)findViewById(R.id.splash_wait_text);
		}

		@Override
		protected void onPreExecute() {
			progress.setVisibility(View.VISIBLE);
			tv.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			int size = ginfoList.size()-1;
			for(int i=0;i<size;i++){
				publishProgress(Math.round(
						(100/size)*(i+1))
						);
				List<GroupMemberInfo> gmInfo = new ArrayList<GroupMemberInfo>();
				int group_id = ginfoList.get(i).getGroup_id();
				gmInfo = kloungeGrouphttp.getGroupMemberList(AppUser.user_id, group_id);
				group_member_list.append(group_id, gmInfo);
			}
			getNewMessageList(this, tv);
			
			String most_recent_ver = "";
			String stored_ver = "";
			try{
				most_recent_ver = new CheckUpdate().check(pi.packageName);
				stored_ver = prefs.getString(KEY_VER_PREFERENCE, "");
				if(most_recent_ver == null){
					Log.e(TAG, "most_recent_ver null");
				}
				if(stored_ver == null){
					Log.e(TAG, "stored_ver null");
				}

				if(!most_recent_ver.equals(stored_ver)){
					// 같지 않다. 즉 upgrade version 있다.
					editor.putBoolean(UPGRADE_DIALOG_STATUS, false);
				}
				
				editor.putString(KEY_VER_PREFERENCE, most_recent_ver);
				// 현재 가장 최신 버젼이 몇인지 저장
				editor.commit();
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}
			
			//새로운 메세지가 있으면 객체화 하여 저장한다.
			
			return most_recent_ver;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			SparseArray<List<GroupMemberInfo>> gminfo = group_member_list;
			AppUser.GROUP_MEMBER = gminfo;
			//			Log.d(TAG, "MEMBER LIST handler");
			boolean dialog_status = prefs.getBoolean(UPGRADE_DIALOG_STATUS, false);

			String recent_ver_code = result;
			AppUser.MY_APP_VERSION = pi.versionName;
			if(!dialog_status && !(pi.versionName.equals(recent_ver_code))){
				// 만일 Manifest.xml 에 기록된 버젼(pi.versionName)이 최신 버젼보다 낮다면
				//				boolean dialog_status = prefs.getBoolean(UPGRADE_DIALOG_STATUS, false);

				makeCheckUpdateDialog(recent_ver_code, SplashActivity.this);

			}else{
				finish();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progress.setProgress(values[0]);

		}
		
		public void makeCheckUpdateDialog(final String ver_code, Context c){

			View checkboxView = View.inflate(c, R.layout.checkbox_alertdialog, null);
			CheckBox ckBox = (CheckBox)checkboxView.findViewById(R.id.checkbox);
			ckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(AppConfig.DEBUG)Log.d(TAG, "save shared preference");
					editor.putBoolean(UPGRADE_DIALOG_STATUS, isChecked);
					// version upgrade 여부에 상관없이 preference 에 현재 release 된 최신버젼을 저장 해 둔다.
					editor.commit();
				}
			});

			ckBox.setText("다시 보지 않기");

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
			alertDialog.setTitle("업데이트 알림");
			alertDialog.setView(checkboxView);
			alertDialog.setMessage(
					"K-Rounge 의 "+
							ver_code+
					" 버젼이 업데이트 되었습니다.\n다운로드 하고 설치 하시겠습니까?");

			alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+pi.packageName)));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id"+pi.packageName)));
					}
					finish();
				}
			});
			alertDialog.setNegativeButton("나중에 하기", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			alertDialog.show();
		}
		public void doProgress(int value){
			publishProgress(value);
		}
	}
	public void getNewMessageList(GetDataTask async, TextView tv){
//		async.doProgress(0);
//		tv.setText("새 메세지를 받아 오고 있습니다.");
//		ArrayList<NewMessage> nList = new ArrayList<NewMessage>();
		
		final String FILENAME = "new_message_data.json";
		try {
			FileInputStream fis = openFileInput(FILENAME);
			byte in[] = new byte[fis.available()];
			fis.read(in);
			ArrayList<NewMessage> nList = new ArrayList<NewMessage>();
			JSONObject existFileObj = new JSONObject(new String(in));
			JSONArray existJsonArray = existFileObj.getJSONArray("new_message");

			int length = existJsonArray.length();
			
			for(int i=0; i<length;i++){
				NewMessage nMessage = new NewMessage();
				JSONObject existJsonObject = existJsonArray.getJSONObject(i);
				async.doProgress(Math.round(
						(100/length)*(i+1)));
				if(async.isCancelled()){
					break;
				}
				nMessage.setMessage(existJsonObject.getString("message"));
				nMessage.setGroup_id(existJsonObject.getInt("group_id"));
				nMessage.setGroup_name(existJsonObject.getString("group_name"));
				nMessage.setSuper_id(existJsonObject.getInt("super_id"));
				nMessage.setUser_id(existJsonObject.getInt("user_id"));
				nMessage.setUser_name(existJsonObject.getString("user_name"));
				nMessage.setUser_photo(existJsonObject.getString("user_photo"));
				nMessage.setType1(existJsonObject.getString("type_1"));
				nMessage.setType2(existJsonObject.getString("type_2"));
				
				nList.add(nMessage);
			}
			AppUser.NEW_MESSAGE = nList;
			
			fis.close();
			File file = new File(FILENAME);
			file.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
