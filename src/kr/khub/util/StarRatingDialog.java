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
package kr.khub.util;

import kr.khub.R;
import kr.khub.db.KLoungeRequest;
import kr.khub.model.AppUser;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;

public class StarRatingDialog extends Activity implements OnClickListener{
	private RatingBar starRating;
	private int post_id;
	private String TAG = StarRatingDialog.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Bundle bun = getIntent().getExtras();
		post_id = bun.getInt("p_id");

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.star_point_dialog);

		starRating = (RatingBar)findViewById(R.id.rating_bar);
		Button commit_btn = (Button)findViewById(R.id.commit_button);
		Button cancel_btn = (Button)findViewById(R.id.cancel_button);

		commit_btn.setOnClickListener(this);
		cancel_btn.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.commit_button:
			float rating = starRating.getRating();
			Log.d(TAG, "rating: "+rating);
			int point = (int)(rating * 2);
			Log.d(TAG, "point: "+point);
			
			new SendRatingPointTask().execute(point, post_id, AppUser.user_id);

			break;
		case R.id.cancel_button:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}
	class SendRatingPointTask extends AsyncTask<Integer, Void, Void>{
		KLoungeRequest httprequest;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			httprequest = new KLoungeRequest();
		}

		@Override
		protected Void doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			try{
				int point = params[0];
				int post_id = params[1];
				int user_id = params[2];

				boolean result = httprequest.sendRatingPoint(post_id, user_id, point);
			}catch(NullPointerException ne){
				ne.printStackTrace();
			}catch(ArrayIndexOutOfBoundsException ae){
				ae.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Intent intent = new Intent();
			intent.putExtra("post_id", post_id);
			intent.putExtra("user_id", AppUser.user_id);
			setResult(RESULT_OK, intent);
			finish();
		}
		
	}
}