package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.util.LoadImageUtil;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Color;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.FragmentActivity;
import android.telephony.*;
import android.text.Html;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Myprofile extends FragmentActivity{
	private final String SAVED_INSTANCE_MYPROFILE = this.toString()+"MYPROFILE";
	private final String SAVED_INSTANCE_USER_PHOTO = this.toString()+"USER_PHOTO";
	private SharedPreferences pref;
	private ImageView personal_image;
	private TextView personal_user_name;
	private TextView user_phoneNumber;
	private Button closebtn;
	public ProgressDialog pd;
	private int puser_id = 0;
	private Context context;
	private String puser_name = "";
	private String puser_photo = "";
	private String TAG = Myprofile.class.getSimpleName();
	DisplayUtil du;
//	LoadImageUtil imageUtil;
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private final String NO_IMAGE_URL = CommonUtilities.SERVICE_URL + "/images/sns/no_photo_small.gif";
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(savedInstanceState != null){
			Bundle bundle = savedInstanceState.getBundle(SAVED_INSTANCE_MYPROFILE);
			AppUser.user_photo = bundle.getString(SAVED_INSTANCE_USER_PHOTO);
		}
		context = getApplicationContext();

		du = new DisplayUtil(context);
		pref = getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);

		puser_id = 	pref.getInt("user_id", 0);
		puser_name = pref.getString("user_name", "");
		puser_photo = AppUser.user_photo;

		super.onCreate(savedInstanceState);

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(this, mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_myprofile);
//		imageUtil = new LoadImageUtil();
		makeView();
	}

	void makeView(){
		personal_user_name = (TextView)findViewById(R.id.user_name);
		personal_user_name.setText(Html.fromHtml("<b>"+puser_name+"</b>"+ " 님"));
		int margin = du.PixelToDP(10);
		
		personal_image = (ImageView)findViewById(R.id.user_imageview);
		LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(du.PixelToDP(400), du.PixelToDP(400));
		ivParams.setMargins(margin,margin,margin,margin);

		TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNum = telManager.getLine1Number();
		
		if(!puser_photo.equals("")){
			puser_photo = puser_photo.replace(" ", "%20");
//			mImageFetcher.loadImage(puser_photo, personal_image);
//			imageUtil.loadImage(personal_image, puser_photo, puser_id);
			mImageFetcher.loadImage(puser_photo, personal_image);
		}else{
			personal_image.setImageResource(R.drawable.no_profile_photo);
		}
		
		personal_image.setScaleType(ImageView.ScaleType.FIT_XY);
		personal_image.setLayoutParams(ivParams);

		user_phoneNumber = (TextView)findViewById(R.id.user_info_phone);
		user_phoneNumber.setText(phoneNum.replaceAll("^(01[0-9])([0-9]+)([0-9][0-9][0-9][0-9])$", "$1"+"-"+"$2"+"-"+"$3"));
		user_phoneNumber.setBackgroundColor(Color.WHITE);
		closebtn = (Button)findViewById(R.id.close);
		closebtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle bundle = new Bundle();
		bundle.putString(SAVED_INSTANCE_USER_PHOTO, AppUser.user_photo);
	}

	@Override
	public void onResume() {
		mImageFetcher.setExitTasksEarly(false);
		super.onResume();
	}

	@Override
	public void onPause() {
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
//		imageUtil = null;
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		mImageFetcher.closeCache();
		super.onDestroy();
	}
	class DisplayUtil {
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
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = 12;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}
}