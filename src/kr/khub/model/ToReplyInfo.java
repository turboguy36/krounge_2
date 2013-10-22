package kr.khub.model;

import kr.khub.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class ToReplyInfo {
	int to_user_length;
	String to_user_name;
	Context mContext;
	int reply_post_id;
	
	public ToReplyInfo(String user_name, int pid, Context c) {
		super();
		
		to_user_name = user_name;
		mContext = c;
		reply_post_id = pid;
		to_user_length = user_name.length();
	}
	
	
	public int getTo_user_length() {
		return to_user_length;
	}


	public void setTo_user_length(int to_user_length) {
		this.to_user_length = to_user_length;
	}


	public String getTo_user_name() {
		return to_user_name;
	}


	public void setTo_user_name(String to_user_name) {
		this.to_user_name = to_user_name;
	}


	public Context getmContext() {
		return mContext;
	}


	public void setmContext(Context mContext) {
		this.mContext = mContext;
	}


	public int getReply_post_id() {
		return reply_post_id;
	}


	public void setReply_post_id(int reply_post_id) {
		this.reply_post_id = reply_post_id;
	}


	public SpannableStringBuilder makeToUserBox(){
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		
		TextView tv = createToUserTextView(to_user_name);
		BitmapDrawable bd = (BitmapDrawable)convertViewToDrawable(tv);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		
		sb.append(to_user_name);
		sb.setSpan(new ImageSpan(bd), 0, to_user_length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		return sb;
	}
	
	public static Object convertViewToDrawable(View view) {
		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);
	}
	
	public TextView createToUserTextView(String text){
		//creating textview dynamically
		TextView tv = new TextView(mContext);
		tv.setText(text);
		tv.setTextSize(28);
		tv.setTextColor(mContext.getResources().getColor(R.color.thickString));
		tv.setBackgroundResource(R.drawable.background_transparent_box);
		return tv;
	}
}
