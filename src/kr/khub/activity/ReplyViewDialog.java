package kr.khub.activity;

import kr.khub.R;
import kr.khub.activity.fragment.ReplyViewFragment;
import kr.khub.model.SnsAppInfo;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Window;

public class ReplyViewDialog extends SherlockFragmentActivity {
	private Fragment mContent;
	private static final String TAG = ReplyViewDialog.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// set the Above View
		if (savedInstanceState != null){
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
			Log.d(TAG, "savedInstanceState is not null");
		}
		if (mContent == null)
			mContent = new ReplyViewFragment();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// set the Above View
		setContentView(R.layout.empty_transparent_frame);
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
	}

}
