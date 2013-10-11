package kr.co.ktech.cse.activity;

import java.util.ArrayList;
import java.util.List;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.bitmapfun.ui.ImageGridActivity;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.GroupMemberInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TableLayout.LayoutParams;
import static kr.co.ktech.cse.CommonUtilities.IMAGE_CACHE_DIR;
import static kr.co.ktech.cse.CommonUtilities.SHARED_PREFERENCE;

public class KLoungeGroupList extends Activity{
	String TAG = "KLoungeGroupList";
	private static final int GROUP_NAME_SIZE = 17;
	private static final int GROUP_MEMBER_WIDTH = 80;
	private static final int GROUP_MEMBER_HEIGHT = 45;
	private static final int GROUP_LOUNGE_IMAGE_WIDTH = 40;
	private static final int GROUP_LOUNGE_IMAGE_HEIGHT = 40;
	public static final int tabsBG = 0xFFCFDDE8;
	private static final int ForGRoupMemberList = 1;
	private String WARNING_DIALOG_STATUS = "Warning Status";
	SharedPreferences pref;
	int user_id;
	TableLayout tbl;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private KLoungeGroupRequest kloungehttp;
	SharedPreferences.Editor editor;
	SparseArray<List<GroupMemberInfo>> group_member_list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_list);
		kloungehttp = new KLoungeGroupRequest();
		tbl = (TableLayout)findViewById(R.id.group_list_box);
		tbl.setBackgroundColor(Color.WHITE);
		user_id = AppUser.user_id;
		AppUser.CURRENT_TAB = AppUser.GROUP_LIST_TAB;
		setGroupList();
	}
	
	public void setGroupList() {
		String strGroupList = pref.getString("group_list", "");

		//hard_coding by hglee It has to be retrieved
		strGroupList = strGroupList + "|0";
		
		List<GroupInfo> groupList = new ArrayList<GroupInfo>();

		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
			Log.i(TAG+" STR GL: ",strGroupList);
			String[] arrGroup = strGroupList.split(CommonUtilities.SPLIT_SIGN_PARENT);
			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				
				String[] arrGinfo = strGroup.split(CommonUtilities.SPLIT_SIGN_CHILD);
				
				gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
				gInfo.setGroup_name(arrGinfo[1]);
				gInfo.setGroup_total_number(Integer.parseInt(arrGinfo[2]));
				
				groupList.add(gInfo);
			}
		}

		TableRow tr = null;
		TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		// 그룹 리스트 출력. groupList.size()-1을 하는 이유는 마지막 "공개라운지" 를 제외시키기 위해서 
		for(int i=0; i<groupList.size()-1; i++) {
			TableRow emptyRow = new TableRow(this);
			
			View gapView = new View(this);
			TableRow.LayoutParams gapRowParams = 
					new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 
											LayoutParams.WRAP_CONTENT, 10.0f);
			gapRowParams.height = 1;
			gapRowParams.span = 4;
			gapRowParams.bottomMargin = CommonUtilities.DPFromPixel(this, 10);
			gapRowParams.topMargin = CommonUtilities.DPFromPixel(this, 20);
			gapView.setLayoutParams(gapRowParams);
			gapView.setBackgroundColor(tabsBG);
			emptyRow.addView(gapView);
			gapView.invalidate();

			GroupInfo groupinfo = groupList.get(i);
			final int group_id = groupinfo.getGroup_id();
			final String group_name = groupinfo.getGroup_name();
			final int group_total_number = groupinfo.getGroup_total_number();
			
			tr = new TableRow(this);
			tr.setWeightSum(10.0f);
			tr.setLayoutParams(trParams);
			tr.setGravity(Gravity.CENTER);
			tr.setPadding(0, CommonUtilities.DPFromPixel(this,5), 0, 0);
			tr.setBackgroundColor(Color.WHITE);

			ImageView ivCrossIcon = new ImageView(this);
			TableRow.LayoutParams ivIconParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f);
			ivIconParams.gravity = Gravity.CENTER_VERTICAL;
			ivCrossIcon.setImageResource(R.drawable.cross_expand01);
			tr.addView(ivCrossIcon);

			TextView tvGroupName = new TextView(this);
			tvGroupName.setText(group_name);
			tvGroupName.setGravity(Gravity.LEFT);
			tvGroupName.setTextColor(Color.BLACK);
			tvGroupName.setBackgroundColor(Color.WHITE);
			tvGroupName.setTextSize(TypedValue.COMPLEX_UNIT_SP, GROUP_NAME_SIZE);
			TableRow.LayoutParams tvParams = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 7.0f);
			tvParams.leftMargin = CommonUtilities.DPFromPixel(this, 5);
			tvGroupName.setLayoutParams(tvParams);
			
			tvGroupName.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					//					Log.i("current_group_id", String.valueOf(group_id));
					// K-Lounge 에 메시지 출력
					TabActivity tabHost = (TabActivity) KLoungeGroupList.this.getParent();
					//Intent intent = new Intent(KLoungeGroupList.this, KLoungeGroupMember.class);
					Intent intent = getParent().getIntent();
					intent.putExtra("group_id", group_id);
					intent.putExtra("group_name", group_name);
					AppUser.SHARED_GROUPID = group_id;

					tabHost.getTabHost().setCurrentTab(AppUser.KLOUNGE_TAB);
				}
			});
			tr.addView(tvGroupName);

			// 그룹 메시지 보기 아이콘 (go KLoungeMsg)
			ImageView ivMessage = new ImageView(this); 
			ivMessage.setImageResource(R.drawable.balloon_active);
			TableRow.LayoutParams ivParams2 = new TableRow.LayoutParams(0,
					CommonUtilities.DPFromPixel(this, GROUP_LOUNGE_IMAGE_HEIGHT), 1.5f);
			ivMessage.setLayoutParams(ivParams2);
			// group_id 저장
			ivMessage.setTag(group_id);
			ivMessage.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { 
					vibrator.vibrate(VIBRATE_PERIOD);
					//Log.i("current_group_id", String.valueOf(group_id));
					// K-Lounge 에 메시지 출력
					TabActivity tabHost = (TabActivity) KLoungeGroupList.this.getParent();
					//Intent intent = new Intent(KLoungeGroupList.this, KLoungeGroupMember.class);
					Intent intent = getParent().getIntent();
					intent.putExtra("group_id", group_id);
					intent.putExtra("group_name", group_name);
					AppUser.SHARED_GROUPID = group_id;

					tabHost.getTabHost().setCurrentTab(AppUser.KLOUNGE_TAB);
				}
			});
			tr.addView(ivMessage);
			// 그룹 멤버 보기 아이콘
			TextView tvGroupMember = new TextView(this); 
			tvGroupMember.setBackgroundResource(R.drawable.textbox_selector);
			TableRow.LayoutParams tvGMParams = new TableRow.LayoutParams(0, 
					CommonUtilities.DPFromPixel(this, GROUP_MEMBER_HEIGHT), 1.5f);
			tvGMParams.leftMargin = 15;
