package kr.co.ktech.cse.activity.fragment;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.BaseActivity;
import kr.co.ktech.cse.activity.FileSearchListActivity;
import kr.co.ktech.cse.activity.customview.listViews.EntryAdapter;
import kr.co.ktech.cse.activity.customview.listViews.EntryItem;
import kr.co.ktech.cse.activity.customview.listViews.Item;
import kr.co.ktech.cse.activity.customview.listViews.SectionItem;
import kr.co.ktech.cse.model.CateInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SlideMenuFragment extends ListFragment{
//	LeftMenuImageArrayAdapter adapter;
//	private String CATE_KEY = "cate_key";
	private String TAG = SlideMenuFragment.class.getSimpleName();
	ArrayList<Item> items = new ArrayList<Item>();
	EntryAdapter adapter;
	private String[] categories;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		categories = getResources().getStringArray(R.array.file_category);
		ArrayList<CateInfo> cate_list = null;
		Boolean IS_PUBLIC = true;
		try{
			cate_list = getArguments().getParcelableArrayList(BaseActivity.CATEGORY_KEY);
			IS_PUBLIC = getArguments().getBoolean(BaseActivity.PRIVATE_CATEGORY_KEY);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		if(IS_PUBLIC){
			items.add(new SectionItem(categories[0]));
		}else{
			items.add(new SectionItem(categories[2]));
		}
		
		if(cate_list != null){
			for(int i=0;i<cate_list.size();i++){
				items.add(new EntryItem(cate_list.get(i).getName(), cate_list.get(i).getId()));
			}
		}
		
		adapter = new EntryAdapter(getActivity(), items);
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {
		Bundle bundle = new Bundle();
		bundle.putInt(BaseActivity.CATE_KEY, (int)lv.getAdapter().getItemId(position));
		
		Fragment newContent = new HomeFragment();
		newContent.setArguments(bundle);
		
		if (newContent != null)
			switchFragment(newContent);
	}

	// the meat of switching the above fragment
	private void switchFragment(Fragment fragment) {
		if (getActivity() == null){
			Log.d(TAG, "return");
			return;
		}
		if (getActivity() instanceof FileSearchListActivity) {
			FileSearchListActivity main_activity = (FileSearchListActivity) getActivity();
//			main_activity.switchContent(fragment);
		}
	}
	
}