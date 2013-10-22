package kr.khub.model;

import android.os.Parcel;
import android.os.Parcelable;


/*
 * CateInfo.java
 *
 * Created on 2007�� 11�� 19�� (��), ���� 2:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author khkim
 */
public class CateInfo implements Parcelable{
    private int id;
    private String name;
    private int priority;
    
    /** Creates a new instance of CateInfo */
    public CateInfo() {
    	id = -1;
    	name = "";
    }
    public CateInfo(Parcel in){
    	readFromParcel(in);
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public String toString() {
        return id+" "+name;
    }
    public boolean equals(Object o){
    	CateInfo f = (CateInfo)o;
    	if(this.id == f.id)
    		return true;
    	return false;
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(id);
		dest.writeInt(priority);
		dest.writeString(name);
	} 
	
	private void readFromParcel(Parcel in) {
    	id = in.readInt();
    	priority = in.readInt();
    	name = in.readString();
    }
	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public CateInfo createFromParcel(Parcel in) {
			int id = (Integer)in.readValue(getClass().getClassLoader());
			int priority = (Integer)in.readValue(getClass().getClassLoader());
			String name = (String)in.readValue(getClass().getClassLoader());
			CateInfo cInfo = new CateInfo();
			cInfo.setId(id);
			cInfo.setPriority(priority);
			cInfo.setName(name);
			
			return cInfo;
		}

		public CateInfo[] newArray(int size) {
			return new CateInfo[size];
		}
	};
}
