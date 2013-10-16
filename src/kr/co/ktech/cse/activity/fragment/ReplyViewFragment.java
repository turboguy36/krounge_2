package kr.co.ktech.cse.activity.fragment;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.ReplyViewDialog;
import kr.co.ktech.cse.activity.WriteMessage;
import kr.co.ktech.cse.adapter.ContentArrayAdapter;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.model.ToReplyInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

import com.actionbarsherlock.app.SherlockFragment;

public class ReplyViewFragment extends SherlockFragment implements OnClickListener, OnItemClickListener{

	private static final String TAG = ReplyViewFragment.class.getSimpleName();
	private ImageFetcher mImageFetcher;
	private ReplyViewDialog mActivity;
	private RelativeLayout base_view;
	private int THIS_POST_ID;

	// 유저를 태그 하고 글 작성 중인지 판별
	private boolean IS_WRITING_REPLY_TO_USER = false;
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mImageFetcher = Utils.getImageFetcher((ReplyViewDialog)activity);
		mActivity = (ReplyViewDialog)activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		base_view = (RelativeLayout)inflater.inflate(R.layout.reply_dialog, null);

		/*
		 * 필요한 정보 받는다.
		 * */
		Bundle bundle = getActivity().getIntent().getExtras();
		SnsAppInfo snsinfo = bundle.getParcelable("snsAppInfo");

		StringBuffer reply_title = new StringBuffer();
		reply_title.append(snsinfo.getReply_count())
		.append(getActivity().getResources().getString(R.string.reply_title));

		/*
		 * View 들을 정의 해 준다.
		 * */
		LinearLayout top_bar = (LinearLayout)base_view.findViewById(R.id.top_bar);

		TextView title = (TextView)top_bar.findViewById(R.id.reply_title);
		title.setText(reply_title.toString());

		LinearLayout bottom_bar = (LinearLayout)base_view.findViewById(R.id.input_bar);
		EditText input_view = (EditText)bottom_bar.findViewById(R.id.input_text);
		input_view.addTextChangedListener(textWatcher);

		Button send_button = (Button)base_view.findViewById(R.id.input_button);
		send_button.setTag(snsinfo);
		send_button.setOnClickListener(this);

		/*
		 * 댓글 리스트 들을 보여 준다.
		 * */
		ListView listview = (ListView)base_view.findViewById(R.id.reply_list);
		listview.setOnItemClickListener(this);

