package kr.co.ktech.cse.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.model.GroupMemberInfo;
import kr.co.ktech.cse.util.LoadImageUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImageAdapter extends BaseAdapter{
	private Context mContext;
	//	private final String[] memberNames;
	private List<GroupMemberInfo> memberList;
	private static LoadImageUtil liu = new LoadImageUtil();

	public ImageAdapter(Context c, List<GroupMemberInfo> gmlist){
		mContext = c;
		memberList = gmlist;
	}
	public int getCount() {
		return memberList.size();
	}

	public GroupMemberInfo getItem(int pos) {
		return memberList.get(pos);
	}

	public long getItemId(int pos) {
		return memberList.get(pos).getId();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = null;
		View gridView;
		if(convertView == null){
			inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			gridView = inflater.inflate(R.layout.one_member, null);
		}else{
			gridView = convertView;
		}

		final GroupMemberInfo gmi = memberList.get(position);

		if(gmi != null){
			//Toast.makeText(mContext, gmi.getMember() , Toast.LENGTH_SHORT).show();
			gridView.setTag(gmi);
		} else {
			Toast.makeText(mContext, "Failed to load", Toast.LENGTH_SHORT).show();
		}
		GroupMemberInfo loadgmi = (GroupMemberInfo)gridView.getTag();

		// set value into textview
		TextView textView = (TextView)gridView.findViewById(R.id.grid_item_label);
		textView.setText(gmi.getMember());

		// set image based on selected text
		final ImageView imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);

		TextView hiddenView = (TextView)gridView.findViewById(R.id.grid_item_userId);
		hiddenView.setText(String.valueOf(gmi.getUserId()));
//		hiddenView.setVisibility(View.GONE);

		TextView uri_view = (TextView)gridView.findViewById(R.id.grid_item_uri);
		uri_view.setText(String.valueOf(gmi.getPhoto()));
//		uri_view.setVisibility(View.GONE);

		liu.loadImage(imageView, gmi.getPhoto(), gmi.getUserId());

		return gridView;
//		return convertView;
	}

}
