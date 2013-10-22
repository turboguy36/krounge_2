package kr.khub.model;

import java.sql.Timestamp;

public class SnsInfo {
	int postId;
	int superId;
	String title;
	String body;
	int userId;
	String userName;
	int count;
	Timestamp date;
	int groupId;
	int puser_id;
	String attach;
	String photoVideo;
	String photo;
	
	public SnsInfo() {
		postId = 0;
		userId = 1;
		userName = "관리자";
		count = 0;
	}
	
	public int getPostId() {
		return postId;
	}
	public void setPostId(int postId) {
		this.postId = postId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public int getSuperId() {
		return superId;
	}
	public void setSuperId(int superId) {
		this.superId = superId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
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

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getPostId()).append(" ").append(this.getTitle());
		return sb.toString();
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getPuser_id() {
		return puser_id;
	}

	public void setPuser_id(int puser_id) {
		this.puser_id = puser_id;
	}

	public String getAttach() {
		return attach;
	}

	public void setAttach(String attach) {
		this.attach = attach;
	}
	
	public String getPhotoVideo() {
		return photoVideo;
	}

	public void setPhotoVideo(String photoVideo) {
		this.photoVideo = photoVideo;
	}
	
	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
