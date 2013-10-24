package kr.co.ktech.cse.activity;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.model.CateInfo;
import kr.co.ktech.cse.model.GroupCateInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class BaseActivity extends SherlockFragmentActivity{ 
	private ImageFetcher mImageFetcher;
	private int mTitleRes;
	protected ListFragment mFrag;
	private ActionBar aBar;
	private String TAG = BaseActivity.class.getSimpleName();
	public ArrayList<CateInfo> cateList;
	public ArrayList<GroupCateInfo> groupCateList;
	public static final String PRIVATE_CATEGORY_KEY = "private_category_array_key";
	public static final String CATEGORY_KEY = "category_array_key";
	public static final String GROUP_CATEGORY_KEY = "group_category_array_key";
	public static final String CATE_KEY = "cate_key";
	
	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mImageFetcher = Utils.getImageFetcher(BaseActivity.this);
		
		setTitle(mTitleRes);

		// customize the Action Bar Sherlock
		aBar = getSupportActionBar();
		aBar.setLogo(R.drawable.icon_klounge_small);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
		
		if(mImageFetcher != null){
			mImageFetcher.closeCache();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mImageFetcher != null){
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mImageFetcher != null){
			// image cache process 
			mImageFetcher.setExitTasksEarly(false);
		}
	}
}