//			tvGMParams.gravity = Gravity.CENTER;

			tvGroupMember.setLayoutParams(tvGMParams);
			tvGroupMember.setGravity(Gravity.CENTER);
			tvGroupMember.setTextSize(15);
			tvGroupMember.setTextColor(Color.WHITE);
			tvGroupMember.setText(group_total_number+"명");

			tvGroupMember.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					TextView tv = (TextView)v;
					vibrator.vibrate(VIBRATE_PERIOD);
					
//					prefs = getSharedPreferences(SHARED_PREFERENCE, 0);
					editor = pref.edit();
					boolean bul = pref.getBoolean(WARNING_DIALOG_STATUS, false);
					
//					if(AppConfig.DEBUG)Log.d(TAG, ""+bul);
					
					if(group_total_number > 50 && !bul){
						
//						if(AppConfig.DEBUG)Log.d(TAG, "Too many photos");
						View checkboxView = View.inflate(KLoungeGroupList.this, R.layout.checkbox_alertdialog, null);
						CheckBox ckBox = (CheckBox)checkboxView.findViewById(R.id.checkbox);
						ckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView,
									boolean isChecked) {
								
								editor.putBoolean(WARNING_DIALOG_STATUS, isChecked);
								if(AppConfig.DEBUG)Log.d(TAG, "save shared preference"+isChecked);
								// version upgrade 여부에 상관없이 preference 에 현재 release 된 최신버젼을 저장 해 둔다.
								editor.commit();
							}
						});

						ckBox.setText("다시 보지 않기");
						
						AlertDialog.Builder alertDialog = new AlertDialog.Builder(KLoungeGroupList.this);
						alertDialog.setTitle("경고");
						alertDialog.setView(checkboxView);
						
						/* wi-fi */
						ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						State wifi = conMan.getNetworkInfo(1).getState();
						StringBuffer showMessage = new StringBuffer();
						
						if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
							
						}else{
							showMessage.append("WI-FI 가 연결되어 있지 않습니다. 과도한 데이터가 청구될 수 있습니다.\n\n");
						}
						/* wi-fi 문구*/
						
						alertDialog.setMessage(
								"많은 양의 데이터 로드가 요구됩니다. Wi-Fi 환경에서 " +
								"접속 하기를 권장 합니다. 계속 하시겠습니까?");
						
						alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(KLoungeGroupList.this, ImageGridActivity.class);
								intent.putExtra("group_id", group_id);
								intent.putExtra("group_name", group_name);
								intent.putExtra("group_total_number", group_total_number);
								KLoungeGroupList.this.startActivityForResult(intent, 1);
							}
						});
						alertDialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								
							}
						});
						alertDialog.show();
						
					}else{
						Intent intent = new Intent(KLoungeGroupList.this, ImageGridActivity.class);

						intent.putExtra("group_id", group_id);
						intent.putExtra("group_name", group_name);
						intent.putExtra("group_total_number", group_total_number);
						KLoungeGroupList.this.startActivityForResult(intent, 1);
					}
					
					//					Log.i("group_id", String.valueOf(group_id));
					// 그룹 멤버 출력
				}
			});
			tr.addView(tvGroupMember);

			tbl.addView(tr, new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			tbl.addView(emptyRow);
		}
//		createThread(groupList);
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}


	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case 1:
			if(resultCode == ForGRoupMemberList){
				//Intent intent = new Intent(KLoungeGroupList.this, KLoungeGroupMember.class);
				TabActivity tabHost = (TabActivity) KLoungeGroupList.this.getParent();
				Intent intent = getParent().getIntent();
				intent.putExtra("group_id", data.getStringExtra("group_id"));
				intent.putExtra("group_name", data.getStringExtra("group_name"));
				AppUser.SHARED_GROUPID = data.getIntExtra("group_id",0);
				tabHost.getTabHost().setCurrentTab(AppUser.KLOUNGE_TAB);			
			}
		}
		
	}
}
