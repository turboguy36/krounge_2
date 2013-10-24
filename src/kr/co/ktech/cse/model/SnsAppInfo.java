/**
 * 
 */
package kr.co.ktech.cse.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Spooky
 *
 */
public class SnsAppInfo extends SnsInfo implements Parcelable {
	String write_date;
	int reply_count;
	String photo;
	String reply_to_user_name;
	String group_name;		// 2013.03.20 hscho
	
	public SnsAppInfo(){
		
	}
	public SnsAppInfo(Parcel in){
		readFromParcel(in);
	}
	public String getWrite_date() {
		return write_date;
	}

	public void setWrite_date(String write_date) {
		this.write_date = write_date;
	}

	public int getReply_count() {
		return reply_count;
	}

	public void setReply_count(int reply_count) {
		this.reply_count = reply_count;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getReply_to_user_name() {
		return reply_to_user_name;
	}
	
	public void setReply_to_user_name(String reply_to_user_name) {
		this.reply_to_user_name = reply_to_user_name;
	}
	
	public int describeContents() {

		return 0;
	}
	
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(postId);
		dest.writeInt(superId);
		dest.writeString(title);
		dest.writeString(body);
		dest.writeInt(userId);
		dest.writeString(userName);
		dest.writeInt(groupId);
		dest.writeString(group_name);
		dest.writeInt(puser_id);
		dest.writeString(attach);
		dest.writeString(write_date);
		dest.writeString(photo);
		dest.writeString(reply_to_user_name);
		dest.writeString(photoVideo);
		dest.writeInt(reply_count);
	}
	
	private void readFromParcel(Parcel in) {
		postId = in.readInt();
		superId = in.readInt();
		title = in.readString();
		body = in.readString();
		userId = in.readInt();
		userName = in.readString();
		groupId = in.readInt();
		group_name = in.readString();
		puser_id = in.readInt();
		attach = in.readString();
		write_date = in.readString();
		photo = in.readString();
		reply_to_user_name = in.readString();
		photoVideo = in.readString();
		reply_count = in.readInt();
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public SnsAppInfo createFromParcel(Parcel in) {
			return new SnsAppInfo(in);
		}

		public SnsAppInfo[] newArray(int size) {
			return new SnsAppInfo[size];
		}
	};



}
