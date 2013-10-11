package kr.co.ktech.cse.activity.fragment;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.FileSearchListActivity;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.DataInfo;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SnsAppInfo;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ShareDataGroupSns extends SherlockFragment{
	private ImageFetcher mImageFetcher;
	private FileSearchListActivity mActivity;
	private final String TAG = ShareDataGroupSns.class.getSimpleName();
	private ActionBar aBar;
	private DataInfo dinfo = new DataInfo();
	private final int CK = 4;
	private RelativeLayout base;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mImageFetcher = Utils.getImageFetcher((FileSearchListActivity)activity);
		mActivity = (FileSearchListActivity)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		
		/*
		 * Action Bar Setting
		 * */
		setHasOptionsMenu(true);
		aBar=getSherlockActivity().getSupportActionBar();
		
//		TextView title_view = (TextView)aBar.getCustomView().findViewById(R.id.title);
//		title_view.setText(getActivity().getResources().getString(R.string.share_group_sns));
//		title_view.setSelected(true);
		aBar.setTitle(getActivity().getResources().getString(R.string.share_group_sns));
		
		/*
		 * 필요한 정보 Setting 
		 * */
		try{
			dinfo = getArguments().getParcelable(FileSearchListActivity.DATA_KEY);
			
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		/*
		 * View Setting
		 * */
		base = (RelativeLayout)inflater.inflate(R.layout.share_data_group_sns, null);
		ImageView user_image = (ImageView)base.findViewById(R.id.user_image_view);
		mImageFetcher.loadImage(dinfo.getUser_photo(), user_image);
		
		TextView file_info = (TextView)base.findViewById(R.id.data_information);
		file_info.setText(dinfo.getTitle());
		
		new GetGroupNameTask().execute(dinfo.getBpublic(), dinfo.getGroup_id(), AppUser.user_id);
		return base;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.share_data_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.share_button:
			
			new ShareDataToServer().execute(dinfo.getPostId(), CK, dinfo.getGroup_id());

			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class GetGroupNameTask extends AsyncTask<Integer, Void, ArrayList<GroupInfo>>{
		private KLoungeRequest request;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			request = new KLoungeRequest();
		}
		@Override
		protected ArrayList<GroupInfo> doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			ArrayList<GroupInfo> result = new ArrayList<GroupInfo>();
			
			int bPublic = params[0];
			int group_id = params[1];
			int user_id = params[2];
			
			if(bPublic == 1){
				result = getJoinGroupList(user_id);
			}else{
				try{
					GroupInfo ginfo = new GroupInfo();
					ginfo.setGroup_name(request.getGroupName(group_id));
					ginfo.setGroup_id(group_id);
					result.add(ginfo);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			return result;
		}
		@Override
		protected void onPostExecute(ArrayList<GroupInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
		}
		private ArrayList<GroupInfo> getJoinGroupList(int user_id){
			ArrayList<GroupInfo>result = new ArrayList<GroupInfo>();
			result = request.getJoinGroupList(user_id);
			return result;
		}
		ActionBar.OnNavigationListener callback = new ActionBar.OnNavigationListener() {
			
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				// TODO Auto-generated method stub
				Log.d(TAG, "positon: "+itemPosition);
				return false;
			}
		};
	}
	private class ShareDataToServer extends AsyncTask<Integer, Void, Void>{
		private KLoungeRequest request;
		private ProgressDialog progressBar;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			request = new KLoungeRequest();
			
			progressBar = new ProgressDialog(getActivity());
			progressBar.setCancelable(true);
			progressBar.setMessage("메시지 전송중...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			progressBar.show();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			DataInfo dInfo = new DataInfo();
			dInfo = dinfo;
			
			int post_id = params[0];
			int ck = params[1];
			int group_id = params[2];
			String UrlPath = "../common/redirectLink.jsp?p_id="+post_id+"&check="+ck+"&group_id="+group_id;
			
			String body = dInfo.getBody();
			String title = dInfo.getTitle();
			SnsAppInfo snsinfo = new SnsAppInfo();
			snsinfo.setUserId(dInfo.getUser_id());
			snsinfo.setGroupId(group_id);
			snsinfo.setBody(makeATag(UrlPath, body, title));
			snsinfo.setGroup_name(dInfo.getGroup_name());
			snsinfo.setUserName(dInfo.getUser_name());
			snsinfo.setPhoto(dInfo.getUser_photo());
			request.sendMessage(snsinfo, "body", "group");
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			progressBar.dismiss();
			
			((FileSearchListActivity)getActivity()).onBackPressed();
		}
		
		private String makeATag(String path, String body, String title){
			StringBuffer result = new StringBuffer();
			result.append(body)
					.append("<br/>")
					.append("<a href=\'")
					.append(path)
					.append("\'")
					.append("targer=\'blank\'>")
					.append("<b>")
					.append(title)
					.append("</b>")
					.append("</a>");
			
			return result.toString();
		}
	}
}
