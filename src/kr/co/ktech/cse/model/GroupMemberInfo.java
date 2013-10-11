/*
 * GroupMemberInfo.java
 *
 * Created on 2007년 11월 19일 (월), 오후 2:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package kr.co.ktech.cse.model;

import java.sql.Timestamp;

/**
 *
 * @author khkim
 */
public class GroupMemberInfo {
	private int id;
	private String name;
	private String owner;
	private String member;
	private int authority;
	private int user_id;
	private Timestamp date;
	private String email;
	private String cell;
	private String photo;

	/** Creates a new instance of GroupMemberInfo */
	public GroupMemberInfo() {
		id = -1;
		name = "";
		owner = "";
		authority = 0;
		member = "";
		user_id = -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getAuthority() {
		return authority;
	}

	public void setAuthority(int authority) {
		this.authority = authority;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public int getUserId() {
		return user_id;
	}

	public void setUserId(int user_id) {
		this.user_id = user_id;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String toString() {
		return id+" "+name;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

}
