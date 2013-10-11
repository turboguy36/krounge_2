package kr.co.ktech.cse.activity.fragment;

import kr.co.ktech.cse.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class MoreTabFragment extends SherlockFragment {
	private static final String TAG = MoreTabFragment.class.getSimpleName();

	static MoreTabFragment newInstance(int num){
		MoreTabFragment f = new MoreTabFragment();
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.activity_more_tab, null);

		return v;
	}
}
