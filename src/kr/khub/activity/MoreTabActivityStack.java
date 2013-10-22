package kr.khub.activity;

import java.util.EmptyStackException;
import java.util.Stack;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class MoreTabActivityStack extends ActivityGroup {

	private Stack<String> stack;
	private String TAG = "MoreTabActivityStack";
	private boolean mFlag = false;
	final int CLOSE_MESSAGE = 4001;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (stack == null) stack = new Stack<String>();
		//start default activity
		push("FirstStackActivity", new Intent(this, MoreTab.class));
	}

	@Override
	public void finishFromChild(Activity child) {
		pop();
	}

	@Override
	public void onBackPressed() {
		pop();
	}

	public void push(String id, Intent intent) {
		Window window = getLocalActivityManager()
				.startActivity(id, intent.addFlags(
						Intent.FLAG_ACTIVITY_CLEAR_TOP|
						Intent.FLAG_ACTIVITY_SINGLE_TOP));
		if (window != null) {
			stack.push(id);
			setContentView(window.getDecorView());
		}
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == CLOSE_MESSAGE) {
				mFlag = false;
			} 
		}
	};
	
	public void pop() {
		if (stack.size() == 1) {
			if(!mFlag) {
				Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
				mFlag = true;
				handler.sendEmptyMessageDelayed(CLOSE_MESSAGE, 2000);
			} else {
				finish();
			}
		}else{
			LocalActivityManager manager = getLocalActivityManager();
			try{
				manager.destroyActivity(stack.pop(), true);
			}catch(EmptyStackException e){
				Log.e(TAG, "EmptyStackException"+e);
			}
			if (stack.size() > 0) {
				Intent lastIntent = manager.getActivity(stack.peek()).getIntent();
				Window newWindow = manager.startActivity(stack.peek(), lastIntent);
				setContentView(newWindow.getDecorView());
			}
		}
	}
}