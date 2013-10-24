package kr.co.ktech.cse.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditImageViewActivity extends Activity{
	/*
	 * 사진 다시 찍기 onActivityResult 의 result 값들
	 * */
	private static final int RETAKE_IMAGE = 7010;
	private static final int GALLERY_IMAGE = 7011;
	private static final int RESELECT_IMAGE = 7012;
	
	private String TAG = EditImageViewActivity.class.getSimpleName();
	private Context context;
	
	/*
	 * 화면에 표시되는 Bitmap 인 동시에,
	 * WriteMessage 로 돌아 갈 때 jpg 파일을 만들 때 쓰이는 이미지
	 * */
	private Bitmap bitmap;
	
	private ImageView mImage;
	private RelativeLayout wholeView;
	private LinearLayout bottomBar;
	private TextView topBar;
	private Button rotation_img_btn;
	private Button retake_pic_text;
	private Button apply_pic_text;

	private int visible = View.VISIBLE;
	private boolean from_gallery;
	private int rotation_degree = 90;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (AppConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_image);
		context = getApplicationContext();

		Intent intent = getIntent();
		
		// WriteMessage 에서 이미 만들어서 전달한 경로
		from_gallery = intent.getBooleanExtra("from_gallery", false);
		
		makeView(); 
	}

	private void makeView(){
		String img_uri = getIntent().getStringExtra("photo_uri");
		
		wholeView = (RelativeLayout)findViewById(R.id.preview_rela_layout);

		topBar = (TextView)wholeView.findViewById(R.id.top_bar_editimage);

		bottomBar = (LinearLayout)wholeView.findViewById(R.id.bottom_bar_editimage);
		rotation_img_btn = (Button)bottomBar.findViewById(R.id.btn_rotation_img);
		retake_pic_text = (Button)bottomBar.findViewById(R.id.retake_picture);
		
		if(from_gallery){
			// gallery 에서 선택한 뒤라면
			retake_pic_text.setText("다시 선택");
		}
		
		apply_pic_text = (Button)bottomBar.findViewById(R.id.attach_this_picture);
		
		if(img_uri.contains("http://") || img_uri.contains("https://")){
			// Facebook 이나 백업 cloud 와 같은 online 사진을 선택 했을 경우
			GetImageBitmapTask gTask = new GetImageBitmapTask();
			gTask.execute(img_uri);
		}else{
			mImage = (ImageView)findViewById(R.id.oneEditImage);
			mImage.setImageBitmap(getOriginAngleBitmap(img_uri));
			mImage.setTag(img_uri);
			
			mImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
					setVisibilityOfControlers(visible);
				}
			});
		}
		rotation_img_btn.setOnClickListener(mRotateImageClickListener);
		apply_pic_text.setOnClickListener(mApplyImageClickListener);
		retake_pic_text.setOnClickListener(mRetakeImageClickListener);
	}

	private Bitmap getOriginAngleBitmap(String img_url){
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inSampleSize = 2;
		Bitmap oBitmap = BitmapFactory.decodeFile(img_url, bfo);
		
		try{
			ExifInterface exif;
			exif = new ExifInterface(img_url);
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

			float angle = 0;
			switch(orientation){
			case 3:
				angle = 180;
				break;
			case 6:
				angle = 90;
				break;
			case 8:
				angle = 270;
				break;
			}
			Matrix matrix = new Matrix();
			matrix.postRotate(angle);
			oBitmap = Bitmap.createBitmap(oBitmap, 0, 0, oBitmap.getWidth(), oBitmap.getHeight(),matrix, true);
		}catch(IOException e){
			Log.e(TAG, "EXIF IOException");
			e.printStackTrace();
		}catch(NullPointerException ne){
			Log.e(TAG, "NullPointerException - "+ne);
		}
		bitmap = oBitmap;
		return oBitmap;
	}

	private void setVisibilityOfControlers(int visibility) {
		// TODO Auto-generated method stub
		bottomBar.setVisibility(visibility);
		topBar.setVisibility(visibility);
	}

	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		
		super.onDestroy();
	}

	Button.OnClickListener mRotateImageClickListener =
			new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			mImage.setImageBitmap(rotate(rotation_degree));
		}
	};
	
	Button.OnClickListener mRevertImageClickListener =
			new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String img_uri = (String)mImage.getTag();
			mImage.setImageURI(Uri.parse(img_uri));
		}
	};
	
	Button.OnClickListener mApplyImageClickListener =
			new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String img_uri = (String)mImage.getTag();
			if(img_uri.contains("http://") || img_uri.contains("https://")){
				
			}else{
				MakeEdidtedFile makeFile = new MakeEdidtedFile();
				makeFile.execute();
			}
		}
	};
	
	Button.OnClickListener mRetakeImageClickListener =
			new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			if(from_gallery){
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESELECT_IMAGE);
			}else{
				Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

				File f = null;
				String img_uri = (String)mImage.getTag();
				f = new File(img_uri);
				img_uri = f.getAbsolutePath();
				takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));

				startActivityForResult(takePhotoIntent, RETAKE_IMAGE);
			}
		}
	};
	
	public Bitmap rotate(int degrees){
		if(degrees != 0 && bitmap != null){
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try{
				Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), m, true);
				if(bitmap != converted){
					bitmap.recycle();
					bitmap = converted;
				}
			}
			catch(OutOfMemoryError ex){
				// 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
			}
		}
		return bitmap;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
		case RESELECT_IMAGE:
			Uri reselectedData = data.getData();
			String[] fileColumn = {MediaStore.Images.Media.DATA};
			Cursor cursor_re = getContentResolver().query(reselectedData, fileColumn, null, null, null);
			cursor_re.moveToFirst();
			String img_uri_res = cursor_re.getString(cursor_re.getColumnIndex(MediaStore.MediaColumns.DATA));
			cursor_re.close();
			mImage.setTag(img_uri_res);
			mImage.setImageURI(Uri.parse(img_uri_res));
			bitmap = BitmapFactory.decodeFile(img_uri_res);
			break;
		case RETAKE_IMAGE:
			String img_uri_cam = (String)mImage.getTag();
			mImage.setImageBitmap(getOriginAngleBitmap(img_uri_cam));
			break;
		case GALLERY_IMAGE:
			Uri selectedData = data.getData();
			String[] filePathColumn = {MediaStore.Images.Media.DATA};
			Cursor cursor = getContentResolver().query(selectedData, filePathColumn, null, null, null);
			cursor.moveToFirst();
			String img_uri = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			mImage.setTag(img_uri);
			cursor.close();
			break;
		}
	}
	class MakeEdidtedFile extends AsyncTask<Void, Void, Boolean>{
		String pic_uri;

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			String img_uri = (String)mImage.getTag();
			
			String directory = img_uri.substring(0, img_uri.lastIndexOf("/")+1);
			String filename = img_uri.substring(img_uri.lastIndexOf("/")+1);
			
			pic_uri = directory + "EDITED_" + filename;
			File copyFile = new File(pic_uri);
			OutputStream out = null;
			try{
				copyFile.createNewFile();
				out = new FileOutputStream(copyFile);
				int resizeX = 600;
				int resizeY = 800;
				Bitmap resize = Bitmap.createScaledBitmap(bitmap, resizeX, resizeY, true);

				result = resize.compress(CompressFormat.JPEG, 75, out);

			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					out.flush();
					out.close();
//					copyFile.delete();
				}catch(IOException ie){
					ie.printStackTrace();
				}
			}
			return result;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(result){
				Bundle extra = new Bundle();
				Intent intent = new Intent();
				extra.putString("photo_uri", pic_uri);//pic_uri 가 아닌 img_uri 가 오게되면 이미지가 옆으로 회전이 될지도 모른다. 주의하자.
				intent.putExtras(extra);
				EditImageViewActivity.this.setResult(RESULT_OK, intent);
				EditImageViewActivity.this.finish();
			}else{
				String errorMsg = context.getResources().getString(R.string.make_file_error);
//				displayToast(errorMsg);
			}
		}
	}
	

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private static void recycleBitmap(ImageView iv) {
		Drawable d = iv.getDrawable();
		if (d instanceof BitmapDrawable) {
			Bitmap b = ((BitmapDrawable)d).getBitmap();
			b.recycle();
		} // 현재로서는 BitmapDrawable 이외의 drawable 들에 대한 직접적인 메모리 해제는 불가능하다.
		d.setCallback(null);
	}
	class GetImageBitmapTask extends AsyncTask<String, Void, Bitmap>{
		@Override
		protected Bitmap doInBackground(String... params) {
			return getImageBitmap(params[0]);
		}
		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			bitmap = result;
			mImage = (ImageView)findViewById(R.id.oneEditImage);
			//		mImage.setDisplayType(DisplayType.FIT_TO_SCREEN);
			mImage.setImageBitmap(bitmap);//(Uri.parse(img_uri));
			mImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					visible = (visible == View.VISIBLE) ? View.GONE : View.VISIBLE;
					setVisibilityOfControlers(visible);
				}
			});
		}
		private Bitmap getImageBitmap(String url){
			Bitmap bm = null;
			try {
				URL aURL = new URL(url);
				URLConnection conn = aURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
				bm = BitmapFactory.decodeStream(bis);
				bis.close();
				is.close();
			} catch (IOException e) {
				Log.e(TAG, "Error getting bitmap", e);
			}
			return bm; 
		}
		private void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath) {
			File fileCacheItem = new File(strFilePath);
			OutputStream out = null;
			try{
				fileCacheItem.createNewFile();
				out = new FileOutputStream(fileCacheItem);
				bitmap.compress(CompressFormat.JPEG, 100, out);
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				try{
					out.close();
				}catch (IOException e){
					e.printStackTrace();
				}
			}
		}
	}
}