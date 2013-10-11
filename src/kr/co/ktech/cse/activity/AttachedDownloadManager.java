/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */
package kr.co.ktech.cse.activity;

import java.io.File;
import java.util.List;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.DOWNLOAD_PATH;

public class AttachedDownloadManager extends Activity implements OnClickListener{
	private DownloadManager mgr=null;
	private long lastDownload=-1L;
	private String notiMessage;
	private int post_id;
	private int post_user_id;
	private int ck;
	private String filename;
	private boolean isUrlLink = false;
	private TextView notiMsgTextView;
	private KLoungeHttpRequest httprequest;
	private String _url;
	private Button close_btn;
	private Button start_btn;
	private Button show_file_btn;
	String TAG = "download manager";
	private DownloadManager.Request request;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download_manager);

		httprequest = new KLoungeHttpRequest();
		mgr=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);

		Bundle bun = getIntent().getExtras();
		setInstances(bun);

		notiMsgTextView = (TextView)findViewById(R.id.download_message);
		notiMsgTextView.setText(setShowMessage());

		_url = getUrl();
		
		close_btn = (Button)findViewById(R.id.btn_close_dialog);
		close_btn.setOnClickListener(this);
		start_btn = (Button)findViewById(R.id.start_attach_download);
		start_btn.setOnClickListener(this);
		show_file_btn = (Button)findViewById(R.id.go_download);
		show_file_btn.setOnClickListener(this);
		
		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
	}

	public void startDownload(View v) {
		
		Uri uri=Uri.parse(_url);
		Log.d(TAG, _url);

		File dir=new File(DOWNLOAD_PATH+"/Download/");
		if(!dir.exists()) dir.mkdirs();

		request = new DownloadManager.Request(uri);
		request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
				DownloadManager.Request.NETWORK_MOBILE);
		request.setAllowedOverRoaming(false);
		request.setTitle(filename);
		request.setDescription("위치: "+DOWNLOAD_PATH+"/Download/");
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
				filename);

		lastDownload=mgr.enqueue(request);
		notiMsgTextView.setText("\""+filename+"\" 다운로드 중.. ");
		
		Button pause_btn = (Button)findViewById(R.id.pause_attach_download);
		pause_btn.setVisibility(View.VISIBLE);
		v.setVisibility(View.GONE);
		pause_btn.setOnClickListener(this);
		
	}
	
	class CancelDownload extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			mgr.remove(lastDownload);
//			unregisterReceiver(onComplete);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			notiMsgTextView.setText("\""+filename+ "\""+
					"\n의 다운로드가 취소 되었습니다.");
			close_btn.setText("닫기");
			
			start_btn.setVisibility(View.VISIBLE);
			
			super.onPostExecute(result);
		}
	}
	
	BroadcastReceiver onComplete=new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			Cursor c=mgr.query(new DownloadManager.Query().setFilterById(lastDownload));
//			findViewById(R.id.start_attach_download).setVisibility(View.GONE);
			try{
				if(checkDownloadComplete(lastDownload)){
					notiMsgTextView.setText("\""+filename+ "\""+
							"\n의 다운로드가 성공적으로 완료되었습니다.\n" +
							"위치 : "+DOWNLOAD_PATH+"/Download/");
					start_btn.setVisibility(View.GONE);
					findViewById(R.id.pause_attach_download).setVisibility(View.GONE);
					findViewById(R.id.go_download).setVisibility(View.VISIBLE);
				}else{
					Button startBtn = (Button)findViewById(R.id.start_attach_download);
					startBtn.setVisibility(View.VISIBLE);
					startBtn.setText("재시도");
					startBtn.setEnabled(true);
					findViewById(R.id.go_download).setVisibility(View.GONE);
					findViewById(R.id.pause_attach_download).setVisibility(View.GONE);
				}
				close_btn.setText("닫기");
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				c.close();
			}

		}
	};

	BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			notiMsgTextView.setText("\""+filename+ "\""+
					"\n의 다운로드가 취소 되었습니다.");

			start_btn.setText("재시도");
			start_btn.setVisibility(View.VISIBLE);
			findViewById(R.id.pause_attach_download).setVisibility(View.GONE);
			close_btn.setText("닫기");
			mgr.remove(lastDownload);
