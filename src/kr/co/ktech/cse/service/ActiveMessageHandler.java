package kr.co.ktech.cse.service;

import kr.co.ktech.cse.model.GCMInfo;
import kr.co.ktech.cse.util.BadgeView;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TabWidget;

public class ActiveMessageHandler extends Handler {

	private static ActiveMessageHandler _instance = new ActiveMessageHandler();
	private Activity _activity = null;
	BadgeView badge;
	Context context;
	String TAG = ActiveMessageHandler.class.getSimpleName();

	private String FLAG_BODY_POST = "body";
	private String FLAG_REPLY_POST = "reply";
	private String FLAG_GROUP_LOUNGE = "group";
	private String FLAG_MY_LOUNGE = "my";
	
	private int GROUP_LOUNGE_TAB_NUM = 0;
	private int MY_LOUNGE_TAB_NUM = 1;
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
		
		Log.d(TAG, "ON handler!!");
		
		int gid = bag.getGroupId();
		if(_activity != null && WHERE_IT_FROM == FROM_SERVICE_ON_MESSAGE){
			
			Log.d(TAG, "type2: "+type2+" / type: "+type +" /body: "+bag.getBody()+"\nphoto:"+bag.getPhoto());
			
			if(type2.equals(FLAG_MY_LOUNGE)){
				badge = new BadgeView(context, tabs, MY_LOUNGE_TAB_NUM);
			}else if(type2.equals(FLAG_GROUP_LOUNGE)){
				badge = new BadgeView(context, tabs, GROUP_LOUNGE_TAB_NUM);
			}
		
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
			badge.setText("N");
			badge.show();
		}
		super.handleMessage(message);
		
	}

	public static ActiveMessageHandler instance() {
		return _instance;
	}

	public void setActivity(Activity activity) {
		_activity = activity;
	}

	public Activity getActivity() {
		return _activity;
	}
	
}