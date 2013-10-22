package kr.khub.activity.fragment;

import java.util.ArrayList;

import kr.khub.R;
import kr.khub.activity.FileSearchListActivity;
import kr.khub.activity.customview.listViews.EntryAdapter;
import kr.khub.activity.customview.listViews.EntryCateAdapter;
import kr.khub.activity.customview.listViews.EntryCateItem;
import kr.khub.activity.customview.listViews.Item;
import kr.khub.activity.customview.listViews.SectionItem;
import kr.khub.model.CateInfo;
import kr.khub.model.GroupCateInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SlideGroupMenuFragment extends ListFragment{
//	LeftMenuImageArrayAdapter adapter;
	private String CATE_KEY = "category_key";
	private String TAG = SlideGroupMenuFragment.class.getSimpleName();
	ArrayList<Item> items = new ArrayList<Item>();
	EntryCateAdapter adapter;
	private String[] categories;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ArrayList<GroupCateInfo> cate_list = null;
		categories = getResources().getStringArray(R.array.file_category);
		
		items.add(new SectionItem(categories[1]));
		
		try{
			cate_list = getArguments().getParcelableArrayList(FileSearchListActivity.GROUP_CATEGORY_KEY);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		if(cate_list != null){
			for(int i =0;i<cate_list.size();i++){
				items.add(new SectionItem(cate_list.get(i).getGroup_name()));
				ArrayList<CateInfo> cinfoList = cate_list.get(i).getCate_list();
				for(int j=0;j<cinfoList.size();j++){
					items.add(new EntryCateItem(
							cinfoList.get(j).getName(), 
							cinfoList.get(j).getId(),
							cate_list.get(i).getGroup_id()));
				}
			}
			adapter = new EntryCateAdapter(getActivity(), items);
			setListAdapter(adapter);
		}else{
			Log.d(TAG, "cate list null");
		}
		
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
		EntryCateItem item = (EntryCateItem)lv.getAdapter().getItem(position);
		bundle.putInt(CATE_KEY, item.getCate_id());
		
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