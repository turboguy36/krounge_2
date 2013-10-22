package kr.khub.activity.customview.listViews;


public class EntryCateItem implements Item{

	public final String title;
	public final int cate_id;
	public final int group_id;
	public boolean checked;

	public EntryCateItem(String title, int cate_id, int group_id) {
		this.title = title;
		this.cate_id = cate_id;
		this.group_id = group_id;
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
