package kr.co.ktech.cse.activity;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.fragment.FileContentViewFragment;
import kr.co.ktech.cse.activity.fragment.HomeFragment;
import kr.co.ktech.cse.activity.fragment.PersonalLoungeFragment;
import kr.co.ktech.cse.activity.fragment.WriteMessageFragment;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.CateInfo;
import kr.co.ktech.cse.model.DataInfo;

import com.actionbarsherlock.app.ActionBar;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/**
 * 
 * @author Lee
 *
 * use number 10000 ~ 11000
 */
public class FileSearchListActivity extends BaseActivity implements OnClickListener{
	private String TAG = FileSearchListActivity.class.getSimpleName();
	private Fragment mContent;
	private boolean mFlag = false;
	private final int CLOSE_MESSAGE = 10001;
	
	public ArrayList<CateInfo> cateList;
	public static final String CATEGORY_KEY = "category_array_key";
	public static final String GROUP_CATEGORY_KEY = "group_category_array_key";
	public static final String FILE_SEARCH_TITLE = "INIT_TITLE"; 
	public static final String DATA_KEY = "DATA_INFORMATION";
	int mStackLevel = 0;
	
	private ActionBar aBar;
	
	public FileSearchListActivity() {
		super(R.string.file_search_button_text);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(AppConfig.DEBUG){
			Utils.enableStrictMode();
		}
		
		super.onCreate(savedInstanceState);
		
		// set the Above View
		if (savedInstanceState != null){
			mContent = getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
			Log.d(TAG, "savedInstanceState is not null");
		}
		if (mContent == null)
			mContent = new HomeFragment();
		
		// set the Above View
		setContentView(R.layout.content_frame);
		
		getSupportFragmentManager()
		.beginTransaction()
		.replace(R.id.content_frame, mContent)
		.commit();
		
		aBar = getSupportActionBar();
		// set the Behind View
		try{
			FrameLayout backbutton = (FrameLayout)aBar.getCustomView().findViewById(R.id.actionbarsherlock_icon);
			backbutton.setOnClickListener(this);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "mContent", mContent);
	}
	
