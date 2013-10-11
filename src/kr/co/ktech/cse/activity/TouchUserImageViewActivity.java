package kr.co.ktech.cse.activity;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchDoubleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.OnDrawableChangeListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.FileDownloader;
import kr.co.ktech.cse.util.ImageDownloader;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.Html;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.DOWNLOAD_PATH;
public class TouchUserImageViewActivity extends Activity {
	private static final String TAG = "TouchUserImageViewActivity";
	ImageViewTouch mImage;
	private SimpleDateFormat format;
	private Date ORIGINAL_IMAGE_REMAIN_DATE = null;
	private ImageDownloader imagedownloader;
	private FileDownloader fileDownloader;
	private Context context;
	private int displayWidth;
	private int displayHeight;
	private Boolean showBar = true;
	private RelativeLayout wholeView;
	private ImageView img;
	private Button doneButton;
	private Button downButton;
	private Button rotateButton;
	private String file_path;
	private String img_uri;
	private Bitmap bitmap;
	private ProgressDialog pd;
	private SaveImage saveImage;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private Matrix savedMatrix2 = new Matrix();
	private float mLastMotionX;
	private float mLastMotionY;
	private Handler mHandler;
	private static final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	private static final int DOUBLETAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
	// We can be in one of these 3 states
	private static final int SHOW_PRESS = 1;
	private static final int LONG_PRESS = 2;
	private static final int TAP = 3;

	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	private static final int WIDTH = 0;
	private static final int HEIGHT = 1;
	int visible = View.VISIBLE;
	
	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	private int rotation_degree = 0;
	// from AppClickListener.java
	public TouchUserImageViewActivity(){
		format = new SimpleDateFormat("yyyyMMddHmsS");
		try {
			ORIGINAL_IMAGE_REMAIN_DATE = format.parse("2013041000000000");
		} catch (ParseException e) {
			Log.i(this.toString(),e.toString());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.big_image);

		context = getApplicationContext();
		imagedownloader = new ImageDownloader(this);
		fileDownloader = new FileDownloader();
		Intent intent = getIntent();
		file_path = intent.getStringExtra("user_photo");
		img_uri = intent.getStringExtra("photo_uri");
		
		bitmap = (Bitmap)intent.getParcelableExtra("BitmapImage");
//		createThreadAndDialog();
		if(bitmap == null){
			makeView(); //이 함수에서 화면을 전부 구성 한다.
		}else{
			makeView(bitmap);
		}

	}

	void createThreadAndDialog() {
//		pd = ProgressDialog.show(TouchUserImageViewActivity.this, "", "", true);
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// Image URL 을 정한다. 
				// 일정 기간 이전에 올라온 이미지라면 p_2013*** 와 같은 이미지를,
				// 그 이후라면 ori_p_2013*** 와 같은 URL 을 받는다.
				
				int subUrl_start = file_path.lastIndexOf('/');

				String filename = file_path.substring(subUrl_start+1);
				String url_dir = file_path.substring(0, subUrl_start);
				String dateFormat = filename.substring(2);

				int sub_ext_point = dateFormat.indexOf(".");

				dateFormat = dateFormat.substring(0,sub_ext_point);

				Date img_date = null;
				try {
					img_date = format.parse(dateFormat);
				} catch (ParseException e) {
					Log.i(this.toString(),e.toString());
				}

				if(img_date.after(ORIGINAL_IMAGE_REMAIN_DATE)){
					filename = "ori_" + filename;
					file_path = url_dir +"/"+ filename;
				}

				Message msg = Message.obtain();
				msg.obj = file_path;
				handler.sendMessage(msg);
			}

		});
		thread.start();
	}
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String imgUrl = (String)msg.obj;
			imagedownloader.download(imgUrl, img);

//			if(pd!=null)pd.dismiss();
		}
	};
	
	private void makeView(){
		LinearLayout bottomBar = (LinearLayout)findViewById(R.id.bottom_bar_bigimage);
		bottomBar.setVisibility(View.GONE);
//		String msg_body =  KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();

		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		wholeView = (RelativeLayout)findViewById(R.id.overview_rela_layout);

//		img = (ImageView)wholeView.findViewById(R.id.oneBigImage);
		mImage = (ImageViewTouch)findViewById(R.id.oneBigImage);
		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
		
		mImage.setSingleTapListener( new OnImageViewTouchSingleTapListener() {

			@Override
			public void onSingleTapConfirmed() {
				visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
				doneButton.setVisibility(visible);
			}
		} );

		mImage.setDoubleTapListener( new OnImageViewTouchDoubleTapListener() {
			@Override
			public void onDoubleTap() {
			}
		} );

		mImage.setOnDrawableChangedListener( new OnDrawableChangeListener() {

			@Override
			public void onDrawableChanged( Drawable drawable ) {
				Log.i( TAG, "onBitmapChanged: " + drawable );
			}
		} );
		
//		final Bitmap bitmap = BitmapFactory.decodeFile(file_path);
//		mImage.setImageBitmap(bitmap);
		
		doneButton = (Button)wholeView.findViewById(R.id.btn_done);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		Log.d(TAG, "Uri: "+img_uri);
//		Log.d(TAG, "path: "+file_path);
		
		uri = Uri.parse(img_uri);
		mImage.setImageURI(uri);
		
		downButton = (Button)findViewById(R.id.btn_img_down);
		downButton.setVisibility(View.GONE);
		rotateButton = (Button)findViewById(R.id.btn_rotation_img);
		rotateButton.setVisibility(View.VISIBLE);
		rotateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BitmapFactory.Options bfo = new BitmapFactory.Options();
				bfo.inSampleSize = 4;
				rotation_degree += 90;
				mImage.setImageBitmap(rotate(BitmapFactory.decodeFile(img_uri, bfo), rotation_degree));
			}
		});
