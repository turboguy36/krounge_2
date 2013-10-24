package kr.co.ktech.cse.activity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.adapter.PersonalSpinAdapter;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.GROUP_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.IMAGE_CACHE_DIR;

public class PersonalLounge extends FragmentActivity implements OnItemSelectedListener {

	private static boolean IS_MORE_MESSAGE = true;
	private ArrayList<GroupInfo> group_list;
	private GroupInfo view_groupinfo;
	private int messageNumber;
	private PersonalSpinAdapter adapter;
	private Spinner spin_group_list;
	private LinearLayout linear;
	private Button btn_write_message;
	private ImageView personal_image;
	private TextView personal_user_name;
	private ImageView imageView;
	private TextView textView;
	private TextView message_empty;
	public ProgressDialog pd;
	private int puser_id = 0;
	private String puser_name = "";
	private String puser_photo = "";
	private PersonalKLoungeMessageThread message_thread;
	private ImageFetcher mImageFetcher;
	private DisplayUtil du;
	SparseArray<List<GroupInfo>> memInfoArray = new SparseArray<List<GroupInfo>>();
	private String TAG = PersonalLounge.class.getSimpleName();
	//	LoadImageUtil imageUtil;
	PullToRefreshScrollView mPullRefreshScrollView;
	ScrollView mScrollView;
	String INTENT_GROUP_ID = "to_group_id";
	private int mImageThumbSize;
	private int mImageSize;
	private String json_filename = "personal_json.json";
	private GetPersonalListTask gTask = null;
	
