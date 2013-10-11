package kr.co.ktech.cse.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.co.ktech.cse.AppConfig;
import android.util.Log;

public class CheckUpdate {
	String TAG = CheckUpdate.class.getSimpleName();

	public String check(String AppID) {
		Log.d(TAG, "check");
		String MarketVersionName = null;
		try {
			final String FAppID =AppID;
			String Html =GetHtml("https://play.google.com/store/apps/details?id="+AppID);
			if(AppConfig.DEBUG)Log.d("URl", "https://play.google.com/store/apps/details?id="+AppID);

			Pattern pattern = Pattern.compile("softwareVersion\">[^<]*</div");
			Matcher matcher = pattern.matcher(Html);
			matcher.find();

			MarketVersionName = matcher.group(0).substring(matcher.group(0).indexOf(">")+1, matcher.group(0).indexOf("<")).trim();
			if(AppConfig.DEBUG)Log.d(TAG, MarketVersionName);
		}catch(Exception e){
			e.printStackTrace();
		}
		return MarketVersionName;
	}

	String GetHtml(String url1) {
		String str = "";
		try {
			URL url = new URL(url1);
			URLConnection spoof = url.openConnection();
			spoof.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)");
			BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
			String strLine = "";
			// Loop through every line in the source
			while ((strLine = in.readLine()) != null) {
				str = str + strLine;
			}
		} catch (Exception e) {
		}
		return str;
	}
}