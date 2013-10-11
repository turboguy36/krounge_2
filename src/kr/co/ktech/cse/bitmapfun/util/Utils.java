/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.ktech.cse.bitmapfun.util;

import kr.co.ktech.cse.bitmapfun.ui.ImageGridActivity;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;

/**
 * Class containing some static utility methods.
 */
public class Utils {
	private Utils() {};
	private static final String IMAGE_CACHE_DIR = "imageFetcher";
	
	@TargetApi(11)
	public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                vmPolicyBuilder
                        .setClassInstanceLimit(ImageGridActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }
	public static String durationInSecondsToString(int sec){
		int hours = sec / 3600; 
		int minutes = (sec / 60) - (hours * 60);
		int seconds = sec - (hours * 3600) - (minutes * 60) ;
		String formatted = String.format("%d:%02d:%02d", hours, minutes, seconds);
		return formatted;
	}
	public static boolean hasFroyo() {
		// Can use static final constants like FROYO, declared in later versions
		// of the OS since they are inlined at compile time. This is guaranteed behavior.
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}

	public static boolean hasGingerbread() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}

	public static boolean hasHoneycomb() {
		// was released on 24 February 2011 (11)
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	public static boolean hasHoneycombMR1() {
		// was released on 10 May 2011
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
	}
	
	public static boolean hasIceCreamSandwich() {
		// was publicly released on 19 October 2011
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}
	
	public static boolean hasIceCreamSandwichMR1() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
	}
	
	public static boolean hasJellyBean(){
		//4.1 Jelly Bean was released to the Android Open Source Project on 9 July 2012 (16)
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}
	/*
	public static boolean hasJellyBeanMR1(){
		// was released on 13 November 2012 (17)
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}
	*/
	public static ImageFetcher getImageFetcher(final FragmentActivity activity) {
		ImageCacheParams cacheParams = new ImageCacheParams(activity, IMAGE_CACHE_DIR);
		// Set memory cache to 25% of mem class
		cacheParams.setMemCacheSizePercent(activity, 0.25f);
		ImageFetcher imageFetcher = new ImageFetcher(activity);
		imageFetcher.setImageFadeIn(true);
		
		imageFetcher.addImageCache(activity.getSupportFragmentManager(), cacheParams);
		return imageFetcher;
	}
}
