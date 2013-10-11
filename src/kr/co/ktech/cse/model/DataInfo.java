package kr.co.ktech.cse.model;

import java.sql.Timestamp;

import android.os.Parcel;
import android.os.Parcelable;

public class DataInfo implements Parcelable{
	int p_id;
	String title;
	String attach;
	String body;
	int count;
	Timestamp date;
	int bpublic;
	int user_id;
	int cate_id;
	String keyword;
	int group_id;
	String user_name;
	int point;
	int level_id;
	int pubcate_id;
	String file_ext;
	String user_photo;
	String group_name;
	
	
	public DataInfo(){
		title = "";
		attach = "";
		body = "";
		keyword = "";
		user_name = "";
	}
	public DataInfo(Parcel in){
		readFromParcel(in);
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getPostId() {
		return p_id;
	}
	public void setPostId(int p_id) {
		this.p_id = p_id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAttach() {
		return attach;
	}
	public void setAttach(String attach) {
		this.attach = attach;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date = date;
	}
	public int getBpublic() {
		return bpublic;
	}
	public void setBpublic(int bpublic) {
		this.bpublic = bpublic;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getCate_id() {
		return cate_id;
	}
	public void setCate_id(int cate_id) {
		this.cate_id = cate_id;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public int getGroup_id() {
		return group_id;
	}
	public void setGroup_id(int group_id) {
		this.group_id = group_id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	public int getLevel_id() {
		return level_id;
	}
	public void setLevel_id(int level_id) {
		this.level_id = level_id;
	}
	public int getPubcate_id() {
		return pubcate_id;
	}
	public void setPubcate_id(int pubcate_id) {
		this.pubcate_id = pubcate_id;
	}
	public String getFile_ext() {
		return file_ext;
	}
	public void setFile_ext(String file_ext) {
		this.file_ext = file_ext;
	}
	
	public String getUser_photo() {
		return user_photo;
	}
	public void setUser_photo(String user_photo) {
		this.user_photo = user_photo;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(p_id);
		dest.writeString(title);
		dest.writeString(attach);
		dest.writeString(body);
		dest.writeInt(count);
		dest.writeString(String.valueOf(date));
		dest.writeInt(bpublic);
		dest.writeInt(user_id);
		dest.writeInt(cate_id);
		dest.writeString(keyword);
		dest.writeInt(group_id);
		dest.writeString(user_name);
		dest.writeInt(point);
		dest.writeInt(level_id);
		dest.writeInt(pubcate_id);
		dest.writeString(file_ext);
		dest.writeString(user_photo);
		dest.writeString(group_name);
	}
	
	private void readFromParcel(Parcel in){
		this.p_id = in.readInt();
		this.title = in.readString();
		this.attach = in.readString();
		this.body = in.readString();
		this.count = in.readInt();
		this.date = Timestamp.valueOf(in.readString());
		this.bpublic = in.readInt();
		this.user_id = in.readInt();
		this.cate_id = in.readInt();
		this.keyword = in.readString();
		this.group_id = in.readInt();
		this.user_name = in.readString();
		this.point = in.readInt();
		this.level_id = in.readInt();
		this.pubcate_id = in.readInt();
		this.file_ext = in.readString();
		this.user_photo = in.readString();
		this.group_name = in.readString();
	}
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

		@Override
		public Object createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			return new DataInfo(source);
		}

		@Override
		public Object[] newArray(int size) {
			// TODO Auto-generated method stub
			return new DataInfo[size];
		}
	};
}
