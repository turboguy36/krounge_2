package kr.co.ktech.cse.db;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.GroupMemberInfo;


public class KLoungeGroupRequest {
	private String TAG = "KLoungeGroupRequest";
	private KLoungeHttpRequest httprequest;

	public KLoungeGroupRequest() {
		httprequest = new KLoungeHttpRequest();
	}
	/*
	public List<GroupInfo> getGroupList(int user_id) {
		List<GroupInfo> result = new ArrayList<GroupInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLoungeGroup.jsp";
			String parameter = "user_id="+user_id;
			addr = addr+"?"+parameter;
			if(AppConfig.DEBUG)Log.d(TAG, addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//파싱
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject groupObj = jsonObj.getJSONObject("group_list");
			JSONArray groupArray = groupObj.getJSONArray("group");

			for(int i=0; i<groupArray.length(); i++) {
				JSONObject group = groupArray.getJSONObject(i);
				GroupInfo groupInfo = new GroupInfo();
				groupInfo.setGroup_id(group.getInt("group_id"));
				groupInfo.setGroup_name(group.getString("group_name"));

				result.add(groupInfo);
			}

		} catch (Exception e) {
			Log.e(TAG, "It might JSON Exception -"+e);
		}

		return result;
	}

	public List<GroupInfo> getGroupList(int user_id, int puser_id) {
		List<GroupInfo> result = new ArrayList<GroupInfo>();
		if(AppConfig.DEBUG)Log.d(TAG, user_id +"/"+puser_id);
		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLoungeGroup.jsp";
			String parameter = "user_id="+user_id+"&puser_id="+puser_id;
			addr = addr+"?"+parameter;
			if(AppConfig.DEBUG)Log.d(TAG,addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			//			if(AppConfig.DEBUG)Log.d(TAG, strJSON);

			//파싱
			JSONObject jsonObj = null;
			JSONObject groupObj = null;
			JSONArray groupArray = null;
			try{
				jsonObj = new JSONObject(strJSON);
				groupObj = jsonObj.getJSONObject("group_list");
				groupArray = groupObj.getJSONArray("group");
			}catch(JSONException j){
				Log.e(TAG, "JSON EXCEPTION -"+j);
				List<GroupInfo> ginfoList = new ArrayList<GroupInfo>();
				GroupInfo ginfo = new GroupInfo();
				ginfoList.add(ginfo);
				return ginfoList;
			}
			String group_name = null;
			for(int i=0; i<groupArray.length(); i++) {
				JSONObject group = groupArray.getJSONObject(i);
				GroupInfo groupInfo = new GroupInfo();
				groupInfo.setGroup_id(group.getInt("group_id"));
				group_name = group.getString("group_name");

				if(group_name.equals("전체")){
					try{
						group_name = group_name.replace("전체", "공개라운지");
						groupInfo.setGroup_name(group_name);
					}catch(StringIndexOutOfBoundsException e){
						Log.e(TAG, "StringIndexOutOfBoundsException -"+e);
					}
					//					if(AppConfig.DEBUG)Log.d(TAG, group_name);
				}
				groupInfo.setGroup_name(group_name);

				result.add(groupInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
*/
	public List<GroupMemberInfo> getGroupMemberList(int user_id, int group_id) {
		List<GroupMemberInfo> result = new ArrayList<GroupMemberInfo>();
		//		Log.i("GROUPMEMBER","GROUPID: "+group_id);
		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLoungeGroup.jsp";
			String parameter = "user_id="+user_id+"&group_id="+group_id;
			addr = addr+"?"+parameter;
//			Log.i("URL",addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			//			Log.i("TAG",strJSON);
			//파싱
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject groupMemberLObj = jsonObj.getJSONObject("group_member_list");
			JSONArray memberArray = groupMemberLObj.getJSONArray("member");

			for(int i=0; i<memberArray.length(); i++) {
				JSONObject member = memberArray.getJSONObject(i);
				GroupMemberInfo gmInfo = new GroupMemberInfo();

				String strDate = member.getString("member_date");
				//Timestamp date = new Timestamp(new SimpleDateFormat().parse(strDate).getTime());
				//gmInfo.setDate(date);
				gmInfo.setUserId(member.getInt("member_id"));
				gmInfo.setMember(member.getString("member_name"));
				gmInfo.setEmail(member.getString("member_email"));
				gmInfo.setCell(member.getString("member_cell"));
				gmInfo.setPhoto(member.getString("member_photo"));

				result.add(gmInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
