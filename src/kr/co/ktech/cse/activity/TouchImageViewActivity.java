package kr.co.ktech.cse.activity;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.OnDrawableChangeListener;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.DownloadImage;
import kr.co.ktech.cse.util.FileDownloader;
import kr.co.ktech.cse.util.ImageDownloader;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.DOWNLOAD_PATH;

public class TouchImageViewActivity extends Activity{
	ImageViewTouch mImage;
	private String TAG = "TouchImageViewActivity";
	private SimpleDateFormat format;
	private Date ORIGINAL_IMAGE_REMAIN_DATE = null;
	private ImageDownloader imagedownloader;
	private FileDownloader fileDownloader;
	private Context context;
	private int displayWidth;
	private int displayHeight;
	private RelativeLayout wholeView;
	private LinearLayout bottomBar;
	private TextView overviewText;
	private TextView overviewReply;
	private Button doneButton;
	private Button downButton;
	private SnsAppInfo snsinfo;
	private String img_url;
	private SaveImage saveImage;
	private static final int TAP = 3;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	private static final int WIDTH = 0;
	private static final int HEIGHT = 1;
	int visible = View.VISIBLE;
	ProgressBar progressBar;
	private int PROGRESS_NOTIFICATION = 8000;
	// Remember some things for zooming
	Vibrator vibrator;
	Intent download_intent;

	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	// from AppClickListener.java
	public TouchImageViewActivity(){
		// original 이미지가 저장 되는 시점 이후로는 original 크기의 이미지를 가져 올 것이다.
		format = new SimpleDateFormat("yyyyMMddHmsS", Locale.KOREA);
		try {
			ORIGINAL_IMAGE_REMAIN_DATE = format.parse("2013041000000000");
		} catch (ParseException e) {
			Log.i(TAG, "Date Parse Exception"+e);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.big_image);
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		context = getApplicationContext();
		imagedownloader = new ImageDownloader(this);
		fileDownloader = new FileDownloader();
		//		Intent intent = getIntent();

		Bundle bundle = getIntent().getExtras();
		snsinfo = bundle.getParcelable("snsAppInfo");

		thread.start();

		makeView(); 
		//이 함수에서 화면을 전부 구성 한다.

	}

