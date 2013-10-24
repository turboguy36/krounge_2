package kr.co.ktech.cse.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class KLoungeFormatUtil {
	public static String dateFormat(Timestamp date) {
		String result_date = new SimpleDateFormat("yyyy-MM-dd hh:mm").format(date);		
		String tempDate = "전";
		Date d = new Date();
		
		long tmp = d.getTime()-date.getTime();
		if(tmp <= 86400000){
			long caldate = tmp/1000/60;
			if(caldate < 1){
				tempDate = "방금 "+tempDate;
			}else{
				long h = caldate/60;
				long m = caldate - (60*h);
				if(m != 0) tempDate = m+"분 "+tempDate;
				if(h != 0) tempDate = h+"시간 "+tempDate;				
			}
			result_date = tempDate;
		}
		return result_date;
	}
	
	public static StringBuffer bodyURLFormat(String body) {
		//body = body.replaceAll("\"", "");
//		Log.i("KRoungeFormatUtil", body);
		Pattern p = Pattern.compile("(http|https·ftp)://[^\\s^\\.]+(\\.[^\\s^\\.]+)*");
		StringBuffer sb = new StringBuffer();
		Matcher mm = p.matcher(body.replace("\n", "<br />"));
//		Matcher mm = p.matcher(body);
		
		if(body.indexOf("<a href=") != -1) { 
			sb.append(body);
		}else{			
			while (mm.find()) {
				String strLink = mm.group();
				if(strLink.length() > 80) strLink = strLink.substring(0, 80) + "...";
			    mm.appendReplacement(sb,"<a href='" + mm.group()+"' target='_blank'>" + strLink + "</a>");
			}
			mm.appendTail(sb);
		}
//		Log.i("KRoungeFormatUtil", sb.toString());
		return sb;
	}
}
