package kr.co.ktech.cse.activity.customview.listViews;


public class EntryItem implements Item{

	public final String title;
	public final int cate_id;
	public boolean checked;

	public EntryItem(String title, int cate_id) {
		this.title = title;
		this.cate_id = cate_id;
	}
	
	@Override
	public boolean isSection() {
		return false;
	}
	
	public void setCheck(boolean check){
		checked = check;
	}

	public int getCate_id() {
		return cate_id;
	}
	
}
