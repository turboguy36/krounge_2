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

package kr.khub.bitmapfun.ui;

import kr.khub.AppConfig;
import kr.khub.R;
import kr.khub.bitmapfun.util.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */

public class ImageGridActivity extends FragmentActivity {
	private static final String TAG = ImageGridActivity.class.getSimpleName();
	int group_id;
	String group_name;
	int group_total_number;
	private static final String SAVED_INSTANCE_GROUP_ID = ImageGridActivity.class.getSimpleName() +"GROUP_ID";
	private static final String SAVED_INSTANCE_GROUP_NAME = ImageGridActivity.class.getSimpleName() +"GROUP_NAME";
	private static final String SAVED_INSTANCE_GROUP_NUM = ImageGridActivity.class.getSimpleName() +"GROUP_NUM";
	private static final String SAVED_INSTANCE_IMAGEGRID = ImageGridActivity.class.getSimpleName() +"ImageGrid";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (AppConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		try{
			group_id = intent.getIntExtra("group_id", 0);
			group_name = intent.getStringExtra("group_name");
			group_total_number = intent.getIntExtra("group_total_number", 0);
		}catch(NullPointerException ne){
			if(savedInstanceState != null){
				Bundle bundle = savedInstanceState.getBundle(SAVED_INSTANCE_IMAGEGRID);
				group_id = bundle.getInt(SAVED_INSTANCE_GROUP_ID);
				group_name = bundle.getString(SAVED_INSTANCE_GROUP_NAME);
				group_total_number = bundle.getInt(SAVED_INSTANCE_GROUP_NUM);
			}
			Log.e(TAG, "NullPointerException - "+ne);
		}
//		Log.d(TAG, "onCreate /group_id: "+group_id+"/group_name: "+group_name+"/number: "+group_total_number);
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new ImageGridFragment(), TAG);
			ft.commit();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
		Bundle bundle = new Bundle();
		bundle.putInt(SAVED_INSTANCE_GROUP_ID, group_id);
		bundle.putString(SAVED_INSTANCE_GROUP_NAME, group_name);
		bundle.putInt(SAVED_INSTANCE_GROUP_NUM, group_total_number);
		outState.putBundle(SAVED_INSTANCE_IMAGEGRID, bundle);
	}
	
}
