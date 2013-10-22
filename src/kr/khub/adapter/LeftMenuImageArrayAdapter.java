package kr.khub.adapter;

import java.util.ArrayList;
import java.util.List;

import kr.khub.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

public class LeftMenuImageArrayAdapter extends ArrayAdapter<String>{
	private Context mContext;
	private ArrayList<String> cateList;
//	TypedArray icons;
	
	public LeftMenuImageArrayAdapter(Context context, int textViewResourceId,
			ArrayList<String> list) {
		super(context, textViewResourceId, list);
		// TODO Auto-generated constructor stub
		
		this.mContext = context;
		this.cateList = list;
		
//		icons = mContext.getResources().obtainTypedArray(R.array.slide_menu_icons);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return super.getCount();
	}
	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return super.getItem(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}
	@Override
	public int getPosition(String item) {
		// TODO Auto-generated method stub
		return super.getPosition(item);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflator = null;
		RelativeLayout list_view;

		inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		list_view = (RelativeLayout)inflator.inflate(R.layout.list_item, null);
		ListItemHolder holder = (ListItemHolder)list_view.getTag();
		if(holder == null){
			holder = new ListItemHolder(list_view);
			list_view.setTag(holder);
		}
		DisplayUtil du = new DisplayUtil(mContext);
		int left = du.PixelToDP(16);
		int top = du.PixelToDP(16);
		int right = du.PixelToDP(16);
		int bottom = du.PixelToDP(16);
		
		holder.icon.setPadding(left, top, right, bottom);
//		holder.icon.setImageDrawable(icons.getDrawable(position));
		holder.text.setText(cateList.get(position));
		holder.indicator.setPadding(0, 0, right, 0);
//		holder.indicator.setImageResource(R.drawable.icon_sidebar_0);
		return list_view;
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
