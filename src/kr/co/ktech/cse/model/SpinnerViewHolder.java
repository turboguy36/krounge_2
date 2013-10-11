package kr.co.ktech.cse.model;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.util.BadgeView;
import android.view.View;
import android.widget.TextView;

public class SpinnerViewHolder {
	public TextView group_name = null;
	public BadgeView badge = null;
	public SpinnerViewHolder(View row){
		group_name = (TextView)row.findViewById(R.id.spinner_text_dropdown);
		badge = (BadgeView)row.findViewById(R.id.badgeview_spinner);
	}
}
