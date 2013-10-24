package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.PushWakeLock;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PopupMessage extends Activity {
	private String type;
	private String type2;
	private SnsAppInfo snsinfo;
	private int group_id;
	private int super_post_id;
	private String TAG = PopupMessage.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup_manager);
		
		Intent intent = getIntent();
		type = intent.getStringExtra("popup_type");
		type2 = intent.getStringExtra("popup_type2");
		group_id = intent.getIntExtra("popup_group_id", 0);
		snsinfo = intent.getParcelableExtra("popup_snsAppInfo");
		
		super_post_id = snsinfo.getSuperId();
		
		// 이 부분이 바로 화면을 깨우는 부분 이다.
        // 화면이 잠겨있을 때 보여주기
		PushWakeLock.acquireCpuWakeLock(this);
		
		TextView tvMessageBody = (TextView)findViewById(R.id.download_message);
		
		if(getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE).getBoolean(CommonUtilities.POPUP_PREVIEW_SETTING, true)){
//			Log.d(TAG, snsinfo.getBody());
			StringBuffer message_body = new StringBuffer();
			message_body.append(snsinfo.getTitle())
						.append("\n")
						.append(snsinfo.getBody());
			tvMessageBody.setText(message_body.toString());
		}else{
			tvMessageBody.setText(getResources().getString(R.string.popup_do_not_show_message));
		}
		
		Button btnView = (Button)findViewById(R.id.start_attach_download);
		btnView.setText(getResources().getString(R.string.view));
		btnView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 보기 버튼을 누르면 앱의 런처액티비티를 호출한다.
	            Intent intent = new Intent(PopupMessage.this, LoginActivity.class);
	            
	            intent.putExtra("popup_type", type);
		        intent.putExtra("popup_type2", type2);
		        intent.putExtra("popup_snsAppInfo", snsinfo);
		        intent.putExtra("popup_group_id", group_id);
		        intent.putExtra("popup_super_post_id", super_post_id);
	            startActivity(intent);
	            finish();
			}
		});
		
		Button btnClose = (Button)findViewById(R.id.btn_close_dialog);
		btnClose.setText(getResources().getString(R.string.close));
		btnClose.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
				finish();
			}
		});
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// WakeLock 해제.
		        PushWakeLock.releaseCpuLock();
			}
		},3000);
	}
	
}
