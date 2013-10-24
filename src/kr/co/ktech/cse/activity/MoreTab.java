package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.KEY_VER_PREFERENCE;
import static kr.co.ktech.cse.CommonUtilities.LICENSE;
import static kr.co.ktech.cse.CommonUtilities.PRIVACY_POLICY;
import static kr.co.ktech.cse.CommonUtilities.SHARED_PREFERENCE;
import static kr.co.ktech.cse.CommonUtilities.TERM_CONDITION;
import static kr.co.ktech.cse.CommonUtilities.VERSION_CHECK_CODE;
import static kr.co.ktech.cse.CommonUtilities.WHERE_IT_FROM;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.adapter.BaseExpandableAdapter;
import kr.co.ktech.cse.util.CheckUpdate;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.widget.ExpandableListView.OnChildClickListener;

public class MoreTab extends ExpandableListActivity implements OnChildClickListener{

	public static Context context;
	ExpandableListView morelist;
	
	List<Map<String, String>> dicType = new ArrayList<Map<String, String>>();
	//moretap리스트의 data를 저장할 List Map을 생성한다. List안에 List안에 Map의 형태를 뛴다.
	
	List<List<Map<String, String>>> dicArrName = new ArrayList<List<Map<String, String>>>();
	//List의 Map에 key와 데이터를 저장하기 위한 코드
	
