package kr.co.ktech.cse.model;

import kr.co.ktech.cse.R;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListViewHolder {
	public ImageView icon = null;
	public TextView text = null;
	public ImageView indicator = null;
	public CheckBox checkbox = null;
	public TextView versionText = null;
	public TextView sub_text = null;
	public ExpandableListViewHolder(View row){
		this.icon = (ImageView)row.findViewById(R.id.expandable_list_icon);
		this.text = (TextView)row.findViewById(R.id.expandable_list_text);
		this.sub_text = (TextView)row.findViewById(R.id.expandable_list_subtext);
		this.indicator = (ImageView)row.findViewById(R.id.expandable_list_indicator);
		this.checkbox = (CheckBox)row.findViewById(R.id.expandable_list_checkbox);
		this.versionText = (TextView)row.findViewById(R.id.expandable_list_version);
	}
}
