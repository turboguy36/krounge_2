package kr.co.ktech.cse.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.CateInfo;
import kr.co.ktech.cse.model.DataInfo;
import kr.co.ktech.cse.model.GroupCateInfo;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SnsAppInfo;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import static kr.co.ktech.cse.CommonUtilities.ENCODING;

public class KLoungeRequest {
	final int IMAGE_MAX_SIZE_W = 600;
	final int IMAGE_MAX_SIZE_H = 800;
	private KLoungeHttpRequest httprequest;
	String TAG = KLoungeRequest.class.getSimpleName();
	public KLoungeRequest() {
		httprequest = new KLoungeHttpRequest();
	}
	public DataInfo getDataInfo(final int post_id, final int group_id, final int user_id){
		DataInfo result = new DataInfo();
		
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetDataInfo.jsp";
			String param = "group_id="+group_id + "&post_id="+post_id +"&user_id="+user_id;
			addr = addr + "?" + param;

			String strJson = httprequest.getJSONHttpURLConnection(addr);
			
			result = parseDataInfo(strJson);
			result.setGroup_id(group_id);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public String getGroupName(int group_id){
		String result = "";
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetGroupName.jsp";
			String param = "group_id="+group_id;
			addr = addr + "?" + param;
			String strJson = httprequest.getJSONHttpURLConnection(addr);
			try{
				JSONObject jsonObj = new JSONObject(strJson);
				JSONObject nameObj = jsonObj.getJSONObject("klounge");
				result = nameObj.getString("group_name");

			}catch(JSONException je){
				je.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public boolean sendRatingPoint(int post_id, int user_id, int point){
		boolean result = false;
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appDataStarPointAddProc.jsp";
			String parameter = "point="+point
					+"&post_id="+post_id+"&user_id="+user_id;

			addr = addr + "?" + parameter;

			Log.d(TAG, addr);

			String strJson = httprequest.getJSONHttpURLConnection(addr);
			result = parseStarPointResult(strJson);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public ArrayList<Integer> checkRating(int post_id, int user_id){
		ArrayList<Integer> result = new ArrayList<Integer>();
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appCheckRating.jsp";
			String parameter = "post_id="+post_id
					+"&user_id="+user_id;
			addr = addr + "?" + parameter;
			String strJson = httprequest.getJSONHttpURLConnection(addr);
			result = parseCheckRating(strJson);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	public boolean increaseDataCount(int post_id, int count){
		boolean result = false;
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appIncreaseCount.jsp";
			String parameter = "post_id="+post_id
					+"&count="+count;
			addr = addr + "?" + parameter;
			String strJson = httprequest.getJSONHttpURLConnection(addr);
			result = parseIncreaseCount(strJson);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public String getUserPhoto(int user_id){
		String result = "";
		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetUserInfo.jsp";
			String parameter = "user_id="+user_id;
			addr = addr+"?"+parameter;

			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			result = parsePhotoJSON(strJSON);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public List<SnsAppInfo> getGroupMessageList(int user_id, int group_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "user_id="+user_id+"&group_id="+group_id+"&reload="+reload;
			addr = addr+"?"+parameter;

			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			result = parseStrJSON(strJSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	public List<SnsAppInfo> getMyLoungeMessageList(int user_id, int group_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=mylounge&user_id="+user_id+"&group_id="+group_id+"&reload="+reload;
			addr = addr+"?"+parameter;
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//파싱
			Log.d(TAG, "MyLoungeMessageList: "+result.toString());
			result = parseStrJSON(strJSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<SnsAppInfo> getPersonalLoungeMessageList(int user_id, int group_id, int puser_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=personallounge&user_id="+user_id+"&group_id="+group_id+"&puser_id="+puser_id+"&reload="+reload;
			addr = addr+"?"+parameter;
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//파싱
			result = parseStrJSON(strJSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public ArrayList<CateInfo> getPrivateCategoryList(int user_id){
		ArrayList<CateInfo> result = new ArrayList<CateInfo>();
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetMyPrivateFileCategory.jsp";
			String param = "user_id="+user_id;
			addr = addr + "?" +param;

			String strJSON = httprequest.getJSONHttpGet(addr);

			result = parseCategoryJSON(strJSON);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public ArrayList<CateInfo> getPublicCategoryList(){
		ArrayList<CateInfo> result = new ArrayList<CateInfo>();
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetAllSharedFileCategoryList.jsp";

			String strJSON = httprequest.getJSONHttpGet(addr);

			result = parseCategoryJSON(strJSON);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public ArrayList<GroupCateInfo> getGroupCategoryList(ArrayList<Integer> group_id_list, ArrayList<String> group_name_list){
		ArrayList<GroupCateInfo> group_cate_list = new ArrayList<GroupCateInfo>();

		try{
			for(int i =0; i < (group_id_list.size()-1) ; i++){
				GroupCateInfo gcInfo = new GroupCateInfo();
				gcInfo.setGroup_id(group_id_list.get(i));
				gcInfo.setGroup_name(group_name_list.get(i));

				ArrayList<CateInfo> result = new ArrayList<CateInfo>();

				String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetGroupSharedFileCategory.jsp";
				String param = "group_id="+group_id_list.get(i);
				addr = addr + "?" +param;

				//				Log.d(TAG, i+ " - addr: "+addr);

				String strJSON = httprequest.getJSONHttpGet(addr);

				//				Log.d(TAG, "strJSON: "+strJSON);
				result = parseCategoryJSON(strJSON);

				gcInfo.setCate_list(result);
				group_cate_list.add(gcInfo);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return group_cate_list;
	}
	public ArrayList<DataInfo> getPublicDataList(int user_id, int data_cate_id, String query){
		ArrayList<DataInfo> result = new ArrayList<DataInfo>();
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetAllSharedFileList.jsp";
			String params = "user_id="+user_id +"&data_cate_id="+data_cate_id+"&query="+URLEncoder.encode(query, "utf-8");
			addr = addr + "?" + params;

			String strJSON = httprequest.getJSONHttpGet(addr);
			result = parseDataInfoList(strJSON);

		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	public ArrayList<GroupInfo> getJoinGroupList(int user_id){
		String addr = CommonUtilities.SERVICE_URL + "/mobile/appdbbroker/appKLoungeGroup.jsp";
		String parameter = "user_id="+user_id;
		addr = addr+"?"+parameter;

		String strJSON = "";

		StringBuilder json = new StringBuilder();
		try {
			URL url = new URL(addr);

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			if(conn != null) {
				conn.setConnectTimeout(3000);
				conn.setUseCaches(false);
				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
					for(;;){
						String line = br.readLine();
						if(line == null) break;
						json.append(line);
					}
					br.close();
				}
				conn.disconnect();
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(SocketTimeoutException s){
			s.printStackTrace();
		}catch(IOException i){
			i.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		strJSON = json.toString();
		
		return parseGroupNames(strJSON);
	}
	private DataInfo parseDataInfo(String strJson){
		DataInfo dInfo = new DataInfo();

		JSONObject object;
		try {
			object = new JSONObject(strJson);

			JSONObject krounge = object.getJSONObject("klounge");
			JSONObject data = krounge.getJSONObject("data_list");

			dInfo.setTitle(data.getString("title"));
			dInfo.setDate(Timestamp.valueOf(data.getString("date")));
			dInfo.setUser_id(Integer.parseInt(data.getString("user_id")));
			dInfo.setCount(Integer.parseInt(data.getString("count")));
			dInfo.setAttach(data.getString("attach"));
			dInfo.setPostId(Integer.parseInt(data.getString("post_id")));
			dInfo.setBody(data.getString("body"));
			dInfo.setPubcate_id(Integer.parseInt(data.getString("pubCateId")));
			dInfo.setBpublic(Integer.parseInt(data.getString("bPublic")));
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dInfo;
	}
	private ArrayList<GroupInfo> parseGroupNames(String strJSON){
		ArrayList<GroupInfo> result = new ArrayList<GroupInfo>();
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
			ArrayList<GroupInfo> ginfoList = new ArrayList<GroupInfo>();
			GroupInfo ginfo = new GroupInfo();
			ginfoList.add(ginfo);
			return ginfoList;
		}
		String group_name = null;
		try {
			for(int i=0; i<groupArray.length(); i++) {
				JSONObject group;
				group = groupArray.getJSONObject(i);
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
				}
				groupInfo.setGroup_name(group_name);

				result.add(groupInfo);
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return result;
	}
	private ArrayList<DataInfo> parseDataInfoList(String strJSON){
		ArrayList<DataInfo> result = new ArrayList<DataInfo>();
		try{
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray dataArray = kloungeObj.getJSONArray("file_list");

			for(int index = 0;index <dataArray.length();index++){
				JSONObject indexObj = dataArray.getJSONObject(index);
				DataInfo dInfo = new DataInfo();
				dInfo.setTitle(indexObj.getString("title"));
				dInfo.setDate(Timestamp.valueOf(indexObj.getString("date")));
				dInfo.setUser_id(Integer.parseInt(indexObj.getString("user_id")));
				dInfo.setCount(Integer.parseInt(indexObj.getString("count")));
				dInfo.setAttach(indexObj.getString("attach"));
				dInfo.setPostId(Integer.parseInt(indexObj.getString("post_id")));
				dInfo.setGroup_id(Integer.parseInt(indexObj.getString("group_id")));
				dInfo.setKeyword(indexObj.getString("keyword"));
				dInfo.setBody(indexObj.getString("body"));
				dInfo.setPubcate_id(Integer.parseInt(indexObj.getString("pubCateId")));
				result.add(dInfo);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	private ArrayList<CateInfo> parseCategoryJSON(String strJSON){
		ArrayList<CateInfo> result = new ArrayList<CateInfo>();
		try{
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray cateArray = kloungeObj.getJSONArray("category");

			for(int index = 0; index < cateArray.length(); index ++){
				JSONObject indexObj = cateArray.getJSONObject(index);
				CateInfo cInfo = new CateInfo();
				cInfo.setId(indexObj.getInt("id"));
				cInfo.setPriority(indexObj.getInt("depth"));
				cInfo.setName(indexObj.getString("name"));
				result.add(cInfo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private boolean parseStarPointResult(String strJson){
		Log.d(TAG, "strJson: "+strJson);
		boolean result = false;
		try{
			JSONObject jsonObj = new JSONObject(strJson);
			JSONObject count_result = jsonObj.getJSONObject("klounge");
			result = Boolean.getBoolean(count_result.getString("result"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	private boolean parseIncreaseCount(String strJson){
		boolean result = false;
		try{
			JSONObject jsonObj = new JSONObject(strJson);
			JSONObject count_result = jsonObj.getJSONObject("klounge");
			result = Boolean.getBoolean(count_result.getString("count"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	private ArrayList<Integer> parseCheckRating(String strJson){
		ArrayList<Integer> result = new ArrayList<Integer>();
		try{
			JSONObject jsonObj = new JSONObject(strJson);
			JSONObject count_result = jsonObj.getJSONObject("klounge");

			result.add(0, Integer.parseInt(count_result.getString("check")));
			result.add(1, Integer.parseInt(count_result.getString("number")));
			result.add(2, Integer.parseInt(count_result.getString("average")));

		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	private String parsePhotoJSON(String strJSON) {
		StringBuffer result = new StringBuffer();
		try {
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject infoArray = jsonObj.getJSONObject("user");
			result.append(infoArray.getString("user_photo"));
			result.append("::");
			result.append(infoArray.getString("user_name"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	public List<SnsAppInfo> parseStrJSON(String strJSON) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();
		try {
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray messageArray = kloungeObj.getJSONArray("message");
			int group_id = kloungeObj.getInt("group_id");
			for(int i=0; i<messageArray.length(); i++) {
				JSONObject messageObj = messageArray.getJSONObject(i);
				SnsAppInfo saInfo = new SnsAppInfo();
				saInfo.setGroupId(group_id);
				saInfo.setPostId(messageObj.getInt("post_id"));
				saInfo.setUserId(messageObj.getInt("user_id"));
				saInfo.setPhoto(messageObj.getString("photo"));
				saInfo.setUserName(messageObj.getString("user_name"));
				saInfo.setWrite_date(messageObj.getString("date"));
				saInfo.setBody(messageObj.getString("comment"));
				saInfo.setAttach(messageObj.getString("attach_file"));
				saInfo.setPhotoVideo(messageObj.getString("photo_video_file"));
				saInfo.setReply_count(messageObj.getInt("reply_count"));

				if(i<1) {
					//					Log.i("message", saInfo.getUserId()+":"+saInfo.getUserName()+":"+saInfo.getWrite_date()+":"+saInfo.getBody()
					//							+":"+saInfo.getBody()+":"+saInfo.getReply_count());					
				}

				result.add(saInfo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public List<SnsAppInfo> getReplyMessageList(int post_id) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=reply&post_id="+post_id;
			addr = addr+"?"+parameter;
			Log.i(TAG,addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			//			Log.i(TAG, strJSON);
			//파싱
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray replyArray = kloungeObj.getJSONArray("reply");

			for(int i=0; i<replyArray.length(); i++) {
				JSONObject replyObj = replyArray.getJSONObject(i);
				SnsAppInfo saInfo = new SnsAppInfo();
				saInfo.setPostId(replyObj.getInt("reply_post_id"));
				saInfo.setUserId(replyObj.getInt("reply_user_id"));
				saInfo.setPhoto(replyObj.getString("photo"));
				saInfo.setUserName(replyObj.getString("reply_user_name"));
				saInfo.setReply_to_user_name(replyObj.getString("reply_to_user_name"));
				saInfo.setWrite_date(replyObj.getString("reply_date"));
				saInfo.setBody(replyObj.getString("reply_comment"));
				saInfo.setAttach(replyObj.getString("reply_attach_file"));

				result.add(saInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public boolean sendMessageWithImage(
			int group_id, int user_id, String message_body, 
			String imagepath, int puser_id, Context context)
					throws Exception, OutOfMemoryError {
		boolean result = false;
		Log.d(TAG, "path: "+imagepath);

		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessageWithFile.jsp";

		String imagename = new String(imagepath.substring(imagepath.lastIndexOf('/')+1));

		File file = new File(imagepath);
		if(file.canRead()){
			Log.d(TAG, "can file read!");
		}
		try {
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			RandomAccessFile raf = new RandomAccessFile(imagepath, "rw");
			byte[] b = new byte[(int)file.length()];
			raf.read(b);

			entity.addPart("ftype", new StringBody("img"));
			entity.addPart("user_id", new StringBody(String.valueOf(user_id)));
			entity.addPart("group_id", new StringBody(String.valueOf(group_id)));
			entity.addPart("puser_id", new StringBody(String.valueOf(puser_id)));
			entity.addPart("message_body", new StringBody(URLEncoder.encode(message_body, "UTF-8")));
			entity.addPart("uploadimage", new ByteArrayBody(b, imagename));

			result = httprequest.executeHttpPost(url, entity);

		}catch(NullPointerException ne){
			Log.e(TAG, "NullPointerException: " + ne);
		}catch(Exception e) {
			Log.i(TAG,e.toString());
			return false;
		}
		return result;
	}
	public boolean sendMessageWithImage(String type, String type2,
			SnsAppInfo sinfo, String imagepath, Context context)
					throws Exception, OutOfMemoryError {
		boolean result = true;

		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessageWithFile.jsp";

		String imagename = new String(imagepath.substring(imagepath.lastIndexOf('/')+1));

		File file = new File(imagepath);
		if(file.canRead()){
			Log.d(TAG, "can file read!");
		}
		try {
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			RandomAccessFile raf = new RandomAccessFile(imagepath, "rw");
			byte[] b = new byte[(int)file.length()];
			raf.read(b);

			entity.addPart("ftype", new StringBody("img"));
			entity.addPart("user_id", new StringBody(String.valueOf(sinfo.getUserId())));
			entity.addPart("group_id", new StringBody(String.valueOf(sinfo.getGroupId())));
			entity.addPart("puser_id", new StringBody(String.valueOf(sinfo.getPuser_id())));
			entity.addPart("message_body", new StringBody(URLEncoder.encode(sinfo.getBody(), "UTF-8")));
			entity.addPart("group_name", new StringBody(URLEncoder.encode(sinfo.getGroup_name(), "UTF-8")));
			entity.addPart("user_name", new StringBody(URLEncoder.encode(sinfo.getUserName(), "UTF-8")));
			entity.addPart("photo", new StringBody(sinfo.getPhoto()));
			entity.addPart("type", new StringBody(type));
			entity.addPart("type2", new StringBody(type2));
			entity.addPart("photo_video", new StringBody(sinfo.getPhotoVideo()));
			entity.addPart("uploadimage", new ByteArrayBody(b, imagename));

			result = httprequest.executeHttpPost(url, entity);
			raf.close();
		}catch(NullPointerException ne){
			Log.e(TAG, "NullPointerException: " + ne);
			return false;
		}catch(Exception e) {
			Log.i(TAG,e.toString());
			return false;
		}
		return result;
	}
	public boolean sendGCM(String type, String type2,
			SnsAppInfo sinfo, String imagepath, Context context){
		boolean result = false;
		httprequest = new KLoungeHttpRequest();
		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendGCM.jsp";
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

		try{
			params.add(new BasicNameValuePair("user_id", String.valueOf(sinfo.getUserId())));
			params.add(new BasicNameValuePair("group_id", String.valueOf(sinfo.getGroupId())));
			params.add(new BasicNameValuePair("message_body", URLEncoder.encode(sinfo.getBody(), "UTF-8")));
			params.add(new BasicNameValuePair("group_name", URLEncoder.encode(sinfo.getGroup_name(), "UTF-8")));
			params.add(new BasicNameValuePair("user_name", URLEncoder.encode(sinfo.getUserName(), "UTF-8")));
			params.add(new BasicNameValuePair("user_photo", sinfo.getPhoto()));
			params.add(new BasicNameValuePair("puser_id", String.valueOf(sinfo.getPuser_id())));
			params.add(new BasicNameValuePair("type", URLEncoder.encode(type, "UTF-8")));
			params.add(new BasicNameValuePair("type2", URLEncoder.encode(type2, "UTF-8")));

		}catch(UnsupportedEncodingException ue){
			ue.printStackTrace();
		}
		return result;
	}
	public boolean sendMessage(int group_id, int user_id, 
			String message_body, int post_id, String type, int puser_id, 
			String user_name, String user_photo) {
		boolean result = false;
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessage.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
			nameValuePairs.add(new BasicNameValuePair("group_id", String.valueOf(group_id)));
			nameValuePairs.add(new BasicNameValuePair("message_body", URLEncoder.encode(message_body, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("type", URLEncoder.encode(type, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("puser_id", String.valueOf(puser_id)));
			nameValuePairs.add(new BasicNameValuePair("user_name", URLEncoder.encode(user_name, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("user_photo", user_photo));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}    

		result = httprequest.executeHttpPost(url, nameValuePairs, false);

		return result;
	}
	public boolean sendMessage(SnsAppInfo sinfo, String type, String type2) {
		boolean result = false;
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessage.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(sinfo.getUserId())));
			nameValuePairs.add(new BasicNameValuePair("group_id", String.valueOf(sinfo.getGroupId())));
			nameValuePairs.add(new BasicNameValuePair("message_body", URLEncoder.encode(sinfo.getBody(), "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(sinfo.getPostId())));
			nameValuePairs.add(new BasicNameValuePair("type", URLEncoder.encode(type, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("puser_id", String.valueOf(sinfo.getPuser_id())));
			nameValuePairs.add(new BasicNameValuePair("type2", URLEncoder.encode(type2, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("group_name", URLEncoder.encode(sinfo.getGroup_name(), "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("user_name", URLEncoder.encode(sinfo.getUserName(), "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("user_photo", sinfo.getPhoto()));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}    

		result = httprequest.executeHttpPost(url, nameValuePairs, false);

		return result;
	}
	public boolean deleteMessage(int user_id, int post_id, int r) {
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appDeleteMessage.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("r", String.valueOf(r)));

		} catch (Exception e) {
			e.printStackTrace();
		}     

		return httprequest.executeHttpPost(url, nameValuePairs, false);
	}

	public void dataDownload2(int post_id, int post_user_id, int ck, String filename){
		//		Log.i("dataDownload ", post_user_id+" / " + post_id);
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appDataDown.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("post_user_id", String.valueOf(post_user_id)));
			nameValuePairs.add(new BasicNameValuePair("ck", String.valueOf(ck))); // 메인 0 : 답글 1

			httprequest.executeDataDownload(url, nameValuePairs, filename);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dataDownload(int post_id, int post_user_id, int ck, String filename){
		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/appDataDown.jsp");
		//		_url = _url + "?post_id=2125&post_user_id=17&ck=0";
		sb_url.append("?post_id="+post_id);
		sb_url.append("&post_user_id="+post_user_id);
		sb_url.append("&ck="+ck);
		String _url = sb_url.toString();
		//		Log.i("URL",_url);
		String location = CommonUtilities.DOWNLOAD_PATH+"/Download/";
		InputStream in = null;       

		try {
			in = OpenHttpConnection(_url);
			if(in!=null){
				saveToInternalStorage(location,in,filename);
				in.close();
			}
		} catch (Exception e1) {
			Log.i("EXCEPTION",e1.toString());
		}
	}
	private static InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
		//		Log.i("URL", urlString);
		URL url = new URL(urlString);

		URLConnection conn = url.openConnection();
		if (!(conn instanceof HttpURLConnection))                    
			throw new IOException("Not an HTTP connection");
		try {
			System.out.println("OpenHttpConnection called");

			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setDoOutput(true);
			httpConn.connect();
			response = httpConn.getResponseCode();			

			//			Log.i("ENC", httpConn.getHeaderField("Content-Type"));
			//			Log.i("response is",response);
			//			Log.i("connection is",HttpURLConnection.HTTP_OK+"");

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				int contentLength = httpConn.getContentLength();
				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " +  new String(disposition.getBytes("8859-1"), "utf-8"));
				System.out.println("Content-Length = " +contentLength);

				//System.out.println("Connection Ok");
				return in;
			}
		} catch (Exception ex) {
			throw new IOException("Error connecting");           
		}
		return in;
	}
	private static void saveToInternalStorage(String location,InputStream in,String filename) {

		try {
			int len1  = 0;
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(location+filename),ENCODING));
			String str_buffer =null;

			File dir=new File(location);
			if(!dir.exists()) dir.mkdirs();

			FileOutputStream fos = new FileOutputStream(location+filename);

			//byte[] buffer=new byte[4096];
			StringBuilder sbuilder = new StringBuilder();
			while ( (str_buffer = br.readLine() )!= null ) {
				bw.write(str_buffer,0,str_buffer.length());
				bw.newLine();
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			Log.i("EXCEPTION",e.toString());
		} 
	}
	public String getFileName(int pid){
		String result = "";
		try{
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appGetFileName.jsp";
			String parameter = "post_id="+pid;
			addr = addr+"?"+parameter;
			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject filenameObj = jsonObj.getJSONObject("filename");
			result = filenameObj.getString("name");
			//			if(AppConfig.DEBUG)Log.d(TAG, result);
		}catch(Exception e){
			Log.e(TAG, "JSON Exception - "+e);
		}
		return result;
	}
}
