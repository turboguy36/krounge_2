package kr.khub.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupCateInfo implements Parcelable{
	int group_id;
	String group_name;
	ArrayList<CateInfo> cate_list;
	public GroupCateInfo(){
		
	}
	public GroupCateInfo(int gid, String gname, ArrayList<CateInfo> cInfo){
		group_id = gid;
		group_name = gname;
		cate_list = cInfo;
	}
	public GroupCateInfo(Parcel in){
		
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
	public ArrayList<CateInfo> getCate_list() {
		return cate_list;
	}
	public void setCate_list(ArrayList<CateInfo> cate_list) {
		this.cate_list = cate_list;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(group_id);
		dest.writeString(group_name);
		dest.writeValue(cate_list);
	}
	private void readFromParcel(Parcel in){
		group_id = in.readInt();
		group_name = in.readString();
		cate_list = in.readArrayList(getClass().getClassLoader());
	}
	public static final Parcelable.Creator<GroupCateInfo> CREATOR = new Parcelable.Creator<GroupCateInfo>() {

		@Override
		public GroupCateInfo createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GroupCateInfo[] newArray(int size) {
			// TODO Auto-generated method stub
			return new GroupCateInfo[size];
		}
	};
}
