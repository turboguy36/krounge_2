package kr.co.ktech.cse.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SpinnerViewHolder;
import kr.co.ktech.cse.util.RecycleUtils;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

public class SpinAdapter extends ArrayAdapter<GroupInfo> {
	private static final int SPINNER_TEXT_COLOR = 0xAA000000;
	private static final int SPINNER_TEXT_SIZE = 17;
	private static final int SPINNER_HEIGHT = 47;
	private static final int SPINNER_BGCOLOR = 0xFFBECCD7;
	private Context context;
	private String TAG = SpinAdapter.class.getSimpleName();

	private GroupInfo[] ginfo;

	private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

	public SpinAdapter(final Context context, 
			final int textViewResourceId, 
			final ArrayList<GroupInfo> objects) {

		super(context, textViewResourceId, objects);
		this.context = context;

		ginfo = new GroupInfo[objects.size()];
		for(int i=0; i<ginfo.length; i++) {
			ginfo[i] = new GroupInfo();
			ginfo[i].setGroup_id(objects.get(i).getGroup_id());
			ginfo[i].setGroup_name(objects.get(i).getGroup_name());
			ginfo[i].setHasNewMessage(objects.get(i).hasNewMessage());
		}
	}

	//onDestory에서 쉽게 해제할 수 있도록 메소드 생성
	public void recycle() {
		for (WeakReference<View> ref : mRecycleList) {
			RecycleUtils.recursiveRecycle(ref.get());
		}
	}

	@Override
	public int getCount() {
		return ginfo.length;
	}
	@Override
	public GroupInfo getItem(int position) {
		GroupInfo result = null;
		try{
			result = ginfo[position];
		}catch(ArrayIndexOutOfBoundsException a){
			Log.e(TAG, "ArrayIndexOutOfBoundsException: "+a);
			return new GroupInfo();
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return result;
	}
	@Override
	public long getItemId(int position) {
		long result = 0L;
		try{
			result = ginfo[position].getGroup_id();
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return result;
	}
	public GroupInfo getGroupInfo(int pos) {
		GroupInfo result_ginfo = null;
		try{
			result_ginfo = ginfo[pos];
		}catch(ArrayIndexOutOfBoundsException ae){
			
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return result_ginfo;
	}

	@Override
	public int getPosition(GroupInfo group_info) {
		int pos = 0;
		try{
			for(pos = 0; pos < ginfo.length; pos++) {
				GroupInfo gi = ginfo[pos];
				if(gi.getGroup_id() == group_info.getGroup_id()) break;
			}
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return pos;
	}

	@Override
	public FrameLayout getView(int position, View convertView, ViewGroup parent) {
		Boolean IS_GETVIEW = true;
		FrameLayout return_layout = null;
		try{
			return_layout = customView(position, 
					convertView, 
					parent, 
					IS_GETVIEW, 
					ginfo[position].hasNewMessage());
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return return_layout;
	}
	@Override
	public FrameLayout getDropDownView(int position, View convertView, ViewGroup parent) {
		Boolean IS_GETVIEW = false;
		FrameLayout dropdown_view = null;
		try{
			dropdown_view = customView(position, 
					convertView, 
					parent, 
					IS_GETVIEW, 
					ginfo[position].hasNewMessage());
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}
		return dropdown_view;
	}

	private FrameLayout customView(int position, 
			View convertView, ViewGroup parent, Boolean flag, Boolean badge){

		Log.d(TAG, "customView(): ");

		LayoutInflater inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		FrameLayout parent_view = 
				(FrameLayout)inflator.inflate(R.layout.spinner_image_style, null);

		SpinnerViewHolder holder = (SpinnerViewHolder)parent_view.getTag();
		if(holder == null){
			holder = new SpinnerViewHolder(parent_view);
			parent_view.setTag(holder);
		}

		int topNbottom = flag? 0 : 10;
		int spinner_height = flag? SPINNER_HEIGHT : SPINNER_HEIGHT + 23;
		int left,top,right,bottom;
		left = CommonUtilities.DPFromPixel(context, 20);
		top = CommonUtilities.DPFromPixel(context, topNbottom);
		right = CommonUtilities.DPFromPixel(context, 0);
		bottom = CommonUtilities.DPFromPixel(context, topNbottom);

		try{
			holder.group_name.setText(ginfo[position].getGroup_name());
			holder.group_name.setGravity(Gravity.CENTER_VERTICAL);
			holder.group_name.setTextColor(SPINNER_TEXT_COLOR);
			holder.group_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, SPINNER_TEXT_SIZE);
			holder.group_name.setHeight(CommonUtilities.DPFromPixel(context, spinner_height));
			holder.group_name.setPadding(left, top, right, bottom);
			if(!flag)holder.group_name.setBackgroundColor(SPINNER_BGCOLOR);

			if(badge){holder.badge.setVisibility(View.VISIBLE);}
			else{holder.badge.setVisibility(View.INVISIBLE);}

		}catch(ArrayIndexOutOfBoundsException a){
			Log.e(TAG, "ArrayIndexOutOfBoundsException : "+a);
		}catch(NullPointerException ne){
			ne.printStackTrace();
		}

		// 메모리 해제할 View를 추가
		mRecycleList.add(new WeakReference<View>(parent_view));

		return parent_view;
	}
}
