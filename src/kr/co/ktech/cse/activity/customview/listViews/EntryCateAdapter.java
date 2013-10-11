package kr.co.ktech.cse.activity.customview.listViews;

import java.util.ArrayList;

import kr.co.ktech.cse.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryCateAdapter extends ArrayAdapter<Item> {

	private Context context;
	private ArrayList<Item> items;
	private LayoutInflater vi;

	public EntryCateAdapter(Context context, ArrayList<Item> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
		vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Item getItem(int position) {
		// TODO Auto-generated method stub
		Item item = null;
		try{
			item = items.get(position);
		}catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		return item;
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
				EntryCateItem ei = (EntryCateItem)i;
				v = vi.inflate(R.layout.list_item_entry, null);
				final TextView title = (TextView)v.findViewById(R.id.list_item_entry_title);
				
				if (title != null) 
					title.setText(ei.title);
				v.setTag(ei);
			}
		}
		return v;
	}

}
