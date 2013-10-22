package kr.khub;

import android.app.Application;

public class KloungeApplication extends Application {
	private static int pendingNotificationsCount = 0;
	private static boolean isLogin = false;
	
	public static int getPendingNotificationsCount() {
		return pendingNotificationsCount;
	}

	public static void setPendingNotificationsCount(int pendingNotificationsCount) {
		KloungeApplication.pendingNotificationsCount = pendingNotificationsCount;
	}

	public static boolean isLogin() {
		return isLogin;
	}

	public static void setLogin(boolean isLogin) {
		KloungeApplication.isLogin = isLogin;
	}
}
