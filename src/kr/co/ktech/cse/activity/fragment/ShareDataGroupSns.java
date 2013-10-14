package kr.co.ktech.cse.activity.fragment;

import java.util.ArrayList;
import java.util.List;

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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
	private AlertDialog.Builder dialog;
	
	private String group_name;
	private int group_id;
	private ArrayList<GroupInfo> ginfoList;
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
		
		aBar.setTitle(getActivity().getResources().getString(R.string.share_group_sns));
		
		/*
		 * 필요한 정보 Setting 
		 * */
		try{
			setDinfo((DataInfo)getArguments().getParcelable(FileSearchListActivity.DATA_KEY));
			
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		
		/*
		 * View Setting
		 * */
		base = (RelativeLayout)inflater.inflate(R.layout.share_data_group_sns, null);
		ImageView user_image = (ImageView)base.findViewById(R.id.user_image_view);
		mImageFetcher.loadImage(AppUser.user_photo, user_image);
		
		TextView file_info = (TextView)base.findViewById(R.id.data_information);
		file_info.setText(getDinfo().getTitle());
		
		dialog = new AlertDialog.Builder(getActivity());
		new GetGroupNameTask().execute(getDinfo().getBpublic(), getDinfo().getGroup_id(), AppUser.user_id);
		
		return base;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		inflater.inflate(R.menu.share_data_menu, menu);
		
		if(getDinfo().getBpublic() == 2){
			menu.removeItem(R.id.choose_group);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.choose_group:
			if(dialog != null){
				dialog.show();
			}
			return true;
		case R.id.share_button:
			new ShareDataToServer().execute(getDinfo().getPostId(), CK, getSendGroupId());
			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}
	private void setSendGroupName(String gname){
		TextView gname_text = (TextView)base.findViewById(R.id.group_name);
		gname_text.setText(gname);
		this.group_name = gname;
	}
	private String getSendGroupName(){
		return this.group_name;
	}
	private void setSendGroupId(int gid){
		this.group_id = gid;
	}
	private int getSendGroupId(){
		return this.group_id;
	}
	private void setGroupInfoList(ArrayList<GroupInfo> gList){
		this.ginfoList = gList;
	}
	private ArrayList<GroupInfo> getGroupInfoList(){
		return ginfoList;
	}
	private DataInfo getDinfo() {
		return dinfo;
	}
	private void setDinfo(DataInfo dinfo) {
		this.dinfo = dinfo;
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
			ArrayList<GroupInfo> joinGroupList = new ArrayList<GroupInfo>();
			
			int bPublic = params[0];
			int group_id = params[1];
			int user_id = params[2];
			
			if(bPublic == 1){
				joinGroupList = getJoinGroupList(user_id);
			}else{
				try{
					GroupInfo ginfo = new GroupInfo();
					ginfo.setGroup_name(request.getGroupName(group_id));
					ginfo.setGroup_id(group_id);
					joinGroupList.add(ginfo);
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			setGroupInfoList(joinGroupList);
			
			return joinGroupList;
		}
		@Override
		protected void onPostExecute(ArrayList<GroupInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result.size() > 1){
				List<String> group_names = new ArrayList<String>();
				List<Integer> group_ids = new ArrayList<Integer>();
				for(GroupInfo info:result){
					group_names.add(info.getGroup_name());
					group_ids.add(info.getGroup_id());
				}
				final String [] dialog_items = group_names.toArray(new String[result.size()]);
				
				setAlertDialogOnClickGroupList(dialog_items, group_ids);
				dialog.show();
			}else{
				setSendGroupName(result.get(0).getGroup_name());
			}
		}
		private ArrayList<GroupInfo> getJoinGroupList(int user_id){
			ArrayList<GroupInfo>result = new ArrayList<GroupInfo>();
			result = request.getJoinGroupList(user_id);
			
			/*
			 * 마지막 줄에 공개 라운지 추가하자
			 * */
			GroupInfo public_group = new GroupInfo();
			public_group.setGroup_id(0);
			public_group.setGroup_name(mActivity.getResources().getString(R.string.group_all));
			result.add(public_group);
			
			return result;
		}
		private void setAlertDialogOnClickGroupList(final String[] dialog_items, final List<Integer>group_ids){
			dialog.setCancelable(true);
			dialog.setTitle(mActivity.getResources().getString(R.string.select_group_dialog_title));
			dialog.setItems(dialog_items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					setSendGroupName(dialog_items[which]);
					setSendGroupId(group_ids.get(which));
				}
			});
		}
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
			
			EditText input_text = (EditText)base.findViewById(R.id.edit_text);
			
			String body = input_text.getText().toString();
					//dInfo.getBody();
			String title = dInfo.getTitle();
			SnsAppInfo snsinfo = new SnsAppInfo();
			snsinfo.setUserId(AppUser.user_id);
			snsinfo.setGroupId(group_id);
			snsinfo.setBody(makeAnchorTag(UrlPath, body, title));
			snsinfo.setGroup_name(getSendGroupName());
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
		
		private String makeAnchorTag(String path, String body, String title){
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
