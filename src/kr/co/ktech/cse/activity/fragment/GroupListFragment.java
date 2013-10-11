package kr.co.ktech.cse.activity.fragment;

import kr.co.ktech.cse.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class GroupListFragment extends SherlockFragment {
	private static final String TAG = GroupListFragment.class.getSimpleName();

	static GroupListFragment newInstance(int num){
		GroupListFragment f = new GroupListFragment();
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.activity_group_list, null);

		return v;
	}
}
