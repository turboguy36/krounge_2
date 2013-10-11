package kr.co.ktech.cse.adapter;

import kr.co.ktech.cse.R;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ListViewHolder {
	public ImageView photo;
	public TextView user_name;
	public TextView to_name;
	public TextView date;
	public TextView body;
	
	public ListViewHolder(RelativeLayout row) {
		photo = (ImageView)row.findViewById(R.id.user_image_view);
		user_name = (TextView)row.findViewById(R.id.user_name);
		to_name  = (TextView)row.findViewById(R.id.reply_to_user);
		body = (TextView)row.findViewById(R.id.reply_body);
		date = (TextView)row.findViewById(R.id.written_date);
	}
}
