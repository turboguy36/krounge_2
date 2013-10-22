package kr.khub.activity.fragment;

import kr.khub.R;
import kr.khub.activity.KLoungeMainTabs;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class KLoungeMsgFragment extends SherlockFragment {
	private static final String TAG = KLoungeMsgFragment.class.getSimpleName();
	
	static KLoungeMsgFragment newInstance(int num){
		KLoungeMsgFragment f = new KLoungeMsgFragment();
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.activity_klounge_msg, null);
		
		return v;
	}

}
