package kr.co.ktech.cse.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.CommonUtilities;

import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author Spooky13
 * 
 * 
 */

public class FileUploader {

	int serverResponseCode = 0;
	String TAG = "FileUploader";
	final private String SERVICE_URL = CommonUtilities.SERVICE_URL;
	
	public int uploadFile(String sourceFileUri, int user_id
			,int group_id, int puser_id, String message_body) {
		String upLoadServerUri = SERVICE_URL + "/mobile/appdbbroker/appSendMessageWithFile.jsp";
		String params = "ftype="+"img"
						+"&user_id="+user_id
						+"&group_id="+group_id
						+"&puser_id="+puser_id
						+"&message_body="+message_body;
//						
//		upLoadServerUri = upLoadServerUri + "?" +params;
		
		// String <span id="IL_AD2" class="IL_AD">fileName</span> =
		// sourceFileUri;
		String fileName = sourceFileUri;
		if(AppConfig.DEBUG)Log.d(TAG, "file Name : "+fileName);
		
		if(AppConfig.DEBUG)Log.d(TAG, sourceFileUri);
		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);
		if (!sourceFile.isFile()) {
			Log.e("uploadFile", "Source File Does not exist");
			return 0;
		}else{
			
		}
//		File renamedFile = fur.rename(sourceFile);
//		String renamedFilename = renamedFile.getName();
		try { // open a URL connection to the Servlet
			FileInputStream fileInputStream = new FileInputStream(sourceFile);
			URL url = new URL(upLoadServerUri);
			String filename = //fur.rename(sourceFile).getName();
					URLEncoder.encode(fileName.substring(fileName.lastIndexOf("/")+1), "utf-8");
			if(AppConfig.DEBUG)Log.d(TAG, "file name : "+ filename);
			conn = (HttpURLConnection) url.openConnection();  // Open a HTTP
			// connection to
			// the URL
			conn.setDoInput(true); // Allow Inputs
			conn.setDoOutput(true); // Allow Outputs
			conn.setUseCaches(false); // Don't use a Cached Copy
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("ENCTYPE", "multipart/form-data");
			conn.setRequestProperty("Content-Type",	"multipart/form-data;boundary=" + boundary);
			conn.setRequestProperty("uploadimage", filename);
			dos = new DataOutputStream(conn.getOutputStream());
			
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + filename + "\"" + lineEnd);
			dos.writeBytes(lineEnd);
			
			bytesAvailable = fileInputStream.available(); // create a buffer of
			// maximum size

			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and <span id="IL_AD11" class="IL_AD">write</span> it
			// into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);

			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			
			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			dos.writeBytes(URLEncoder.encode(params, "utf-8"));
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			// Responses from the server (code and message)
			serverResponseCode = conn.getResponseCode();
			String serverResponseMessage = conn.getResponseMessage();

			Log.i(TAG, "HTTP Response is : " + serverResponseMessage
					+ "(" + serverResponseCode+")");
			if (serverResponseCode == 200) {
				new Thread(new Runnable() {
					public void run() {
						// tv.setText("File Upload Completed.");
						// Toast.makeText(UploadImageDemo.this,
						// "File Upload Complete.", Toast.LENGTH_SHORT).show();
					}
				});
			}

			// close the streams //
			fileInputStream.close();
			dos.flush();
			dos.close();

		} catch (MalformedURLException ex) {
			// dialog.dismiss();
			// ex.printStackTrace();
			// Toast.makeText(UploadImageDemo.this, "MalformedURLException",
			// Toast.LENGTH_SHORT).show();
			Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
		} catch (Exception e) {
			// dialog.dismiss();
			// e.printStackTrace();
			// Toast.makeText(UploadImageDemo.this, "Exception : " +
			// e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e("Upload file to server Exception",
					"Exception : " + e.getMessage(), e);
		}
		// dialog.dismiss();
		return serverResponseCode;
	}
}
