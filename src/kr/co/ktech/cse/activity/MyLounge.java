package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.MY_MESSAGE_LIST;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.adapter.SpinAdapter;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.NewMessage;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class MyLounge extends Activity implements OnItemSelectedListener {

	private static int MY_MESSAGE_LIST = 1;
	private static boolean IS_MORE_MESSAGE = true;
	private SharedPreferences pref;
	private ArrayList<GroupInfo> group_list;
	private GroupInfo view_groupinfo;
	private Context context;
	private SpinAdapter adapter;
	private Spinner spin_group_list;
	private LinearLayout linear;
	private Button btn_write_message;
	private ProgressDialog pd;
	private MyKLoungeMessageThread message_thread;
	private ImageFetcher mImageFetcher;
	private String TAG = MyLounge.class.getSimpleName();
	private int previous_gid = -1;
	private int FLAG = -1;
	PullToRefreshScrollView mPullRefreshScrollView;
	ScrollView mScrollView;
	public static int RELOAD = 1;
	/*
	 * onResume(), onRefresh(), onItemSelected() 
	 * 일 때 마다 RELOAD 를 1로 만들어야 한다.
	 * 스크롤바 내려서 화면 하단의 글 더 불러 올 때 RELOAD Number 가
	 * 4,5 로 점점 늘어나면 빈 List를 받아오기 때문이다. 
	 */
	private GetListTask gTask = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
//		createProgressDialog();
		context = getApplicationContext();
		group_list =  new ArrayList<GroupInfo>();
		pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);

		AppUser.user_id = pref.getInt("user_id", 0);
		AppUser.user_name = pref.getString("user_name", "");
		AppUser.CURRENT_TAB = AppUser.MYLOUNGE_TAB;
		view_groupinfo = new GroupInfo();

		String strGroupList = pref.getString("group_list", "");
		
		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
			String[] arrGroup = strGroupList.split(CommonUtilities.SPLIT_SIGN_PARENT);
			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				String[] arrGinfo = strGroup.split(CommonUtilities.SPLIT_SIGN_CHILD);
				gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
				gInfo.setGroup_name(arrGinfo[1]);
				
				if(isGroupInfoHasNewMessage(gInfo.getGroup_id())){
					gInfo.setHasNewMessage(true);
				}
				
				group_list.add(gInfo);
			}
		}

		// 그룹 리스트 중 제일 첫번째 그룹을 보여준다. 
		if(group_list.size() > 0 && view_groupinfo.getGroup_id() <= 0) {
			view_groupinfo = group_list.get(0);
		} else if (group_list.size() < 0 && view_groupinfo.getGroup_id() < 0){
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		AppUser.GROUP_LIST = group_list;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_lounge);
		if(AppConfig.DEBUG){}
		LinearLayout.LayoutParams spinParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

		spin_group_list = (Spinner)findViewById(R.id.mylounge_group_list_spinner);
		adapter = new SpinAdapter(this, android.R.layout.simple_spinner_item, group_list); 
		spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
		spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 320);
		spin_group_list.setAdapter(adapter);
		spin_group_list.setOnItemSelectedListener(this);
		//		int pos = adapter.getPosition(view_groupinfo);
		//		spin_group_list.setSelection(pos);
		//spin_group_list.setLayoutParams(spinParams);
		spin_group_list.invalidate();

		LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

		btn_write_message = (Button)findViewById(R.id.btn_send_my_message);
		//btn_write_message.setLayoutParams(btnParams);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 120);

		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MyLounge.this, WriteMessage.class);
				intent.putExtra("to_group_id", view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", view_groupinfo.getGroup_name());
				MyLounge.this.startActivityForResult(intent, CommonUtilities.AFTER_WRITE_MESSAGE);
			}
		});
		StateListDrawable states = new StateListDrawable();
		states.addState(new int[]{-android.R.attr.state_enabled}, context.getResources().getDrawable(R.drawable.btn_send));
		states.addState(new int[]{android.R.attr.state_pressed}, context.getResources().getDrawable(R.drawable.btn_send_pressed));
		btn_write_message.setBackgroundDrawable(states);

		linear = (LinearLayout)findViewById(R.id.layout_mylounge_message);

		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.mylounge_msg_scrollview);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				RELOAD = 1;
				gTask = new GetListTask();
				gTask.execute(view_groupinfo.getGroup_id());
			}
		});

		mScrollView = mPullRefreshScrollView.getRefreshableView();

		//mylounge.setMyLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), false);
		mScrollView.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				ScrollView sc = (ScrollView)v;
				int totalHeight = sc.getChildAt(0).getHeight();
				int currentPos = sc.getHeight()+sc.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					if(IS_MORE_MESSAGE) {
						//						Log.i("실행 수", "  ");
						IS_MORE_MESSAGE = false;

						message_thread = new MyKLoungeMessageThread(handler);
						message_thread.setParameter(view_groupinfo.getGroup_id());
						message_thread.start();

					}
				}
				return false;
			}
		});
		mImageFetcher = AppUser.mImageFetcher;
		
		if(getParent().getIntent().getIntExtra("popup_super_post_id", 0) > 0){
			
		}
	}
	boolean isGroupInfoHasNewMessage(int group_id){
		ArrayList<NewMessage> nMessage = AppUser.NEW_MESSAGE;
		int size = nMessage.size();
		boolean result = false;
		
		for(int index = 0; index <size ; index ++){
			NewMessage message = nMessage.get(index);
			if(message.getType2().equals("my")
					&& (message.getGroup_id() == group_id)
					&& !(message.getUser_id() == AppUser.user_id)){
				result = true;
			}
		}
		return result;
	}
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if(msg.what == MY_MESSAGE_LIST) {
				List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj; 
				if(messageList.size() <=0 && msg.arg1 < 1){
					Toast.makeText(MyLounge.this, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_LONG).show();
				}
				MessageLayoutSetting mls = new MessageLayoutSetting(MyLounge.this, linear);
				
				if(messageList.size() > 0) {
					for(int i=0; i<messageList.size(); i++) {
						SnsAppInfo sInfo = messageList.get(i);

						//mls.setMessageLayout(context, sInfo, linear);
						//			    		mls.setMessageContentUsingRelativeLayout(linear, sInfo);
						mls.setMessageContentUsingRelativeLayout(sInfo, mImageFetcher, FLAG, view_groupinfo.getGroup_id());
					}
					IS_MORE_MESSAGE = true;
				}
			}
			if(pd != null)pd.dismiss();
		}
	};

	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long pos_long) {
		if(gTask!=null){
			gTask.cancel(true);
			gTask = null;
		}
		RELOAD = 1;
		GroupInfo group_info = (GroupInfo)spin_group_list.getSelectedItem();
		if(AppConfig.DEBUG)Log.i("group_id", "onItemSelected"+String.valueOf(group_info.getGroup_id()));

		view_groupinfo.setGroup_id(group_info.getGroup_id());
		view_groupinfo.setGroup_name(group_info.getGroup_name());
		//mylounge.setMyLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), false);

		linear.removeAllViews();
		
		new GetListTask().execute(view_groupinfo.getGroup_id());
		
		int gid = (int)adapter.getItemId(pos);
		newMessageControl(gid);
	}
	void newMessageControl(int gid){
		// 새 메시지 확인 했으니 리스트에서 지운다.
		for(int index = 0;index<AppUser.NEW_MESSAGE.size();){
			if(gid == AppUser.NEW_MESSAGE.get(index).getGroup_id()
					&& AppUser.NEW_MESSAGE.get(index).getType2().equals("my")
					&& AppUser.NEW_MESSAGE.get(index).getType1().equals("body")){
				AppUser.NEW_MESSAGE.remove(index);
			}else{
				index++;
			}
		}
	}
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			switch(requestCode){
			case CommonUtilities.AFTER_WRITE_MESSAGE: 
				int group_id = view_groupinfo.getGroup_id();
				if(group_id < 0){
					group_id = data.getExtras().getInt(CommonUtilities.INTENT_GROUP_ID);
				}
				gTask = new GetListTask(); 
				gTask.execute(group_id);
			}
		}
	}
	@Override
	public void onBackPressed() {
		if(gTask!=null)gTask.cancel(true);
		this.getParent().onBackPressed();
	}

	@Override
	public void onResume() {
		int shared_group_id = AppUser.SHARED_GROUPID;
//		Log.d(TAG, "popup group id: "+getIntent().getIntExtra("popup_group_id", -1));
		int popup_group_id = getIntent().getIntExtra("popup_group_id", -1);
		
		shared_group_id = (popup_group_id > 0) ? popup_group_id : shared_group_id;
		
		if(shared_group_id < 0){
			// 탭이동, 제일 처음 일 때
			RELOAD = 1;
		}else{
			if(shared_group_id == previous_gid){
				//RELOAD 값 유지
			}else{
				view_groupinfo.setGroup_id(shared_group_id);
				RELOAD = 1;
				int pos = adapter.getPosition(view_groupinfo);
				spin_group_list.setSelection(pos);
			}
			view_groupinfo.setGroup_id(shared_group_id);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		if(gTask!=null)gTask.cancel(true);
		AppUser.SHARED_GROUPID = view_groupinfo.getGroup_id();
		previous_gid = view_groupinfo.getGroup_id();
		//		if(AppConfig.DEBUG)Log.d(TAG, "on pause id : "+previous_gid);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		if (adapter != null)
			adapter.recycle();
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
	void createProgressDialog(){
		pd = ProgressDialog.show(this, "", getResources().getText(R.string.msg_contents), true, true);
	}
	private class GetListTask extends AsyncTask<Integer, Void, List<SnsAppInfo>> {
		private List<SnsAppInfo> messageList;
		private String TAG = GetListTask.class.getSimpleName();
		private int RELOAD = 0;
		private ProgressBar progressBar;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleInverse);
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			LinearLayout.LayoutParams progressParam = new LinearLayout.LayoutParams(width, height);
			
			progressParam.gravity = Gravity.CENTER;
			
			progressBar.setLayoutParams(progressParam);
			linear.addView(progressBar);
		}
		
		@Override
		protected void onCancelled() {
			mPullRefreshScrollView.onRefreshComplete();
			super.onCancelled();
		}

		@Override
		protected List<SnsAppInfo> doInBackground(Integer... gid) {
			try {
				String addr = CommonUtilities.SERVICE_URL + "/mobile/appdbbroker/appKLounge.jsp";
				String parameter = "type=mylounge&user_id="+AppUser.user_id+"&group_id="+gid[0]+"&reload="+RELOAD;
				addr = addr+"?"+parameter;

				StringBuilder json = new StringBuilder();

				URL url = new URL(addr);

				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if(conn != null) {
					conn.setConnectTimeout(3000);
					conn.setUseCaches(false);
					if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
						for(;;){
							String line = br.readLine();
							if(isCancelled()){
								br.close();
								conn.disconnect();
								return null;
							}
							if(line == null) break;
							json.append(line);
						}
						br.close();
					}
					conn.disconnect();
				}
				messageList = parseStrJSON(json.toString());
			}catch (MalformedURLException e) {
				e.printStackTrace();
			}catch(SocketTimeoutException s){
				s.printStackTrace();
			}catch(IOException i){
				i.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			return messageList;
		}

		@Override
		protected void onPostExecute(List<SnsAppInfo> groupList) {
			// Do some stuff here
			linear.removeAllViews();
			if(groupList.size() > 0){
				
				List<SnsAppInfo> messageList = groupList;

				MessageLayoutSetting mls = new MessageLayoutSetting(MyLounge.this, linear);
				int msgSize = messageList.size(); 
				for(int i=0; i<msgSize; i++) {
					SnsAppInfo sInfo = messageList.get(i);
					mls.setMessageContentUsingRelativeLayout(sInfo , mImageFetcher, FLAG, view_groupinfo.getGroup_id());
				}
				IS_MORE_MESSAGE = true;
				
			}else{
				progressBar.setVisibility(View.GONE);
				TextView empty_msg = new TextView(context, null, android.R.attr.textAppearanceMediumInverse);
				empty_msg.setText(context.getResources().getString(R.string.sorry_empty_list));
				int width = ViewGroup.LayoutParams.MATCH_PARENT;
				int height = ViewGroup.LayoutParams.MATCH_PARENT;
				LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(width, height);
				
				empty_msg.setGravity(Gravity.CENTER);
				empty_msg.setLayoutParams(textParam);
				
				linear.addView(empty_msg);
			}
			mPullRefreshScrollView.onRefreshComplete();
		}
		private List<SnsAppInfo> parseStrJSON(String strJSON) {
			List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();
			try {
				JSONObject jsonObj = new JSONObject(strJSON);
				JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
				JSONArray messageArray = kloungeObj.getJSONArray("message");
				int group_id = kloungeObj.getInt("group_id");
				for(int i=0; i<messageArray.length(); i++) {
					JSONObject messageObj = messageArray.getJSONObject(i);
					SnsAppInfo saInfo = new SnsAppInfo();
					saInfo.setGroupId(group_id);
					saInfo.setPostId(messageObj.getInt("post_id"));
					saInfo.setUserId(messageObj.getInt("user_id"));
					saInfo.setPhoto(messageObj.getString("photo"));
					saInfo.setUserName(messageObj.getString("user_name"));
					saInfo.setWrite_date(messageObj.getString("date"));
					saInfo.setBody(messageObj.getString("comment"));
					saInfo.setAttach(messageObj.getString("attach_file"));
					saInfo.setPhotoVideo(messageObj.getString("photo_video_file"));
					saInfo.setReply_count(messageObj.getInt("reply_count"));

					result.add(saInfo);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			return result;
		}
	}
}

class MyKLoungeMessageThread extends Thread {
	private Handler handler;
	private List<SnsAppInfo> messageList;
	private KLoungeRequest kloungehttp;
	private int RELOAD = 0;
	private int group_id;

	public MyKLoungeMessageThread(Handler handler, ProgressDialog pdialog) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		pdialog.show();
		group_id = 0;
	}
	public MyKLoungeMessageThread(Handler handler) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();

		group_id = 0;
	}

	public void setParameter(int group_id) {
		this.group_id = group_id;
	}
	public void setParameter(int group_id, int reload) {
		this.group_id = group_id;
		MyLounge.RELOAD = reload;
	}
	@Override
	public void run() {
		//		Log.d("MyLounge", "user_id: "+AppUser.user_id +"/g_id: "+ group_id);
		messageList = kloungehttp.getMyLoungeMessageList(AppUser.user_id, group_id, MyLounge.RELOAD);

		Message msg = Message.obtain();

		msg.what = MY_MESSAGE_LIST;
		msg.obj = messageList;
		msg.arg1 = MyLounge.RELOAD;
		handler.sendMessage(msg);

		MyLounge.RELOAD++;
	}	
}