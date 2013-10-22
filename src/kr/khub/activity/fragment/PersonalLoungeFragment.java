package kr.khub.activity.fragment;

import static kr.khub.CommonUtilities.GROUP_MESSAGE_LIST;

import java.io.BufferedReader;
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

import kr.khub.AppConfig;
import kr.khub.CommonUtilities;
import kr.khub.R;
import kr.khub.activity.FileSearchListActivity;
import kr.khub.adapter.PersonalSpinAdapter;
import kr.khub.bitmapfun.util.ImageFetcher;
import kr.khub.bitmapfun.util.Utils;
import kr.khub.db.KLoungeRequest;
import kr.khub.model.AppUser;
import kr.khub.model.DataInfo;
import kr.khub.model.GroupInfo;
import kr.khub.model.SnsAppInfo;
import kr.khub.processes.MessageLayoutSetting;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;

public class PersonalLoungeFragment extends SherlockFragment implements OnItemSelectedListener{
	private ActionBar aBar;
	private ImageFetcher mImageFetcher;
	private String TAG = PersonalLoungeFragment.class.getSimpleName();
	
	private ArrayList<GroupInfo> group_list;
	private GroupInfo current_view_groupinfo;
	
	private DisplayUtil du;
	
	private int puser_id = 0;
	private String puser_name = "";
	private String puser_photo = "";
	public static int RELOAD = 1;
	/*
	 * onResume(), onRefresh(), onItemSelected() 
	 * 일 때 마다 RELOAD 를 1로 만들어야 한다.
	 * 스크롤바 내려서 화면 하단의 글 더 불러 올 때 RELOAD Number 가
	 * 4,5 로 점점 늘어나면 빈 List를 받아오기 때문이다. 
	 */
	private static boolean IS_MORE_MESSAGE = true;
	private int FLAG = -1;
	private int messageNumber;
	private int mImageThumbSize;
	private String json_filename = "personal_json.json";
	
	private PersonalSpinAdapter adapter;
	private Spinner spin_group_list;
	private ImageView personal_image;
//	private TextView personal_user_name;
	PullToRefreshScrollView mPullRefreshScrollView;
	private LinearLayout linear;
	ScrollView mScrollView;
//	private Button btn_write_message;
	public ProgressDialog pd;
	
	private GetPersonalListTask gTask = null;
	private PersonalKLoungeMessageThread message_thread;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		setHasOptionsMenu(true);
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		DataInfo dinfo = getArguments().getParcelable(FileSearchListActivity.DATA_KEY);
		puser_id = dinfo.getUser_id();
		puser_name = dinfo.getUser_name();
		puser_photo = dinfo.getUser_photo();
		
		mImageFetcher = Utils.getImageFetcher(getActivity());
		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		int mImageSize = getResources().getDimensionPixelSize(R.dimen.image_size);
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageFetcher.setImageSize(mImageSize);
		
		group_list =  new ArrayList<GroupInfo>();
		current_view_groupinfo = new GroupInfo();
		
		du = new DisplayUtil(getActivity());
		
		aBar=getSherlockActivity().getSupportActionBar();
		TextView title_view = (TextView)aBar.getCustomView().findViewById(R.id.title);
		title_view.setText(puser_name + " " +getResources().getString(R.string.lounge_of_whom));
		title_view.setSelected(true);
		
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_personal_lounge, null);
		linear = (LinearLayout)view.findViewById(R.id.personal_lounge_message_layout);
		
		spin_group_list = (Spinner)view.findViewById(R.id.personal_group_list_spinner);
		spin_group_list.setOnItemSelectedListener(this);
		
//		personal_user_name = (TextView)view.findViewById(R.id.personal_user_name);
//		personal_user_name.setText(puser_name);

		personal_image = (ImageView)view.findViewById(R.id.personal_imageview);
		RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(du.PixelToDP(100), du.PixelToDP(100));

		puser_photo = puser_photo.replace(" ", "%20");
		if(AppConfig.DEBUG)Log.d(TAG, "URL: "+puser_photo);

		personal_image.setLayoutParams(ivParams);
		personal_image.setScaleType(ImageView.ScaleType.FIT_XY);
		mImageFetcher.loadImage(puser_photo, personal_image);
		
		mPullRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.personal_lounge_scrollview);
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				RELOAD = 1;
				gTask = new GetPersonalListTask();
				gTask.execute(current_view_groupinfo.getGroup_id());
			}
		});
		mScrollView = mPullRefreshScrollView.getRefreshableView();
