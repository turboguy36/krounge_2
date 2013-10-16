package kr.co.ktech.cse.activity.fragment;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.FileSearchListActivity;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.DataInfo;
import kr.co.ktech.cse.util.FileDownloadManager;
import kr.co.ktech.cse.util.StarRatingDialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

public class FileContentViewFragment extends SherlockFragment implements OnClickListener{
	private final String TAG = FileContentViewFragment.class.getSimpleName(); 
	private ActionBar aBar;
	private ImageFetcher mImageFetcher;
	private ImageView userImageView;
	View base_view;
	private String EMPTY_BODY_STRING = "본문이 없습니다.";
	private String EMPTY_TAG_STRING = "등록된 키워드가 없습니다.";
	DataInfo dInfo;
	private static final int STAR_RATING_DIALOG = 2001;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mImageFetcher = Utils.getImageFetcher(getActivity());
		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		
		if(savedInstanceState != null){
			dInfo = savedInstanceState.getParcelable(TAG);
		}else{
			dInfo = getArguments().getParcelable(FileSearchListActivity.DATA_KEY);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		String content_title ="";
		String user_name = "";
		String written_date = "";
		int read_count = 0;
		String file_name = "";
		String body_msg = "";
		String keywords = "";
		int post_user_id = -1;
		float star_point = 0f;
		int post_id = -1;
		
		if(dInfo != null){
			content_title = dInfo.getTitle();
			user_name = dInfo.getUser_name();
			
			FileSearchListActivity pActivity = (FileSearchListActivity)getActivity();
			written_date = pActivity.ConvertDateFormat(String.valueOf(dInfo.getDate()));
			
			read_count = dInfo.getCount();
			file_name = dInfo.getAttach();
			body_msg = dInfo.getBody();
			keywords = dInfo.getKeyword();
			post_user_id = dInfo.getUser_id();
			star_point = (float)dInfo.getPoint()/20;
			post_id = dInfo.getPostId();
		}
		if(dInfo.getBpublic() > 0){
			setHasOptionsMenu(true);
		}else{
			setHasOptionsMenu(false);
		}
		//------------------------------------액션바 타이틀 정보------------------------------------//
		
		aBar=getSherlockActivity().getSupportActionBar();
		aBar.setTitle(content_title);
		
		aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		// file 이름을 title 로 한다. 
		// selected 로 해 놓아야 만약에 title 이 길어 지면 
		// marquee 효과를 낼 수 있다.
		
		base_view = inflater.inflate(R.layout.file_content_view, null);
		
		RelativeLayout user_info_layout = (RelativeLayout)base_view.findViewById(R.id.user_view);
		
		//------------------------------------파일 등록자 정보--------------------------------------//
		
		userImageView = (ImageView)base_view.findViewById(R.id.user_image_file_content_view);
		
		TextView user_name_view = (TextView)user_info_layout.findViewById(R.id.post_user_name);
		TextView written_date_view = (TextView)user_info_layout.findViewById(R.id.posted_date);
		TextView count_text = (TextView)user_info_layout.findViewById(R.id.posted_count);

		user_name_view.setText(user_name);
		written_date_view.setText(written_date);
		count_text.setText(read_count + " 읽음");
		
		//------------------------------------첨부된 파일 정보--------------------------------------//
		
		RelativeLayout attach_info_layout = (RelativeLayout)base_view.findViewById(R.id.attach_view);
		RelativeLayout file_view_layout = (RelativeLayout)base_view.findViewById(R.id.attach_file_view);
		
		ImageView attach_image = (ImageView)attach_info_layout.findViewById(R.id.attach_image);
		ImageButton share_group_sns_btn = (ImageButton)attach_info_layout.findViewById(R.id.attach_send_group_sns);
		
		int src = FileSearchListActivity.getFileTypeIcon(file_name.substring(file_name.lastIndexOf(".")+1));
		attach_image.setImageResource(src);
		TextView attach_title = (TextView)attach_info_layout.findViewById(R.id.attach_title_text_view);
		
		if(file_name.length() == 0 || file_name.equals("")){
			file_name = getActivity().getResources().getString(R.string.sorry_no_attach);
			TextView download_text = (TextView)file_view_layout.findViewById(R.id.attach_download_text_view);
			download_text.setVisibility(View.GONE);
		}
		
		attach_title.setText(file_name);
		attach_title.setSelected(true);
		// selected 로 해 놓아야 만약에 title 이 길어 지면 
		// marquee 효과를 낼 수 있다.
		
		//------------------------------------본문 정보--------------------------------------//
		
		RelativeLayout body_layout = (RelativeLayout)base_view.findViewById(R.id.body_view);
		TextView body = (TextView)body_layout.findViewById(R.id.body_text);
		
		if(body_msg.equals("") || body_msg.length() ==0){
			body.setText(EMPTY_BODY_STRING);
		}else{
			body.setText(body_msg);
		}
		
		//------------------------------------키워드 정보--------------------------------------//
		
		RelativeLayout keyword_layout = (RelativeLayout)base_view.findViewById(R.id.keyword_view);
		TextView tags = (TextView)keyword_layout.findViewById(R.id.tag_text);
		
		if(keywords.equals("") || keywords.length() ==0){
			tags.setText(EMPTY_TAG_STRING);
		}else{
			tags.setText(keywords);
		}
		
		//------------------------------------별점 정보--------------------------------------//
		Button rating_button;
		rating_button = (Button)base_view.findViewById(R.id.score_button);
		RatingBar star_rating = (RatingBar)base_view.findViewById(R.id.star_rating_bar);
		star_rating.setRating(star_point);
		TextView rating_count = (TextView)base_view.findViewById(R.id.rating_text_point);
		
		rating_count.setText(String.valueOf(star_point));
		
		new GetUserPhoto().execute(post_user_id);
		// 파일 올린 사람 User Image 받아오기 & 이름 받아오기
		
		new IncreaseCountTask().execute(post_id, read_count);
		// 조회수 증가
		
		new CheckRatingTask().execute(post_id, AppUser.user_id);
		// 별점 참여 했는지 안했는지 체크
		
		// Listener 들 등록
		userImageView.setOnClickListener(this);
		file_view_layout.setOnClickListener(this);
		share_group_sns_btn.setOnClickListener(this);
		rating_button.setOnClickListener(this);
		
		return base_view;
	}
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(base_view.getWindowToken(), 0);
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		// TODO Auto-generated method stub
		menu.clear();
		inflater.inflate(R.menu.file_search_menu , menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		switch(item.getItemId()){
		case R.id.share_button:
			ShareDataGroupSns fragment = new ShareDataGroupSns();

			Bundle bundle = new Bundle();
			bundle.putParcelable(FileSearchListActivity.DATA_KEY, dInfo);
			fragment.setArguments(bundle);

			((FileSearchListActivity)getActivity()).addFragmentToStack(fragment);

			return true;
		default :
			return super.onOptionsItemSelected(item);
		}
	}
	
