package kr.co.ktech.cse.adapter;

import java.util.ArrayList;
import java.util.Date;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.KLoungeActivity;
import kr.co.ktech.cse.activity.FileSearchListActivity;
import kr.co.ktech.cse.model.DataInfo;
import kr.co.ktech.cse.util.BadgeView;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DataInfoArrayAdapter extends ArrayAdapter<DataInfo>{
	private final String TAG = DataInfoArrayAdapter.class.getSimpleName();
	private Context mContext;
	private ArrayList<DataInfo> dInfo;
	private long current_time;
	
	public DataInfoArrayAdapter(Context context, int textViewResourceId,
			ArrayList<DataInfo> info) {
		super(context, textViewResourceId, info);
		// TODO Auto-generated constructor stub
		mContext = context;
		dInfo = info;
		current_time = System.currentTimeMillis();
	}
	@Override
	public DataInfo getItem(int position) {
		// TODO Auto-generated method stub
		DataInfo result = null;
		try{
			result = dInfo.get(position);
		}catch(ArrayIndexOutOfBoundsException a){
			a.printStackTrace();
		}
		return result;
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = null;
		View view;
		DisplayUtil du = new DisplayUtil(mContext);

		FileSearchListActivity pActivity = (FileSearchListActivity)mContext;
		
		if(convertView == null){
			inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.file_search_list_item, null);
		}else{
			view = convertView;
		}
		view.setTag(dInfo.get(position));
		
		/*	file icon setting	*/
		ImageView icon = (ImageView)view.findViewById(R.id.file_ext_list_icon);
		icon.setMaxHeight(du.PixelToDP(32));
		icon.setMaxWidth(du.PixelToDP(32));
		// 확장자에 맞는 아이콘을 찾아 보여준다.
		icon.setBackgroundResource(FileSearchListActivity.getFileTypeIcon(dInfo.get(position).getAttach()));
		
		/*	제목	텍스트	*/
		TextView title = (TextView)view.findViewById(R.id.file_list_title);
		title.setText(dInfo.get(position).getTitle());
		
		/*	 별점 셋팅	*/
		// 100점 만점에서 -10점씩 차이나는 점수제
		RatingBar rating_count = (RatingBar)view.findViewById(R.id.star_point_bar);
		rating_count.setStepSize((float)0.5);	// 10점에 별이 반개씩 줄어든다.
		float rating = (float)dInfo.get(position).getPoint()/20; 
		rating_count.setRating(rating);

		// 별의 갯수를 조정 할 수 없게 하기
		rating_count.setIsIndicator(true);	
		rating_count.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		rating_count.setFocusable(false);
		
		// 몇점인지 수치로 보여 준다.
		TextView rating_text = (TextView)view.findViewById(R.id.rating_bar_text);
		rating_text.setText(String.valueOf(rating));

		/*	   등록된 날짜 | 조회수 셋팅	*/
		StringBuffer written_date_n_count = new StringBuffer();
		String date = pActivity.ConvertDateFormat(String.valueOf(dInfo.get(position).getDate()));
		written_date_n_count.append(date);
		written_date_n_count.append("  |  ");
		written_date_n_count.append(dInfo.get(position).getCount());
		written_date_n_count.append(mContext.getResources().getString(R.string.be_read_count));
		
		TextView date_n_count = (TextView)view.findViewById(R.id.file_list_date);
		date_n_count.setText(written_date_n_count);
		
		/*	 가장 오른 쪽 화살표 셋팅	*/
		ImageView indicator = (ImageView)view.findViewById(R.id.menu_list_indicator);
		indicator.setVisibility(View.VISIBLE);
		
		return view;
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
}
