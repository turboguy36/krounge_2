package kr.co.ktech.cse.adapter;

import java.util.List;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ContentArrayAdapter extends BaseAdapter implements OnClickListener{
	private final Context context;
	private List<SnsAppInfo> sinfo_array;
	private ImageFetcher mImageFetcher;
	private static final String TAG = ContentArrayAdapter.class.getSimpleName();
	
	public ContentArrayAdapter(Context context, 
			ImageFetcher fetcher, List<SnsAppInfo> sinfoList) {
		this.context = context;
		this.mImageFetcher = fetcher;
		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		sinfo_array = sinfoList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rowView = (RelativeLayout)inflater.inflate(R.layout.reply_one_content, null);
		
		ListViewHolder holder = (ListViewHolder)rowView.getTag();
		if(holder == null){
			holder = new ListViewHolder(rowView);
			rowView.setTag(holder);
		}
		SnsAppInfo sinfo = sinfo_array.get(position);
		
		try{
			/*
			 * user photo 를 셋팅
			 * */
			mImageFetcher.loadImage(sinfo.getPhoto().replace(" ", "%20") , holder.photo);
			holder.photo.setTag(R.id.arg1, sinfo.getUserId());
			holder.photo.setTag(R.id.arg2, sinfo.getUserName());
			holder.photo.setTag(R.id.arg3, sinfo.getPhoto());
			holder.photo.setOnClickListener(this);
			
			/*
			 * user name 을 셋팅
			 * */
			holder.user_name.setText(sinfo.getUserName());
			holder.user_name.setTag(R.id.arg1, sinfo.getPhoto());
			holder.user_name.setTag(R.id.arg2, sinfo.getUserId());
			holder.user_name.setOnClickListener(this);
			
			/*
			 * 댓글 to user 셋팅
			 * */
			if(!(sinfo.getReply_to_user_name().equals("")) ||
				sinfo.getReply_to_user_name().length() > 0){
				String to_user_name = "in reply to " + sinfo.getReply_to_user_name();
				holder.to_name.setText(to_user_name);
				holder.to_name.setVisibility(View.VISIBLE);
			}

			/*
			 * 댓글 body 를 셋팅
			 * */
			holder.body.setText(KLoungeFormatUtil.bodyURLFormat(sinfo.getBody()));
			
			/*
			 * 날짜 셋팅
			 * */
			holder.date.setText(String.valueOf(sinfo.getWrite_date()));
		}catch(ArrayIndexOutOfBoundsException ae){
			ae.printStackTrace();
		}
		
		return rowView;
	}

	@Override
	public int getCount() {
		return sinfo_array.size();
	}

	@Override
	public Object getItem(int position) throws ArrayIndexOutOfBoundsException{
		return sinfo_array.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return sinfo_array.get(position).getPostId();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String user_name = "";
		String user_photo = "";
		int user_id = -1;
		
		switch(v.getId()){
		case R.id.user_name:
			user_name = ((TextView)v).getText().toString();
			user_photo = (String)v.getTag(R.id.arg1);
			user_id = (Integer)v.getTag(R.id.arg2);
			break;
		case R.id.user_image_view:
			user_id = (Integer)v.getTag(R.id.arg1);
			user_name = (String)v.getTag(R.id.arg2);
			user_photo = (String)v.getTag(R.id.arg3);
			break;
		
		}
		Intent intent = new Intent(context, PersonalLounge.class);
		intent.putExtra("puser_id", String.valueOf(user_id));
		intent.putExtra("puser_name", user_name);
		intent.putExtra("puser_photo", user_photo);
		context.startActivity(intent);
	}
} 
