package kr.khub.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import kr.khub.AppConfig;
import kr.khub.R;
import kr.khub.activity.fragment.TouchImageViewFragment;
import kr.khub.bitmapfun.util.Utils;
import kr.khub.util.RecycleUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
//import android.util.Log;

public class TouchImageViewActivity extends SherlockFragmentActivity{
//	private String TAG = TouchImageViewActivity.class.getSimpleName();
	private Fragment mContent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getIntent().getExtras();
		if (savedInstanceState != null){
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
		}
		if (mContent == null){
			mContent = new TouchImageViewFragment();
			mContent.setArguments(bundle);
		}
		setContentView(R.layout.content_frame);
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
	}
	
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
}