/*
		btn_write_message = (Button)view.findViewById(R.id.personal_write_message_btn);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(getActivity(), 110);
		btn_write_message.getLayoutParams().height = CommonUtilities.DPFromPixel(getActivity(), 55);
		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), WriteMessage.class);
				intent.putExtra("to_group_id", current_view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", current_view_groupinfo.getGroup_name());
				intent.putExtra("to_puser_id", puser_id);
//				PersonalLounge.this.startActivityForResult(intent, CommonUtilities.AFTER_WRITE_MESSAGE);
			}
		});

		StateListDrawable states = new StateListDrawable();
		states.addState(new int[]{-android.R.attr.state_enabled}, getResources().getDrawable(R.drawable.btn_send));
		states.addState(new int[]{android.R.attr.state_pressed}, getResources().getDrawable(R.drawable.btn_send_pressed));
		btn_write_message.setBackgroundDrawable(states);
*/
		mScrollView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				ScrollView sv = (ScrollView)v;
				int totalHeight = sv.getChildAt(0).getHeight();
				int currentPos = sv.getHeight()+sv.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					if(IS_MORE_MESSAGE) {
						IS_MORE_MESSAGE = false;
						message_thread = new PersonalKLoungeMessageThread(handler);
						message_thread.setParameter(current_view_groupinfo.getGroup_id(), puser_id);
						message_thread.start();
					}
				}
				return false;
			}
		});
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		menu.clear();
		inflater.inflate(R.menu.activity_personal_lounge, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		new GetGroupListTask().execute();
	}

	@Override
	public void onResume() {
		RELOAD = 1;
		super.onResume();
	}

	@Override
	public void onStop() {
		storeJsonFile();
		super.onStop();
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CommonUtilities.AFTER_WRITE_MESSAGE && resultCode == Activity.RESULT_OK){
			int group_id = current_view_groupinfo.getGroup_id();
			if(group_id < 0){
				group_id = data.getExtras().getInt(CommonUtilities.INTENT_GROUP_ID);
			}

			gTask = new GetPersonalListTask();
			gTask.execute(group_id);			
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		((FileSearchListActivity)getActivity()).addFragmentToStack(WriteMessageFragment.class.getSimpleName());
		return super.onOptionsItemSelected(item);
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3) {
		Log.d(TAG, "onItemSelected");
		if(gTask!=null){
			gTask.cancel(true);
			gTask = null;
		}
		RELOAD = 1;
		GroupInfo group_info = (GroupInfo)parent.getAdapter().getItem(position);
//				spin_group_list.getSelectedItem();
		//		if(AppConfig.DEBUG)Log.i(TAG, String.valueOf(group_info.getGroup_id()));

		current_view_groupinfo.setGroup_id(group_info.getGroup_id());
		current_view_groupinfo.setGroup_name(group_info.getGroup_name());
		//personallounge.setPersonalLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), puser_id, false);
		linear.removeAllViews();
		new GetPersonalListTask().execute(current_view_groupinfo.getGroup_id());
		/*
		message_thread = new PersonalKLoungeMessageThread(handler, pd);
		message_thread.setParameter(view_groupinfo.getGroup_id(), puser_id, 0);
		message_thread.start();
		*/
	}
	private void storeJsonFile(){
		Log.d(TAG, "PersonalLounge store Json File!!");
		try {
			JSONObject obj = new JSONObject();
			obj.put("app_user_id", AppUser.user_id);
			obj.put("puser_name", puser_name);
			obj.put("puser_photo", puser_photo);
			obj.put("share_group_id", AppUser.SHARED_GROUPID);
			FileOutputStream fos = getActivity().openFileOutput(json_filename, Context.MODE_PRIVATE);
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
	private class GetGroupListTask extends AsyncTask<Void, Integer, ArrayList<GroupInfo>>{
		@Override
		protected ArrayList<GroupInfo> doInBackground(Void... Params) {
			try {
				ArrayList<GroupInfo> result = new ArrayList<GroupInfo>();
				if(AppConfig.DEBUG)Log.d(TAG, AppUser.user_id +"/"+puser_id);
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
						//					if(AppConfig.DEBUG)Log.d(TAG, group_name);
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
			adapter = new PersonalSpinAdapter(getActivity(), android.R.layout.simple_spinner_item, result);

			spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(getActivity(), 300);
			spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
			spin_group_list.setAdapter(adapter);

			int groupId = AppUser.SHARED_GROUPID;

			if(groupId >= 0){
				current_view_groupinfo.setGroup_id(groupId);
				int pos = adapter.getPosition(current_view_groupinfo);
				spin_group_list.setSelection(pos);
			}
		}
	};
	private class GetPersonalListTask extends AsyncTask<Integer, Void, List<SnsAppInfo>> {
		private List<SnsAppInfo> messageList;
//		private int group_id;
		private int RELOAD = 0;

		@Override
		protected List<SnsAppInfo> doInBackground(Integer... gid) {
			try {
				String addr = CommonUtilities.SERVICE_URL + "/mobile/appdbbroker/appKLounge.jsp";
				String parameter = "type=personallounge&user_id="+AppUser.user_id+"&group_id="+gid[0]+"&puser_id="+puser_id+"&reload="+RELOAD;
				addr = addr+"?"+parameter;
				Log.d(TAG, "addr : " + addr);
				
				StringBuilder json = new StringBuilder();

				URL url = new URL(addr);

				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if(conn != null) {
					conn.setConnectTimeout(3000);
					conn.setUseCaches(false);
					if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
						for(;;){
							//								Log.d(TAG, "for loop");
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

			if(messageList.size() <=0){
				Toast.makeText(getActivity(), getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_SHORT).show();
			}
			MessageLayoutSetting mls = new MessageLayoutSetting(getActivity(), linear);
			int msgSize = messageList.size(); 

			if(msgSize > 0) {
				for(int i=0; i<msgSize; i++) {
					SnsAppInfo sInfo = messageList.get(i);
					mls.setMessageContentUsingRelativeLayout(sInfo , mImageFetcher, FLAG, current_view_groupinfo.getGroup_id());
				}
				IS_MORE_MESSAGE = true;
			}
			mPullRefreshScrollView.onRefreshComplete();
		}
		private List<SnsAppInfo> parseStrJSON(String strJSON) {
			Log.d(TAG, strJSON);
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
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what){
			case GROUP_MESSAGE_LIST :
				//				if(msg.what == GROUP_MESSAGE_LIST) {
				List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj;
				messageNumber  = msg.arg1;
				if(messageNumber == 0){
					if(pd!=null)pd.dismiss();
					Toast.makeText(getActivity(), getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_SHORT).show();
				}else if(messageNumber < 0){
					if(pd!=null)pd.dismiss();
					Toast.makeText(getActivity(), getResources().getText(R.string.sorry_text), Toast.LENGTH_SHORT).show();
					Log.e(TAG,"Loading Message List Error "+this.getClass().toString());
//					finish();
				}
				
				MessageLayoutSetting mls = new MessageLayoutSetting(getActivity(), linear);

				if(messageList.size() > 0) {
					mImageFetcher.setImageSize(mImageThumbSize);
					for(int i=0; i<messageList.size(); i++) {
						SnsAppInfo sInfo = messageList.get(i);
						mls.setMessageContentUsingRelativeLayout(sInfo, mImageFetcher, puser_id, current_view_groupinfo.getGroup_id());
					}
					IS_MORE_MESSAGE = true;			    	
				}
				break;
			}
			if(pd!=null)pd.dismiss();
		}
	};

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}
class PersonalKLoungeMessageThread extends Thread {
//	private static final String TAG = PersonalKLoungeMessageThread.class.getSimpleName();
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
		messageList = kloungehttp.getPersonalLoungeMessageList(AppUser.user_id, group_id, puser_id, PersonalLoungeFragment.RELOAD);

		Message msg = Message.obtain();

		msg.what = GROUP_MESSAGE_LIST;
		msg.obj = messageList;
		msg.arg1 = PersonalLoungeFragment.RELOAD;
		handler.sendMessage(msg);

		PersonalLoungeFragment.RELOAD++;
	}	
}
