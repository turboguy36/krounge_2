package kr.co.ktech.cse.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R.id;
import kr.co.ktech.cse.activity.AttachedDownloadManager;
import kr.co.ktech.cse.activity.KLoungeActivity;
import kr.co.ktech.cse.activity.KLoungeMsg;
import kr.co.ktech.cse.activity.MyLounge;
import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.activity.ReplyActivity;
import kr.co.ktech.cse.activity.ReplyViewDialog;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.NewMessage;
import kr.co.ktech.cse.model.PostAttachHolder;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.BadgeView;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.listener.AppClickListener;
import static kr.co.ktech.cse.CommonUtilities.FLAG_IF_REDIRECT_LINK;

public class MessageLayoutSetting{
	private final int MAIN_MESSAGE = 1;
	private final int REPLY_MESSAGE = 0;
	
	private final int REPLY_BADGEVIEW_ID = 90082;
	private final int TEXTVIEW_DELETE_ID = 9009;

	private final int REPLY_RL_HEAD_LEFT = 9014;
	private final int REPLY_RL_HEAD = 9015;
	private final int REPLY_USER_IMG = 9016;
	private final int REPLY_USER_NAME = 9017;
	private final int REPLY_TOUSER_NAME = 9018;
	private final int REPLY_BODY = 9019;
	private final int REPLY_WRITE_DATE = 9020;
	private final int REPLY_GO_WRITE_REPLY = 9021;

	private final int REPLY_USER_IMG_ID = 0;
	private final int REPLY_USER_NAME_ID = 0;
	private final int REPLY_TOUSER_NAME_ID = 1;
	private final int REPLY_BODY_ID = 2;
	private final int REPLY_WRITE_DATE_ID = 3;
	private final int REPLY_GO_WRITE_REPLY_ID = 0;
	private final int REPLY_DELETE_BTN_ID = 1;

	private static final int MESSAGE_REPLY_TEXT_SIZE = 12;
	private static final int MESSAGE_IMAGE_MAX_WIDTH = 70;

	public final int REPLY_TAIL_HEIGHT = 50;
	private SnsAppInfo sns_info;
	private Context context;
	
	private LinearLayout baseLinearLayout;
	private EditText etReplyText;
	final String TO_REPLY_MARK = "@";
	final String SEPARATOR= " ";
	final int REPLY_POST_ID_TAG = 1;

	private DisplayUtil du;
	AsyncTask<Void, Void, Void> mTask;

	private int totalMessage;
	TextView file = null;
	ImageView file_img = null;
	String url_filename;
	String htmlmessage;
	int download_Pid = 0;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private String TAG = MessageLayoutSetting.class.getSimpleName();
	ImageFetcher imageFetcher;
	int super_user_id;

	public MessageLayoutSetting(final Context context, LinearLayout linear) {
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);