		if(snsinfo != null){
			try{
				int post_id = snsinfo.getPostId();
				int super_id = snsinfo.getSuperId();
				new GetReplyListTask().execute(post_id, super_id);
				THIS_POST_ID = post_id;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return base_view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.input_button:
			SnsAppInfo sinfo = (SnsAppInfo)v.getTag();
			new SendMessageTask().execute(sinfo.getPostId(), sinfo.getSuperId(), sinfo.getGroupId(), sinfo.getPuser_id(), sinfo.getGroup_name());
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long lPosition) {
		// TODO Auto-generated method stub
		final SnsAppInfo replyPostInfo = (SnsAppInfo)parent.getItemAtPosition(position);

		if(replyPostInfo.getUserId() == AppUser.user_id){
			// 내가 post 한 댓글이라면 (댓글 삭제, 댓글 달기, 취소) 
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			
			setAlertDialogOnClickMyPost(dialog, replyPostInfo);
			
			dialog.show();
		}else{
			// @ to_user_name 을 붙여서 post 할 수 있게 해 줘야
			String name_to = replyPostInfo.getUserName();
			int post_id = replyPostInfo.getPostId();
			
			setToUserTagBox(name_to, post_id);
		}
	}
	
	private void setToUserTagBox(String name_to, int post_id){
		EditText input_text = (EditText)base_view.findViewById(R.id.input_text);
		ToReplyInfo rInfo = new ToReplyInfo(name_to, post_id, mActivity);
		
		input_text.setText(rInfo.makeToUserBox());
		// TAG 된 유저의 이름을 보여주는 BOX 만들었다.
		input_text.setSelection((name_to.length()));
		
		if(input_text.isFocusable()){
			/*
			 * 키보드 보여주기
			 * */
			input_text.requestFocus();
			InputMethodManager imm  = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
		}
		IS_WRITING_REPLY_TO_USER = true;
		input_text.setTag(rInfo);
	}
	
	private void setAlertDialogOnClickMyPost(AlertDialog.Builder dialog, 
			final SnsAppInfo sinfo){
		// 내가 쓴 댓글을 클릭 했을 때
		String [] dialog_items = getActivity().getResources().getStringArray(R.array.reply_dialog);
		
		dialog.setCancelable(true);
		dialog.setItems(dialog_items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				int user_id = sinfo.getUserId();
				int post_id = sinfo.getPostId();
				String to_user_name = sinfo.getUserName();
				
				switch(which){
				case 0:
					new DeleteMessageTask().execute(user_id, post_id);
					break;
				case 1:
					setToUserTagBox(to_user_name, post_id);
					break;
				case 2:
					dialog.dismiss();
					break;
				}
			}
		});
	}
	
	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
			Button input_btn = (Button)getActivity().findViewById(R.id.input_button);
			EditText text_view = (EditText)getActivity().findViewById(R.id.input_text);
			
			if(count>0){
				/*
				 * 한 글자라도 입력 했을 경우
				 * */
				LayoutParams button_param = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2f);
				input_btn.setLayoutParams(button_param);
				input_btn.setVisibility(View.VISIBLE);

				LayoutParams text_param = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 7f);
				text_view.setLayoutParams(text_param);
			}else if(s.length()==0){
				LayoutParams button_param = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0f);
				input_btn.setLayoutParams(button_param);
				input_btn.setVisibility(View.GONE);

				LayoutParams text_param = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 9f);
				text_view.setLayoutParams(text_param);

				if(IS_WRITING_REPLY_TO_USER){
					IS_WRITING_REPLY_TO_USER = false;	
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
		}
	};
	public boolean isTagSequence(CharSequence input){
		boolean result = false;
		
		Pattern pattern = Pattern.compile("@[^<]* ");
		Matcher matcher = pattern.matcher(input);
		
		result = matcher.find();
		return result;
	}
	private class SendMessageTask extends AsyncTask<Object, String, List<SnsAppInfo>>{
		KLoungeRequest kloungehttp;
		String reply_body;
		ProgressDialog progress;
		Context context;
		final String MESSAGE_REPLY_TYPE = "reply";
		final String MESSAGE_MY_LOUNGE = "my";
		private View dialog_custom_view;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			EditText input_view = (EditText)base_view.findViewById(R.id.input_text);
			reply_body = input_view.getText().toString();
			kloungehttp = new KLoungeRequest();

			context = (Context)getActivity();
			if(context != null){
				dialog_custom_view = setProgressAndShow();
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			TextView message_view = (TextView)dialog_custom_view.findViewById(R.id.dialog_message);
			message_view.setText(values[0]);
		}

		@Override
		protected List<SnsAppInfo> doInBackground(Object... params) {
			// TODO Auto-generated method stub
			List<SnsAppInfo> replyResultList;
			
			int parent_post_id = (Integer) params[0];
			int super_post_id = (Integer) params[1];
			int groupId = (Integer) params[2];
			int puser_id = (Integer) params[3];
			String group_name = (String) params[4];
			
			int reply_post_id = super_post_id;
			
			if(IS_WRITING_REPLY_TO_USER){
				EditText input_text = (EditText)base_view.findViewById(R.id.input_text);
				ToReplyInfo rInfo = (ToReplyInfo)input_text.getTag();
				//setToUserTagBox(...) 에서 input_text.setTag(rInfo);
			
				// reply box 안에 있는 이름을 잘라 낸다.
				reply_body = reply_body.substring(rInfo.getTo_user_length());
				
				// reply_to 를 등록 할 상위 포스트 셋팅
				reply_post_id = rInfo.getReply_post_id();
			}

			SnsAppInfo sending_reply_info = new SnsAppInfo();
			/*
			 * 보낼 댓글의 정보를 셋팅 한다.
			 * */
			sending_reply_info.setUserId(AppUser.user_id);
			sending_reply_info.setBody(reply_body);
			sending_reply_info.setUserName(AppUser.user_name);
			sending_reply_info.setPhoto(AppUser.user_photo);
			sending_reply_info.setGroupId(groupId);
			sending_reply_info.setPuser_id(puser_id);
			sending_reply_info.setGroup_name(group_name);
			
			if(reply_post_id > 0 ) {
				sending_reply_info.setPostId(reply_post_id);
				kloungehttp.sendMessage(sending_reply_info, MESSAGE_MY_LOUNGE, MESSAGE_REPLY_TYPE);
			}else{
				sending_reply_info.setPostId(parent_post_id);
				kloungehttp.sendMessage(sending_reply_info, MESSAGE_MY_LOUNGE, "");
			}

			/*
			 * 전송 완료 라고 글을 변경 한다.
			 * */
			publishProgress(mActivity.getResources().getString(R.string.complete_task));

			/*
			 * 방금 보낸 글이 적용 된 리스트를 받아 온다.
			 * */
			replyResultList = kloungehttp.getReplyMessageList(THIS_POST_ID);
			return replyResultList;
		}

		@Override
		protected void onPostExecute(List<SnsAppInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			/*
			 * 몇개의 댓글이 등록 되었는지
			 * */
			StringBuffer reply_title = new StringBuffer();
			reply_title.append(result.size())
			.append(getActivity().getResources().getString(R.string.reply_title));
			TextView title = (TextView)base_view.findViewById(R.id.reply_title);
			title.setText(reply_title.toString());

			/*
			 * 등록된 댓글들의 실제 내용 셋팅
			 * */
			if(result.size() > 0) {
				ContentArrayAdapter adapter = new ContentArrayAdapter(mActivity, mImageFetcher, result);
				ListView listview = (ListView)mActivity.findViewById(R.id.reply_list);
				listview.setAdapter(adapter);
			}

			/*
			 * Soft Keyboard 숨기고, 입력란 비우는 작업
			 * */
			EditText reply_text = (EditText)base_view.findViewById(R.id.input_text);
			if(reply_text.isFocusable()) {
				reply_text.setText("");
				reply_text.clearFocus();
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(reply_text.getWindowToken(),0);
			}

			progress.dismiss();
			IS_WRITING_REPLY_TO_USER = false;
		}

		private View setProgressAndShow(){
			
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.dialog_delete_reply, null);

			progress = new ProgressDialog(context);
			progress.setTitle(null);
			progress.setMessage(context.getResources().getString(R.string.loading_msg));
			progress.setIndeterminate(true);
			progress.setCancelable(true);
			progress.show();
			progress.setContentView(view);

			TextView message_view = (TextView)view.findViewById(R.id.dialog_message);
			message_view.setText(mActivity.getResources().getString(R.string.loading_msg));
			
			return view;
		}
	}
	private class GetReplyListTask extends AsyncTask<Integer, Void, List<SnsAppInfo>>{
		List<SnsAppInfo> replyList;
		KLoungeRequest kloungehttp;
		private ProgressBar progressBar;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			kloungehttp = new KLoungeRequest();
			setProgressBar();
		}

		@Override
		protected List<SnsAppInfo> doInBackground(Integer... params) {
			// TODO => 글에 해당하는 댓글 리스트를 리턴한다.
			int post_id = params[0];
			int super_id = params[1];
			try {
				if(super_id > 0){ 
					replyList = kloungehttp.getReplyMessageList(super_id);
				}else{
					replyList = kloungehttp.getReplyMessageList(post_id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return replyList;
		}

		@Override
		protected void onPostExecute(List<SnsAppInfo> result) {
			// TODO 리스트에 Adapter 를 적용 한다.
			super.onPostExecute(result);
			if(result.size() == 0){
				/*
				 * 등록된 댓글이 하나도 없을 때
				 * */
				RelativeLayout view = (RelativeLayout)base_view.findViewById(R.id.empty_list_view);
				view.setVisibility(View.VISIBLE);
				TextView empty_msg = (TextView)view.findViewById(R.id.empty_list_text);
				empty_msg.setText(mActivity.getResources().getString(R.string.reply_empty_text));
			}else{
				ContentArrayAdapter adapter = new ContentArrayAdapter(mActivity, mImageFetcher, result);
				ListView listview = (ListView)mActivity.findViewById(R.id.reply_list);
				listview.setAdapter(adapter);
			}
			progressBar.setVisibility(View.GONE);
		}
		private void setProgressBar(){
			progressBar = (ProgressBar)base_view.findViewById(R.id.list_progress);
			progressBar.setVisibility(View.VISIBLE);
		}
	}
	private class DeleteMessageTask extends AsyncTask<Integer, Void, List<SnsAppInfo>>{
		private ProgressDialog progress;
		private final int REPLY_MESSAGE = 0;
		KLoungeRequest kreq;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			kreq = new KLoungeRequest();

			if(mActivity!=null){
				progress = new ProgressDialog(mActivity);
				progress.show();
				progress.setContentView(R.layout.dialog_delete_reply);
			}
		}

		@Override
		protected List<SnsAppInfo> doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int user_id = params[0];
			int post_id = params[1];

			kreq.deleteMessage(user_id, post_id, REPLY_MESSAGE);

			return kreq.getReplyMessageList(THIS_POST_ID);
		}

		@Override
		protected void onPostExecute(List<SnsAppInfo> result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			/*
			 * 몇개의 댓글이 등록 되었는지
			 * */
			StringBuffer reply_title = new StringBuffer();
			reply_title.append(result.size())
			.append(getActivity().getResources().getString(R.string.reply_title));
			TextView title = (TextView)base_view.findViewById(R.id.reply_title);
			title.setText(reply_title.toString());

			/*
			 * 새로 받아온 댓글 리스트 보여주기
			 * */
			ContentArrayAdapter adapter = new ContentArrayAdapter(mActivity, mImageFetcher, result);
			ListView listview = (ListView)mActivity.findViewById(R.id.reply_list);
			listview.setAdapter(adapter);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					progress.dismiss();
				}
			},100);
		}
	}
}