//			unregisterReceiver(onComplete);
		}
	};
	
	/**
	 * 파일의 확장자 조회
	 * 
	 * @param fileStr
	 * @return
	 */
	public static String getExtension(String fileStr) {
		return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
	}
	
	public void viewFile(View v) {
		viewFile(this, DOWNLOAD_PATH+"/Download", filename);
		finish();
	}
	/**
	 * Viewer로 연결
	 * 
	 * @param ctx
	 * @param filePath
	 * @param fileName
	 */
	public static void viewFile(Context ctx, String filePath, String fileName) {
		// TODO Auto-generated method stub
		Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
		fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
		File file = new File(filePath, fileName);
//		Uri uri = Uri.fromFile(file);
		//확장자 구하기
		String fileExtend = getExtension(file.getAbsolutePath());
		// 파일 확장자 별로 mime type 지정해 준다.
		if (fileExtend.equalsIgnoreCase("mp3")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "audio/*");
		} else if (fileExtend.equalsIgnoreCase("mp4")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "vidio/*");
		} else if (fileExtend.equalsIgnoreCase("jpg")
				|| fileExtend.equalsIgnoreCase("jpeg")
				|| fileExtend.equalsIgnoreCase("gif")
				|| fileExtend.equalsIgnoreCase("png")
				|| fileExtend.equalsIgnoreCase("bmp")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "image/*");
		} else if (fileExtend.equalsIgnoreCase("txt")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "text/*");
		} else if (fileExtend.equalsIgnoreCase("doc")
				|| fileExtend.equalsIgnoreCase("docx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/msword");
		} else if (fileExtend.equalsIgnoreCase("xls")
				|| fileExtend.equalsIgnoreCase("xlsx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/vnd.ms-excel");
		} else if (fileExtend.equalsIgnoreCase("ppt")
				|| fileExtend.equalsIgnoreCase("pptx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/vnd.ms-powerpoint");
		} else if (fileExtend.equalsIgnoreCase("pdf")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
		} else if (fileExtend.equalsIgnoreCase("hwp")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/haansofthwp");
		} else if(fileExtend.equalsIgnoreCase("zip")){
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/zip");
		}
		PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent,
				PackageManager.GET_META_DATA);
		if (list.size() == 0) {
			Toast.makeText(ctx, fileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.",
					Toast.LENGTH_SHORT).show();
		} else {
			ctx.startActivity(fileLinkIntent);
		}
	}
	private boolean checkDownloadComplete(long downloadID){
		Cursor downloadCursor=mgr.query(new DownloadManager.Query().setFilterById(lastDownload));
		int reason = -1;
		int status = -1;
		try{
			if (downloadCursor != null){
				downloadCursor.moveToFirst();
				int statusKey = downloadCursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int reasonKey = downloadCursor.getColumnIndex(DownloadManager.COLUMN_REASON);

				status = downloadCursor.getInt(statusKey);
				reason = downloadCursor.getInt(reasonKey);

				if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.ERROR_FILE_ALREADY_EXISTS){
					Log.d(TAG, "DownloadManager succed");
					return true;
				}else if(status == DownloadManager.STATUS_FAILED){
					notiMsgTextView.setText("\""+filename+ "\""+
							"\n의 다운로드가 " +
							errorReason(reason) +
							"중지 되었습니다.\n");
					Log.d(TAG, "DownloadManager failed");
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			downloadCursor.close();
		}
		return false;
	}
	private String errorReason(int reason){
		String error_code = "";
		switch(reason){
		case DownloadManager.ERROR_UNKNOWN:
			error_code = "알 수 없는 오류로 인하여";
			break;
		case DownloadManager.ERROR_FILE_ERROR:
			error_code = "파일의 오류로 인하여";
			break;
		case DownloadManager.ERROR_HTTP_DATA_ERROR:
			error_code = "네트워크 오류로 인하여";
			break;
		case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
			error_code = "네트워크의 오류로 인하여";
			break;
		case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
			error_code = "너무 많은 재 접속 경로로 인하여";
			break;
		case DownloadManager.ERROR_INSUFFICIENT_SPACE:
			error_code = "기기의 용량이 부족하여";
			break;
		case DownloadManager.ERROR_DEVICE_NOT_FOUND:
			error_code = "저장할 공간을 찾지 못하여";
			break;
		case DownloadManager.ERROR_CANNOT_RESUME:
			error_code = "기기의 오작동으로 인하여";
			break;
		case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
			error_code = "중복된 파일로 인해";
			break;
		
		}
		return error_code;
	}
	private void setInstances(Bundle bundle){
		notiMessage = bundle.getString("notiMessage");
		post_id = bundle.getInt("p_id");
		post_user_id = bundle.getInt("user_id");
		ck = bundle.getInt("ck");
		filename = bundle.getString("filename");
		isUrlLink = bundle.getBoolean("url_linker");
	}
	private String setShowMessage(){
		StringBuffer showMessage = new StringBuffer();
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(1).getState();
		if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
		}else{
			showMessage.append("WI-FI 가 연결되어 있지 않습니다. 과도한 데이터가 청구될 수 있습니다.\n\n");
		}
		showMessage.append("\""+filename+"\"\n를/(을) ").append(notiMessage);
		if(filename == null){
			showMessage.setLength(0);
			showMessage.append("삭제 되었거나 존재 하지 않는 파일 입니다.");
			findViewById(R.id.start_attach_download).setEnabled(false);
		}else{
		}
		return showMessage.toString();
	}
	private String getUrl(){
		String jspFilename = isUrlLink? "appDataDownDataInfo.jsp" : "appDataDown.jsp";
		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/").append(jspFilename);
		sb_url.append("?post_id="+post_id);
		sb_url.append("&post_user_id="+post_user_id);
		sb_url.append("&ck="+ck);
		return sb_url.toString();
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		
		unregisterReceiver(onComplete);
		unregisterReceiver(onNotificationClick);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.start_attach_download:
			String sdcardState = android.os.Environment.getExternalStorageState();
			if(sdcardState.contentEquals(android.os.Environment.MEDIA_MOUNTED)){
				startDownload(v);
			}else{
				Toast.makeText(getApplicationContext(), "sd card unmounted", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_close_dialog:
			finish();
			break;
		case R.id.go_download:
			viewFile(v);
			break;
		case R.id.pause_attach_download:
			new CancelDownload().execute();
			break;
		}
	}
}