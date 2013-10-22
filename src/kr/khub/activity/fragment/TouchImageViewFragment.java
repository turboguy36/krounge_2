package kr.khub.activity.fragment;

import static kr.khub.CommonUtilities.DOWNLOAD_PATH;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.OnDrawableChangeListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import kr.khub.CommonUtilities;
import kr.khub.R;
import kr.khub.activity.ReplyViewDialog;
import kr.khub.bitmapfun.util.Utils;
import kr.khub.model.SnsAppInfo;
import kr.khub.util.DownloadImage;
import kr.khub.util.ImageDownloader;
import kr.khub.util.KLoungeFormatUtil;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TouchImageViewFragment extends SherlockFragment{
	private String TAG = TouchImageViewFragment.class.getSimpleName();
	private SimpleDateFormat format;
	private Date ORIGINAL_IMAGE_REMAIN_DATE = null;
	private Activity mActivity;
	private ImageViewTouch mImage;
	private int visible = View.VISIBLE;
	private Vibrator vibrator;
	private Intent download_intent;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private DownloadManager mgr=null;
	private String file_name;
	private DownloadManager.Request request;
	private String img_url;
	private long lastDownload=-1L;

	private String getFile_name() {
		return file_name;
	}

	private void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	private String getImg_url() {
		return img_url;
	}

	private void setImg_url(String img_url) {
		this.img_url = img_url;
	}
	
	public long getLastDownload() {
		return lastDownload;
	}

	public void setLastDownload(long lastDownload) {
		this.lastDownload = lastDownload;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		setHasOptionsMenu(true);
		mActivity = activity;
		vibrator = (Vibrator)activity.getSystemService(Context.VIBRATOR_SERVICE);
		format = new SimpleDateFormat("yyyyMMddHmsS", Locale.KOREA);
		mgr = (DownloadManager)activity.getSystemService(Context.DOWNLOAD_SERVICE);
		try {
			ORIGINAL_IMAGE_REMAIN_DATE = format.parse("2013041000000000");
		} catch (ParseException e) {
			Log.i(TAG, "Date Parse Exception"+e);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SnsAppInfo snsinfo = getArguments().getParcelable("snsAppInfo");
		
		View v = inflater.inflate(R.layout.big_image, null);
		mImage = (ImageViewTouch)v.findViewById(R.id.oneBigImage);
		makeView(v, snsinfo);
		
		String _url = makeOriginalImageUrl(snsinfo.getPhotoVideo());
		new ImageDownloadTask().execute(_url);
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.picture_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.pic_down:
			startDownload(getImg_url(), getFile_name());
			break;
		case R.id.pic_cancel:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void makeView(View base, final SnsAppInfo snsinfo){
		TextView overviewText;
		TextView overviewReply;
		final Button doneButton = (Button)base.findViewById(R.id.btn_done);
		final Button downButton = (Button)base.findViewById(R.id.btn_img_down);
		final LinearLayout bottomBar = (LinearLayout)base.findViewById(R.id.bottom_bar_bigimage);
		
		String msg_body =  KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();
		overviewText = (TextView)base.findViewById(R.id.bodyText_overview);
		if(overviewText != null)
			overviewText.setText(Html.fromHtml(msg_body));

		overviewReply = (TextView)base.findViewById(R.id.replyText_overview);
		int reply_cnt = snsinfo.getReply_count();
		overviewReply.setText("댓글( "+reply_cnt+" )");
		ImageViewTouch mImage = (ImageViewTouch)base.findViewById(R.id.oneBigImage);
		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);

		mImage.setSingleTapListener(new OnImageViewTouchSingleTapListener() {
			@Override
			public void onSingleTapConfirmed() {
				setVisibilityOfControls(downButton, doneButton, bottomBar);
			}
		} );

		mImage.setDoubleTapListener( new OnImageViewTouchDoubleTapListener() {

			@Override
			public void onDoubleTap() {
				Log.d( TAG, "onDoubleTap" );
			}
		} );

		mImage.setOnDrawableChangedListener( new OnDrawableChangeListener() {

			@Override
			public void onDrawableChanged( Drawable drawable ) {
				Log.i( TAG, "onBitmapChanged: " + drawable );
			}
		} );

		
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				getActivity().finish();
			}
		});
		
		String imgURL = snsinfo.getPhotoVideo();
		int splitPoint = imgURL.lastIndexOf("/");
		String dir = imgURL.substring(0, splitPoint);
		String filename = imgURL.substring(splitPoint+1);
		String allURL = null;

		allURL = CommonUtilities.ORI_IMAGE_PRESERVED ? dir+"/ori_"+filename : dir +"/"+ filename;

		download_intent = new Intent(getActivity(), DownloadImage.class);
		download_intent.putExtra("download_url", allURL);
		download_intent.putExtra("download_filename", filename);
		
		downButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				getActivity().openOptionsMenu();
			}
		});
		overviewReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				Intent reply_intent = new Intent(mActivity, ReplyViewDialog.class);
				reply_intent.putExtra("snsAppInfo", snsinfo);
				startActivity(reply_intent);
			}
		});
	}
	private void setVisibilityOfControls(View... v) {
		visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
		for(View view:v){
			view.setVisibility(visible);	
		}
	}
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void startDownload(String str_uri, String filename) {
		Uri uri=Uri.parse(str_uri);

		File dir=new File(DOWNLOAD_PATH+"/Download/");
		if(!dir.exists()) dir.mkdirs();

		request = new DownloadManager.Request(uri)
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
						DownloadManager.Request.NETWORK_MOBILE)
				.setAllowedOverRoaming(false)
				.setTitle(getFile_name())
				.setDescription("위치: "+DOWNLOAD_PATH+"/Download/")
				.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
						filename);
		if(Utils.hasHoneycomb()){
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		
		lastDownload=mgr.enqueue(request);
	}
	private String makeOriginalImageUrl(String url){
		// Image URL 을 정한다. 
		// 일정 기간 이전에 올라온 이미지라면 p_2013*** 와 같은 이미지를,
		// 그 이후라면 ori_p_2013*** 와 같은 URL 을 받는다.
		String imgUrl=url;
		if(imgUrl.contains(".flv")){
			return null;
		}
		int subUrl_start = 0;
		String filename =null;
		String url_dir =null;
		String dateFormat = null;
		int sub_ext_point = 0;
		try{
			subUrl_start = imgUrl.lastIndexOf('/');
			filename = imgUrl.substring(subUrl_start+1);
			url_dir = imgUrl.substring(0, subUrl_start);
			dateFormat = filename.substring(2);
			sub_ext_point = dateFormat.indexOf(".");
			dateFormat = dateFormat.substring(0,sub_ext_point);

			setFile_name(filename);
		}catch(StringIndexOutOfBoundsException s){
			Log.e(TAG, "out of index bound - "+s);
		}

		Date img_date = null;
		try {
			img_date = format.parse(dateFormat);
		} catch (ParseException e) {
			Log.i(TAG,"ParseException -"+e);
		}

		imgUrl = CommonUtilities.ORI_IMAGE_PRESERVED 
				? (img_date.after(ORIGINAL_IMAGE_REMAIN_DATE) ? (url_dir+ "/ori_" + filename) : (url_dir +"/"+ filename)) 
						: url_dir +"/" +filename;
		setImg_url(imgUrl);
		
		return imgUrl;
	}
	
	private class ImageDownloadTask extends AsyncTask<String, Void, Void>{
		private ImageDownloader imagedownloader;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			imagedownloader = new ImageDownloader(mActivity);
		}

		@Override
		protected Void doInBackground(String... params) {
			String imgUrl = params[0];
			imagedownloader.download(imgUrl, mImage);
			return null;
		}
	}
}
