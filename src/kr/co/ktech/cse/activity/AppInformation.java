package kr.co.ktech.cse.activity;



import static kr.co.ktech.cse.CommonUtilities.KEY_VER_PREFERENCE;
import static kr.co.ktech.cse.CommonUtilities.SHARED_PREFERENCE;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.util.FileUploader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;

public class AppInformation extends Activity{

	DisplayMetrics metrics;
	Button button;
	SharedPreferences pref;
	String TAG = "MoreTab";
	TextView my_versioin;
	TextView recent_version;
	PackageInfo pi = null;
	Vibrator vibrator;
	LinearLayout baseLayout;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	FileUploader fu = new FileUploader();
	TextView tv;
	MoreTabActivityStack stackActivity;
	private ImageView imageView;
	private TextView textView;
	String utf = "UTF-8";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		stackActivity = (MoreTabActivityStack)getParent();
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_more_tab);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);

		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
		}
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCE, 0);

		my_versioin = (TextView)findViewById(R.id.numtext_version);
		recent_version = (TextView)findViewById(R.id.numtext_recent_version);
		recent_version.setText("v"+prefs.getString(KEY_VER_PREFERENCE, ""));

		recent_version.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		my_versioin.setText("v"+pi.versionName);
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
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}
}