	private int getActionBarHeight() {
	    int actionBarHeight = getSupportActionBar().getHeight();
	    if (actionBarHeight != 0)
	        return actionBarHeight;
	    final TypedValue tv = new TypedValue();
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
	            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
	    } else if (getTheme().resolveAttribute(com.actionbarsherlock.R.attr.actionBarSize, tv, true))
	        actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
	    return actionBarHeight;
	}
	
	public void addFragmentToStack(String fragment, DataInfo info) {
        mStackLevel++;
        // Instantiate a new fragment.
        
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_KEY, info);
        Fragment newFragment = null;
        if(fragment.equalsIgnoreCase(PersonalLoungeFragment.class.getSimpleName())){
        	newFragment = new PersonalLoungeFragment();
        }else if(fragment.equalsIgnoreCase(FileContentViewFragment.class.getSimpleName())){
        	newFragment = new FileContentViewFragment();
        }
        newFragment.setArguments(bundle);
        
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit);
        ft.replace(R.id.content_frame, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
	
	public void addFragmentToStack(String fragment) {
        mStackLevel++;
        Fragment newFragment = null;
        newFragment = new WriteMessageFragment();
        
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit);
        ft.replace(R.id.content_frame, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

	public void addFragmentToStack(Fragment fragment) {
        mStackLevel++;
        
        // Add the fragment to the activity, pushing this transaction
        // on to the back stack.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit);
        ft.replace(R.id.content_frame, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		mStackLevel--;
		onBackPressed();
	}
	public static int getFileTypeIcon(String extension){
		extension = extension.substring(extension.lastIndexOf(".")+1);
		if(extension.equals("") || extension.length() == 0){
			return R.drawable.blank_32;
		}else if(extension.equalsIgnoreCase("accdb")){
			return R.drawable.accdb_32;
		}else if(extension.equalsIgnoreCase("avi")){
			return R.drawable.avi_32;
		}else if(extension.equalsIgnoreCase("bmp")){
			return R.drawable.bmp_32;
		}else if(extension.equalsIgnoreCase("css")){
			return R.drawable.css_32;
		}else if(extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("docx")){
			return R.drawable.docx_32;
		}else if(extension.equalsIgnoreCase("eml")){
			return R.drawable.eml_32;
		}else if(extension.equalsIgnoreCase("eps")){
			return R.drawable.eps_32;
		}else if(extension.equalsIgnoreCase("fla")){
			return R.drawable.fla_32;
		}else if(extension.equalsIgnoreCase("gif")){
			return R.drawable.gif_32;
		}else if(extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("htm")){
			return R.drawable.html_32;
		}else if(extension.equalsIgnoreCase("ind")){
			return R.drawable.ind_32;
		}else if(extension.equalsIgnoreCase("ini")){
			return R.drawable.ini_32;
		}else if(extension.equalsIgnoreCase("jpeg")||extension.equalsIgnoreCase("jpg")){
			return R.drawable.jpeg_32;
		}else if(extension.equalsIgnoreCase("jsf")){
			return R.drawable.jsf_32;
		}else if(extension.equalsIgnoreCase("midi")){
			return R.drawable.midi_32;
		}else if(extension.equalsIgnoreCase("mov")){
			return R.drawable.mov_32;
		}else if(extension.equalsIgnoreCase("mp3")){
			return R.drawable.mp3_32;
		}else if(extension.equalsIgnoreCase("mpeg")||extension.equalsIgnoreCase("mpg")){
			return R.drawable.mpeg_32;
		}else if(extension.equalsIgnoreCase("pdf")){
			return R.drawable.pdf_32;
		}else if(extension.equalsIgnoreCase("png")){
			return R.drawable.png_32;
		}else if(extension.equalsIgnoreCase("pptx")||extension.equalsIgnoreCase("ppt")){
			return R.drawable.pptx_32;
		}else if(extension.equalsIgnoreCase("proj")){
			return R.drawable.proj_32;
		}else if(extension.equalsIgnoreCase("psd")){
			return R.drawable.psd_32;
		}else if(extension.equalsIgnoreCase("pst")){
			return R.drawable.pst_32;
		}else if(extension.equalsIgnoreCase("pub")){
			return R.drawable.pub_32;
		}else if(extension.equalsIgnoreCase("rar")){
			return R.drawable.rar_32;
		}else if(extension.equalsIgnoreCase("readme")){
			return R.drawable.readme_32;
		}else if(extension.equalsIgnoreCase("settings")){
			return R.drawable.settings_32;
		}else if(extension.equalsIgnoreCase("text")||extension.equalsIgnoreCase("txt")){
			return R.drawable.text_32;
		}else if(extension.equalsIgnoreCase("tiff")){
			return R.drawable.tiff_32;
		}else if(extension.equalsIgnoreCase("url")){
			return R.drawable.url_32;
		}else if(extension.equalsIgnoreCase("vsd")){
			return R.drawable.vsd_32;
		}else if(extension.equalsIgnoreCase("wav")){
			return R.drawable.wav_32;
		}else if(extension.equalsIgnoreCase("wma")){
			return R.drawable.wma_32;
		}else if(extension.equalsIgnoreCase("wmv")){
			return R.drawable.wmv_32;
		}else if(extension.equalsIgnoreCase("xlsx")||extension.equalsIgnoreCase("xls")){
			return R.drawable.xlsx_32;
		}else if(extension.equalsIgnoreCase("zip")){
			return R.drawable.zip_32;
		}else if(extension.equalsIgnoreCase("hwp")){
			return R.drawable.hwp_32;
		}else{
			return R.drawable.unknown_32;
		}
	}
	/**
	 * String 을 받아서 몇분, 몇시간 혹은 날짜에 대한 정보를 리턴
	 * @param strTime
	 * @return String
	 */
	public String ConvertDateFormat(String strTime){
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);
		Date date = null;
		try {
			date = sdf.parse(strTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Timestamp timestamp = new Timestamp(date.getTime());
		
		Date d = new Date();
		
		String answer_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.KOREA).format(timestamp);	
		
		String answer_tempDate = "전";
		long answer_tmp = d.getTime()-timestamp.getTime();
		if(answer_tmp <= 86400000){
			long caldate = answer_tmp/1000/60;
			if(caldate < 1){
				answer_tempDate = "방금 "+answer_tempDate;
			}else{
				long h = caldate/60;
				long m = caldate - (60*h);
				if(m != 0) answer_tempDate = m+"분 "+answer_tempDate;
				if(h != 0) answer_tempDate = h+"시간 "+answer_tempDate;				
			}
			answer_date = answer_tempDate;
		}
		else{
			answer_date = answer_date.substring(0,10);
		}
		return answer_date;
	}
}