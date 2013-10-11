package kr.co.ktech.cse.db;

import android.content.SharedPreferences;

public class LoginRequest {
	String TAG = "LoginRequest"; 
	KLoungeHttpRequest httprequest;
	
	public LoginRequest() {
		httprequest = new KLoungeHttpRequest();
	}
	
	public String login(String id, String passwd, SharedPreferences pref) {
//		if(AppConfig.DEBUG)Log.d("Login",id+"/"+passwd);
		String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appLogin.jsp";

		String parameter = "login_id="+id+"&passwd="+passwd;
		addr = addr+"?"+parameter;

//		if(AppConfig.DEBUG)Log.d("Login",addr);

		return httprequest.getJSONHttpURLConnection(addr);
		
	}
		
}
