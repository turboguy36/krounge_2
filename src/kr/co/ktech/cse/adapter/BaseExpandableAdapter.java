package kr.co.ktech.cse.adapter;

import static kr.co.ktech.cse.CommonUtilities.KEY_VER_PREFERENCE;
import static kr.co.ktech.cse.CommonUtilities.SHARED_PREFERENCE;

import java.util.ArrayList;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.ExpandableListViewHolder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class BaseExpandableAdapter extends BaseExpandableListAdapter{
	private Context mContext;
	private String[][] mContents;
	private String[] mTitles;
	private String TAG = BaseExpandableAdapter.class.getSimpleName();
	int resource;
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
	BaseExpandableAdapter adapter;
	
	public BaseExpandableAdapter(Context c, String[] titles, String[][] contents, int _resource){
		super();

		if(titles.length != contents.length) {
			throw new IllegalArgumentException("Titles and Contents must be the same size.");
		}
		mContext = c;
		mContents = contents;
		mTitles = titles;
		resource = _resource;

		prefs = mContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);

		editor = prefs.edit();
		adapter = this;
		
		ArrayList<String> settings = new ArrayList<String>();
		settings.add(CommonUtilities.POPUP_SETTING);
		settings.add(CommonUtilities.POPUP_PREVIEW_SETTING);
		settings.add(CommonUtilities.POPUP_SOUND_SETTING);
		settings.add(CommonUtilities.POPUP_VIBRATE_SETTING);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mContents[groupPosition][childPosition];
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mContents[groupPosition].length;
	}
	@Override
	public String[] getGroup(int groupPosition) {
		return mContents[groupPosition];
	}
	@Override
	public int getGroupCount() {
		return mContents.length;
	}
	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		LayoutInflater inflator = null;
		RelativeLayout expandable_list_view;

		inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		expandable_list_view = (RelativeLayout)inflator.inflate(R.layout.expandable_list_item, null, false);
		ExpandableListViewHolder holder = (ExpandableListViewHolder)expandable_list_view.getTag();

		if(holder == null){
			holder = new ExpandableListViewHolder(expandable_list_view);
			expandable_list_view.setTag(holder);
		}
		switch(groupPosition){
		case 0:
			holder.icon.setImageResource(R.drawable.person_icon);
			break;
		case 1:
			holder.icon.setImageResource(R.drawable.setting_icon);
			break;
		case 2:
			holder.icon.setImageResource(R.drawable.inform_icon);
			break;
		case 3:
			holder.icon.setImageResource(R.drawable.setting_icon);
			break;
		}
		holder.text.setText(mTitles[groupPosition]);
		expandable_list_view.setPadding(0, 10, 0, 10);
		expandable_list_view.setBackgroundColor(Color.parseColor("#77a6bed1"));
		return(expandable_list_view);
	}
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		LayoutInflater inflator = null;
		RelativeLayout expandable_list_view;

		inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		expandable_list_view = (RelativeLayout)inflator.inflate(R.layout.expandable_list_item, null, false);
		ExpandableListViewHolder holder = (ExpandableListViewHolder)expandable_list_view.getTag();

		if(holder == null){
			holder = new ExpandableListViewHolder(expandable_list_view);
			expandable_list_view.setTag(holder);
		}

		holder.text.setText(mContents[groupPosition][childPosition]);
		/*
		if(groupPosition==1&&childPosition==0)holder.checkbox.setVisibility(View.VISIBLE);
		 */
		expandable_list_view.setBackgroundColor(Color.parseColor("#88ebf1f6"));

		if(groupPosition == 1){
			holder.checkbox.setVisibility(View.VISIBLE);
			
			setCheckBoxStature(holder, childPosition);
			grantOnCheckedChangeListener(holder, groupPosition, childPosition);
			
			switch(childPosition){
			case 0:
				AppConfig.USE_PUSH_MESSAGE = holder.checkbox.isChecked();
				// 가장 위쪽에 있는 체크박스가 체크 해제되면 모든 푸시 알림이 꺼진다.
				break;
			case 1:
				
				holder.checkbox.setEnabled(AppConfig.USE_PUSH_MESSAGE);
				
				holder.sub_text.setVisibility(View.VISIBLE);
				holder.sub_text.setText(mContext.getResources().getString(R.string.previewMessage));
				break;
			case 2:
				
				holder.checkbox.setEnabled(AppConfig.USE_PUSH_MESSAGE);
				holder.sub_text.setVisibility(View.VISIBLE);
				holder.sub_text.setText(mContext.getResources().getString(R.string.group_sns_subtext));
				break;
			case 3:
				
				holder.checkbox.setEnabled(AppConfig.USE_PUSH_MESSAGE);
				
				holder.sub_text.setVisibility(View.VISIBLE);
				holder.sub_text.setText(mContext.getResources().getString(R.string.settingSound));
				break;
			case 4:
				holder.checkbox.setEnabled(AppConfig.USE_PUSH_MESSAGE);
				break;
			}
		}else if(groupPosition == 2){
			holder.indicator.setImageResource(R.drawable.light_navigation_next_item);

			switch(childPosition){

			case 3:
				holder.indicator.setVisibility(View.GONE);
				String versionText = AppUser.MY_APP_VERSION;
				holder.versionText.setVisibility(View.VISIBLE);
				holder.versionText.setText(versionCode(versionText));
				break;
			}
		}
		return expandable_list_view;
	}
	/**
	 * Shared Preference 에서 값을 받아서 유저가 지정 한 옵션값에 따라 check 값을 보여준다.
	 * @param holder
	 * @param childPosition
	 */
	void setCheckBoxStature(ExpandableListViewHolder holder, final int childPosition){
		SharedPreferences prefs;
		prefs = mContext.getSharedPreferences(CommonUtilities.SHARED_PREFERENCE, Context.MODE_PRIVATE);
		
		String key = "";
		switch(childPosition){
		case 0:
			key = CommonUtilities.POPUP_SETTING;
			break;
		case 1:
			key = CommonUtilities.POPUP_PREVIEW_SETTING;
			break;
		case 2:
			key = CommonUtilities.POPUP_GROUP_SNS_SETTING;
			holder.checkbox.setChecked(prefs.getBoolean(key, false));
			return;
		case 3:
			key = CommonUtilities.POPUP_SOUND_SETTING;
			break;
		case 4:
			key = CommonUtilities.POPUP_VIBRATE_SETTING;
			break;
		}
		holder.checkbox.setChecked(prefs.getBoolean(key, true));
	}
	
	/**
	 * 옵션의 체크박스가 눌려 졌을 때 이벤트를 부여하기 위한 함수
	 * 
	 * @param holder : view holder list view 의 한 줄에 해당 하는 객체
	 * @param parentPosition : int
	 * @param childPosition : int
	 */
	void grantOnCheckedChangeListener(ExpandableListViewHolder holder, final int parentPosition, final int childPosition){
		
		holder.checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			String key = "";
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				switch(childPosition){
				case 0:
					key = CommonUtilities.POPUP_SETTING;
					editor.putBoolean(key, isChecked);
					editor.commit();
					notifyDataSetChanged();
					return;
				case 1:
					key = CommonUtilities.POPUP_PREVIEW_SETTING;
					break;
				case 2:
					key = CommonUtilities.POPUP_GROUP_SNS_SETTING;
					break;
				case 3:
					key = CommonUtilities.POPUP_SOUND_SETTING;
					break;
				case 4:
					key = CommonUtilities.POPUP_VIBRATE_SETTING;
					break;
				}
			editor.putBoolean(key, isChecked);
			editor.commit();
			}
		});

	}
	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	private String versionCode(String my_app_version){
		SharedPreferences prefs;
		prefs = mContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE);
		String stored_ver = prefs.getString(KEY_VER_PREFERENCE, "");
		stored_ver = "v_"+stored_ver;
		return stored_ver.equals(my_app_version)? "가장 최신 버젼" : my_app_version;
	}

}
