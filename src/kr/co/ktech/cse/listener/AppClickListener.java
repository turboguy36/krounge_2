package kr.co.ktech.cse.listener;

import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.activity.TouchImageViewActivity;
import kr.co.ktech.cse.activity.TouchUserImageViewActivity;
import kr.co.ktech.cse.model.SnsAppInfo;
import android.R.bool;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class AppClickListener implements OnClickListener{
	private String url;
	private int reply_cnt;
	private String body;
	private SnsAppInfo snsInfo_inListener;
	private Context context;
	private String img_url;
	private String file_path;
	Intent intent;
	Bitmap bm;
	private boolean whichOfOne = false; // user_photo ? body_content_photo
	
	public AppClickListener(String img_url, int cnt, String html_body){
		url = img_url;
		reply_cnt = cnt;
		body = html_body;
		whichOfOne = false;
	}
	public AppClickListener(SnsAppInfo sainfo, Context mContext){
		snsInfo_inListener = sainfo;
		context = mContext;
		whichOfOne = true;
	}
	public AppClickListener(String url, Context mContext){
		whichOfOne = false;
		img_url = url;
		context = mContext;
	}
	public AppClickListener(String url, String filepath, Context mContext){
		whichOfOne = false;
		img_url = url;
		file_path = filepath;
		context = mContext;
	}
	public AppClickListener(Bitmap bitmap, Context mContext){
		whichOfOne = false;
		bm = bitmap;
		context = mContext;
	}
	
	@Override
	public void onClick(View v) {
		if(whichOfOne){
			intent = new Intent(context, TouchImageViewActivity.class);
			intent.putExtra("snsAppInfo", snsInfo_inListener);
		}else{
			if(img_url != null){
				intent = new Intent(context, TouchUserImageViewActivity.class);
				intent.putExtra("user_photo", file_path);
				intent.putExtra("photo_uri", img_url);
			}else if(bm != null){
				intent = new Intent(context, TouchUserImageViewActivity.class);
				intent.putExtra("BitmapImage", bm);
			}
		}
		context.startActivity(intent);
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}