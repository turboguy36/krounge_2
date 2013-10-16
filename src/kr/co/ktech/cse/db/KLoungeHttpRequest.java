package kr.co.ktech.cse.db;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.util.RetryHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

public class KLoungeHttpRequest {
	private String TAG = KLoungeHttpRequest.class.getSimpleName();
	final private String SERVICE_URL = CommonUtilities.SERVICE_URL;
	final private String DOWNLOAD_PATH = CommonUtilities.DOWNLOAD_PATH;
	final int MAX_BUFFER_SIZE = 2048;

	public String getService_URL() {
		//		if(AppConfig.DEBUG)Log.d(TAG, SERVICE_URL);
		return SERVICE_URL;
	}

	public String getJSONHttpURLConnection(String addr) {
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
						if(line == null) {
							break;
						}
						json.append(line);
					}
					br.close();
				}else if(conn.getResponseCode() >= 500){
					json.append(CommonUtilities.SERVER_UNAVAILABLE);
				}
				conn.disconnect();
			}else{
				json.append(CommonUtilities.SERVER_UNAVAILABLE);
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch(SocketTimeoutException s){
			s.printStackTrace();
		}catch(IOException i){
			i.printStackTrace();
		}
//		if(AppConfig.DEBUG)Log.d(TAG, "json string: "+json.toString());
		return json.toString();
	}

	public String getJSONHttpGet(String addr) {
		StringBuilder json = new StringBuilder();
		HttpGet httpget = new HttpGet(addr);
		DefaultHttpClient client = new DefaultHttpClient();

		try {
			HttpResponse response = client.execute(httpget);
			String line = null;

			if(response.getStatusLine().getStatusCode() == 200) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				while((line=br.readLine()) != null){
					json.append(line);
				}
				br.close();
			}	
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	public String connect(String url) {

		// Create the httpclient
		HttpClient httpclient = new DefaultHttpClient();
		// Prepare a request object
		HttpGet httpget = new HttpGet(url);
		// Execute the request
		HttpResponse response;
		// return string
		String returnString = null;

		try {
			// Open the webpage.
			response = httpclient.execute(httpget);
			if (response.getStatusLine().getStatusCode() == 200) {
				// Connection was established. Get the content.
				HttpEntity entity = response.getEntity();
				// If the response does not enclose an entity, there is no need
				// to worry about connection release
				if (entity != null) {
					// A Simple JSON Response Read
					InputStream instream = entity.getContent();
					returnString = convertStreamToString(instream);

					// Cose the stream.
					instream.close();
				}
			} else {
				// code here for a response othet than 200. A response 200 means
				// the webpage was ok
				// Other codes include 404 - not found, 301 - redirect etc...
				// Display the response line.
				returnString = "Unable to load page - " + response.getStatusLine();
			}
		} catch (IOException ex) {
			// thrown by line 80 - getContent();
			// Connection was not established
			returnString = "Connection failed; " + ex.getMessage();
		} 
		return returnString;
	}
	private static final int DEFAULT_MAX_CONNECTIONS = 10;
	private static final int DEFAULT_SOCKET_TIMEOUT = 3 * 1000;
	private static final int DEFAULT_MAX_RETRIES = 3;
	private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8 * 1024;
	private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
	private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
	DefaultHttpClient httpClient;
	private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
	private static final String ENCODING_GZIP = "gzip";
	private Map<String, String> clientHeaderMap;

	public boolean executeHttpPost(String url, ArrayList<NameValuePair> nameValuePairs, boolean file) {
		boolean result = false;
		String returnString = null;

		BasicHttpParams httpParams = new BasicHttpParams();

		ConnManagerParams.setTimeout(httpParams, socketTimeout);
		ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
		ConnManagerParams.setMaxTotalConnections(httpParams, DEFAULT_MAX_CONNECTIONS);

		HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
		HttpConnectionParams.setTcpNoDelay(httpParams, true);
		HttpConnectionParams.setSocketBufferSize(httpParams, DEFAULT_SOCKET_BUFFER_SIZE);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);

		try{
			httpClient = new DefaultHttpClient(cm, httpParams);
			httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
				@Override
				public void process(HttpRequest request, HttpContext context) {
					if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
						request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
					}
					for (String header : clientHeaderMap.keySet()) {
						request.addHeader(header, clientHeaderMap.get(header));
					}
				}
			});

			httpClient.setHttpRequestRetryHandler(new RetryHandler(DEFAULT_MAX_RETRIES));

			clientHeaderMap = new HashMap<String, String>();
			HttpPost httppost = new HttpPost(url);

			httppost.setHeader("Connection", "Keep-Alive");
			httppost.setHeader("Accept-Charset", "UTF-8");
			if(file)httppost.setHeader("ENCTYPE", "multipart/form-data");
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpClient.execute(httppost);
			int status = response.getStatusLine().getStatusCode();
			if(status == HttpStatus.SC_OK){
				result = true;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				convertStreamToString(instream);
				instream.close();
			}

		}catch(Exception e){
			System.out.println("Error in http connection "+e.toString());
		}finally{
			httpClient.getConnectionManager().shutdown();
		}

		return result;
	}

	public boolean executeHttpPost(String url, MultipartEntity entity) {
		boolean result = true;
		
		BasicHttpParams httpParams = new BasicHttpParams();
		
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.  
		int timeoutConnection = 3000;
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection); 
		// Set the default socket timeout (SO_TIMEOUT)    
		// in milliseconds which is the timeout for waiting for data.   
		int timeoutSocket = 5000;   
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);   
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters); 
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Connection", "Keep-Alive");
			httppost.setHeader("Accept-Charset", "UTF-8");
			httppost.setHeader("ENCTYPE", "multipart/form-data");
			httppost.setEntity(entity);
			HttpResponse response = httpClient.execute(httppost);
			HttpEntity httpentity = response.getEntity();
			int status = response.getStatusLine().getStatusCode();

			if (entity != null) {
				InputStream instream = httpentity.getContent();
				convertStreamToString(instream);
				instream.close();
			}
			
			if(status == HttpStatus.SC_OK){
				result = true;
				Log.d(TAG, "response success - "+ result);
			}else{
				result = false;
				Log.d(TAG, "response fail");
			}
		}catch(Exception e){
			Log.e(TAG, "Exception - "+e);
			return false;
		}
		
		return result;
	}

	public boolean executeDataDownload(String url, ArrayList<NameValuePair> nameValuePairs, String filename) {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;

		Log.i("download url", url);
		//url = "http://www.knowledgetech.co.kr/etrihub/data/17/image/p_20121109145627195.jpg";
		//filename = "p_20121109145627195.jpg";
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);

			httppost.setHeader("Connection", "Keep-Alive");
			httppost.setHeader("Accept-Charset", "UTF-8");

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Log.i("tpye", entity.getContentType().getValue());
				bis = new BufferedInputStream(entity.getContent());
				String file_path = DOWNLOAD_PATH + "/" + filename;
				File f = new File(file_path);
				Log.i("file_path", file_path);
				if (f.exists() == false) {
					f.createNewFile();
				} else {
					f.delete();
				}
				bos = new BufferedOutputStream(new FileOutputStream(f));

				long totalBytes = entity.getContentLength();
				Log.i("totalBytes", String.valueOf(totalBytes));
				long readBytes = 0;

				byte[] buffer = new byte[MAX_BUFFER_SIZE];
				Log.i("file download", "start");
				while(totalBytes > readBytes) {
					int read = bis.read(buffer);
					readBytes += read;
					//publishProgress(readBytes , totalBytes);
					bos.write(buffer, 0, read);
				}

				Log.i("file download", "end");
				bos.flush();
			}

		} catch(Exception e){
			System.out.println("Error in http connection "+e.toString());
		} finally {
			try {
				if (bos != null)bos.close();
				if (bis != null) bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private String convertResponseToString(HttpResponse response)
			throws IllegalStateException, IOException {

		String res = "";
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = response.getEntity().getContent();
		int contentLength = (int) response.getEntity().getContentLength(); // getting content length…..

		if (contentLength < 0) {

		} else {
			byte[] data = new byte[512];
			int len = 0;
			try {
				while (-1 != (len = inputStream.read(data))) {
					buffer.append(new String(data, 0, len)); // converting to string and appending to stringbuffer…..
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				inputStream.close(); // closing the stream…..
			} catch (IOException e) {
				e.printStackTrace();
			}
			res = buffer.toString(); // converting stringbuffer to string…..

			//Toast.makeText(UploadImage.this, "Result : " + res,	Toast.LENGTH_LONG).show();
			// System.out.println("Response => " +
			// EntityUtils.toString(response.getEntity()));
		}
		return res;
	}
}
