package kr.khub.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;
import static kr.khub.CommonUtilities.DOWNLOAD_PATH;

public class FileDownloader {
	public boolean downloadfile(String DownloadURL, String FileName) {
		boolean result = false;
		try {
			InputStream inputStream = new URL(DownloadURL).openStream();
			String file_path = DOWNLOAD_PATH;
			File dirFile = new File(file_path);
			if(dirFile.mkdir()){
				Log.i("MKDIR","Failed");
			}
			File file = new File(file_path+"/"+FileName);
			OutputStream out = new FileOutputStream(file);
			writeFile(inputStream, out);
			out.close();
			result = true;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			result = false;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	public void writeFile(InputStream is, OutputStream os) throws IOException
	{
	     int c = 0;
	     while((c = is.read()) != -1)
	         os.write(c);
	     os.flush();
	} 
}