	public static int RELOAD = 1;
	/*
	 * onResume(), onRefresh(), onItemSelected() 
	 * 일 때 마다 RELOAD 를 1로 만들어야 한다.
	 * 스크롤바 내려서 화면 하단의 글 더 불러 올 때 RELOAD Number 가
	 * 4,5 로 점점 늘어나면 빈 List를 받아오기 때문이다. 
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		group_list =  new ArrayList<GroupInfo>();
		view_groupinfo = new GroupInfo();
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_personal_lounge);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

		du = new DisplayUtil(this);

		Intent intent = getIntent();
		try{
			puser_id = Integer.parseInt(intent.getStringExtra("puser_id"));
			puser_name = intent.getStringExtra("puser_name");
			puser_photo = intent.getStringExtra("puser_photo").replace(" ", "%20");
			Log.d(TAG, "puser_id: "+puser_id);
			Log.d(TAG, "puser_name: "+puser_name);
			Log.d(TAG, "puser_photo: "+puser_photo);
			
		}catch(NumberFormatException ne){
			ne.printStackTrace();
		}catch(NullPointerException e){
			e.printStackTrace();
		}

		if(AppUser.user_id <= 0){
			loadJsonFile();
		}

		new GetGroupListTask().execute();

		if(view_groupinfo == null){
			if(AppConfig.DEBUG)Log.d(TAG, "view group info null");
		}else if(group_list == null){
			if(AppConfig.DEBUG)Log.d(TAG, "group list null");
		}
		int gsize = group_list.size();

		if(gsize > 0 && view_groupinfo.getGroup_id() <= 0) {
			view_groupinfo = group_list.get(0);
		} else if (gsize < 0 && view_groupinfo.getGroup_id() < 0){
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageSize = getResources().getDimensionPixelSize(R.dimen.image_size);
		if(AppUser.mImageFetcher != null){
			// 정상적 사용자
			mImageFetcher = AppUser.mImageFetcher;
		}else{
			// 다른 App 쓰다가 돌아온 사용자
			mImageFetcher = new ImageFetcher(this, mImageThumbSize);
			mImageFetcher.setLoadingImage(R.drawable.no_photo);
			AppUser.mImageFetcher = mImageFetcher;
			ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.25f); 
			mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		}

		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		spin_group_list = (Spinner)findViewById(R.id.personal_group_list_spinner);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.personal_lounge_scrollview);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				RELOAD = 1;
				gTask = new GetPersonalListTask();
				gTask.execute(view_groupinfo.getGroup_id());
			}
		});
		mScrollView = mPullRefreshScrollView.getRefreshableView();
		makeView();
	}

	void makeView(){
		personal_user_name = (TextView)findViewById(R.id.personal_user_name);
		personal_user_name.setText(puser_name);

		personal_image = (ImageView)findViewById(R.id.user_image_view);
//		LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(du.PixelToDP(100), du.PixelToDP(100));

		puser_photo = puser_photo.replace(" ", "%20");
		if(AppConfig.DEBUG)Log.d(TAG, "URL: "+puser_photo);

//		personal_image.setLayoutParams(ivParams);
//		personal_image.setScaleType(ImageView.ScaleType.FIT_XY);
		
		mImageFetcher.setImageSize(mImageSize);
		mImageFetcher.loadImage(puser_photo, personal_image);
		
		//		imageUtil = new LoadImageUtil();
		//		imageUtil.loadImage(personal_image, puser_photo, puser_id);

		spin_group_list.setOnItemSelectedListener(this);

		linear = (LinearLayout)findViewById(R.id.personal_lounge_message_layout);

		btn_write_message = (Button)findViewById(R.id.personal_write_message_btn);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(this, 110);
		btn_write_message.getLayoutParams().height = CommonUtilities.DPFromPixel(this, 55);
		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(PersonalLounge.this, WriteMessage.class);
				intent.putExtra("to_group_id", view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", view_groupinfo.getGroup_name());
				intent.putExtra("to_puser_id", puser_id);
				PersonalLounge.this.startActivityForResult(intent, CommonUtilities.AFTER_WRITE_MESSAGE);
			}
		});

		StateListDrawable states = new StateListDrawable();
		states.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.btn_send));
		states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.btn_send_pressed));
		btn_write_message.setBackgroundDrawable(states);

		mScrollView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				ScrollView sv = (ScrollView)v;
				int totalHeight = sv.getChildAt(0).getHeight();
				int currentPos = sv.getHeight()+sv.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					if(IS_MORE_MESSAGE) {
						IS_MORE_MESSAGE = false;
						message_thread = new PersonalKLoungeMessageThread(handler);
						message_thread.setParameter(view_groupinfo.getGroup_id(), puser_id);
						message_thread.start();
					}
				}
				return false;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CommonUtilities.AFTER_WRITE_MESSAGE && resultCode == RESULT_OK){
			int group_id = view_groupinfo.getGroup_id();
			if(group_id < 0){
				group_id = data.getExtras().getInt(CommonUtilities.INTENT_GROUP_ID);
			}

			gTask = new GetPersonalListTask();
			gTask.execute(group_id);			
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case GROUP_MESSAGE_LIST :
				//				if(msg.what == GROUP_MESSAGE_LIST) {
				List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj;
				messageNumber  = msg.arg1;
				if(messageNumber == 0){
					if(pd!=null)pd.dismiss();
					Toast.makeText(PersonalLounge.this, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_SHORT).show();
				}else if(messageNumber < 0){
					if(pd!=null)pd.dismiss();
					Toast.makeText(PersonalLounge.this, getResources().getText(R.string.sorry_text), Toast.LENGTH_SHORT).show();
					Log.e(TAG,"Loading Message List Error "+this.getClass().toString());
					finish();
				}
				
				MessageLayoutSetting mls = new MessageLayoutSetting(PersonalLounge.this, linear);

				if(messageList.size() > 0) {
					mImageFetcher.setImageSize(mImageThumbSize);
					for(int i=0; i<messageList.size(); i++) {
						SnsAppInfo sInfo = messageList.get(i);
						Log.d(TAG, i +" / " +puser_id);
						mls.setMessageContentUsingRelativeLayout(sInfo, mImageFetcher, puser_id, view_groupinfo.getGroup_id());
					}
					IS_MORE_MESSAGE = true;			    	
				}
				break;
			}
			if(pd!=null)pd.dismiss();
		}
	};
	
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onPause() {
		if(gTask!=null)gTask.cancel(true);
		super.onPause();
	}

	@Override
	protected void onResume() {
		RELOAD = 1;
		super.onResume();
	}

	@Override
	protected void onStop() {
		storeJsonFile();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		personal_image = null;
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

	class DisplayUtil {
		private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

		private final float scale;

		public DisplayUtil(Context context) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		public int PixelToDP(int pixel) {
			return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
		}
		public int DPToPixel(final Context context, int DP) {
			return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
		}
	}

	private class GetGroupListTask extends AsyncTask<Void, Integer, ArrayList<GroupInfo>>{
		@Override
		protected ArrayList<GroupInfo> doInBackground(Void... Params) {
			try {
				ArrayList<GroupInfo> result = new ArrayList<GroupInfo>();
				String addr = CommonUtilities.SERVICE_URL + "/mobile/appdbbroker/appKLoungeGroup.jsp";
				String parameter = "user_id="+AppUser.user_id+"&puser_id="+puser_id;
				addr = addr+"?"+parameter;
				if(AppConfig.DEBUG)Log.d(TAG,addr);

				String strJSON = "";

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
							if(line == null) break;
							if(isCancelled()){
								br.close();
								conn.disconnect();
								return null;
							}
							json.append(line);
						}
						br.close();
					}
					conn.disconnect();
				}

				strJSON = json.toString();

				//파싱
				JSONObject jsonObj = null;
				JSONObject groupObj = null;
				JSONArray groupArray = null;
				try{
					jsonObj = new JSONObject(strJSON);
					groupObj = jsonObj.getJSONObject("group_list");
					groupArray = groupObj.getJSONArray("group");
				}catch(JSONException j){
					Log.e(TAG, "JSON EXCEPTION -"+j);
					ArrayList<GroupInfo> ginfoList = new ArrayList<GroupInfo>();
					GroupInfo ginfo = new GroupInfo();
					ginfoList.add(ginfo);
					return ginfoList;
				}
				String group_name = null;
				
				for(int i=0; i<groupArray.length(); i++) {
					if(isCancelled())return null;
					JSONObject group = groupArray.getJSONObject(i);
					GroupInfo groupInfo = new GroupInfo();
					groupInfo.setGroup_id(group.getInt("group_id"));
					group_name = group.getString("group_name");

					if(group_name.equals("전체")){
						try{
							group_name = group_name.replace("전체", "공개라운지");
							groupInfo.setGroup_name(group_name);
						}catch(StringIndexOutOfBoundsException e){
							Log.e(TAG, "StringIndexOutOfBoundsException -"+e);
						}
					}
					groupInfo.setGroup_name(group_name);

					result.add(groupInfo);
				}

				group_list = result;

			}catch (MalformedURLException e) {
				e.printStackTrace();
			}catch(SocketTimeoutException s){
				s.printStackTrace();
			}catch(IOException i){
				i.printStackTrace();
			}catch (Exception e) {
				e.printStackTrace();
			}
			return group_list;
		}
		@Override
		protected void onPostExecute(ArrayList<GroupInfo> result) {
			adapter = new PersonalSpinAdapter(PersonalLounge.this, android.R.layout.simple_spinner_item, result);

			spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(PersonalLounge.this, 300);
			spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
			spin_group_list.setAdapter(adapter);

			int groupId = AppUser.SHARED_GROUPID;

			if(groupId >= 0){
				view_groupinfo.setGroup_id(groupId);
				int pos = adapter.getPosition(view_groupinfo);
				spin_group_list.setSelection(pos);
			}
		}
	};
	private class GetPersonalListTask extends AsyncTask<Integer, Void, List<SnsAppInfo>> {
		private List<SnsAppInfo> messageList;
		private int group_id;
		private int RELOAD = 0;
		private ProgressBar progressBar;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			progressBar = new ProgressBar(PersonalLounge.this , null, android.R.attr.progressBarStyle);
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			LinearLayout.LayoutParams progressParam = new LinearLayout.LayoutParams(width, height);
			
			progressParam.gravity = Gravity.CENTER;
			
			progressBar.setLayoutParams(progressParam);
			linear.addView(progressBar);
		}
		@Override
		protected List<SnsAppInfo> doInBackground(Integer... gid) {
			try {
				String addr = CommonUtilities.SERVICE_URL + "/mobile/appdbbroker/appKLounge.jsp";
				String parameter = "type=personallounge&user_id="+AppUser.user_id+"&group_id="+gid[0]+"&puser_id="+puser_id+"&reload="+RELOAD;
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
							if(line == null) break;
							if(isCancelled())return null;
							json.append(line);
						}
						br.close();
					}
					conn.disconnect();
				}

				//파싱
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
			if(AppConfig.DEBUG)Log.d(TAG, "post execute");
			linear.removeAllViews();
			List<SnsAppInfo> messageList = groupList;

			MessageLayoutSetting mls = new MessageLayoutSetting(PersonalLounge.this, linear);
			int msgSize = messageList.size(); 

			if(msgSize > 0) {
				for(int i=0; i<msgSize; i++) {
					SnsAppInfo sInfo = messageList.get(i);
					mls.setMessageContentUsingRelativeLayout(sInfo , mImageFetcher, puser_id, view_groupinfo.getGroup_id());
				}
				IS_MORE_MESSAGE = true;
			}else{
				progressBar.setVisibility(View.GONE);
				TextView empty_msg = new TextView(PersonalLounge.this, null, android.R.attr.textAppearanceMediumInverse);
				empty_msg.setText(PersonalLounge.this.getResources().getString(R.string.sorry_empty_list));
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
	private boolean loadJsonFile(){
		boolean result = false;
		FileInputStream fis = null;
		try {
			fis = openFileInput(json_filename);
			byte in[] = new byte[fis.available()];
			fis.read(in);
			JSONObject obj = new JSONObject(new String(in));
//			Log.d(TAG, "loadJsonFile: "+obj.toString());
			AppUser.user_id = obj.getInt("app_user_id");
			AppUser.SHARED_GROUPID = obj.getInt("share_group_id");
			puser_name = obj.getString("puser_name");
			puser_photo = obj.getString("puser_photo");

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
	private void storeJsonFile(){
		Log.d(TAG, "PersonalLounge store Json File!!");
		try {
			JSONObject obj = new JSONObject();
			obj.put("app_user_id", AppUser.user_id);
			obj.put("puser_name", puser_name);
			obj.put("puser_photo", puser_photo);
			obj.put("share_group_id", AppUser.SHARED_GROUPID);
			FileOutputStream fos = openFileOutput(json_filename, Context.MODE_PRIVATE);
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
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "onItemSelected");
		
		if(gTask!=null){
			gTask.cancel(true);
			gTask = null;
		}
		RELOAD = 1;
		GroupInfo group_info = (GroupInfo)spin_group_list.getSelectedItem();
		//		if(AppConfig.DEBUG)Log.i(TAG, String.valueOf(group_info.getGroup_id()));

		view_groupinfo.setGroup_id(group_info.getGroup_id());
		view_groupinfo.setGroup_name(group_info.getGroup_name());

		linear.removeAllViews();
		
		new GetPersonalListTask().execute(view_groupinfo.getGroup_id());
	}
}

class PersonalKLoungeMessageThread extends Thread {
	private static final String TAG = PersonalKLoungeMessageThread.class.getSimpleName();
	private Handler handler;
	private List<SnsAppInfo> messageList;
	private KLoungeRequest kloungehttp;
	private int group_id;
	private int puser_id;

	public PersonalKLoungeMessageThread(Handler handler) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();

		group_id = 0;
		puser_id = 0;
	}
	public PersonalKLoungeMessageThread(Handler handler, ProgressDialog pdialog) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		pdialog.show();
		group_id = 0;
		puser_id = 0;
	}

	public void setParameter(int group_id, int puser_id) {
		this.group_id = group_id;
		this.puser_id = puser_id;
	}
	
	@Override
	public void run() {
		messageList = kloungehttp.getPersonalLoungeMessageList(AppUser.user_id, group_id, puser_id, PersonalLounge.RELOAD);

		Message msg = Message.obtain();

		msg.what = GROUP_MESSAGE_LIST;
		msg.obj = messageList;
		msg.arg1 = PersonalLounge.RELOAD;
		handler.sendMessage(msg);

		PersonalLounge.RELOAD++;
	}	
}
