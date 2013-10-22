package kr.khub.activity.fragment;

import kr.khub.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class MyLoungeFragment extends SherlockFragment {
	private static final String TAG = MyLoungeFragment.class.getSimpleName();

	static MyLoungeFragment newInstance(int num){
		MyLoungeFragment f = new MyLoungeFragment();
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.activity_my_lounge, null);

		return v;
	}
}
