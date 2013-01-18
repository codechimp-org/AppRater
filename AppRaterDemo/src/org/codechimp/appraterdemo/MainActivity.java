package org.codechimp.appraterdemo;

import org.codechimp.apprater.AppRater;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;

public class MainActivity extends Activity {

	private Button buttonTest;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		buttonTest = (Button) findViewById(R.id.button1);
		
		buttonTest.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				// This forces display of the rate prompt.
				// It should only be used for testing purposes
				AppRater.showRateDialog(v.getContext());
			}
		});
		

		// This will keep a track of when the app was first used and whether to show a prompt
		// It should be the default implementation of AppRater
		AppRater.app_launched(this);

	}
}
