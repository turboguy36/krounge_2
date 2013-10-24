package kr.co.ktech.cse.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.imageintent.AlbumStorageDirFactory;
import kr.co.ktech.cse.imageintent.BaseAlbumDirFactory;
import kr.co.ktech.cse.imageintent.FroyoAlbumDirFactory;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WriteMessage extends Activity{
	private final static String TAG = WriteMessage.class.getSimpleName();
	private final static int RESULT_LOAD_IMAGE = 7001;
	private static final int ACTION_TAKE_VIDEO = 7002;
	private final static int RESULT_TAKE_IMAGE = 7003;
	private final static int RESULT_EDIT_IMAGE = 7004;
	private final String STORED_FILE_NAME = "PICTURE_ABSOLUTE_PATH";
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private boolean FILE_CHECK = false;
	private RelativeLayout bottomBarLayout;
	private TextView tv_group_name;
	private TextView tv_message_body;
	private Button btn_load_image;
	private Button btn_take_picture;
	private Button btn_take_video;
	private Button btn_go_message;
	private ImageView imageView;
	private TextView textView;
	private ImageView attach_img_thumb;
	private String FILE_PATH;
	private int group_id = -1;
	private String group_name = "";
	private int puser_id = 0;
	private Context context;
//	private int height;
	private ImageView attached_img;
//	private Rect r = null;
	private FrameLayout.LayoutParams thumbParams = null;
//	private int zeroline = 75;
//	private int cur_line = 1;
//	private boolean mFlag = false;
//	private final int CLOSE_MESSAGE = 1;
	private String mCurrentPhotoPath;
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private Bundle extra;
	private Intent intent;
	private String INTENT_GROUP_ID = "to_group_id";
	private String INTENT_PUSER_ID = "to_puser_id";
	Bitmap mImageBitmap;
	private String json_filename = "writemsg_json.json";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null){
			Bundle bundle = savedInstanceState.getBundle("save_data");
			mCurrentPhotoPath = bundle.getString(STORED_FILE_NAME);
			FILE_PATH = mCurrentPhotoPath;
		}
		
		//custom title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_wirte_message);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);

		context = WriteMessage.this;
		Intent getintent = getIntent();
		group_id = getintent.getIntExtra(INTENT_GROUP_ID, 0);
		group_name = getintent.getStringExtra("to_group_name");
		puser_id = getintent.getIntExtra(INTENT_PUSER_ID, 0);
		
		if(AppUser.user_id <= 0){
			// Null pointer Exception 을 대비하여 파일에 저장 해 둔 앱 구동에 필요한 파일들을 로드 한다.
			loadJsonFile();
		}
		
		int thumbnail_size = CommonUtilities.DPFromPixel(context, 110);
		thumbParams = new FrameLayout.LayoutParams(thumbnail_size,thumbnail_size);
		
		makeView("",mCurrentPhotoPath);
		
