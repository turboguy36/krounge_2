package kr.co.ktech.cse.adapter;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.DataInfo;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CustomBaseAdapter extends BaseAdapter{
	private Context mContext = null;
	private ArrayList<DataInfo> dInfoList= null;
	private final String TAG = CustomBaseAdapter.class.getSimpleName();
	
	public CustomBaseAdapter(Context c, ArrayList<DataInfo> array){
		mContext = c;
		dInfoList = array;
		Log.d(TAG, "dInfoList.size(): "+dInfoList.size());
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DataInfo getItem(int position) {
		// TODO Auto-generated method stub
		DataInfo result = null;
		try{
			result = dInfoList.get(position);
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}
		return result;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = null;
		View view;
		if(convertView == null){
			inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_item, null);
		}else{
			view = convertView;
		}
		TextView title = (TextView)view.findViewById(R.id.menu_list_text);
		title.setText(dInfoList.get(position).getTitle());
		Log.d(TAG, "title: "+dInfoList.get(position).getTitle());
		return view;
	}
	
	/*
	private Activity activity;
//	private SnsAppInfo[] data;
	//	private static LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;
	private String TAG = CustomBaseAdapter.class.getSimpleName();
	public static final String FLAG_IF_REDIRECT_LINK = "/common/redirectLink.jsp?";
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private final int MAIN_MESSAGE = 1;
	private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();
	private List<SnsAppInfo> sData;
	
	public CustomBaseAdapter(Activity a, List<SnsAppInfo> sInfoList, ImageFetcher imgFetcher) {
		
		activity = a;
		sData = sInfoList;
		
		mImageFetcher = imgFetcher;
		//		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public int getCount() {
		int count = 0;
		try{
			count = sData.size();//data.length;
		}catch(NullPointerException n){
			n.printStackTrace();
		}
		return count;
	}

	@Override
	public SnsAppInfo getItem(int position) {
		return sData.get(position);//data[position];
	}

	@Override
	public long getItemId(int position) {
		return sData.get(position).getPostId();//data[position].getPostId();
	}
	@Override
	public boolean hasStableIds() {
		return true;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = null;
		RelativeLayout parent_view;
		
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		parent_view = (RelativeLayout)inflater.inflate(R.layout.one_content, null);
		MessageListViewHolder holder = (MessageListViewHolder)parent_view.getTag();
		if(holder == null){
			holder = new MessageListViewHolder(parent_view);
			parent_view.setTag(holder);
		}
		try{
			final SnsAppInfo cur_data = sData.get(position);
			String htmlmessage = KLoungeFormatUtil.bodyURLFormat(cur_data.getBody()).toString();//data[position].getBody()).toString();
			
			// 사용자 이미지를 셋팅, 클릭 했을 때 이벤트 부여하기
			setUserPhoto(holder.writer_photo, 
					cur_data.getPhoto(), 
					cur_data);
			
			// 글쓴이의 이름 셋팅, 클릭 했을 때 이벤트 부여
			WriterSetTextAndClickListener(holder.writer, 
					cur_data.getUserName(), 
					cur_data);
			
			// 본문 메세지 셋팅
			setBodyMsg(holder.body, htmlmessage);
			if(htmlmessage.contains(FLAG_IF_REDIRECT_LINK)){
				// 본문 메세지에서 첨부파일 추출해 내기
				makeAttachedFile(htmlmessage, holder.attach_layout);
			}

			// 글 쓴 날짜
			holder.date.setText(cur_data.getWrite_date());
			
			// 이미지나 동영상 셋팅, 클릭 했을 때 이벤트
			if(cur_data.getPhotoVideo() != null 
					&& !cur_data.getPhotoVideo().equals("")){
				//이미지나 동영상이 있을 때
				setImageOrVideo(holder.image_layout, 
						holder.imageView, 
						holder.play_btn, 
						cur_data.getPhotoVideo());
			}
			
			// 댓글 
			holder.reply.setText("댓글("+cur_data.getReply_count()+")");
			holder.reply.setBackgroundDrawable(
					activity.getResources().getDrawable(R.drawable.reply_txt_selector));
			holder.reply.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					// 새로운 창 추가
					try {
						Intent intent = new Intent(activity, ReplyActivity.class);
						intent.putExtra("snsAppInfo", cur_data);
						activity.startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}								
				}
			});
			
			// 첨부파일 있는 경우
			String attach = cur_data.getAttach();
			if(!attach.equals("") && !attach.equals("null")){
				makeAttachedFile(holder.attach_layout, cur_data.getAttach(), cur_data);
			}
			
			// 삭제 버튼
			if(AppUser.user_id == cur_data.getUserId()){
				setDeleteButtonAndClickListener(holder.delete_btn, cur_data);
			}
		}catch(ArrayIndexOutOfBoundsException a){
			a.printStackTrace();
		}
		mRecycleList.add(new WeakReference<View>(parent_view));
		return parent_view;
	}
	
	private void setDeleteButtonAndClickListener(ImageButton ivDelete, final SnsAppInfo snsinfo){
		DisplayUtil du = new DisplayUtil(activity);
		ivDelete.setVisibility(View.VISIBLE);
		ivDelete.setPadding(du.PixelToDP(25), du.PixelToDP(17), 
				du.PixelToDP(25), du.PixelToDP(17));
		ivDelete.setBackgroundDrawable(
				activity.getResources().getDrawable(R.drawable.reply_txt_selector));
		ivDelete.setImageResource(R.drawable.navigation_cancel);
	}
	private void setReplyTextAndClickListener(TextView reply_view, 
			int reply_num, final int pos){
		
		reply_view.setText("댓글("+reply_num+")");
		reply_view.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				// 새로운 창 추가
				try {
					
					Intent intent = new Intent(activity, ReplyActivity.class);
					intent.putExtra("snsAppInfo", getItem(pos));
					activity.startActivity(intent);
					
				} catch (Exception e) {
					e.printStackTrace();
				}								
			}
		});
	}
	
	private void setImageOrVideo(FrameLayout layout, ImageView img_layout, 
			ImageView button, final String added_imageurl){

		layout.setVisibility(View.VISIBLE);
		mImageFetcher.setLoadingImage(R.drawable.no_picture);

		if(added_imageurl.contains(".flv")){
			button.setVisibility(View.VISIBLE);
			String flv_thumb = added_imageurl.replace("/video/", "/image/");
			flv_thumb = flv_thumb.replace(".flv", ".png");
			mImageFetcher.loadImage(flv_thumb, img_layout);
		}else{
			mImageFetcher.setImageSize(activity.getResources().getDimensionPixelSize(R.dimen.big_image_size));
			mImageFetcher.loadImage(added_imageurl, img_layout);
		}
		img_layout.setScaleType(ImageView.ScaleType.FIT_START);
	}
	private void WriterSetTextAndClickListener(TextView name, String user_name, final SnsAppInfo curAppInfo){
		name.setText(user_name);
		name.setBackgroundDrawable(
				activity.getResources().getDrawable(R.drawable.reply_txt_selector));
		name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PersonalLounge.class);
				String user_id = String.valueOf(curAppInfo.getUserId());
				String user_name = curAppInfo.getUserName();
				String user_photo = curAppInfo.getPhotoVideo();
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);
				if(curAppInfo.getUserId() == curAppInfo.getPuser_id()){
					Toast.makeText(activity, "현재 "+user_name+" 님의 라운지에 위치 해 있습니다.", Toast.LENGTH_SHORT).show();
				}else{
					activity.startActivity(intent);
				}
			}
		});
	}

	private void setBodyMsg(TextView msg, String body){
		body = body.replaceAll("<[^>]*>","");
		body = body.replaceAll("&nbsp;", "");
		body = body.replaceAll("&amp;", "&");
		body = body.replaceAll("&gt;", "<");
		body = body.replaceAll("&lt;", ">");
		msg.setText(body);
	}
	private void setUserPhoto(ImageView user_photo, String imageurl, final SnsAppInfo curAppInfo){
		if(imageurl.contains("/images/sns/no_photo_small.gif")){
			user_photo.setImageResource(R.drawable.no_photo);
		}else{
			mImageFetcher.setLoadingImage(R.drawable.no_photo);
			mImageFetcher.loadImage(imageurl.replace(" ", "%20"), user_photo);
		}
		user_photo.setScaleType(ImageView.ScaleType.FIT_XY);

		user_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, PersonalLounge.class);
				String user_id = String.valueOf(curAppInfo.getUserId());
				String user_name = curAppInfo.getUserName();
				String user_photo = curAppInfo.getPhotoVideo();
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);
				if(curAppInfo.getUserId() == curAppInfo.getPuser_id()){
					Toast.makeText(activity, "현재 "+user_name+" 님의 라운지에 위치 해 있습니다.", Toast.LENGTH_SHORT).show();
				}else{
					activity.startActivity(intent);
				}
			}
		});
	}
	private void makeAttachedFile(LinearLayout attach_layout, final String attach, final SnsAppInfo snsinfo){
		attach_layout.setBackgroundDrawable(
				activity.getResources().getDrawable(R.drawable.reply_txt_selector));
		attach_layout.setVisibility(View.VISIBLE);
		attach_layout.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					Bundle bun = new Bundle();
					bun.putString("notiMessage", "다운로드 하시겠습니까?");
					bun.putInt("p_id", snsinfo.getPostId());
					bun.putInt("user_id", snsinfo.getUserId());
					bun.putInt("ck", MAIN_MESSAGE*0);
					bun.putString("filename", attach);
					Intent popupIntent = new Intent(activity, AttachedDownloadManager.class);

					popupIntent.putExtras(bun);

					PendingIntent pi = PendingIntent.getActivity(activity, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
					try{
						pi.send();
					}catch(Exception e){
						Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show();
					}
				}
			});
	}
	private void makeAttachedFile(String htmlmessage, LinearLayout attach_layout)throws NumberFormatException{

		String post_id = null;
		String ck = null;
		try{
			post_id = htmlmessage.substring(htmlmessage.indexOf("?p_id=")+6, htmlmessage.indexOf("&check"));
			ck = htmlmessage.substring(htmlmessage.indexOf("&check=")+7, htmlmessage.indexOf("&group_id"));
		}catch(StringIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		final int download_Pid = Integer.parseInt(post_id);
		int intCk = -1;
		try{
			intCk = Integer.parseInt(ck);
		}catch(NumberFormatException e1){
			e1.printStackTrace();
		}
		if(intCk == 3 || intCk==4){
			attach_layout.setVisibility(View.VISIBLE);
			attach_layout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					new GetFileTask().execute(download_Pid);
				}
			});
		}
	}
	private class GetFileTask extends AsyncTask<Integer, Void, PostAttachHolder>{
		ProgressDialog dialog;
		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(activity, "","로딩 중..", true);
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
			Bundle bun = new Bundle();
			bun.putString("notiMessage", "다운로드 하시겠습니까?");
			//Log.i("HTML", bundle_filename);
			bun.putInt("p_id", result.getPost_id());
			bun.putInt("user_id", Integer.parseInt("1"));
			bun.putInt("ck", MAIN_MESSAGE*0);
			bun.putString("filename", filename);
			bun.putBoolean("url_linker", true);
			Intent popupIntent = new Intent(activity, AttachedDownloadManager.class);
			popupIntent.putExtras(bun);
			PendingIntent pi = PendingIntent.getActivity(activity, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
			try{
				pi.send();
			}catch(CanceledException e){
				Log.e(TAG, "Cancel Exception -"+e);
			}
			dialog.dismiss();
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
	*/
}
