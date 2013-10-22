package kr.khub.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import kr.khub.R;
import kr.khub.activity.FileSearchListActivity;
import kr.khub.bitmapfun.util.ImageFetcher;
import kr.khub.bitmapfun.util.Utils;
import kr.khub.db.KLoungeRequest;
import kr.khub.model.AppUser;
import kr.khub.model.DataInfo;
import kr.khub.model.GroupInfo;
import kr.khub.model.SnsAppInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
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
	
	private String group_name;
	private int group_id;
	private ArrayList<GroupInfo> ginfoList;
	private String[] group_name_items;
	private ArrayList<Integer> group_id_items;
	private int choiced_item_num;
	
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
		
		TextView group_name_guide = (TextView)base.findViewById(R.id.group_name);
		group_name_guide.setText(mActivity.getResources().getString(R.string.where_to_post));
		
		TextView file_info = (TextView)base.findViewById(R.id.data_information);
		file_info.setText(getDinfo().getTitle());
		
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
			setAlertDialogOnClickGroupList(getGroup_name_items(), getGroup_id_items(), getChoiced_item_num());
			return true;
		case R.id.share_button:
			setAlertDialogOnSendButtonClick();
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
	
	private String[] getGroup_name_items() {
		return group_name_items;
	}

	private void setGroup_name_items(String[] group_name_items) {
		this.group_name_items = group_name_items;
	}

	private ArrayList<Integer>  getGroup_id_items() {
		return group_id_items;
	}

	private void setGroup_id_items(ArrayList<Integer> group_id_items) {
		this.group_id_items = group_id_items;
	}

	private DataInfo getDinfo() {
		return dinfo;
	}
	private void setDinfo(DataInfo dinfo) {
		this.dinfo = dinfo;
	}
	
	private int getChoiced_item_num() {
		return choiced_item_num;
	}

	private void setChoiced_item_num(int choiced_item_num) {
		this.choiced_item_num = choiced_item_num;
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
			if(result.size() == 1){
				setSendGroupName(result.get(0).getGroup_name());
				setSendGroupId(result.get(0).getGroup_id());
			}else{
				List<String> group_names = new ArrayList<String>();
				ArrayList<Integer> group_ids = new ArrayList<Integer>();
				for(GroupInfo info:result){
					group_names.add(info.getGroup_name());
					group_ids.add(info.getGroup_id());
				}
				final String [] dialog_items = group_names.toArray(new String[result.size()]);
				
				setGroup_id_items(group_ids);
				setGroup_name_items(dialog_items);
				
				setAlertDialogOnClickGroupList(dialog_items, group_ids, 0);
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
	}
	private void setAlertDialogOnClickGroupList(final String[] dialog_items, final List<Integer>group_ids, final int choiced_idx){
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setCancelable(false);
		dialog.setTitle(mActivity.getResources().getString(R.string.select_group_dialog_title));
		dialog.setSingleChoiceItems(dialog_items, choiced_idx, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				setSendGroupName(dialog_items[which]);
				setSendGroupId(group_ids.get(which));
				setChoiced_item_num(which);
				
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	private void setAlertDialogOnSendButtonClick(){
		final String confirm_button = mActivity.getResources().getString(R.string.text_confirm);
		final String cancel_button = mActivity.getResources().getString(R.string.text_cancel);
		final String dialog_title = mActivity.getResources().getString(R.string.send_dialog_title);
		final String dialog_message = mActivity.getResources().getString(R.string.confirm_send_message);
		final Drawable iconId = mActivity.getResources().getDrawable(R.drawable.question_icon);
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setCancelable(false);
		dialog.setTitle(dialog_title);
		dialog.setIcon(iconId);
		dialog.setMessage(dialog_message);
		dialog.setPositiveButton(confirm_button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				new ShareDataToServer().execute(getDinfo().getPostId(), CK, getSendGroupId());
				dialog.dismiss();
			}
		});
		dialog.setNegativeButton(cancel_button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	private class ShareDataToServer extends AsyncTask<Integer, Void, Boolean>{
		private KLoungeRequest request;
		private ProgressDialog progressBar;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			request = new KLoungeRequest();
			
			progressBar = new ProgressDialog(getActivity());
			progressBar.setCancelable(true);
			progressBar.setMessage(mActivity.getResources().getString(R.string.share_data_sending));
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			progressBar.show();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			DataInfo dInfo = new DataInfo();
			dInfo = dinfo;
			
			if(getSendGroupName()  == null){
				// 만약 group name 이 셋팅 되어 있지 않다면
				return false;
			}
			
			int post_id = params[0];
			int ck = params[1];
			int group_id = params[2];
			
			StringBuilder UrlPath = new StringBuilder(); 
			UrlPath.append("../common/redirectLink.jsp?p_id=")
					.append(post_id)
					.append("&check=")
					.append(ck)
					.append("&group_id=")
					.append(group_id);
			
			EditText input_text = (EditText)base.findViewById(R.id.edit_text);
			String body = input_text.getText().toString();
			
			String title = dInfo.getTitle();
			
			SnsAppInfo snsinfo = new SnsAppInfo();
			snsinfo.setUserId(AppUser.user_id);
			snsinfo.setGroupId(group_id);
			snsinfo.setBody(makeAnchorTag(UrlPath.toString(), body, title));
			snsinfo.setGroup_name(getSendGroupName());
			snsinfo.setUserName(dInfo.getUser_name());
			snsinfo.setPhoto(dInfo.getUser_photo());
			request.sendMessage(snsinfo, "body", "group");
			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			progressBar.dismiss();
			if(result){
				((FileSearchListActivity)getActivity()).onBackPressed();
			}
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