	DialogInterface.OnClickListener positive_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
	};
	DialogInterface.OnClickListener negative_listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			
		}
	};
	private void sendEmailWithAttachment(){
		ArrayList<String>emailList;
		emailList = new ArrayList<String>();
		emailList.add("turboguy36@gmail.com");
		
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("text/html");

		StringBuffer link_file_url = new StringBuffer(); 
		link_file_url.append("<a href=\"")
					.append(getUrl())
					.append("\">")
					.append(dInfo.getTitle())
					.append("</a>");
		//	Log.d(TAG,"url: "+ link_file_url.toString());
		sharingIntent.putExtra(Intent.EXTRA_EMAIL, emailList.toArray(new String[emailList.size()]));
		sharingIntent.putExtra(Intent.EXTRA_SUBJECT, dInfo.getTitle());
		sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(link_file_url.toString()));
		startActivity(Intent.createChooser(sharingIntent,"Share using"));
	}
	
	private String getUrl(){
		KLoungeHttpRequest httprequest = new KLoungeHttpRequest();
		String jspFilename = "appFileDownFromFileLibrary.jsp";
		
		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/").append(jspFilename);
		sb_url.append("?post_id="+dInfo.getPostId());
		sb_url.append("&post_user_id="+dInfo.getUser_id());
		
		return sb_url.toString();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.attach_file_view:
			try{
				Bundle bun = new Bundle();

				bun.putInt("p_id", dInfo.getPostId());
				bun.putInt("user_id", dInfo.getUser_id());
				bun.putString("filename", dInfo.getAttach());

				Intent popupIntent = new Intent(getActivity(), FileDownloadManager.class);
				popupIntent.putExtras(bun);
				PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, 
						popupIntent, PendingIntent.FLAG_ONE_SHOT);
				pi.send();
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		break;
		case R.id.user_image_file_content_view:
//			((SlideMenuActivity)getActivity()).addFragmentToStack(PersonalLoungeFragment.class.getSimpleName(), dInfo);
			break;
		case R.id.attach_send_group_sns:
			Toast.makeText(getActivity(), "share sns", Toast.LENGTH_SHORT).show();
			break;
		case R.id.score_button:
//			Toast.makeText(getActivity(), "score", Toast.LENGTH_SHORT).show();
			try{
				Bundle bun = new Bundle();

				bun.putInt("p_id", dInfo.getPostId());
				bun.putInt("user_id", dInfo.getUser_id());

				Intent popupIntent = new Intent(getActivity(), StarRatingDialog.class);
				popupIntent.putExtras(bun);
//				PendingIntent pi = PendingIntent.getActivity(getActivity(), 0, 
//						popupIntent, PendingIntent.FLAG_ONE_SHOT);
//				pi.send();
				startActivityForResult(popupIntent, STAR_RATING_DIALOG);
				
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putParcelable(TAG, dInfo);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case STAR_RATING_DIALOG:
			if(resultCode == Activity.RESULT_OK){
				int post_id = data.getIntExtra("post_id", -1);
				int user_id = data.getIntExtra("user_id", -1);
				new CheckRatingTask().execute(post_id, user_id);
			}else if(resultCode == Activity.RESULT_CANCELED){
				
			}
			break;
		}
	}

	private class GetUserPhoto extends AsyncTask<Integer, Void, String[]>{
		KLoungeRequest httprequest;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			httprequest = new KLoungeRequest();
		}
		@Override
		protected String[] doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			String request_result;
			String[] user_info = null;
			try{
				request_result = httprequest.getUserPhoto(params[0]);
				user_info = request_result.split("::");
			}catch(NullPointerException e){
				e.printStackTrace();
			}
			return user_info;
		}
		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			try{
				String user_photo_uri = result[0].trim().replace(" ", "%20");
				dInfo.setUser_photo(user_photo_uri);
				String user_name_str = result[1];
				dInfo.setUser_name(user_name_str);
				
				mImageFetcher.loadImage(user_photo_uri, userImageView);
				
				TextView tv = (TextView)base_view.findViewById(R.id.post_user_name);
				tv.setText(result[1]);
			}catch(ArrayIndexOutOfBoundsException ae){
				ae.printStackTrace();
			}
			super.onPostExecute(result);
		}
	}
	
	class IncreaseCountTask extends AsyncTask<Integer, Void, Void>{
		KLoungeRequest httprequest;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			httprequest = new KLoungeRequest();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			try{
				int post_id = params[0];
				int count = params[1]+1;
				httprequest.increaseDataCount(post_id, count);
			}catch(NullPointerException e){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	class CheckRatingTask extends AsyncTask<Integer, Void, ArrayList<Integer>>{
		KLoungeRequest httprequest;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			httprequest = new KLoungeRequest();
		}
		@Override
		protected ArrayList<Integer> doInBackground(Integer... params) {
			ArrayList<Integer> result = new ArrayList<Integer>();
			try{
				int post_id = params[0];
				int user_id = params[1];
				result = httprequest.checkRating(post_id, user_id);
				
			}catch(NullPointerException e){
				e.printStackTrace();
			}
			return result;
		}
		@Override
		protected void onPostExecute(ArrayList<Integer> result) {
			super.onPostExecute(result);
			try{
				int check = result.get(0);
				
				if(check != 0){// 잘 등록이 되었다면
					Button score_button = (Button)base_view.findViewById(R.id.score_button);
					score_button.setText(getActivity().getResources().getString(R.string.rating_button_disable));
					score_button.setEnabled(false);
				}
				
				int number = result.get(1); // 몇명이 점수 참여 했는지
				float average = (float)result.get(2)/20; // 평균 점수가 어떻게 되는지
				
				RatingBar starPoint = (RatingBar)base_view.findViewById(R.id.star_rating_bar);
				starPoint.setRating(average);
				
				TextView starText = (TextView)base_view.findViewById(R.id.rating_text_point);
				starText.setText(String.valueOf(average));
				
				TextView participants = (TextView)base_view.findViewById(R.id.rating_text_participant);
				StringBuffer participants_text = new StringBuffer();
				participants_text.append("(")
							.append(number)
							.append(" 명 참여)");
				participants.setText(participants_text.toString());
				
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
