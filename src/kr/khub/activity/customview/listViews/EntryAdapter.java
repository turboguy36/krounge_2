package kr.khub.activity.customview.listViews;

import java.util.ArrayList;

import kr.khub.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryAdapter extends ArrayAdapter<Item> {

	private Context context;
	private ArrayList<Item> items;
	private LayoutInflater vi;

	public EntryAdapter(Context context,ArrayList<Item> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		final Item i = items.get(position);
		if (i != null) {
			if(i.isSection()){
				SectionItem si = (SectionItem)i;
				v = vi.inflate(R.layout.list_item_section, null);
				v.setBackgroundColor(context.getResources().getColor(R.color.titleColor));
				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setTextColor(context.getResources().getColor(android.R.color.white));
				sectionView.setText(si.getTitle());
			}else{
				EntryItem ei = (EntryItem)i;
				v = vi.inflate(R.layout.list_item_entry, null);
				final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
//				final ImageView image = (ImageView)v.findViewById(R.id.list_item_entry_drawable);
				
				if (title != null) 
					title.setText(ei.title);
//				if(image != null){
//					if(ei.checked){
//						image.setBackgroundResource(R.drawable.btn_like_selected);
//					}else{
//						image.setBackgroundResource(R.drawable.btn_like_defult);
//					}
//				}
				v.setTag(ei);
			}
		}
		return v;
	}

}