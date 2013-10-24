package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.IMAGE_CACHE_DIR;

import java.util.List;
import java.util.concurrent.ExecutionException;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.PostAttachHolder;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.model.SnsInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.util.LoadImageUtil;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.ImageView.ScaleType;

public class ReplyActivity extends FragmentActivity {
	private final String SAVED_INSTANCE_SNS_INFO = this.toString()+"SNS_INFO";
	private final String SAVED_INSTANCE_USER_ID = this.toString()+"USER_ID";
	private final String SAVED_INSTANCE_REPLY = this.toString()+"REPLY";

	String TAG = ReplyActivity.class.getSimpleName();
	final String MESSAGE_REPLY_TYPE = "reply";
	final char TO_REPLY_MARK = '@';
	final String SEPARATOR= " ";
	final int REPLY_POST_ID_TAG = 1;
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private EditText reply_text;
	private Button send_reply_btn;
	private ImageView imageView;
	SnsAppInfo snsinfo = null;
	private String reply_body = null;
	private ImageFetcher mImageFetcher;
	AsyncTask<Integer, Void, List<SnsAppInfo>> mTask;
	AsyncTask<Integer, Void, Void> mWriteTask;
	LinearLayout baseLayout;
	KLoungeRequest kloungehttp;
	private Context context;
	LoadImageUtil imageUtil;
	DisplayUtil du;
	TextView attachFileText;
	private int mImageThumbSize;
	boolean NAV_DOWN = true;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private TextView titleTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(AppUser.mImageFetcher != null){
			// 정상적 사용자
			mImageFetcher = AppUser.mImageFetcher;
		}else{
			// 다른 App 쓰다가 돌아온 사용자
			mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
			mImageFetcher = new ImageFetcher(context, mImageThumbSize);
			mImageFetcher.setLoadingImage(R.drawable.no_photo);
			AppUser.mImageFetcher = mImageFetcher;
			ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.25f); 
			mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		}

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.view_reply);

		imageUtil = new LoadImageUtil();
		context = getApplicationContext();

		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

		//custom title bar
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		titleTextView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		findViewById(R.id.reply_main_message).setBackgroundColor(Color.parseColor("#FFFFFFFF"));
		findViewById(R.id.reply_information).setBackgroundColor(Color.parseColor("#FFFFFFFF"));

		makeMainView();
		makeReplyView();

		getIntent().removeExtra("snsAppInfo");
	}
	void makeReplyView(){
		// 답글 출력하기
		kloungehttp = new KLoungeRequest();
		baseLayout = (LinearLayout)findViewById(R.id.reply_message);

		mTask = new AsyncTask<Integer, Void, List<SnsAppInfo>>() {
			List<SnsAppInfo> replyList;

			@Override
			protected List<SnsAppInfo> doInBackground(Integer... params) {
				//				Log.d(TAG, "post id: "+params[0]);
				try {
					if(snsinfo.getSuperId() > 0){ 
						replyList = kloungehttp.getReplyMessageList(snsinfo.getSuperId());
					}else{ 
						replyList = kloungehttp.getReplyMessageList(snsinfo.getPostId());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return replyList;
			}

			@Override
			protected void onPostExecute(List<SnsAppInfo> result) {

				baseLayout.removeAllViews();
				MessageLayoutSetting mls = new MessageLayoutSetting(ReplyActivity.this, baseLayout);
				if(result.size() > 0) {
					for(int i=0; i<result.size(); i++) {

						SnsAppInfo sInfo = result.get(i);
//						mls.setReplyLayout(ReplyActivity.this, sInfo, mImageFetcher);
					}
				}
				mTask = null;
			}
		};
		mTask.execute(snsinfo.getPostId());

		reply_text = (EditText)findViewById(R.id.reply_text);

		send_reply_btn = (Button)findViewById(R.id.send_reply);
//		send_reply_btn.setBackgroundResource(R.drawable.btn_disabled);
		send_reply_btn.setEnabled(false);
		send_reply_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//KLoungeRequest kreq = new KLoungeRequest();
				snsinfo.setPuser_id(snsinfo.getUserId());

				reply_body = reply_text.getText().toString();
				if(reply_text != null || reply_text.length() != 0){
					int reply_post_id = 0;
					try{
						if(reply_body.charAt(0) == TO_REPLY_MARK) {
							SnsInfo si = (SnsInfo)reply_text.getTag();
							if(si != null) reply_post_id = si.getPostId();
							reply_body = reply_body.substring(reply_body.indexOf(SEPARATOR)+1);
						}
					}catch(StringIndexOutOfBoundsException s){
						s.printStackTrace();
					}

					mWriteTask = new AsyncTask<Integer, Void, Void>() {
						List<SnsAppInfo> replyList;
						ProgressDialog progress;
						
						@Override
						protected void onPreExecute() {
							// TODO Auto-generated method stub
							super.onPreExecute();
							progress = new ProgressDialog(ReplyActivity.this);
							progress.setTitle(null);
							progress.setMessage(context.getResources().getString(R.string.loading_msg));
							progress.setIndeterminate(true);
							progress.setCancelable(true);
							progress.show();
						}
						@Override
						protected Void doInBackground(Integer... params) {
							try {
								final int reply_post_id = params[0].intValue();

								if(reply_post_id > 0 ) {
									kloungehttp.sendMessage(snsinfo.getGroupId(), 
											AppUser.user_id, reply_body, reply_post_id, 
											MESSAGE_REPLY_TYPE, snsinfo.getPuser_id(),
											AppUser.user_name, AppUser.user_photo);
								}else {
									kloungehttp.sendMessage(snsinfo.getGroupId(), 
											AppUser.user_id, reply_body, snsinfo.getPostId(), 
											"", snsinfo.getPuser_id(),
											AppUser.user_name, AppUser.user_photo);
								}

								replyList = kloungehttp.getReplyMessageList(snsinfo.getPostId());
							} catch (Exception e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							progress.dismiss();
							
							baseLayout.removeAllViews();
							
							MessageLayoutSetting mls = new MessageLayoutSetting(ReplyActivity.this, baseLayout);
							if(replyList.size() > 0) {
								for(int i=0; i<replyList.size(); i++) {
									SnsAppInfo sInfo = replyList.get(i);
//									mls.setReplyLayout(ReplyActivity.this, sInfo, mImageFetcher);
								}
							}
							
							if(reply_text.isFocusable()) {
								reply_text.setText("");
								reply_text.clearFocus();
								InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(reply_text.getWindowToken(),0);
							}
							Toast.makeText(ReplyActivity.this, "댓글이 등록되었습니다.", LENGTH_TO_SHOW).show();
							mWriteTask = null;
						}

					};
					mWriteTask.execute(reply_post_id, null, null);
					
				}else{
					Toast.makeText(ReplyActivity.this, "글을 입력 해 주세요.", LENGTH_TO_SHOW).show();
				}
			}
		});
		reply_text.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() == 0){
//					send_reply_btn.setBackgroundResource(R.drawable.btn_disabled);
					send_reply_btn.setEnabled(false);
				}else if(s.length() > 0){
//					send_reply_btn.setBackgroundResource(R.drawable.btn_send);
					send_reply_btn.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		reply_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				v.clearFocus();
				return false;
			}
		});

	}

	void makeMainView(){

		FrameLayout fl = (FrameLayout)findViewById(R.id.mov_file_layout);
		ImageView ivMainMessageAttachedPhoto = (ImageView)findViewById(R.id.reply_imageview);
		ImageView play_video_btn = (ImageView)findViewById(R.id.mov_play_btn);
		View mov_views[]={fl, ivMainMessageAttachedPhoto, play_video_btn};

		FrameLayout attach_fl = (FrameLayout)findViewById(R.id.attach_file_layout);
		ImageView attachFileBtn = (ImageView)findViewById(R.id.reply_attach_image);
		attachFileText = (TextView)findViewById(R.id.reply_attach_text);

		FrameLayout attach_inform = (FrameLayout)findViewById(R.id.attach_file_inform);
		ImageView attach_inform_btn = (ImageView)findViewById(R.id.reply_attach_nav_btn);
		TextView attach_inform_txt = (TextView)findViewById(R.id.reply_attach_nav_text);

		View attach_views[] ={
				attach_inform, attach_inform_btn, attach_inform_txt,
				attach_fl, attachFileBtn, attachFileText
		};

		TextView tvMainMessageUserName = (TextView)findViewById(R.id.main_message_user_name);
		TextView tvMainMessageWriteDate = (TextView)findViewById(R.id.main_message_date);
		ImageView ivMainMessageUserPhoto = (ImageView)findViewById(R.id.user_image_view);
		TextView tvMainMessageBody = (TextView)findViewById(R.id.main_message_body);
		tvMainMessageBody.setBackgroundColor(Color.parseColor("#ffffffff"));
		// 메인 메시지 정보 받아오기
		Bundle bundle = getIntent().getExtras();
		if(bundle == null){
			finish();
		}
		snsinfo = bundle.getParcelable("snsAppInfo");

		String user_name = snsinfo.getUserName();
		String write_date = snsinfo.getWrite_date();
		String photo_video = snsinfo.getPhotoVideo();
		String user_photo = snsinfo.getPhoto();
		String body = snsinfo.getBody();
		String attached_file = snsinfo.getAttach();
		int puser_id = snsinfo.getPuser_id();
		int post_id = snsinfo.getPostId();
		int user_id = snsinfo.getUserId();

		// 이름
		tvMainMessageUserName.setText(user_name);

		// 글 쓴 날짜
		tvMainMessageWriteDate.setText(write_date);

		// Image 
		final String attached_img_url = photo_video;
		if(attached_img_url != null && !attached_img_url.equals("")) {
			int left = 0;
			int top = CommonUtilities.DPFromPixel(this, 5);
			int right = 0;
			int bottom = CommonUtilities.DPFromPixel(this, 10);
			ivMainMessageAttachedPhoto.setPadding(left, top, right, bottom);
			ivMainMessageAttachedPhoto.setBackgroundColor(getResources().getColor(R.color.contentBG));

			//imagedownloader.download(img_url, ivMainMessageAttachedPhoto);
			if(attached_img_url.contains(".flv")){
				setViewsVisibilities(mov_views, View.VISIBLE);
				/*
				fl.setVisibility(View.VISIBLE);
				play_video_btn.setVisibility(View.VISIBLE);
				ivMainMessageAttachedPhoto.setVisibility(View.VISIBLE);
				 */
				if(AppConfig.DEBUG)Log.d(TAG, attached_img_url);
				String flv_thumb = attached_img_url.replace("/video/", "/image/");
				flv_thumb = flv_thumb.replace(".flv", ".png");
				if(AppConfig.DEBUG)Log.d(TAG, flv_thumb);

				mImageFetcher.loadImage(flv_thumb, ivMainMessageAttachedPhoto);

				play_video_btn.setBackgroundResource(R.drawable.xh_play_over_video);

			}else{
				//				setViewsVisibilities(mov_views, View.GONE);

				mImageFetcher.setLoadingImage(R.drawable.no_picture);
				mImageFetcher.loadImage(attached_img_url, ivMainMessageAttachedPhoto);
			}
			ivMainMessageAttachedPhoto.setScaleType(ScaleType.FIT_START);
			FrameLayout.LayoutParams ivParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ivParams.gravity = Gravity.CENTER;
			left = 0;
			top = 0;
			right = 0;
			bottom = CommonUtilities.DPFromPixel(this, 5);
			ivParams.setMargins(left, top, right, bottom);
			ivMainMessageAttachedPhoto.setLayoutParams(ivParams);
			if(attached_img_url.contains(".flv")){
				final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReplyActivity.this);

				/* wi-fi */
				ivMainMessageAttachedPhoto.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
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
									if(AppConfig.DEBUG)Log.d(TAG, attached_img_url);
									playVideo(ReplyActivity.this, attached_img_url);
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
							playVideo(ReplyActivity.this, attached_img_url);
						}
					}
				});
			}else{
				kr.co.ktech.cse.listener.AppClickListener acListener = new kr.co.ktech.cse.listener.AppClickListener(snsinfo, this);//new AppClickListener(added_imageurl, replyCount, htmlmessage);
				ivMainMessageAttachedPhoto.setOnClickListener(acListener);
			}
		}

		String user_imgUrl = user_photo.toString().replace(" ", "%20");

		imageUtil.loadImage(ivMainMessageUserPhoto, user_imgUrl, puser_id);

		//		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		//		mImageFetcher.loadImage(user_imgUrl,ivMainMessageUserPhoto);

		du = new DisplayUtil(context);
		FrameLayout.LayoutParams photoParams = new FrameLayout.LayoutParams(du.PixelToDP(100),du.PixelToDP(100));
		photoParams.setMargins(du.PixelToDP(8), 0, 0, 0);
		ivMainMessageUserPhoto.setLayoutParams(photoParams);

		ivMainMessageUserPhoto.setScaleType(ScaleType.FIT_XY);

		String htmlmessage = KLoungeFormatUtil.bodyURLFormat(body).toString();
		//첨부 파일
		if(htmlmessage.contains(CommonUtilities.FLAG_IF_REDIRECT_LINK)){

			String strPost_id = null;
			String ck = null;
			try{
				strPost_id = htmlmessage.substring(htmlmessage.indexOf("?p_id=")+6, htmlmessage.indexOf("&check"));
				ck = htmlmessage.substring(htmlmessage.indexOf("&check=")+7, htmlmessage.indexOf("&group_id"));
			}catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
			}
			int intCk = -1;
			int intPid = -1;
			try{
				intCk = Integer.parseInt(ck);
				intPid = Integer.parseInt(strPost_id);
			}catch(NumberFormatException e1){
				e1.printStackTrace();
			}
			if(intCk == 3|| intCk ==4){

				makeAttachInformViews(intPid, intCk, attach_views);

				tvMainMessageBody.setText(removeTags(htmlmessage));

			}else{
				tvMainMessageBody.setText(Html.fromHtml(htmlmessage));
				tvMainMessageBody.invalidate();
			}
		}else if(!attached_file.equals("") && !attached_file.equals("null")){
			if(AppConfig.DEBUG)Log.d(TAG, "Attach File");
			makeAttachInformViews(post_id, 0 , user_id, attached_file, attach_views);
			tvMainMessageBody.setText(Html.fromHtml(htmlmessage));
			tvMainMessageBody.invalidate();
		}else{
			setViewsVisibilities(attach_views, View.GONE);

			tvMainMessageBody.setText(Html.fromHtml(htmlmessage));
			tvMainMessageBody.invalidate();
		}

	}

	private void setViewsVisibilities(View[] views, int visibility){
		for(View v:views){
			v.setVisibility(visibility);
		}
	}

	void setClickListener(final int post_id, final String fileName, FrameLayout layout)throws PendingIntent.CanceledException{

		layout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				Bundle bun = new Bundle();
				bun.putString("notiMessage", "다운로드 하시겠습니까?");

				//Log.i("HTML", bundle_filename);
				bun.putInt("p_id", post_id);
				bun.putInt("user_id", Integer.parseInt("1"));
				bun.putInt("ck", 0);
				bun.putString("filename", fileName);
				bun.putBoolean("url_linker", true);
				Intent popupIntent = new Intent(context, AttachedDownloadManager.class);

				popupIntent.putExtras(bun);

				final PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);

				try{
					pi.send();
				}catch(CanceledException e){
					Log.e(TAG, "Cancel Exception -"+e);
				}
			}
		});
	}

	private void makeAttachInformViews(final int post_id, int ck, final View... args){

		setViewsVisibilities(args, View.VISIBLE);

		FrameLayout fl_inform = (FrameLayout)args[0];
		final ImageView attach_inform_img = (ImageView)args[1];
//		TextView attach_inform_txt = (TextView)args[2];
		final FrameLayout fl_file = (FrameLayout)args[3];
		final ImageView attach_img = (ImageView)args[4];
		final TextView attach_txt = (TextView)args[5];

		fl_file.setVisibility(View.INVISIBLE);
		attach_img.setBackgroundResource(R.drawable.file);
		attach_img.setVisibility(View.INVISIBLE);

		FileDownloadTask asyncTask = new FileDownloadTask();

		try {
			final String actual_filename = asyncTask.execute(post_id, attach_txt).get().getFileName();
			if(AppConfig.DEBUG)Log.d(TAG,actual_filename);

			attach_txt.setVisibility(View.INVISIBLE);
			fl_inform.setBackgroundResource(R.drawable.reply_txt_selector);
			fl_inform.setOnClickListener(new View.OnClickListener() {

				View file_views[] = {args[3], attach_img, attach_txt};
				@Override
				public void onClick(View v) {
					if(NAV_DOWN){
						vibrator.vibrate(VIBRATE_PERIOD);
						attach_inform_img.setBackgroundResource(R.drawable.navigation_up_item);	
						setViewsVisibilities( file_views , View.VISIBLE);
						try {
							setClickListener(post_id, actual_filename, fl_file);
						} catch (CanceledException e) {
							Log.e(TAG, "CanceledException - "+e);
						}
						NAV_DOWN = false;
					}else{
						vibrator.vibrate(VIBRATE_PERIOD);
						attach_inform_img.setBackgroundResource(R.drawable.navigation_down_item);
						setViewsVisibilities( file_views , View.INVISIBLE);
						NAV_DOWN = true;
					}
				}
			});
		} catch (InterruptedException e) {
			Log.e(TAG, "InterruptedException - "+e);
		} catch (ExecutionException e) {
			Log.e(TAG, "ExecutionException - "+e);
		}
		if(AppConfig.DEBUG)Log.d(TAG, "makeAttachInformViews");
	}
	private void makeAttachInformViews(final int post_id,final int ck, final int user_id, final String fileName, final View... args){

		setViewsVisibilities(args, View.VISIBLE);

		FrameLayout fl_inform = (FrameLayout)args[0];
		final ImageView attach_inform_img = (ImageView)args[1];
//		TextView attach_inform_txt = (TextView)args[2];

		final FrameLayout fl_file = (FrameLayout)args[3];
		final ImageView attach_img = (ImageView)args[4];
		final TextView attach_txt = (TextView)args[5];

		fl_file.setVisibility(View.INVISIBLE);
		fl_file.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				Bundle bun = new Bundle();
				bun.putString("notiMessage", "다운로드 하시겠습니까?");
				bun.putInt("p_id", post_id);
				bun.putInt("user_id", user_id);
				bun.putInt("ck", ck*0);
				bun.putString("filename", fileName);
				Intent popupIntent = new Intent(context, AttachedDownloadManager.class);

				popupIntent.putExtras(bun);

				PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
				try{
					pi.send();
				}catch(Exception e){
					Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
				}
			}
		});
		attach_img.setBackgroundResource(R.drawable.file);
		attach_img.setVisibility(View.INVISIBLE);
		String user_show_filename ="";
		int display_txt_length = 15; 

		Log.d(TAG, display_txt_length +"");

		if(fileName.length() > display_txt_length){
			user_show_filename = fileName.substring(0, display_txt_length) + "...";
		}else{
			user_show_filename = fileName;
		}
		//		new FileDownloadTask().execute(post_id, attach_txt);
		attach_txt.setText(user_show_filename);

		attach_txt.setVisibility(View.INVISIBLE);
		//		fl_inform.setBackground(getResources().getDrawable(R.drawable.reply_txt_selector));
		fl_inform.setBackgroundResource(R.drawable.reply_txt_selector);
		fl_inform.setOnClickListener(new View.OnClickListener() {
			View file_views[] = {args[3], attach_img, attach_txt};
			@Override
			public void onClick(View v) {
				if(NAV_DOWN){
					vibrator.vibrate(VIBRATE_PERIOD);
					attach_inform_img.setBackgroundResource(R.drawable.navigation_up_item);	
					setViewsVisibilities( file_views , View.VISIBLE);

					NAV_DOWN = false;
				}else{
					vibrator.vibrate(VIBRATE_PERIOD);
					attach_inform_img.setBackgroundResource(R.drawable.navigation_down_item);
					setViewsVisibilities( file_views , View.INVISIBLE);
					NAV_DOWN = true;
				}
			}
		});
		if(AppConfig.DEBUG)Log.d(TAG, "makeAttachInformViews");
	}
	private String removeTags(String message){
		String displayMsg = message;

		displayMsg = displayMsg.replaceAll("<[^>]*>","");
		displayMsg = displayMsg.replaceAll("&nbsp;", "");
		displayMsg = displayMsg.replaceAll("&amp;", "&");
		displayMsg = displayMsg.replaceAll("&gt;", "<");
		displayMsg = displayMsg.replaceAll("&lt;", ">");

		return displayMsg;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle bundle = new Bundle();
		bundle.putParcelable(SAVED_INSTANCE_SNS_INFO, snsinfo);
		bundle.putInt(SAVED_INSTANCE_USER_ID, AppUser.user_id);
		outState.putBundle(SAVED_INSTANCE_REPLY, bundle);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onRestart();
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		//		Log.i("ReplyActivity destroy", "remove snsAppInfo intent");
		getIntent().removeExtra("snsAppInfo");
		System.gc();
	}

	private class FileDownloadTask extends AsyncTask<Object, Void, PostAttachHolder>{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(ReplyActivity.this, "",
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
			// AttachedDownloadManager Activity 를 열어서 사용자에게 Dialog 제공

			String filename = result.getFileName();
			String user_show_filename ="";
			if(filename.length() > 20){
				user_show_filename = filename.substring(0, 20) + "...";
			}else{
				user_show_filename = filename;
			}
			attachFileText.setText(user_show_filename);
			dialog.dismiss();
		}
		@Override
		protected PostAttachHolder doInBackground(Object... arg) {
			int p_id = (Integer)arg[0];

			String filename = null;
			PostAttachHolder result = new PostAttachHolder();

			KLoungeRequest kreq = new KLoungeRequest();

			filename = kreq.getFileName(p_id);

			result.setPost_id(p_id);
			result.setFileName(filename);

			if(filename.equals("") || filename.length() == 0){
//				Log.d(TAG, "no file");
				return null;
			}

			return result;
		}
	}
	public void playVideo(Context c, String video_url){
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse(video_url), "video/flv");
		c.startActivity(i);
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
	}
}
