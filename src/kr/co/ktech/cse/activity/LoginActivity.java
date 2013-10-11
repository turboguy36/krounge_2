package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.KLOUNGE_STORAGE_LOCATION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.KloungeApplication;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.db.LoginRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.LoginInformation;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity implements OnClickListener, OnKeyListener, OnEditorActionListener{
	private String TAG = LoginActivity.class.getSimpleName();
	private String filename = "login_data.json";
	File myInternalFile;
	EditText etID;
	EditText etPasswd;
	private RelativeLayout whole_view;
	boolean LOGIN = false;
	boolean loginState = false;
	SharedPreferences pref;
	ProgressDialog dialog;
	ImageView imageView;
	ImageView login_button;
	TextView textView;
	LoginRequest loginproc;
	Intent intent;
	ImageView remove_btn_id;
	ImageView remove_btn_pwd;
	final int CLOSE_MESSAGE = 227;
	LoginInformation inform;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
		loginproc = new LoginRequest(); // 서버와 통신할 객체 선언
		inform = new LoginInformation();
		inform.setLoginState(loadFile());
		loginState = inform.isLoginState();
		if(loginState){
			// 이미 로그인한 사용자라면
			makeView(inform.getUser_id(), inform.getUser_pwd());
		}else{
			// 로그인을 하도록 유도 하려면
			makeView(inform.getUser_id());
		}
		File dir = getCacheDir();
		KLOUNGE_STORAGE_LOCATION = dir.getPath();	// 캐쉬 저장소 초기화
	}
	private void makeView(String file_id){
		// 첫 시작 화면 셋팅
		setContentView(R.layout.login);

		whole_view = (RelativeLayout)findViewById(R.id.LoginLayout);

		etID = (EditText)findViewById(R.id.etID);
		etPasswd = (EditText)findViewById(R.id.etPasswd);
		login_button = (ImageView)findViewById(R.id.ivLogin);
		remove_btn_id = (ImageView)findViewById(R.id.remove_all_text);
		remove_btn_pwd = (ImageView)findViewById(R.id.remove_all_pwd);

		if(file_id != null){
			etID.setText(file_id);
		}

		login_button.setOnClickListener(this);
		etID.setOnKeyListener(this);
		etPasswd.setOnEditorActionListener(this);
		whole_view.setOnClickListener(this);
		remove_btn_id.setOnClickListener(this);
		remove_btn_pwd.setOnClickListener(this);
	}
	private void makeView(String id_from_file, String pwd_from_file) {
		// 이미 로그인 되어있는 상태
		setContentView(R.layout.activity_splash);
		TextView tv = (TextView)findViewById(R.id.splash_wait_text);
		tv.setText("로그인 중입니다..");
		tv.setVisibility(View.VISIBLE);
		
		byte[] decoded = new Base64().decode(pwd_from_file.getBytes());
		
		loginWithAsyncTask(id_from_file, new String(decoded), loginState);
		// 원래는 KLoungeActivity.java 로 바로 넘어 갔었는데, 
		// preference 에 group_list 를 저장 하는 과정이 필요 하여 로그인 되어 있어도
		// mTask 를 execute 해 주어야 한다.
	}
	
	private void loginWithAsyncTask(String id, String pwd, boolean logined){
		LoginTask loginTask = new LoginTask(logined);
		loginTask.execute(id,pwd);
	}
	public Intent loadExtrasFromPopup(Intent intent){
		Intent popup_intent = getIntent();
		
		intent.putExtra("popup_type", popup_intent.getStringExtra("popup_type"));
		intent.putExtra("popup_type2", popup_intent.getStringExtra("popup_type2"));
		intent.putExtra("popup_group_id", popup_intent.getIntExtra("popup_group_id", -1));
		intent.putExtra("popup_super_post_id", popup_intent.getIntExtra("popup_super_post_id", -1));

		// notification message 올 때 count 숫자를 0 으로 다시 init
		KloungeApplication.setPendingNotificationsCount(0);
		
		return intent;
	}
	private class LoginTask extends AsyncTask<String, ProgressInfo, Boolean>{
		boolean IS_SERVER_AVAILABLE = true;
		String async_login_id = "";
		String async_login_pwd = "";
		//Async 에서 쓰이는 변수 login_id, login_pwd
		ProgressBar progress;
		TextView tv;
		boolean already_logined = false;
		KLoungeHttpRequest httprequest;
		private final String server_communication_msg = "서버에 로그인 정보를 요청하고 있습니다.";
		private final String making_group_list = "그룹 리스트를 생성하고 있습니다."; 
		public LoginTask(boolean whereFrom){
			already_logined = whereFrom;
			if(already_logined){
				progress = (ProgressBar)findViewById(R.id.splash_progress);
				tv = (TextView)findViewById(R.id.splash_wait_text);
			}
			httprequest = new KLoungeHttpRequest();
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			Toast.makeText(getApplicationContext(), "로그인이 취소되었습니다", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(already_logined){
				try{
					progress.setVisibility(View.VISIBLE);
					tv.setVisibility(View.VISIBLE);
				}catch(NullPointerException n){

				}
			}
			if(!loginState){
				dialog = ProgressDialog.show(LoginActivity.this, "",
						"로그인 중입니다. 잠시 기다려주세요", true);
			}
		}
		@Override
		protected void onProgressUpdate(ProgressInfo... values) {
			super.onProgressUpdate(values);
			if(already_logined){
				try{
					progress.setProgress(values[0].getPercent());
					tv.setText(values[0].getMessage());
				}catch(NullPointerException n){

				}
			}
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			async_login_id = params[0];
			async_login_pwd = params[1];
			if((async_login_id.length() > 0 || !async_login_id.equals("")) 
					&& (async_login_pwd.length() > 0 || !async_login_pwd.equals(""))){
				String get_group_list = login(async_login_id, async_login_pwd, pref);
				publishProgress(new ProgressInfo(100, server_communication_msg));
				
				loginState = setGroupInformation(get_group_list);
//				Log.d(TAG, "loginState: "+loginState);
			}else{
				loginState = false;
			}
			storeJsonFile(async_login_id, async_login_pwd, loginState);
			return loginState;
		}
		@Override
		protected void onPostExecute(Boolean login_state) {
			super.onPostExecute(login_state);

			SharedPreferences.Editor edit = pref.edit();
			if(login_state) {
				byte[] encoded = Base64.encodeBase64(async_login_pwd.getBytes());
				edit.putString("login_id", async_login_id);
				edit.commit();

				Intent intent = new Intent(LoginActivity.this, KLoungeActivity.class);
				intent.putExtra("app_user_id", async_login_id);
				intent.putExtra("app_user_passwd", new String(encoded));
				
				startActivity(loadExtrasFromPopup(intent));
				
				AppUser.login_id = async_login_id;

				finish();
			} else {
				String error_message = "";
				
				if(dialog != null)dialog.dismiss();
				
				int string_id = -1;
				int gravity = -1;
				
				if(!isOnline()){
					string_id = R.string.network_error;
					gravity = Gravity.CENTER;
				}else if(!IS_SERVER_AVAILABLE){
					string_id = R.string.server_error;
					gravity = Gravity.CENTER;
				}else{
					string_id = R.string.login_error;
					gravity = Gravity.CENTER_HORIZONTAL;
				}
				error_message = getResources().getString(string_id);
				Toast toast = Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_LONG);
				toast.setGravity(gravity, 0, 0);
				toast.show();
				
				finish();
				startActivity(new Intent(LoginActivity.this,LoginActivity.class));
			}
			if(dialog != null)dialog.dismiss();
		}
		public String login(String id, String passwd, SharedPreferences pref) {
//			if(AppConfig.DEBUG)Log.d("Login",id+"/"+passwd);
			publishProgress(new ProgressInfo(10, server_communication_msg));
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appLogin.jsp";
			
			String parameter = "login_id="+id+"&passwd="+passwd;
			addr = addr+"?"+parameter;

//			if(AppConfig.DEBUG)Log.d("Login",addr);

			return httprequest.getJSONHttpURLConnection(addr);
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
		
		private boolean setGroupInformation(String strJSON){
//			Log.d(TAG, "strJSON: "+strJSON);
			publishProgress(new ProgressInfo(0, making_group_list));
			if(strJSON.equals(CommonUtilities.SERVER_UNAVAILABLE)){
				IS_SERVER_AVAILABLE = false;
				return false;
			}
			boolean result = false;
//			if(strJSON == null)return false;
			JSONObject jsonObj = null;
			try {
				jsonObj = new JSONObject(strJSON);
				int login_result = Integer.parseInt(jsonObj.getString("login_result"));
				if(login_result == 1){
					Log.d(TAG, "Login result: " + login_result);
					result = true;
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try{
				if(strJSON.length() > 0) {
					//파싱
					JSONObject userObj = jsonObj.getJSONObject("user");
					int user_id = Integer.parseInt(userObj.getString("user_id"));

					String user_name = userObj.getString("user_name");
					String user_photo = userObj.getString("user_photo");

					JSONArray groupArray = jsonObj.getJSONArray("group");

					StringBuffer grouplist = new StringBuffer();

					int size = groupArray.length();
//					Log.d(TAG, "size: "+size);
					if(size > 0){
						for(int i=0; i < size; i++){
							JSONObject groupObj = groupArray.getJSONObject(i);
							int group_id = Integer.parseInt(groupObj.getString("group_id"));
							String group_name = groupObj.getString("group_name");
							int group_tot_num = groupObj.getInt("group_total_number");
							grouplist.append(group_id+"|"+group_name+"|"+group_tot_num+CommonUtilities.SPLIT_SIGN_PARENT);
							
							if(already_logined){
								publishProgress(new ProgressInfo( Math.round(
										(100/size)*(i+1)) , making_group_list
										));
							}
						}
					}
					// 공개라운지 정보를 삽입
					grouplist.append("0|공개라운지|9999");

					// 사용자 로그인 정보 저장 - 로그인 상태, user_id, 사용자 이름
					SharedPreferences.Editor edit = pref.edit();
					edit.putInt("user_id", user_id);
					edit.putString("user_name", user_name);
					edit.putString("user_photo", user_photo);
					edit.putString("group_list", grouplist.toString());
					edit.commit();
				}
			} catch (JSONException e) {
				e.printStackTrace();
//				result = false;
			} catch (Exception e) {
				e.printStackTrace();
//				result = false;
			}
			return result;
		}
	}
	private static class ProgressInfo{
		final int percent;
		final String message;
		private ProgressInfo(int num, String msg){
			this.percent = num;
			this.message = msg;
		}
		public int getPercent() {
			return percent;
		}
		public String getMessage() {
			return message;
		}
	}
	private boolean loadFile(){
		boolean result = false;
		FileInputStream fis = null;
		try {
			fis = openFileInput(filename);
			byte in[] = new byte[fis.available()];
			fis.read(in);
			JSONObject obj = new JSONObject(new String(in));
			Log.d(TAG, obj.toString());
			inform.setUser_id(obj.getString("login_id"));
			inform.setUser_pwd(obj.getString("login_pwd"));
			result = obj.getBoolean("login_state");
			fis.close();
		}catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		}catch (JSONException e) {
			result = false;
			e.printStackTrace();
		}catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	private void storeJsonFile(String id, String pwd, boolean state){
		try {
			JSONObject obj = new JSONObject();
			obj.put("login_id", id);
			obj.put("login_pwd", new String(Base64.encodeBase64(pwd.getBytes())));
			obj.put("login_state", state);
			
			FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
			byte[] outByte = obj.toString().getBytes();
			fos.write(outByte);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch(v.getId()){
		case R.id.etID:
			if(keyCode == KeyEvent.KEYCODE_ENTER){
				if(event.getAction()== KeyEvent.ACTION_DOWN){
				}else if(event.getAction()== KeyEvent.ACTION_UP){
					etID.setText(etID.getText().toString().trim());
					etPasswd.requestFocus();
				}
				return false;
			}
			break;
		}
		return false;
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.ivLogin:
			loginWithAsyncTask(etID.getText().toString(), etPasswd.getText().toString(), loginState);
			break;
		case R.id.LoginLayout:
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(whole_view.getWindowToken(), 0);
			break;
		case R.id.remove_all_text:
			etID.setText("");
			break;
		case R.id.remove_all_pwd:
			etPasswd.setText("");
			break;
		}
	}
	
	@Override
	public boolean onEditorAction(TextView v,
			int actionId, KeyEvent event) {
		switch(v.getId()){
		case R.id.etPasswd:
			if(actionId == EditorInfo.IME_ACTION_DONE){
				loginWithAsyncTask(etID.getText().toString(), etPasswd.getText().toString(), loginState);
			}
			break;
		}
		return false;
	};
}