	ExpandableListAdapter adapter;
	DisplayMetrics metrics;
	Button button;
	SharedPreferences pref;
	private static final int FILE_SELECT_CODE = 0;
	String TAG = "MoreTab";
	TextView my_versioin;
	TextView recent_version;
	PackageInfo pi = null;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	CheckBox cb_setting = null;
	TextView tv_msg = null;
	private BaseExpandableAdapter eAdapter;
	Dialog mDialog;
	TextView tv;
	String filename = null;
	String dir = null;

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DisplayMetrics metrics;
		int width;
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = metrics.widthPixels;

		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_more_tab2);
		morelist = (ExpandableListView)findViewById(android.R.id.list);
		Drawable d = getResources().getDrawable(R.drawable.list_selector);
		morelist.setIndicatorBounds(
				width - CommonUtilities.DPFromPixel(context, 50), 
				width - CommonUtilities.DPFromPixel(context, 10));
		morelist.setGroupIndicator(d);

		List<Map<String, String>> dicType = new ArrayList<Map<String, String>>(); //사전의 data를 저장할 List Map을 생성한다. List안에 List안에 Map의 형태를 뛴다.
		final List<List<Map<String, String>>> dicArrName = new ArrayList<List<Map<String, String>>>();//List의 Map에 key와 데이터를 저장하기 위한 코드
		
		String[] titles = getResources().getStringArray(R.array.parent_menu);//{"프로필","설정" /*"FAQ" */ ,getResources().getText(R.string.app_name_ko)+" 정보"};
		String[] myInformation = getResources().getStringArray(R.array.user_information);//{"내정보","로그아웃"};
		String[] settings = getResources().getStringArray(R.array.popup_detail_settings);//{"알림 수신"};
		String[] appInformation = getResources().getStringArray(R.array.app_information);//{"개인정보 보호정책","이용약관","오픈소스 라이센스", /*"문의",*/ "버젼정보"};
		String[][] contents = {myInformation, settings, appInformation};

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE, 0);

		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}

		for(int i=0;i<titles.length;i++) {
			Map<String, String> type = new HashMap<String, String>();     //key값과 데이터를 저장
			type.put("Type", titles[i]);     //dicType List에 HashMap을 저장
			dicType.add(type);
			List<Map<String, String>> arrName = new ArrayList<Map<String, String>>();
			for(int j=0;j<contents[i].length;j++) {
				Map<String, String> name = new HashMap<String, String>();
				name.put("firstName", contents[i][j]);
				if (i == 1){
					if(j == 0) name.put("sdName","v"+pi.versionName);
					else name.put("sdName","v"+prefs.getString(KEY_VER_PREFERENCE, ""));
				}else{
					name.put("sdName","");
				}
				arrName.add(name);
			}
			dicArrName.add(arrName);
		}

		int resId = R.layout.expandable_list_item;
		eAdapter = new BaseExpandableAdapter(this, titles, contents, resId);

		setListAdapter(eAdapter);
		
		morelist.setOnChildClickListener(this);
		morelist.invalidate();
	}

	public void makeLogoutConfirm(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getParent());
		alertDialog.setTitle("로그아웃");
		alertDialog.setMessage("로그아웃 하시겠습니까?");

		alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				if(pref==null)pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
				deleteUserInformation(pref.getString("login_id", ""),"",false);

				SharedPreferences.Editor edit = pref.edit();
				//				edit.putBoolean("loginState", false);
				edit.putInt("user_id", 0);
				edit.putString("user_name", "");
				edit.putString("group_list", "");
				//				edit.clear();
				edit.commit();
				try {
					Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
					startActivity(intent);

					getParent().finish();
				} catch (android.content.ActivityNotFoundException anfe) {
					Log.e(TAG, "ActivityNotFoundException"+anfe);
				}
			}
		});
		alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {	   
			}
		});
		alertDialog.show();
	}
	private void deleteUserInformation(String id, String pwd, boolean state){
		try {
			JSONObject obj = new JSONObject();
			obj.put("login_id", id);
			obj.put("login_pwd", new String(Base64.encodeBase64(pwd.getBytes())));
			obj.put("login_state", state);

			FileOutputStream fos = openFileOutput("login_data.json", Context.MODE_PRIVATE);
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
	public void makeAlertDialog(String ver_code){
		if(!("v"+pi.versionName).equals(ver_code)){
			vibrator.vibrate(VIBRATE_PERIOD);
			if(AppConfig.DEBUG){
				Log.d(TAG, "v"+pi.versionName);
				Log.d(TAG, ver_code);
			}
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getParent());
			alertDialog.setTitle("업데이트 알림");
			alertDialog.setMessage("현재 K-Rounge 의 버젼은 "+pi.versionName+" 입니다.\n현재 "+ver_code+" 버젼이 업데이트 되었습니다.\n다운로드 하고 설치 하시겠습니까?");

			alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+pi.packageName)));
					} catch (android.content.ActivityNotFoundException anfe) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id"+pi.packageName)));
					}
				}
			});
			alertDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {	   
				}
			});
			alertDialog.show();
		}
	}

	Thread thread = new Thread(new Runnable() {
		@Override
		public void run() {
			Message msg = Message.obtain();
			msg.obj = new CheckUpdate().check(pi.packageName);
			msg.what = VERSION_CHECK_CODE;
			handler.sendMessage(msg);
		}
	});
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == VERSION_CHECK_CODE) {
				String recent_ver_code = (String)msg.obj;
			}
		}
	};

	private void showFileChooser(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try{
			startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
		}catch(android.content.ActivityNotFoundException ex){
			Toast.makeText(this, "Please install a File Manager", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:      
			if (resultCode == RESULT_OK) {  
				// Get the Uri of the selected file 
				Uri uri = data.getData();
				String filePath = uri.toString();

				// Get the path (한글 포함)
				try {
					String hangul = URLDecoder.decode(filePath, "UTF-8");
					int dir_divider = hangul.lastIndexOf("/");
					filename = hangul.substring(dir_divider+1);
					dir = hangul.substring(0, dir_divider);
					dir = dir.replace("file://", "");
					if(AppConfig.DEBUG)Log.d(TAG, "dir= "+dir +"/" + "filename= "+filename);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tv.setText(dir+"/"+filename);
			}           
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {

		switch(groupPosition){
		case 0:
			switch(childPosition){
			case 0:
//				Myprofile profileview = new Myprofile();
				Intent intent = new Intent(getApplicationContext(), Myprofile.class);
				startActivityForResult(intent, 1);
				break;
			case 1:
				makeLogoutConfirm();
				break;
			}
			break;
		case 1:
			
			break;
		case 2:
			Intent intent = new Intent(getParent(), TextViewActivity.class);
			switch(childPosition){
			case 0:
				intent.putExtra(WHERE_IT_FROM, PRIVACY_POLICY);
				break;
			case 1:
				intent.putExtra(WHERE_IT_FROM, TERM_CONDITION);
				break;
			case 2:
				intent.putExtra(WHERE_IT_FROM, LICENSE);
				break;
			case 3:
				intent = new Intent(getParent(), AppInformation.class);
				break;
			case 4:
				break;
			}
			startActivity(intent);
			break;
		case 3:
			switch(childPosition){
			case 0:
				break;
			}
			break;
		}
		return false;
	}
	
}