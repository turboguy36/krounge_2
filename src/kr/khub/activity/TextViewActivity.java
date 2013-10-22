package kr.khub.activity;

import java.io.InputStream;

import kr.khub.CommonUtilities;
import kr.khub.R;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import static kr.khub.CommonUtilities.LICENSE;
import static kr.khub.CommonUtilities.PRIVACY_POLICY;
import static kr.khub.CommonUtilities.TERM_CONDITION;
public class TextViewActivity extends Activity{
	String TAG = "TextViewActivity";
	private ImageView imageView;
	private TextView textView;
	private final static String SAVED_INSTANCE_WHENCE = "MORETAB";
	private final static String SAVED_INSTANCE_TEXT_VIEW = TextViewActivity.class.toString()+"TEXT_VIEW";
	int whereItComes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Bundle bundle = null;
		
		if(savedInstanceState != null){
			bundle = savedInstanceState.getBundle(SAVED_INSTANCE_TEXT_VIEW);
		}
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.licenses);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		
		TextView textView = (TextView)findViewById(R.id.text_content);
		textView.setTextColor(Color.parseColor("#FF000000"));
		
		Bundle bun = getIntent().getExtras();
		if(bun == null){
			bun = bundle;
		}
		try{
			whereItComes = bun.getInt(SAVED_INSTANCE_WHENCE, 0);
		}catch(NullPointerException ne){
			Log.e(TAG, "NullPointerException: "+ne);
		}
		try{
			Resources res = getResources();
			InputStream ins = null;
			switch(whereItComes){
				case LICENSE:
					ins = res.openRawResource(R.raw.licenses);
					break;
				case PRIVACY_POLICY:
					ins = res.openRawResource(R.raw.policy_privacy);
					break;
				case TERM_CONDITION:
					ins = res.openRawResource(R.raw.terms_and_conditions);
					break;
			}
			byte[] b = new byte[ins.available()];
			ins.read(b);
			textView.setText(Html.fromHtml(new String(b)));
		}catch(Exception e){
			Log.e(TAG, "Exception - "+e);
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle bundle = new Bundle();
		bundle.putInt(SAVED_INSTANCE_WHENCE, whereItComes);
		outState.putBundle(SAVED_INSTANCE_TEXT_VIEW, bundle);
	}
	@Override
	protected void onPause() {
//		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		super.onPause();
	}
	@Override
	protected void onResume() {
//		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		super.onResume();
	}
	
}
