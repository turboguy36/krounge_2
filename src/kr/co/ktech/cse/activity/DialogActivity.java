package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DialogActivity extends Activity{

	private String notiMessage;
	Context context;
	private ImageView adImage;
	private TextView adMessage;
	private Button adButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Bundle bun = getIntent().getExtras();
		notiMessage = bun.getString("notiMessage");

		makeView();
	}
	
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
	private void makeView(){
		int left,top,right,bottom;
		setContentView(R.layout.alertdialog);
		
		RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, 5);
		right = CommonUtilities.DPFromPixel(context, 0);
		bottom = CommonUtilities.DPFromPixel(context, 10);
		imgParams.setMargins(left, top, right, bottom);
		adImage = (ImageView)findViewById(R.id.alertImgView);
		adImage.setLayoutParams(imgParams);
		adImage.setImageResource(R.drawable.icon_klounge);
		adImage.getLayoutParams().height = CommonUtilities.DPFromPixel(context, 90);
		adImage.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 90);
		adImage.invalidate();
		
		RelativeLayout.LayoutParams messageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, 5);
		right = CommonUtilities.DPFromPixel(context, 10);
		bottom = CommonUtilities.DPFromPixel(context, 10);
		messageParams.setMargins(left, top, right, bottom);
		messageParams.addRule(RelativeLayout.RIGHT_OF, adImage.getId());
		adMessage = (TextView)findViewById(R.id.message);
		adMessage.setLayoutParams(messageParams);
		adMessage.setText(notiMessage);
		adMessage.setTextSize(CommonUtilities.DPFromPixel(context, 12));
		adMessage.invalidate();

		RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		left = CommonUtilities.DPFromPixel(context, 0);
		top = CommonUtilities.DPFromPixel(context, 5);
		right = CommonUtilities.DPFromPixel(context, 0);
		bottom = CommonUtilities.DPFromPixel(context, 0);
		btnParams.setMargins(left, top, right, bottom);
		btnParams.addRule(RelativeLayout.BELOW,adMessage.getId());
		btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		adButton = (Button)findViewById(R.id.submit);
		adButton.setLayoutParams(btnParams);
		adButton.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 100);
		adButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}
