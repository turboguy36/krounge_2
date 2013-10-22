package kr.khub.service;

import java.util.ArrayList;
import java.util.List;

import kr.khub.R;
import kr.khub.adapter.SpinAdapter;
import kr.khub.model.GCMInfo;
import kr.khub.model.GroupInfo;
import kr.khub.util.BadgeView;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TabWidget;

public class GroupSpinnerMessageHandler extends Handler implements OnItemSelectedListener{

	private static GroupSpinnerMessageHandler _instance = new GroupSpinnerMessageHandler();
	private Activity _activity = null;
	private Spinner _spinner = null;
	private GroupInfo _viewGInfo = null;
	ArrayList<GroupInfo> _groupInfo = null;
	SpinAdapter _adapter = null;
	Context context;
	String TAG = GroupSpinnerMessageHandler.class.getSimpleName();

	private String FLAG_BODY_POST = "body";
	private String FLAG_REPLY_POST = "reply";
	private String FLAG_GROUP_LOUNGE = "group";
	private String FLAG_MY_LOUNGE = "my";
	
	private final int FROM_SERVICE_ON_MESSAGE = 3001;
	TabWidget tabs;
	@Override
	public void handleMessage(Message message) {
		context = _activity.getApplicationContext();
		
		tabs = (TabWidget)_activity.findViewById(android.R.id.tabs);
		
		int WHERE_IT_FROM = message.what;
		GCMInfo bag = (GCMInfo)message.obj;
			
		String type = bag.getType();
		String type2 = bag.getType2();
		
		Log.d(TAG, "ON Group spinner handler!!");
		
		int gid = bag.getGroupId();
		if(_activity != null && WHERE_IT_FROM == FROM_SERVICE_ON_MESSAGE){
//			Log.d(TAG, "type2: "+type2+" / type: "+type +" /body: "+bag.getBody()+"\nphoto:"+bag.getPhoto());
			
			Log.d(TAG, "gcm group id: "+ gid);
			
			for(int i=0;i<_groupInfo.size();i++){
				if(_groupInfo.get(i).getGroup_id() == gid){
					_groupInfo.get(i).setHasNewMessage(true);
				}
				Log.d(TAG, "app gid: "+ _groupInfo.get(i).getGroup_id() +"");
			}
			
			_spinner = (Spinner)_activity.findViewById(R.id.group_list_spinner);
			_spinner.setPrompt("그룹리스트");
			// 순서가 뒤죽박죽 되는 것을 방지 하기 위해 _spinner 를 새로 할당 해 준다.
			
			_adapter = new SpinAdapter(_activity, R.layout.spinner_image_style, _groupInfo);
			_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_spinner.setAdapter(_adapter);
			
			int pos = _adapter.getPosition(_viewGInfo);
			_spinner.setSelection(pos);
			
			if(type2.equals(FLAG_MY_LOUNGE)){
				
			}else if(type2.equals(FLAG_GROUP_LOUNGE)){
				
			}
			
			_spinner.setOnItemSelectedListener(this);
		
//		if(message.obj.toString().equals("gcmNewMessage") && _activity != null){
//			
//			Log.d(TAG, getActivity().toString());
//			if(getActivity().toString().equals(KLoungeActivity.class.toString())){
//				
//			}
//			
//			//	        ((KLoungeActivity)_activity).repopulateList();
//			
//			Log.d(TAG, ""+message.obj.toString());
//			TabWidget tabs = (TabWidget)_activity.findViewById(android.R.id.tabs);
//			badge = new BadgeView(context, tabs, 0);
//			badge.setText("N");
//			badge.show();
		}
//		}
		super.handleMessage(message);
	}

	public static GroupSpinnerMessageHandler instance() {
		return _instance;
	}
	
	public void setActivity(Activity activity) {
		_activity = activity;
	}

	public Activity getActivity() {
		return _activity;
	}

	public Spinner get_spinner() {
		return _spinner;
	}

	public void set_spinner(Spinner _spinner) {
		this._spinner = _spinner;
	}

	public GroupInfo get_viewGInfo() {
		return _viewGInfo;
	}

	public void set_viewGInfo(GroupInfo _viewGInfo) {
		this._viewGInfo = _viewGInfo;
	}

	public List<GroupInfo> get_groupInfo() {
		return _groupInfo;
	}

	public void set_groupInfo(ArrayList<GroupInfo> _groupInfo) {
		this._groupInfo = _groupInfo;
	}

	public SpinAdapter get_adapter() {
		return _adapter;
	}

	public void set_adapter(SpinAdapter _adapter) {
		this._adapter = _adapter;
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, 
			int pos, long arg3) {
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}

}