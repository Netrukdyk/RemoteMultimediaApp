package com.remotemultimedia;

import com.remotemultimedia.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ActivityAbout extends Activity implements OnClickListener {

	LinearLayout selectServer, selectSoftware;
	ImageButton configServers;
	SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