	Thread thread = new Thread(new Runnable(){
		@Override
		public void run() {
			// Image URL 을 정한다. 
			// 일정 기간 이전에 올라온 이미지라면 p_2013*** 와 같은 이미지를,
			// 그 이후라면 ori_p_2013*** 와 같은 URL 을 받는다.
			img_url = snsinfo.getPhotoVideo();
			if(img_url.contains(".flv")){
				return;
			}
			int subUrl_start = 0;
			String filename =null;
			String url_dir =null;
			String dateFormat = null;
			int sub_ext_point = 0;
			try{
				subUrl_start = img_url.lastIndexOf('/');
				filename = img_url.substring(subUrl_start+1);
				url_dir = img_url.substring(0, subUrl_start);
				dateFormat = filename.substring(2);
				sub_ext_point = dateFormat.indexOf(".");
				dateFormat = dateFormat.substring(0,sub_ext_point);
			}catch(StringIndexOutOfBoundsException s){
				Log.e(TAG, "out of index bound - "+s);
			}

			Date img_date = null;
			try {
				img_date = format.parse(dateFormat);
			} catch (ParseException e) {
				Log.i(TAG,"ParseException -"+e);
			}

			img_url = CommonUtilities.ORI_IMAGE_PRESERVED 
					? (img_date.after(ORIGINAL_IMAGE_REMAIN_DATE) ? (url_dir+ "/ori_" + filename) : (url_dir +"/"+ filename)) 
							: url_dir +"/" +filename;

			Message msg = Message.obtain();
			msg.obj = img_url;
			handler.sendMessage(msg);
		}
	});

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String imgUrl = (String)msg.obj;
			imagedownloader.download(imgUrl, mImage);
			if(AppConfig.DEBUG) Log.d(TAG, imgUrl);
		}
	};

	private void makeView(){

		String msg_body =  KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();

		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		wholeView = (RelativeLayout)findViewById(R.id.overview_rela_layout);

		overviewText = (TextView)findViewById(R.id.bodyText_overview);
		Log.d(TAG, msg_body);
		if(overviewText != null)
			overviewText.setText(Html.fromHtml(msg_body));

		overviewReply = (TextView)findViewById(R.id.replyText_overview);
		int reply_cnt = snsinfo.getReply_count();
		overviewReply.setText("댓글( "+reply_cnt+" )");
		//lineView = (View)findViewById(R.id.line);

		//		img = (ImageView)wholeView.findViewById(R.id.oneBigImage);
		mImage = (ImageViewTouch)findViewById(R.id.oneBigImage);
		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);

		mImage.setSingleTapListener( new OnImageViewTouchSingleTapListener() {

			@Override
			public void onSingleTapConfirmed() {
				visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
				setVisibilityOfControls(visible);
				Log.d( TAG, "onSingleTapConfirmed" );
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

		doneButton = (Button)wholeView.findViewById(R.id.btn_done);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				finish();
			}
		});
		
		String imgURL = snsinfo.getPhotoVideo();
		int splitPoint = imgURL.lastIndexOf("/");
		String dir = imgURL.substring(0, splitPoint);
		String filename = imgURL.substring(splitPoint+1);
		String allURL = null;

		allURL = CommonUtilities.ORI_IMAGE_PRESERVED ? dir+"/ori_"+filename : dir +"/"+ filename;
		saveImage = new SaveImage(allURL, filename);

		download_intent = new Intent(TouchImageViewActivity.this, DownloadImage.class);
		download_intent.putExtra("download_url", allURL);
		download_intent.putExtra("download_filename", filename);
		downButton = (Button)findViewById(R.id.btn_img_down);
		downButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				
				openOptionsMenu();
			}
		});
		bottomBar = (LinearLayout)wholeView.findViewById(R.id.bottom_bar_bigimage);
		overviewReply.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				Intent reply_intent = new Intent(context, ReplyActivity.class);
				reply_intent.putExtra("snsAppInfo", snsinfo);
				startActivity(reply_intent);
			}
		});
	}
	
	private void setVisibilityOfControls(int visibility) {
		downButton.setVisibility(visibility);
		doneButton.setVisibility(visibility);
		bottomBar.setVisibility(visibility);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picture_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.pic_down:
			startActivity(download_intent);
			break;
		case R.id.pic_cancel:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
	@Override
	public void onPause() {
		super.onPause();
	}

	Handler imgDownload_handler = new Handler() {
		public void handleMessage(Message msg) {
			String doneMessage;
			if(msg.what == 0) {
				doneMessage = "이미지 다운로드가 완료 되었습니다.";
			}else{
				doneMessage = "죄송합니다. 네트워크 오류로 이미지 다운로드에 실패 하였습니다.";
			}
			displayToast(doneMessage);
		}
	};

	void displayToast(String display_message){
		Toast.makeText(context, display_message, Toast.LENGTH_SHORT).show();
	}

	private class SaveImage implements Runnable{
		String allURL;
		String filename;

		SaveImage(String Image_url, String fileName){
			allURL = Image_url;
			filename = fileName;
		}

		@Override
		public void run() {
			String sdcardState = android.os.Environment.getExternalStorageState();
			if(sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)){
				final String downlodedPath =DOWNLOAD_PATH+"/"+filename;
				FileDownloader fdown = new FileDownloader();
				try {
					fdown.downloadfile(allURL, filename);
					MediaStore.Images.Media.insertImage(getContentResolver(), downlodedPath, filename, "");
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+DOWNLOAD_PATH)));
					imgDownload_handler.sendEmptyMessage(0);
				} catch (FileNotFoundException e) {
					imgDownload_handler.sendEmptyMessage(1);
				}
			}else{
				Toast.makeText(context, "sd card unmounted", Toast.LENGTH_SHORT).show();
			}
		}
	}
}