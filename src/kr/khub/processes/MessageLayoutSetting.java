package kr.khub.processes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.method.BaseMovementMethod;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import kr.khub.AppConfig;
import kr.khub.CommonUtilities;
import kr.khub.R;
import kr.khub.activity.AttachedDownloadManager;
import kr.khub.activity.FileSearchListActivity;
import kr.khub.activity.KLoungeActivity;
import kr.khub.activity.MyLounge;
import kr.khub.activity.PersonalLounge;
import kr.khub.activity.ReplyViewDialog;
import kr.khub.bitmapfun.util.ImageFetcher;
import kr.khub.db.KLoungeHttpRequest;
import kr.khub.db.KLoungeRequest;
import kr.khub.listener.AppClickListener;
import kr.khub.model.AppUser;
import kr.khub.model.DataInfo;
import kr.khub.model.NewMessage;
import kr.khub.model.PostAttachHolder;
import kr.khub.model.SnsAppInfo;
import kr.khub.util.BadgeView;
import kr.khub.util.KLoungeFormatUtil;
import static kr.khub.CommonUtilities.FLAG_IF_REDIRECT_LINK;

public class MessageLayoutSetting{
	private final int MAIN_MESSAGE = 1;
	
	private final int REPLY_BADGEVIEW_ID = 90082;

	public final int REPLY_TAIL_HEIGHT = 50;
	private Context context;
	
	private LinearLayout baseLinearLayout;
	final String TO_REPLY_MARK = "@";
	final String SEPARATOR= " ";
	final int REPLY_POST_ID_TAG = 1;

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
		totalMessage = 0;
	}

	public int getTotalMessage() {
		return totalMessage;
	}

	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}

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
			// URL 로 가는게 아닌, FileContentViewFragment 로 이동함
			String post_id = null;
			String ck = null;
			String redirect_group_id = null;
			try{
				
				int post_id_index = htmlmessage.indexOf("?p_id=")+6;
				post_id = getParameter(post_id_index);
				
				int ck_index = htmlmessage.indexOf("&check=")+7;
				ck = getParameter(ck_index);
				
				int group_id_index = htmlmessage.indexOf("&group_id=") + 10;
				redirect_group_id = getParameter(group_id_index);
				
				if(post_id !=null){
					download_Pid = Integer.parseInt(post_id);
				}
				
			}catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
			}catch(NumberFormatException e1){
				e1.printStackTrace();
			}
			
			int intCk = -1;
			intCk = Integer.parseInt(ck);
			if(intCk == 3 || intCk==4){
				msg.setTag(R.id.arg1, Integer.parseInt(post_id));
				msg.setTag(R.id.arg2, Integer.parseInt(redirect_group_id));
				
				msg.setText(Html.fromHtml(htmlmessage));
				msg.setOnClickListener(data_open_listener);
				msg.setBackgroundResource(R.drawable.reply_txt_selector);
			}
		}else{
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
				AppClickListener acListener = new AppClickListener(snsinfo, context);
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
	private String getParameter(final int index){
		StringBuffer re_gid = new StringBuffer();
		for(int i=index;i<htmlmessage.length();i++){
			re_gid.append(htmlmessage.charAt(i));
			if(!isNumeric(re_gid.toString())){
				re_gid.deleteCharAt(i - index);
				break;
			}
		}
		return re_gid.toString();
	}
	private boolean isNumeric(String str){  
		try{  
			double d = Double.parseDouble(str);  
		}catch(NumberFormatException nfe){  
			return false;  
		}  
		return true;  
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
	View.OnClickListener data_open_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int post_id = (Integer)v.getTag(R.id.arg1);
			int group_id = (Integer)v.getTag(R.id.arg2);
			new GetDataInfoTask().execute(post_id, group_id);
		}
	};
	class GetDataInfoTask extends AsyncTask<Integer, Void, DataInfo>{
		KLoungeRequest request;
		ProgressDialog progress;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			request = new KLoungeRequest();
			showProgress();
		}

		@Override
		protected DataInfo doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			int post_id = params[0];
			int group_id = params[1];
			
			return request.getDataInfo(post_id, group_id, AppUser.user_id);
		}

		@Override
		protected void onPostExecute(DataInfo result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progress.dismiss();
			
			Bundle bundle = new Bundle();
			bundle.putParcelable(FileSearchListActivity.DATA_KEY, result);
			Intent intent = new Intent(context, FileSearchListActivity.class);
			intent.putExtras(bundle);
			
			context.startActivity(intent);
		}
		private void showProgress(){
			progress = new ProgressDialog(context);
			progress.setCancelable(true);
			progress.setCanceledOnTouchOutside(true);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progress.setMessage("잠시만 기다려 주십시오...");
			progress.show();
		}
	}
	View.OnClickListener delete_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
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
			String added_imageurl = (String)v.getTag();
			playVideo(added_imageurl);
		}
	};
	View.OnClickListener attach_layout_click_listener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int pid = (Integer)v.getTag();
			vibrator.vibrate(VIBRATE_PERIOD);
			new GetFileTask().execute(pid);
		}
	};
	View.OnClickListener user_photo_click_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
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