//		r = new Rect();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		extra = new Bundle();
		intent = new Intent();
		extra.putInt(INTENT_GROUP_ID, group_id);
		intent.putExtras(extra);
		
	}

	private void makeView(String text, String path){
		int left, top, right, bottom = 0;
		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, 10);
		right = CommonUtilities.DPFromPixel(context, 10);
		bottom = CommonUtilities.DPFromPixel(context, 0);

		// 최 상단 그룹명
		tv_group_name = (TextView)findViewById(R.id.to_group_name_textview);
		tv_group_name.setText(group_name);

		// 글 입력 View
		tv_message_body = (TextView)findViewById(R.id.input_message_form);
		tv_message_body.setBackgroundColor(Color.parseColor("#ffffff"));
		
		if(text !=null || text.length() > 0){
			tv_message_body.setText(text);
			if(path != null){
				addAttachedPicture(path);
				FILE_CHECK = true;
			}
		}

		tv_message_body.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {

			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() == 0){
//					btn_go_message.setBackgroundResource(R.drawable.button_disabled);
					btn_go_message.setEnabled(false);
					btn_go_message.setTextColor(getApplication().getResources().getColor(R.color.textcolor_disable));
				}else{
					btn_go_message.setEnabled(true);
					btn_go_message.setTextColor(getApplication().getResources().getColor(R.color.textcolor_enable));
					//					Log.i("write_cur_line", cur_line+"");
					//					Log.i("write_past_line", tv_message_body.getLineCount()+"");
				}
			}
		});
		
		// 첨부된 이미지 위치 조정
		tv_message_body.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.showSoftInput(tv_message_body, 0);
			}
		});

		//하단 바 (사진첨부버튼, 전송버튼)
		bottomBarLayout = (RelativeLayout)findViewById(R.id.bottomBarLayout);
		btn_load_image = (Button)findViewById(R.id.add_image_button);
		btn_load_image.invalidate();
		btn_load_image.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dispatchAttachGalleryPictureIntent(RESULT_LOAD_IMAGE);
			}
		});
		
		btn_take_picture = (Button)bottomBarLayout.findViewById(R.id.take_picture_button);
		btn_take_picture.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dispatchAttachTakePictureIntent(RESULT_TAKE_IMAGE);
			}
		});
		
		btn_take_video = (Button)bottomBarLayout.findViewById(R.id.add_mov_button);
		btn_take_video.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dispatchTakeVideoIntent();
			}
		});
		
		// 전송 버튼
		RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
		btn_go_message = (Button)findViewById(R.id.go_message_button);
		btn_go_message.setLayoutParams(btnParams);
		btn_go_message.setTextSize(15);
		btn_go_message.setBackgroundResource(R.drawable.button);
		btn_go_message.setEnabled(false);
		btn_go_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final String message_body = tv_message_body.getText().toString();

				if(message_body.trim().length()>0){
					//Enter 나 빈칸만 입력 했을 경우는 글 입력이 안됨.
					
					SendMessageTask sendMessage = new SendMessageTask();
					sendMessage.execute(message_body);

				} else {
					Toast.makeText(WriteMessage.this, "게시할 메시지를 입력 해 주세요.", LENGTH_TO_SHOW).show();
				}
			}
		});
		attached_img = (ImageView)findViewById(R.id.attached_pic_view);
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp;
		
		Log.d(TAG, "imageFileName: "+imageFileName);
		
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
	
	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}
	
	private String getAlbumName() {
		return context.getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		return storageDir;
	}
	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
	}
	@Override
	protected void onDestroy(){
		storeJsonFile();
		
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		
		super.onDestroy();
	}
	private boolean loadJsonFile(){
		if(AppConfig.DEBUG)Log.d(TAG, "loadJsonFile");
		boolean result = false;
		FileInputStream fis = null;
		try {
			fis = openFileInput(json_filename);
			byte in[] = new byte[fis.available()];
			fis.read(in);
			JSONObject obj = new JSONObject(new String(in));
			AppUser.user_id = obj.getInt("app_user_id");
			AppUser.user_name = obj.getString("app_user_name");
			AppUser.user_photo = obj.getString("app_user_photo");
			group_id = obj.getInt("group_id");
			group_name = obj.getString("group_name");
			puser_id = obj.getInt("puser_id");
			
//			Log.d(TAG, "appuser: "+AppUser.user_id+
//						"/name: "+AppUser.user_name+
//						"/photo"+AppUser.user_photo+
//						"group_id" + group_id +
//						"group_name" + group_name +
//						"puser_id" + puser_id);
		}catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		}catch (JSONException e) {
			result = false;
			e.printStackTrace();
		}catch (IOException e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	private void storeJsonFile(){
		try {
			JSONObject obj = new JSONObject();
			
			obj.put("app_user_id", AppUser.user_id);
			obj.put("app_user_name", AppUser.user_name);
			obj.put("app_user_photo", AppUser.user_photo);
			obj.put("group_id", group_id);
			obj.put("group_name", group_name);
			obj.put("puser_id", puser_id);
			FileOutputStream fos = openFileOutput(json_filename, Context.MODE_PRIVATE);
			byte[] outByte = obj.toString().getBytes();
			fos.write(outByte);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	@Override
	public void onBackPressed() {
		String title = "글 작성 에서 나가기";
		String message = "이 페이지를 벗어나면 작성중인 내용은 저장되지 않습니다.";

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("확인", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setResult(RESULT_OK, intent);
				finish();    
			}

		})
		.setNegativeButton("취소", null)
		.show();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle bundle = new Bundle();
		bundle.putString(STORED_FILE_NAME, mCurrentPhotoPath);
		outState.putBundle("save_data", bundle);
		
	}
		
	private void dispatchAttachTakePictureIntent(int actionCode) {
		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			Log.e(TAG, "IOException - "+e);
			f = null;
			mCurrentPhotoPath = null;
		}
		startActivityForResult(takePhotoIntent, actionCode);
	}
	private void dispatchAttachGalleryPictureIntent(int actionCode) {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, actionCode);
	}
	private void addAttachedPicture(final String pic_path){
		//이미지 캡쳐 후 화면에 반영
		int left=0;
		int top =0;
		int right=0;
		int bottom = 0;
		
//		int oneLine_height = tv_message_body.getLineHeight();
//		int current_line = tv_message_body.getLineCount();
		
//		Log.d(TAG, "oneLine_height: "+oneLine_height);
//		Log.d(TAG, "current_line: "+current_line);
		
//		left = CommonUtilities.DPFromPixel(context, 10);
//		top = CommonUtilities.DPFromPixel(context, zeroline) + ((current_line-1)*oneLine_height);
		right = CommonUtilities.DPFromPixel(context, 10);
		bottom = CommonUtilities.DPFromPixel(context, 80);
		
		thumbParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		thumbParams.setMargins(left, top, right, bottom);
		attach_img_thumb = (ImageView)findViewById(R.id.attached_pic_view);
		attach_img_thumb.setVisibility(View.VISIBLE);
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inSampleSize = 4;
		Log.d(TAG, "pic_path : "+pic_path);
		attach_img_thumb.setImageBitmap(BitmapFactory.decodeFile(pic_path, bfo));
		attach_img_thumb.setLayoutParams(thumbParams);
		/*
		AppClickListener acListener = new AppClickListener(pic_path, WriteMessage.this);
		attach_img_thumb.setOnClickListener(acListener);
		*/
		
	}
	
	private void addAttachedPicture(Uri uri){
		//Gallery 에서 이미지를 얻어온다
		String picturePath = "";
		int left=0;int top =0;int right=0;int bottom = 0;
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		picturePath = cursor.getString(columnIndex);
		
		int oneLine_height = tv_message_body.getLineHeight();
		int current_line = tv_message_body.getLineCount();
		/*
		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, zeroline) + ((current_line-1)*oneLine_height);
		*/
		right = CommonUtilities.DPFromPixel(context, 10);
		bottom = CommonUtilities.DPFromPixel(context, 80);
//		thumbParams.gravity = Gravity.TOP;
		thumbParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		
		thumbParams.setMargins(left, top, right, bottom);
		attach_img_thumb = (ImageView)findViewById(R.id.attached_pic_view);
		attach_img_thumb.setVisibility(View.VISIBLE);
		
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inSampleSize = 4;
		
		attach_img_thumb.setImageBitmap(BitmapFactory.decodeFile(picturePath, bfo));
		attach_img_thumb.setLayoutParams(thumbParams);
		/*
		AppClickListener acListener = new AppClickListener(uri.toString(), picturePath, WriteMessage.this);
		attach_img_thumb.setOnClickListener(acListener);
		*/
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//사진 첨부 하고 돌아 왔을 때 다시 화면을 구성
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_CANCELED){
			return;
		}else if(resultCode == RESULT_OK){
			switch(requestCode){
			case RESULT_LOAD_IMAGE:
				sendIntoEditor(data);
				break;
			case RESULT_TAKE_IMAGE:
				sendIntoEditor();
				break;
			case RESULT_EDIT_IMAGE:
				addThumbnailView(data);
				break;
			}
		}
	}
	
	private void addThumbnailView(Intent data){
		FILE_PATH = data.getExtras().getString("photo_uri");
		FILE_CHECK = true;
		addAttachedPicture(FILE_PATH);
	}
	
	private void sendIntoEditor(){
		Intent intent = new Intent(context, EditImageViewActivity.class);
		intent.putExtra("photo_uri", mCurrentPhotoPath);
		startActivityForResult(intent, RESULT_EDIT_IMAGE);
	}
	
	private void sendIntoEditor(Intent data){
		Uri selectedData = data.getData();
		
		String[] filePathColumn = { MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(selectedData, filePathColumn, null, null, null);
		cursor.moveToFirst();
		FILE_PATH = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
		cursor.close();
		if(FILE_PATH.contains("http://") || FILE_PATH.contains("https://")){
			Toast.makeText(WriteMessage.this, "업로드 기능이 지원되지 않는 파일입니다.", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(context, EditImageViewActivity.class);
			intent.putExtra("photo_uri", FILE_PATH);
			intent.putExtra("from_gallery", true);
			startActivityForResult(intent, RESULT_EDIT_IMAGE);
		}
	}
	
	private void getGalleryImage(Intent data){
		Uri selectedData = data.getData();
		String picturePath = "";
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(selectedData, filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		picturePath = cursor.getString(columnIndex);
		FILE_PATH = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
		cursor.close();

		addAttachedPicture(selectedData);

		int bottom = 0;
		int top = 0;
		int left = CommonUtilities.PixelFromDP(context, 5);
		int right = CommonUtilities.PixelFromDP(context, 5);
		FrameLayout.LayoutParams btmBarParams = 
				new FrameLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, 
						ViewGroup.LayoutParams.WRAP_CONTENT);
		btmBarParams.gravity = Gravity.BOTTOM; 
		bottomBarLayout.setLayoutParams(btmBarParams);
		bottomBarLayout.setPadding(left, top, right, bottom);
		attach_img_thumb.setTag(picturePath);
		FILE_CHECK = true;
	}
	
	
	private class SendMessageTask extends AsyncTask<String, Integer, Boolean>{
		ProgressDialog progressBar;
		public SendMessageTask(){
			progressBar = new ProgressDialog(WriteMessage.this);
			progressBar.setCancelable(true);
			progressBar.setMessage("메시지 전송중...");
			progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		}
		
		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.show();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressBar.setProgress(values[0]);
		}
		@Override
		protected Boolean doInBackground(String... params) {
			boolean result = true;
			String message_body = params[0];
			KLoungeRequest kreq = new KLoungeRequest();
			
			SnsAppInfo snsinfo = new SnsAppInfo();
			snsinfo.setUserId(AppUser.user_id);
			snsinfo.setGroupId(group_id);
			snsinfo.setBody(message_body);
			snsinfo.setGroup_name(group_name);
			snsinfo.setUserName(AppUser.user_name);
			snsinfo.setPhoto(AppUser.user_photo);
			
			if(FILE_CHECK) {
				StringBuffer photo_url = new StringBuffer();
				photo_url.append(CommonUtilities.SERVICE_URL)
				.append("/data/")
				.append(AppUser.user_id)
				.append("/image/");
				
				snsinfo.setPhotoVideo(photo_url.toString());
				
				publishProgress(10);
				try {
					if(puser_id > 0) {
						progressBar.setMessage("이미지 파일을 업로드 중입니다.");
						snsinfo.setPuser_id(puser_id);
						result = kreq.sendMessageWithImage("body", "my", snsinfo, FILE_PATH, context);
					} else {
						progressBar.setMessage("이미지 파일을 업로드 중입니다.");
						result = kreq.sendMessageWithImage("body", "group", snsinfo, FILE_PATH, context);
					}
				} catch (OutOfMemoryError e) {
					Log.i(TAG+"_OutOfMemoryError",e.toString());
				} catch (Exception e) {
					Log.i(TAG+"_Exception",e.toString());
				}
			} else {
				if(puser_id > 0) {
					snsinfo.setPuser_id(puser_id);
					result = kreq.sendMessage(snsinfo, "body", "my");
				} else {
					result = kreq.sendMessage(snsinfo, "body", "group");
				}
			}

			return result;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
//			if(result){
				setResult(RESULT_OK, intent);
				progressBar.setMessage("메시지 전송을 완료하였습니다.");
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						progressBar.dismiss();
						WriteMessage.this.finish();
					}
				},1000);
			
//			}else{
//				progressBar.setMessage("메시지 전송에 실패하였습니다. 다시 시도하여 주세요");
//				Handler handler = new Handler();
//				handler.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						progressBar.dismiss();
//					}
//				},2000);
//			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
