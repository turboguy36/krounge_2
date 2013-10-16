/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.ktech.cse;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {
	// where this App services 
	public static final String SERVICE_URL = "http://www.khub.ac.kr";
//	public static final String SERVICE_URL = "http://210.117.172.166:8080/cse";
	public static final String NOPHOTO_URL = SERVICE_URL+ "/images/sns/no_photo_small.gif";
	
	public static final String FLAG_IF_REDIRECT_LINK = "/common/redirectLink.jsp?";
	public static final String SHARED_PREFERENCE = "KLounge";
	public static final String KEY_VER_PREFERENCE = "cur_version";
	public static final boolean ORI_IMAGE_PRESERVED = true; // 이미지 이름에 ori_ 붙여서 저장 하는 경우 
	public static final boolean IS_THUMBNAIL = true; // thumb nail Image 만들었는지 
	public static final String IMAGE_CACHE_DIR = "thumbs";
	public static final Long VIBRATE_TIME = 70L;
	public static final String ENCODING = "UTF8";
	static public SimpleDateFormat format=null;
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().toString(); // mnt/sdcard/
	//public static final String PACKAGE_NAME = "kr.co.ktech.cse"; // Activity 에서 getPackageName() 으로 얻을 수 있다.
	public static final String INTENT_GROUP_ID = "to_group_id";
	public static final int AFTER_WRITE_MESSAGE = 106;
	/**
	 * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
	 */
	public static final String GCM_SERVER_URL = SERVICE_URL+"/mobile/gcm";
//	public static final String GCM_SERVER_URL = "http://210.117.172.166/gcm-ktech";
	/**
	 * Google API project id registered to use GCM.
	 */
	public static final String GCM_SENDER_ID = "652439205153";
	
	public static final int GROUP_MESSAGE_LIST = 0;
	public static final int MY_MESSAGE_LIST = 1;
	public static final int PERSONAL_MESSAGE_LIST = 2;
	public static final int REPLY_MESSAGE_LIST = 3;
	public static final int VERSION_CHECK_CODE = 4;
	
	public static final int MESSAGE_VIEW_NUMBER = 10;
	public static String KLOUNGE_STORAGE_LOCATION = "/klounge";
	 
	public static final int KLOUNGE_FILE_CACHE_NUMBER = 100;

	public static boolean IS_POPUP = true;
	
	public static final int LICENSE = 4001;
	public static final int PRIVACY_POLICY = 4002;
	public static final int TERM_CONDITION = 4003;
	public static final int VERSION_INFO = 4003;
	
	public static final String FLAG_BODY_POST = "body";
	public static final String FLAG_REPLY_POST = "reply";
	public static final String FLAG_GROUP_LOUNGE = "group";
	public static final String FLAG_MY_LOUNGE = "my";
	
	public static final String TAG_POST_ID = "K_ROUNGE_POST_ID";
	public static final String TAG_GROUP_ID = "K_ROUNGE_GROUP_ID";

	/**
	 * Tag used on log messages.
	 */
	public static final String TAG = "KLOUNGE";

	/**
	 * Intent used to display a message in the screen.
	 */
	public static final String DISPLAY_MESSAGE_ACTION = "kr.co.ktech.cse.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	public static final String MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";

	public static final String WHERE_IT_FROM = "MORETAB";
	public static final String SPLIT_SIGN_PARENT = "@";
	public static final String SPLIT_SIGN_CHILD = "\\|";
	
	/**
	 * Popup Setting 
	 */
	public static final String FIRST_APP_USE = "FIRST_APP_USE";
	public static final String POPUP_SETTING = "POPUP_SETTING";
	public static final String POPUP_PREVIEW_SETTING = "POPUP_PREVIEW_SETTING";
	public static final String POPUP_GROUP_SNS_SETTING = "GROUP_SNS_SETTING";
	public static final String POPUP_SOUND_SETTING = "POPUP_SOUND_SETTING";
	public static final String POPUP_VIBRATE_SETTING = "POPUP_VIBRATE_SETTING";
	
	/**
	 * If Server is unavailable display the message to User
	 */
	public static final String SERVER_UNAVAILABLE = "SERVER_UNAVAILABLE";
	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by
	 * the UI and the background service.
	 *
	 * @param context application's context.
	 * @param message message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(MESSAGE, message);
		context.sendBroadcast(intent);
	}

	private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

	/**
	 * 픽셀단위를 현재 디스플레이 화면에 비례한 크기로 반환합니다.
	 * 
	 * @param pixel 픽셀
	 * @return 변환된 값 (DP)
	 */
	public static int DPFromPixel(final Context context, int pixel) {
		float scale = context.getResources().getDisplayMetrics().density;

		return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
	}

	/**
	 * 현재 디스플레이 화면에 비례한 DP단위를 픽셀 크기로 반환합니다.
	 * 
	 * @param DP 픽셀
	 * @return 변환된 값 (pixel)
	 */
	public static int PixelFromDP(final Context context, int DP) {
		float scale = context.getResources().getDisplayMetrics().density;

		return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
	}
}
