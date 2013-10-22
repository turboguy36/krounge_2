package kr.khub.model;

import kr.khub.R;
import kr.khub.util.BadgeView;
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
