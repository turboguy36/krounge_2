package kr.co.ktech.cse.activity.fragment;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.FileSearchListActivity;
import kr.co.ktech.cse.adapter.DataInfoArrayAdapter;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.DataInfo;
import kr.co.ktech.cse.util.FileDownloadManager;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SearchViewCompat.OnQueryTextListenerCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class HomeFragment extends SherlockFragment implements OnItemClickListener, OnClickListener{
	private static final String TAG = HomeFragment.class.getSimpleName();

	ActionBar aBar;
	//	ImageFetcher mImageFetcher;

	public static final String EVENT_ID_KEY = "event_id" ;
	public static final String EVENT_TITLE_KEY = "event_title" ;

	private ListView listview;
	private SearchView search_view;
	private Button public_button;
	private Button group_button;
	private Button private_button;
	private Button allShared_button;
	private ProgressDialog progress;

	private static final int ALL_DATA_CATE = -1;
	private static final int PUBLIC_CATE = 0;
	private static final int PRIVATE_CATE = 1;
	private static final int GROUP_CATE = 2;

	private int CUR_CATE_ID = -1;
	//	private String search_text;

	private static final String ALL_DATA_LIST = "ALL_DATA_LIST";

	private ArrayList<DataInfo> allDataList = new ArrayList<DataInfo>();
	private ArrayList<DataInfo> publicDataList = new ArrayList<DataInfo>();
	private ArrayList<DataInfo> groupDataList = new ArrayList<DataInfo>();
	private ArrayList<DataInfo> myDataList = new ArrayList<DataInfo>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if(savedInstanceState == null){
		}else{
			allDataList = savedInstanceState.getParcelableArrayList(ALL_DATA_LIST);
			publicDataList = new ArrayList<DataInfo>();
			groupDataList = new ArrayList<DataInfo>();
			myDataList = new ArrayList<DataInfo>();
			for(DataInfo d:allDataList){
				if(d.getUser_id() == AppUser.user_id)
					myDataList.add(d);
			}
			// 가입 그룹 자료에 대한 리스트 저장
			ArrayList<Integer> group_id_list = getGroupIdList();
			for(DataInfo d:allDataList){
				if(group_id_list.contains(d.getGroup_id())){
					groupDataList.add(d);
				}
			}
			// 공개 자료에 대한 리스트 저장
			for(DataInfo d:allDataList){
				if(d.getPubcate_id() == 1)
					publicDataList.add(d);
			}
		}

		setProgress();
		progress.show();

		new GetDataListTask().execute(new SearchInfo(AppUser.user_id, PUBLIC_CATE, ""));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		aBar = getSherlockActivity().getSupportActionBar();
//		TextView title_view = (TextView)aBar.getCustomView().findViewById(R.id.title);
//		title_view.setText(getActivity().getResources().getString(R.string.file_search_button_text));
		aBar.setTitle(getActivity().getResources().getString(R.string.file_search_button_text));

		View v = inflater.inflate(R.layout.activity_get_file_list, null);
		listview = (ListView)v.findViewById(R.id.file_list);
		listview.setOnItemClickListener(this);

		public_button = (Button)v.findViewById(R.id.public_search);
		group_button = (Button)v.findViewById(R.id.group_search);
		private_button = (Button)v.findViewById(R.id.private_search);
		allShared_button = (Button)v.findViewById(R.id.all_search);
		
		public_button.setOnClickListener(this);
		group_button.setOnClickListener(this);
		private_button.setOnClickListener(this);
		allShared_button.setOnClickListener(this);

		allShared_button.setSelected(true);
		return v;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		// Give some text to display if there is no data.  In a real
		// application this would come from a resource.
		setHasOptionsMenu(true);
		DataInfoArrayAdapter adapter = new DataInfoArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, allDataList);
		listview.setAdapter(adapter);
	}
	private ArrayList<DataInfo> searchWithQuery(int search_where, String query){
		ArrayList<DataInfo> result = new ArrayList<DataInfo>();
		switch(search_where){
		case ALL_DATA_CATE:
			//			Log.d(TAG, "allDataList search");
			for(DataInfo d: allDataList){
				if(d.getTitle().contains(query) || d.getAttach().contains(query)
						|| d.getKeyword().contains(query) || d.getBody().contains(query)){
					result.add(d);
				}
			}
			break;
		case PRIVATE_CATE:
			//			Log.d(TAG, "myDataList search");
			for(DataInfo d : myDataList){
				if(d.getTitle().contains(query) || d.getAttach().contains(query)
						|| d.getKeyword().contains(query) || d.getBody().contains(query)){
					result.add(d);
				}
			}
			break;
		case GROUP_CATE:
			//			Log.d(TAG, "groupDataList search");
			for(DataInfo d : groupDataList){
				if(d.getTitle().contains(query) || d.getAttach().contains(query)
						|| d.getKeyword().contains(query) || d.getBody().contains(query)){
					result.add(d);
				}
			}
			break;
		case PUBLIC_CATE:
			//			Log.d(TAG, "publicDataList search");
			for(DataInfo d : publicDataList){
				if(d.getTitle().contains(query) || d.getAttach().contains(query)
						|| d.getKeyword().contains(query) || d.getBody().contains(query)){
					result.add(d);
				}
			}
			break;	
		}
		DataInfoArrayAdapter adapter = new DataInfoArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, result);
		listview.setAdapter(adapter);
		return result;
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		// TODO Auto-generated method stub
		menu.clear();
		MenuItem item = menu.add("Search");
		item.setIcon(android.R.drawable.ic_menu_search);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		SherlockFragmentActivity activity = (SherlockFragmentActivity)getActivity();
		View view = SearchViewCompat.newSearchView(activity.getSupportActionBar().getThemedContext());

		if(view != null){
			search_view = (SearchView)view;
			if(Utils.hasHoneycomb()){
				search_view.setQueryHint(getResources().getString(R.string.file_query_hint));
			}

			SearchViewCompat.setOnQueryTextListener(view, 
					new OnQueryTextListenerCompat() {

				@Override
				public boolean onQueryTextChange(String newText) {
					// TODO Auto-generated method stub
					searchWithQuery(CUR_CATE_ID, newText);

					return true;
				}

				@Override
				public boolean onQueryTextSubmit(String query) {
					// TODO Auto-generated method stub
					searchWithQuery(CUR_CATE_ID, query);
					InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(search_view.getWindowToken(), 0);
					return true;
				}
			});
			item.setActionView(view);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		getActivity().supportInvalidateOptionsMenu();

		super.onPause();
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

		DataInfo dinfo = ((DataInfo)parent.getAdapter().getItem(position));
		((FileSearchListActivity)getActivity()).addFragmentToStack(FileContentViewFragment.class.getSimpleName(), dinfo);
	}

	void setProgress(){
		progress = new ProgressDialog(getActivity());
		progress.setCancelable(true);
		progress.setCanceledOnTouchOutside(true);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMessage("잠시만 기다려 주십시오...");
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

		switch(v.getId()){
		case R.id.all_search:
			CUR_CATE_ID = ALL_DATA_CATE;

			allShared_button.setSelected(true);
			private_button.setSelected(false);
			group_button.setSelected(false);
			public_button.setSelected(false);
			
			break;
		case R.id.public_search:

			CUR_CATE_ID = PUBLIC_CATE;

			public_button.setSelected(true);
			private_button.setSelected(false);
			group_button.setSelected(false);
			allShared_button.setSelected(false);
			
			break;

		case R.id.group_search:
			CUR_CATE_ID = GROUP_CATE;

			group_button.setSelected(true);
			public_button.setSelected(false);
			private_button.setSelected(false);
			allShared_button.setSelected(false);
			
			break;

		case R.id.private_search:
			CUR_CATE_ID = PRIVATE_CATE;

			private_button.setSelected(true);
			public_button.setSelected(false);
			group_button.setSelected(false);
			allShared_button.setSelected(false);
			
			break;
		}
		if(Utils.hasHoneycomb()){
			searchWithQuery(CUR_CATE_ID, search_view.getQuery().toString());
		}else{
			
		}
	}
	private ArrayList<Integer>getGroupIdList(){
		ArrayList<Integer> group_id_list = new ArrayList<Integer>();
		SharedPreferences prefs;
		prefs = getActivity().getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
		String group_list_json = prefs.getString("group_list", "");
		String[] group_list = group_list_json.split(CommonUtilities.SPLIT_SIGN_PARENT);
		for(String group_object : group_list){
			String[] object = group_object.split(CommonUtilities.SPLIT_SIGN_CHILD);
			group_id_list.add(Integer.parseInt(object[0]));
		}
		return group_id_list;
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putString(TAG, "파일 검색");
		outState.putParcelableArrayList(ALL_DATA_LIST, allDataList);
	}
	public class GetDataListTask extends AsyncTask<SearchInfo, TaskProgressInfo, ArrayList<DataInfo>>{
		private KLoungeHttpRequest httprequest;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			httprequest = new KLoungeHttpRequest();
		}

		@Override
		protected void onProgressUpdate(TaskProgressInfo... info) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(info);
			progress.setProgress(info[0].percentage);
			progress.setMessage(info[0].message);
		}

		@Override
		protected ArrayList<DataInfo> doInBackground(SearchInfo... params) {
			// TODO Auto-generated method stub
			publishProgress(new TaskProgressInfo(0, "잠시만 기다려 주십시오."));
			ArrayList<DataInfo> result = new ArrayList<DataInfo>();
			//			Log.d(TAG, "do in background~!");
			SearchInfo sInfo = params[0];
			int user_id = sInfo.getUser_id();
			int cate_id = sInfo.getCate();
			String query = sInfo.getQuery();
			try{
				publishProgress(new TaskProgressInfo(10, "잠시만 기다려 주십시오."));
				ArrayList<DataInfo> data_list = getPublicDataList(user_id, cate_id, query);
				publishProgress(new TaskProgressInfo(60, "리스트를 분석 중입니다."));
				//개인 자료에 대한 리스트 저장
				for(DataInfo d:data_list){
					if(d.getUser_id() == AppUser.user_id)
						myDataList.add(d);
				}
				publishProgress(new TaskProgressInfo(70, "리스트를 저장 중입니다."));
				//가입 그룹 자료에 대한 리스트 저장
				ArrayList<Integer> group_id_list = getGroupIdList();
				for(DataInfo d:data_list){
					if(group_id_list.contains(d.getGroup_id())){
						groupDataList.add(d);
					}
				}
				publishProgress(new TaskProgressInfo(80, "리스트를 저장 중입니다."));
				// 공개 자료에 대한 리스트 저장
				for(DataInfo d:data_list){
					if(d.getPubcate_id() == 1)
						publicDataList.add(d);

				}
				publishProgress(new TaskProgressInfo(90, "리스트를 저장 중입니다."));
				// 모든 자료에 대한 리스트 저장
				allDataList = data_list;

				Log.d(TAG, "size : " + allDataList.size()
						+"all: " + publicDataList.size()
						+"group: " + groupDataList.size()
						+"my: "+myDataList.size());
				
				result = data_list;
				publishProgress(new TaskProgressInfo(100, "완료 중입니다."));
			}catch(NullPointerException e){
				e.printStackTrace();
			}
			return result;
		}
		@Override
		protected void onPostExecute(ArrayList<DataInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			try{
				if(!isCancelled()){
					DataInfoArrayAdapter adapter = new DataInfoArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, result);
					listview.setAdapter(adapter);
				}
				progress.dismiss();
			}catch(NullPointerException e){
				e.printStackTrace();
			}
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}
		private ArrayList<DataInfo> getPublicDataList(int user_id, int data_cate_id, String query){
			ArrayList<DataInfo> result = new ArrayList<DataInfo>();
			try{
				publishProgress(new TaskProgressInfo(20, "서버에 응답을 요청 중입니다."));

				String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetAllSharedFileList.jsp";
				String params = "user_id="+user_id +"&data_cate_id="+data_cate_id+"&query="+URLEncoder.encode(query, "utf-8");
				addr = addr + "?" + params;
				
				publishProgress(new TaskProgressInfo(30, "서버에 응답을 요청 중입니다."));
				String strJSON = httprequest.getJSONHttpGet(addr);
				
				publishProgress(new TaskProgressInfo(40, "리스트를 분석 중입니다."));

				result = parseDataInfo(strJSON);
				publishProgress(new TaskProgressInfo(50, "리스트를 분석 중입니다."));

			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
		private ArrayList<DataInfo> parseDataInfo(String strJSON){
			//			Log.d(TAG, strJSON);
			ArrayList<DataInfo> result = new ArrayList<DataInfo>();
			try{
				JSONObject jsonObj = new JSONObject(strJSON);
				JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
				JSONArray dataArray = kloungeObj.getJSONArray("file_list");

				for(int index = 0;index <dataArray.length();index++){
					JSONObject indexObj = dataArray.getJSONObject(index);
					DataInfo dInfo = new DataInfo();
					dInfo.setTitle(indexObj.getString("title"));
					dInfo.setDate(Timestamp.valueOf(indexObj.getString("date")));
					dInfo.setUser_id(Integer.parseInt(indexObj.getString("user_id")));
					dInfo.setCount(Integer.parseInt(indexObj.getString("count")));
					dInfo.setAttach(indexObj.getString("attach"));
					dInfo.setPostId(Integer.parseInt(indexObj.getString("post_id")));
					dInfo.setGroup_id(Integer.parseInt(indexObj.getString("group_id")));
					dInfo.setKeyword(indexObj.getString("keyword"));
					dInfo.setBody(indexObj.getString("body"));
					dInfo.setBpublic(Integer.parseInt(indexObj.getString("bPublic")));
					dInfo.setPubcate_id(Integer.parseInt(indexObj.getString("pubCateId")));
					dInfo.setPoint(Integer.parseInt(indexObj.getString("star_point")));
					result.add(dInfo);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return result;
		}
	}
	private static class TaskProgressInfo{
		final int percentage;
		final String message;
		TaskProgressInfo(int p, String m){
			this.percentage = p;
			this.message = m;
		}
	}
	private class SearchInfo{
		final int user_id;
		final int cate;
		final String query;

		public SearchInfo(int user_id, int cate, String query) {
			super();
			this.user_id = user_id;
			this.cate = cate;
			this.query = query;
		}
		public int getUser_id() {
			return user_id;
		}

		public int getCate() {
			return cate;
		}

		public String getQuery() {
			return query;
		}
	}

}