		this.context = context;
		this.baseLinearLayout = linear;
		du = new DisplayUtil(context);
		totalMessage = 0;
	}

	public int getTotalMessage() {
		return totalMessage;
	}

	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}

	/*------------------------------------R--E--P--L--Y _ V--I--E--W _  A--C--T--I--V--I--T--Y---------------------------------------------------------*/
	/*
	 * baseRelativeLayout
	 * 	|_ rl
	 * 	   |_ rl_tail
	 */
	// 댓글을 클릭했을 때의 댓글의 레이아웃(본문 제외)
	public void setReplyLayout(final Context context, SnsAppInfo snsInfo, ImageFetcher mImageFetcher) {
		this.sns_info = snsInfo;

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		RelativeLayout rl = new RelativeLayout(context); //하나의 글
		RelativeLayout rl_head = new RelativeLayout(context);
		rl_head.setId(REPLY_RL_HEAD);

		RelativeLayout rl_head_left = new RelativeLayout(context); // 상단 댓글( repler_photo, repler_name, reply_toWhom_name, etc...)
		rl_head_left.setId(REPLY_RL_HEAD_LEFT);

		RelativeLayout rl_tail = new RelativeLayout(context); // 아래의 bar (댓글달기, 삭제)
		LinearLayout linear_head_right = new LinearLayout(context);

		ImageView repler_photo = new ImageView(context);
		repler_photo.setId(REPLY_USER_IMG);
		TextView repler_name = new TextView(context);
		repler_name.setId(REPLY_USER_NAME);
		TextView reply_toWhom_name = new TextView(context);
		reply_toWhom_name.setId(REPLY_TOUSER_NAME);
		TextView reply_body = new TextView(context);
		reply_body.setId(REPLY_BODY);
		TextView reply_date = new TextView(context);
		reply_date.setId(REPLY_WRITE_DATE);
		Button go_write_reply = new Button(context);
		go_write_reply.setId(REPLY_GO_WRITE_REPLY);
		ImageButton btn_reply = new ImageButton(context);
		btn_reply.setId(TEXTVIEW_DELETE_ID);
		try{
			make_repler_photo(repler_photo, mImageFetcher, snsInfo, rl_head_left);
		}catch(NullPointerException n){
			Log.e(TAG, ""+n);
		}
		make_reply_body(repler_name, reply_toWhom_name, reply_body, reply_date, snsInfo, linear_head_right, rl_head_left.getId());
		make_reply_tail(go_write_reply, btn_reply, snsInfo, rl_tail, rl_head.getId());

		rl_head.addView(rl_head_left);
		rl_head.addView(linear_head_right);

		rl.addView(rl_head);
		rl.addView(rl_tail);

		baseLinearLayout.setLayoutParams(params);
		baseLinearLayout.addView(rl);

	}

	boolean make_reply_tail(Button go_write_reply, ImageButton btn_reply, final SnsAppInfo snsInfo, RelativeLayout parent, int upper_parent_id){
		boolean result = false;
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		RelativeLayout.LayoutParams params = null;

		// 댓글 달기 버튼
		String reply_text = "댓글";
		go_write_reply.setText(reply_text);
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.addRule(RelativeLayout.BELOW, REPLY_USER_IMG);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		go_write_reply.setLayoutParams(params);

		go_write_reply.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_REPLY_TEXT_SIZE);
		go_write_reply.setTextColor(context.getResources().getColor(R.color.thickString));

		go_write_reply.setBackgroundResource(R.drawable.reply_txt_selector);

		left = du.PixelToDP(30);
		top = du.PixelToDP(15);
		right = du.PixelToDP(30);
		bottom = du.PixelToDP(15);
		go_write_reply.setPadding(left, top, right, bottom);
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;

		go_write_reply.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					LinearLayout inputbox = (LinearLayout)v.getRootView().findViewById(R.id.inputReplyBox);
					if(inputbox.getVisibility() > 0){
						inputbox.setVisibility(0);
					}

					etReplyText = (EditText)v.getRootView().findViewById(R.id.reply_text);
					if(etReplyText != null) {
						etReplyText.setText(TO_REPLY_MARK+snsInfo.getUserName()+SEPARATOR);
						etReplyText.setTag(snsInfo);
						etReplyText.setSelection(etReplyText.length());

						etReplyText.requestFocus();
						if(etReplyText.isFocusable()) {
							InputMethodManager mInputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
							mInputMethodManager.showSoftInput(etReplyText, InputMethodManager.SHOW_IMPLICIT);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}								
			}
		});
		parent.addView(go_write_reply, REPLY_GO_WRITE_REPLY_ID);

		//		params.addRule(RelativeLayout.BELOW, REPLY_USER_IMG);
		// 삭제
		if(snsInfo.getUserId() == AppUser.user_id) {
			params = null;
			//			int delBtn_size = du.PixelToDP(MESSAGE_DEL_IMAGE_MAX_WIDTH);
			params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			left = 0;
			top = 0;
			right = 0;
			bottom = 0;
			params.setMargins(left, top, right, bottom);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);

			btn_reply.setLayoutParams(params);
			//			btn_reply.setBackgroundResource(R.drawable.button_line_with_img);

			replyDelButton(btn_reply, snsInfo.getUserId(), snsInfo.getPostId(),  REPLY_MESSAGE);
			// making delete button event

			parent.addView(btn_reply, REPLY_DELETE_BTN_ID);
		}

		RelativeLayout.LayoutParams parent_params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		parent_params.addRule(RelativeLayout.BELOW, upper_parent_id);
		parent.setLayoutParams(parent_params);

		parent.setBackgroundColor(context.getResources().getColor(R.color.bottomContentBG));

		return result;
	}

	private boolean replyDelButton(ImageButton ivDelete, final int user_id, final int post_id, final int r) {
		boolean result = false;
		ivDelete.setTag(sns_info.getPostId());
		ivDelete.setImageResource(R.drawable.navigation_cancel);
		ivDelete.setBackgroundResource(R.drawable.reply_txt_selector);
		ivDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("message delete", user_id+" / "+ post_id);

				mTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						try {
							// DB 삭제 처리 추가
							KLoungeRequest kreq = new KLoungeRequest();
							kreq.deleteMessage(user_id, post_id, r);

							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if(r == MAIN_MESSAGE) {

						} else if(r == REPLY_MESSAGE){
							for(int i=0; i<baseLinearLayout.getChildCount(); i++) {

								View tv = (View)baseLinearLayout.getChildAt(i).findViewById(TEXTVIEW_DELETE_ID);
								Integer id = 0;
								try{
									id = (Integer)tv.getTag();
								}catch(NullPointerException n){
									Log.e(TAG, "NULL POINTER EXCEPTION "+i+"st."+n);
								}
								int tag_post_id = id.intValue();
								Log.i("tag_post_id", String.valueOf(tag_post_id));
								if(tag_post_id==post_id) {
									Log.i("delete index", String.valueOf(i));
									try{
										baseLinearLayout.removeViewAt(i);
									}catch(ClassCastException c){
										c.printStackTrace();
									}

									break;
								}
							}		
						}
						mTask = null;
					}

				};

				AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);
				alertDlg.setTitle("확인");
				alertDlg.setMessage("삭제 하시겠습니까?");
				alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mTask.execute(null, null, null);
					}
				});
				alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				alertDlg.show();
			}
		});
		return result;
	}

	private boolean make_reply_body(TextView repler_name, TextView reply_toUser_name, 
			TextView reply_body, TextView reply_date, final SnsAppInfo snsInfo, LinearLayout parent, int left_parent_id) throws NullPointerException{

		int left = du.PixelToDP(0);
		int top = 0;
		int right = 0;
		int bottom = 0;
		RelativeLayout.LayoutParams params = null;
		final String img_url = snsInfo.getPhoto().replace(" ", "%20");

		boolean result = false;
		String name = snsInfo.getUserName();
		String to_user_name = snsInfo.getReply_to_user_name();
		String body =KLoungeFormatUtil.bodyURLFormat(snsInfo.getBody()).toString();
		String date = snsInfo.getWrite_date();

		// name
		repler_name.setText(name);
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);
		params.setMargins(left, top, right, bottom);
		repler_name.setLayoutParams(params);
		repler_name.setTextColor(context.getResources().getColor(R.color.link_color));
		repler_name.setBackgroundResource(R.drawable.reply_txt_selector);
		repler_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PersonalLounge.class);
				String user_id = String.valueOf(snsInfo.getUserId());
				String user_name = snsInfo.getUserName();
				String user_photo = img_url;
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);
				context.startActivity(intent);

			}
		});
		params = null;
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);

		// to user name
		//		Log.d(TAG, isToUserName(to_user_name)+"/"+to_user_name);
		if(isToUserName(to_user_name)){
			reply_toUser_name.setText("To. "+to_user_name);
			params.addRule(RelativeLayout.BELOW, repler_name.getId());
			reply_toUser_name.setLayoutParams(params);
			reply_toUser_name.setTextColor(context.getResources().getColor(R.color.bodyString));
			reply_toUser_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		}else{		
		}

		params = null;
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);


		//body
		if(isToUserName(to_user_name)){
			//			Log.d(TAG, "TRUE");
			params.addRule(RelativeLayout.BELOW, reply_toUser_name.getId());
		}else{
			//			Log.d(TAG, "FALSE");
			params.addRule(RelativeLayout.BELOW, repler_name.getId());
		}
		reply_body.setLayoutParams(params);

		reply_body.setText(Html.fromHtml(body));
		reply_body.setMovementMethod(LinkMovementMethod.getInstance());
		reply_body.setTextColor(context.getResources().getColor(R.color.slim_string));
		reply_body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

		params = null;
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);

		// date
		reply_date.setText(date);
		params.addRule(RelativeLayout.BELOW, reply_body.getId());
		reply_date.setLayoutParams(params);

		RelativeLayout.LayoutParams linearParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		parent.setOrientation(LinearLayout.VERTICAL);
		linearParams.addRule(RelativeLayout.RIGHT_OF, left_parent_id);
		parent.setLayoutParams(linearParams);

		parent.addView(repler_name, REPLY_USER_NAME_ID);
		parent.addView(reply_toUser_name, REPLY_TOUSER_NAME_ID);
		parent.addView(reply_body, REPLY_BODY_ID);
		parent.addView(reply_date, REPLY_WRITE_DATE_ID);
		parent.setBackgroundColor(context.getResources().getColor(R.color.contentBG));
		return result;
	}
	private boolean isToUserName(String toUser){
		boolean result = false;
		if(toUser.length() != 0){
			//			Log.d(TAG,toUser.length()+"");
			result = true;
		}
		return result;
	}
	private boolean make_repler_photo(ImageView repler_photo, 
			ImageFetcher imageFetcher, final SnsAppInfo snsInfo, RelativeLayout parent) throws NullPointerException{

		boolean result = false;
		int imgView_size = du.PixelToDP(MESSAGE_IMAGE_MAX_WIDTH);
		//		int imgView_height = du.PixelToDP(MESSAGE_IMAGE_MAX_HEIGHT);
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(imgView_size, imgView_size);
		int left, top, right, bottom;

		final String img_url = snsInfo.getPhoto().replace(" ", "%20");

		left = du.PixelToDP(5);
		top = du.PixelToDP(5);
		right = du.PixelToDP(5);
		bottom = du.PixelToDP(5);

		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		imageFetcher.loadImage(img_url, repler_photo);

		repler_photo.setAdjustViewBounds(true);
		repler_photo.setLayoutParams(params);
		repler_photo.setScaleType(ImageView.ScaleType.FIT_XY);

		repler_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PersonalLounge.class);
				String user_id = String.valueOf(snsInfo.getUserId());
				String user_name = snsInfo.getUserName();
				String user_photo = img_url;
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);
				context.startActivity(intent);
			}
		});
		parent.addView(repler_photo, REPLY_USER_IMG_ID);
		parent.setBackgroundColor(context.getResources().getColor(R.color.contentBG));
		return result;
	}
	
	/*-------------------------------------------------------------------------------------------------*/

	public void setMessageContentUsingRelativeLayout(final SnsAppInfo snsinfo, 
			ImageFetcher mImageFetcher,final int puser_id, final int group_id){
		LayoutInflater inflater = null;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rl = (RelativeLayout)inflater.inflate(R.layout.one_content, null);

		// puser_id : 만일 personal lounge 에서 넘어 왔다면 puser_id > 0 일 것이다.
		// 이 값이 snsinfo.getUserId() 와 같다면 사진 눌러도 PersonalLounge 로 이동 하지 않을 것이다.
		imageFetcher = mImageFetcher;
		this.super_user_id = puser_id;
		
		// 사용자 사진
		final ImageView user_photo = (ImageView)rl.findViewById(R.id.user_image_view);
		final String imageurl = snsinfo.getPhoto().replace(" ", "%20");
		if(imageurl.contains("/images/sns/no_photo_small.gif")){
			user_photo.setImageResource(R.drawable.no_photo);
		}else{
			mImageFetcher.setLoadingImage(R.drawable.no_photo);
			mImageFetcher.loadImage(imageurl, user_photo);
		}
		user_photo.setTag(R.id.arg1, snsinfo);
		user_photo.setTag(R.id.arg2, puser_id);
		user_photo.setOnClickListener(user_photo_click_listener);

		// 사용자 이름
		TextView name = (TextView)rl.findViewById(R.id.writer_name);
		name.setText(snsinfo.getUserName());
		name.setTag(R.id.arg1, snsinfo);
		name.setTag(R.id.arg2, puser_id);
		name.setOnClickListener(user_photo_click_listener);

		// 글 등록 날짜
		TextView date = (TextView)rl.findViewById(R.id.written_date);
		date.setText(snsinfo.getWrite_date());

		// 메시지 바디
		TextView msg = (TextView)rl.findViewById(R.id.msg_body);
		htmlmessage = KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();
		if(htmlmessage.contains(FLAG_IF_REDIRECT_LINK)){
			// redirectLink 이면서 ck 가 (3 or 4) 일 때 즉, dataListView.jsp 를 요구 할 때
			// URL 로 가는게 아닌, 파일 다운로드를 직접 할 것을 요구함.
			String post_id = null;
			String ck = null;
			try{
				post_id = htmlmessage.substring(htmlmessage.indexOf("?p_id=")+6, htmlmessage.indexOf("&check"));
				ck = htmlmessage.substring(htmlmessage.indexOf("&check=")+7, htmlmessage.indexOf("&group_id"));
			}catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
			}
			if(post_id !=null){
				try{
					download_Pid = Integer.parseInt(post_id);
				}catch(NumberFormatException e2){
					e2.printStackTrace();
				}
			}
			int intCk = -1;
			try{
				intCk = Integer.parseInt(ck);
			}catch(NumberFormatException e1){
				e1.printStackTrace();
			}
			if(intCk == 3 || intCk==4){
				LinearLayout attachLayout = (LinearLayout)rl.findViewById(R.id.attach_layout);
				attachLayout.setVisibility(View.VISIBLE);

				attachLayout.setTag(download_Pid);
				attachLayout.setOnClickListener(attach_layout_click_listener);

				String displayMsg = htmlmessage;
				displayMsg = displayMsg.replaceAll("<[^>]*>","");
				displayMsg = displayMsg.replaceAll("&nbsp;", "");
				displayMsg = displayMsg.replaceAll("&amp;", "&");
				displayMsg = displayMsg.replaceAll("&gt;", "<");
				displayMsg = displayMsg.replaceAll("&lt;", ">");
				msg.setText(displayMsg);
			}
		}
		else{
			msg.setText(Html.fromHtml(htmlmessage));
			msg.setMovementMethod(LinkMovementMethod.getInstance());
		}

		// 첨부된 사진
		FrameLayout fl = (FrameLayout)rl.findViewById(R.id.added_img_frame);

		ImageView added_image = (ImageView)rl.findViewById(R.id.added_img);
		final String added_imageurl = snsinfo.getPhotoVideo().replace(" ", "%20");
		
		final ImageButton play_video_btn = (ImageButton)rl.findViewById(R.id.added_play_button);
		
		if(added_imageurl != null && !added_imageurl.equals("")) {
			fl.setVisibility(View.VISIBLE);
			added_image.setVisibility(View.VISIBLE);
			mImageFetcher.setLoadingImage(R.drawable.no_picture);
			
			if(added_imageurl.contains(".flv")){// 동영상 파일 일 때
				play_video_btn.setVisibility(View.VISIBLE);
				String flv_thumb = added_imageurl.replace("/video/", "/image/");
				flv_thumb = flv_thumb.replace(".flv", ".png");
				mImageFetcher.loadImage(flv_thumb, added_image);
			}else{ // image file 일 때
				mImageFetcher.setImageSize(context.getResources().getDimensionPixelSize(R.dimen.big_image_size));
				mImageFetcher.loadImage(added_imageurl, added_image);
			}

			/*	*	*	Listener Add Setting (it depends on data type( FLV or IMG )*	*	*/
			if(added_imageurl.contains(".flv")){// 동영상 파일 일 때
				added_image.setTag(added_imageurl);
				play_video_btn.setTag(added_imageurl);
				
				added_image.setOnClickListener(play_video_click_listener);
				play_video_btn.setOnClickListener(play_video_click_listener);
			}else{
				AppClickListener acListener = new AppClickListener(snsinfo, context);//new AppClickListener(added_imageurl, replyCount, htmlmessage);
				added_image.setOnClickListener(acListener);
			}
		}
		
		// 댓글
		LinearLayout replyLayout = (LinearLayout)rl.findViewById(R.id.reply_layout);
		replyLayout.setTag(snsinfo);
		replyLayout.setOnClickListener(reply_click_listener);
		
		TextView tvReply = (TextView)rl.findViewById(R.id.reply_view);
		int replyCount = snsinfo.getReply_count();
		tvReply.setText("댓글("+replyCount+")");

		BadgeView badge = new BadgeView(context, replyLayout);
		badge.setId(REPLY_BADGEVIEW_ID);
		badge.setText("N");
		badge.setTextSize(10);
		badge.setTag(snsinfo.getPostId());
		for(NewMessage n: AppUser.NEW_MESSAGE){
			if(n.getSuper_id() == snsinfo.getPostId()){
				badge.show();
			}
		}

		// 첨부파일
		final String attach = snsinfo.getAttach();
		if(!attach.equals("") && !attach.equals("null")) {
			LinearLayout attachLayout = (LinearLayout)rl.findViewById(R.id.attach_layout);
			attachLayout.setVisibility(View.VISIBLE);
			attachLayout.setTag(snsinfo);
			// 첨부파일 다운로드
			attachLayout.setOnClickListener(file_click_listener);
		}

		// 삭제
		if(AppUser.user_id == snsinfo.getUserId()) {
			ImageButton ivDelete = (ImageButton)rl.findViewById(R.id.delete_btn);
			ivDelete.setVisibility(View.VISIBLE);

			ivDelete.setTag(R.id.arg1, snsinfo);
			ivDelete.setTag(R.id.arg2, group_id);
			ivDelete.setOnClickListener(delete_click_listener);
		}
		
		baseLinearLayout.addView(rl);
	}

	private class GetFileTask extends AsyncTask<Integer, Void, PostAttachHolder>{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(context, "",
					"로딩 중..", true);
			//dialog.show();
			super.onPreExecute();
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(PostAttachHolder result) {
			// AttachedDownloadManager context 를 열어서 사용자에게 Dialog 제공
			String filename = result.getFileName();
			Bundle bun = new Bundle();
			bun.putString("notiMessage", "다운로드 하시겠습니까?");
			//Log.i("HTML", bundle_filename);
			bun.putInt("p_id", result.getPost_id());
			bun.putInt("user_id", Integer.parseInt("1"));
			bun.putInt("ck", MAIN_MESSAGE*0);
			bun.putString("filename", filename);
			bun.putBoolean("url_linker", true);
			Intent popupIntent = new Intent(context, AttachedDownloadManager.class);
			popupIntent.putExtras(bun);
			PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
			try{
				pi.send();
			}catch(CanceledException e){
				Log.e(TAG, "Cancel Exception -"+e);
			}
			dialog.dismiss();
			//			super.onPostExecute(result);
		}

		@Override
		protected PostAttachHolder doInBackground(Integer... arg) {
			int p_id = arg[0];
			String filename = null;
			PostAttachHolder result = new PostAttachHolder();
			KLoungeRequest kreq = new KLoungeRequest();

			filename = kreq.getFileName(p_id);

			result.setPost_id(p_id);
			result.setFileName(filename);

			if(filename.equals("") || filename.length() == 0){
				Log.d(TAG, "no file");
				return null;
			}

			return result;
		}
	}
	public void playVideoUsingSystemPlayer(Context c, String video_url){
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(video_url), "video/flv");
		c.startActivity(i);
	}
	private void playVideo(final String added_imageurl){
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(1).getState();
		State mobile = conMan.getNetworkInfo(0).getState(); //mobile
		if (!(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)) {

			/* wi-fi */
			StringBuffer showMessage = new StringBuffer();

			if(!(mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING)){
				showMessage.append("사용 가능한 네트워크 환경이 준비되어 있지 않습니다. 계속 하시겠습니까?\n");
			}else{
				showMessage.append("3G/4G로 접속 중입니다. 동영상을 재생하시겠습니까?\n");
				/* wi-fi 문구*/
			}

			alertDialog.setMessage(
					showMessage.toString()
					);

			alertDialog.setPositiveButton("네", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if(AppConfig.DEBUG)Log.d(TAG, added_imageurl);
					playVideoUsingSystemPlayer(context, added_imageurl);
				}
			});
			alertDialog.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					//do nothing
				}
			});
			if(AppConfig.DEBUG)Log.d(TAG, "no WiFi");
			alertDialog.show();
		}else{
			if(AppConfig.DEBUG)Log.d(TAG, "WIFI");
			playVideoUsingSystemPlayer(context, added_imageurl);
		}
	}

	private class DisplayUtil {
		private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

		private final float scale;

		public DisplayUtil(Context context) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		public int PixelToDP(int pixel) {
			return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
		}
		public int DPToPixel(final Context context, int DP) {
			return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
		}
	}

	class DeleteTask extends AsyncTask<Void, Void, Boolean>{
		SnsAppInfo snsinfo;
		int cur_group_id;
		DeleteTask(SnsAppInfo sinfo, int group_id){
			cur_group_id = group_id;
			snsinfo = sinfo;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// DB 삭제 처리 추가
			KLoungeRequest kreq = new KLoungeRequest();
			return kreq.deleteMessage(AppUser.user_id, snsinfo.getPostId(), MAIN_MESSAGE);
		}
		@Override
		protected void onPostExecute(Boolean result) {
			// 메인과 답글에 따라 새롭게 메시지 리스트 가져오기
			int childCount = baseLinearLayout.getChildCount();
			
			for(int i=0; i<childCount; i++) {
				View tv = (View)baseLinearLayout.getChildAt(i).findViewById(R.id.delete_btn);
				Integer id = 0;//(Integer)tv.getTag();
				try{
					SnsAppInfo sinfo = (SnsAppInfo)tv.getTag(R.id.arg1);
					id = sinfo.getPostId();
				}catch(NullPointerException e){
					e.printStackTrace();
				}
				int tag_post_id = id.intValue();
				if(tag_post_id==snsinfo.getPostId()) {
					baseLinearLayout.removeViewAt(i);
					break;
				}
			}
			if(childCount == 3){
				// 글이 거의 다 지워 졌을 때 서버에서 새로 글을 받아 온다.
				// 꼭 필요한건 아닌데, 10개 이상을 한꺼번에 지울 때 작동이 이상해지기 때문에
				// 있으면 좋다.
				new LoadMoreListTask().execute(cur_group_id);
			}
		}
		class LoadMoreListTask extends AsyncTask<Integer, Void, List<SnsAppInfo>>{
			private List<SnsAppInfo> messageList;
			private KLoungeRequest kloungehttp;
			private KLoungeHttpRequest httprequest;
			private int load_more_list_group_id;
			public LoadMoreListTask() {
				messageList = new ArrayList<SnsAppInfo>();
				kloungehttp = new KLoungeRequest();
				httprequest = new KLoungeHttpRequest();
			}
			@Override
			protected List<SnsAppInfo> doInBackground(Integer... gid) {
				load_more_list_group_id = gid[0];

				try {
					StringBuffer remote_addr = new StringBuffer();
					remote_addr.append(httprequest.getService_URL())
							.append("/mobile/appdbbroker/appKLounge.jsp")
							.append("?")
							.append("user_id="+AppUser.user_id)
							.append("&group_id="+gid[0])
							.append("&reload="+0)
							.append("&puser_id="+super_user_id);
							
					//check
					if(context instanceof PersonalLounge){
						remote_addr.append("&type=personallounge");
					}else if(context instanceof MyLounge){
						remote_addr.append("&type=mylounge");
					}
//					else{
//						그룹 SNS 에서 메시지 새로 받아 올 때
//					}
					
					StringBuilder json = new StringBuilder();
					try {
						URL url = new URL(remote_addr.toString());

						HttpURLConnection conn = (HttpURLConnection)url.openConnection();
						if(conn != null) {
							conn.setConnectTimeout(3000);
							conn.setUseCaches(false);
							if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
								BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
								for(;;){
									String line = br.readLine();
									if(line == null) {
										break;
									}
									if(isCancelled()){
										br.close();
										conn.disconnect();
										return null;
									}
									json.append(line);
								}
								br.close();
							}
							conn.disconnect();
						}
					}catch (MalformedURLException e) {
						e.printStackTrace();
						return null;
					}catch(SocketTimeoutException s){
						return null;
					}catch(IOException i){
						return null;
					}
					messageList = kloungehttp.parseStrJSON(json.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				return messageList;
			}

			@Override
			protected void onCancelled() {

				super.onCancelled();
			}

			@Override
			protected void onPostExecute(List<SnsAppInfo> result) {
//				Log.d(TAG, "onpost: "+ result.size());
				for(int i=2;i<result.size();i++){
					setMessageContentUsingRelativeLayout(result.get(i),
							imageFetcher, super_user_id, load_more_list_group_id);
				}
				super.onPostExecute(result);
			}

		}
	}
	View.OnClickListener delete_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final SnsAppInfo snsinfo = (SnsAppInfo)v.getTag(R.id.arg1);
			final int group_id = (Integer)v.getTag(R.id.arg2); 
			AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);
			alertDlg.setTitle("확인");
			alertDlg.setMessage("삭제 하시겠습니까?");
			alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try{
						new DeleteTask(snsinfo, group_id).execute();
					}catch(NullPointerException ne){
						ne.printStackTrace();
					}
				}
			});
			alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			alertDlg.show();
		}
	};
	
	View.OnClickListener file_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SnsAppInfo snsinfo = (SnsAppInfo)v.getTag();
			String attach = snsinfo.getAttach();
			vibrator.vibrate(VIBRATE_PERIOD);
			Bundle bun = new Bundle();
			bun.putString("notiMessage", "다운로드 하시겠습니까?");
			bun.putInt("p_id", snsinfo.getPostId());
			bun.putInt("user_id", snsinfo.getUserId());
			bun.putInt("ck", MAIN_MESSAGE*0);
			bun.putString("filename", attach);
			Intent popupIntent = new Intent(context, AttachedDownloadManager.class);

			popupIntent.putExtras(bun);

			PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
			try{
				pi.send();
			}catch(Exception e){
				Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			}
		}
	};
	View.OnClickListener reply_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			SnsAppInfo snsinfo = (SnsAppInfo)v.getTag();
			vibrator.vibrate(VIBRATE_PERIOD);
			// 새로운 창 추가
			try {
				BadgeView badge = ((BadgeView)baseLinearLayout.findViewById(REPLY_BADGEVIEW_ID));
				if(badge.getVisibility() == View.VISIBLE){
					badge.setVisibility(View.INVISIBLE);
					for(int index=0;index<AppUser.NEW_MESSAGE.size();){
						int post_id = (Integer)badge.getTag();
						if(post_id == AppUser.NEW_MESSAGE.get(index).getSuper_id()
								&& AppUser.NEW_MESSAGE.get(index).getType1().equals("reply")
								&& AppUser.NEW_MESSAGE.get(index).getType2().equals("my")){
							// Remove badge
							Log.d(TAG, "badge removed");
							AppUser.NEW_MESSAGE.remove(index);
						}
					}
				}
				Intent intent = new Intent(context, ReplyViewDialog.class);
				intent.putExtra("snsAppInfo", snsinfo);
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	View.OnClickListener play_video_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String added_imageurl = (String)v.getTag();
			playVideo(added_imageurl);
		}
	};
	View.OnClickListener attach_layout_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int pid = (Integer)v.getTag();
			vibrator.vibrate(VIBRATE_PERIOD);
			new GetFileTask().execute(pid);
		}
	};
	View.OnClickListener user_photo_click_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!context.getClass().equals(PersonalLounge.class)){
				SnsAppInfo snsinfo = (SnsAppInfo)v.getTag(R.id.arg1);
				int puser_id = (Integer)v.getTag(R.id.arg2);
				String imageurl = snsinfo.getPhoto().replace(" ", "%20");

				Intent intent = new Intent(context, PersonalLounge.class);
				String user_id = String.valueOf(snsinfo.getUserId());
				String user_name = snsinfo.getUserName();
				String user_photo = imageurl;

				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);

				if(AppUser.user_id == snsinfo.getUserId()){
					// User 가 자신의 라운지에 가고자 할 때
					if(puser_id > 0){
						// 어떤 다른 사람의 Personal Lounge 에서 내 사진을 눌렀을 때
						((Activity)context).finish();
					}
					AppUser.SHARED_GROUPID = snsinfo.getGroupId();
					KLoungeActivity.tabActivity.getTabHost().setCurrentTab(AppUser.MYLOUNGE_TAB);
				}else{
					if(snsinfo.getUserId() == puser_id){
						Toast.makeText(context, "현재 "+user_name+" 님의 라운지에 위치 해 있습니다.", Toast.LENGTH_SHORT).show();
					}else{
						context.startActivity(intent);
					}
				}
			}
		}
	};
}