//		
//		downButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				iTask = new ImageTask();
//				iTask.execute(bitmap);
//			}
//		});
	}
	private void makeView(Bitmap image){
		LinearLayout bottomBar = (LinearLayout)findViewById(R.id.bottom_bar_bigimage);
		bottomBar.setVisibility(View.GONE);

		wholeView = (RelativeLayout)findViewById(R.id.overview_rela_layout);

		mImage = (ImageViewTouch)findViewById(R.id.oneBigImage);
		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
		
		mImage.setSingleTapListener( new OnImageViewTouchSingleTapListener() {

			@Override
			public void onSingleTapConfirmed() {
				visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
				doneButton.setVisibility(visible);
			}
		} );

		mImage.setDoubleTapListener( new OnImageViewTouchDoubleTapListener() {
			@Override
			public void onDoubleTap() {
			}
		} );

		mImage.setOnDrawableChangedListener( new OnDrawableChangeListener() {

			@Override
			public void onDrawableChanged( Drawable drawable ) {
				Log.i( TAG, "onBitmapChanged: " + drawable );
			}
		} );
		
//		final Bitmap bitmap = BitmapFactory.decodeFile(file_path);
		mImage.setImageBitmap(image);
		
		doneButton = (Button)wholeView.findViewById(R.id.btn_done);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	private ImageTask iTask;
	public class ImageTask extends AsyncTask<Bitmap, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			Bitmap bitmap = params[0];
			Log.d(TAG, "return : "+ rotationForImage(context, uri));
			
			Matrix matrix = new Matrix();
			
			float rotation = rotationForImage(context, uri);
			
			if(rotation != 0f){
				matrix.preRotate(rotation);
			}
			Bitmap resizedBitmap = 
					Bitmap.createBitmap(
							bitmap,	0, 0, 
							bitmap.getWidth(), bitmap.getHeight(), 
							matrix, true);
			return resizedBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mImage.setImageBitmap(result);
		}
	}
	Uri uri;
	public static float rotationForImage(Context context, Uri uri) {
		if (uri.getScheme().equals("content")) {
			String[] projection = { Images.ImageColumns.ORIENTATION };
			Cursor c = context.getContentResolver().query(
					uri, projection, null, null, null);
			if (c.moveToFirst()) {
				return c.getInt(0);
			}
		} else if (uri.getScheme().equals("file")) {
			try {
				ExifInterface exif = new ExifInterface(uri.getPath());
				int rotation = (int)exifOrientationToDegrees(
						exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
								ExifInterface.ORIENTATION_NORMAL));
				return rotation;
			} catch (IOException e) {
				Log.e(TAG, "Error checking exif", e);
			}
		}
		return 0f;
	}

	private static float exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

	
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.picture_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.pic_down:
			Thread imgDown_thread = new Thread(saveImage);
			imgDown_thread.start();
			break;
			//case R.id.pic_copy:
			//break;
		case R.id.pic_cancel:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	*/
	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
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
	void createProgressDialog(){
		pd = ProgressDialog.show(this, "", "",  true, true);
	}
	void displayToast(String display_message){
		Toast.makeText(context, display_message, Toast.LENGTH_SHORT).show();
	}
	/*
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		ImageView view = (ImageView) v;
		boolean isFirstTouch = true;
		if(isFirstTouch){
			view.setScaleType(ImageView.ScaleType.MATRIX);
			isFirstTouch = false;
		}
		// Dump touch event to log
		//			dumpEvent(event);
		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			//Log.d(TAG, "mode=DRAG");

			mode = DRAG;

			mHandler.removeMessages(LONG_PRESS);
			mHandler.removeMessages(SHOW_PRESS);

			mHandler.sendEmptyMessageAtTime(SHOW_PRESS, event.getDownTime()+(TAP_TIMEOUT / 2));
			mHandler.sendEmptyMessageAtTime(LONG_PRESS, event.getDownTime()+TAP_TIMEOUT+LONGPRESS_TIMEOUT);

			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			//Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				//Log.d(TAG, "mode=ZOOM");
			}

			break;
		case MotionEvent.ACTION_UP:
			//					Log.d(TAG,"start X: "+start.x+" Y: "+start.y);
			mHandler.removeMessages(SHOW_PRESS);
			mHandler.removeMessages(LONG_PRESS);
			//					Log.d(TAG,"end X: "+event.getX()+" Y: "+event.getY());
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;

			//				Log.d(TAG, "mode=NONE");
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG) {
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x,
						event.getY() - start.y);
				//						Log.d(TAG,"DRAG X"+event.getX()+" Y: "+event.getY());
				if(((start.x - event.getX() > 3) || start.y - event.getY() > 3) && (event.getPointerCount() == 1)){
					//							Log.d(TAG,  event.getPointerCount()+"" );
					mHandler.removeMessages(SHOW_PRESS);
					mHandler.removeMessages(LONG_PRESS);
				}
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				//Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y); 
				}
				mHandler.removeMessages(SHOW_PRESS);
				mHandler.removeMessages(LONG_PRESS);
			}

			break;
		case MotionEvent.ACTION_CANCEL:
			mHandler.removeMessages(SHOW_PRESS);
			mHandler.removeMessages(LONG_PRESS);
			break;
		}

		matrixTurning(matrix, view);
		view.setImageMatrix(matrix);

		return true; // indicate event was handled
	}
	*/
	
	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void matrixTurning(Matrix matrix, ImageView view){
		// 매트릭스 값
		float[] value = new float[9];
		matrix.getValues(value);
		float[] savedValue = new float[9];
		savedMatrix2.getValues(savedValue);

		// 뷰 크기
		int width = view.getWidth();
		int height = view.getHeight();

		// 이미지 크기
		Drawable d = view.getDrawable();
		if (d == null)  return;
		int imageWidth = d.getIntrinsicWidth();
		int imageHeight = d.getIntrinsicHeight();
		int scaleWidth = (int) (imageWidth * value[0]);
		int scaleHeight = (int) (imageHeight * value[4]);

		// 이미지가 바깥으로 나가지 않도록.
		if (value[2] < width - scaleWidth)   value[2] = width - scaleWidth;
		if (value[5] < height - scaleHeight)   value[5] = height - scaleHeight;
		if (value[2] > 0)   value[2] = 0;
		if (value[5] > 0)   value[5] = 0;

		// 10배 이상 확대 하지 않도록
		if (value[0] > 5 || value[4] > 5){
			value[0] = savedValue[0];
			value[4] = savedValue[4];
			value[2] = savedValue[2];
			value[5] = savedValue[5];
		}

		// 화면보다 작게 축소 하지 않도록
		if (imageWidth > width || imageHeight > height){
			if (scaleWidth < width && scaleHeight < height){
				int target = WIDTH;
				if (imageWidth < imageHeight) target = HEIGHT;

				if (target == WIDTH) value[0] = value[4] = (float)width / imageWidth;
				if (target == HEIGHT) value[0] = value[4] = (float)height / imageHeight;

				scaleWidth = (int) (imageWidth * value[0]);
				scaleHeight = (int) (imageHeight * value[4]);

				if (scaleWidth > width) value[0] = value[4] = (float)width / imageWidth;
				if (scaleHeight > height) value[0] = value[4] = (float)height / imageHeight;
			}
		}

		// 원래부터 작은 얘들은 본래 크기보다 작게 하지 않도록
		else{
			if (value[0] < 1)   value[0] = 1;
			if (value[4] < 1)   value[4] = 1;
		}

		// 그리고 가운데 위치하도록 한다.
		scaleWidth = (int) (imageWidth * value[0]);
		scaleHeight = (int) (imageHeight * value[4]);
		if (scaleWidth < width){
			value[2] = (float) width / 2 - (float)scaleWidth / 2;
		}
		if (scaleHeight < height){
			value[5] = (float) height / 2 - (float)scaleHeight / 2;
		}

		matrix.setValues(value);
		savedMatrix2.set(matrix);
	}
	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event) {
		// ...
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		Log.d(TAG, sb.toString());
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
			final String downloadedPath =DOWNLOAD_PATH+"/"+filename;
			FileDownloader fdown = new FileDownloader();

			try {
				fdown.downloadfile(allURL, filename);
				MediaStore.Images.Media.insertImage(getContentResolver(), downloadedPath, filename, "");
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
						Uri.parse("file://"+DOWNLOAD_PATH)));
				imgDownload_handler.sendEmptyMessage(0);
				
			} catch (FileNotFoundException e) {
				imgDownload_handler.sendEmptyMessage(1);
				//				Log.i(TAG,e.toString());
			}
		}

	}
	public Bitmap rotate(Bitmap bitmap, int degrees)
	{
		if(degrees != 0 && bitmap != null)
		{
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try
			{
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), m, true);
				if(bitmap != converted)
				{
					bitmap.recycle();
					bitmap = converted;
				}
			}
			catch(OutOfMemoryError ex)
			{
				// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
			}
		}
		return bitmap;
	}
}