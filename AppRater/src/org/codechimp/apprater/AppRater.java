package org.codechimp.apprater;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {
	private final static int DAYS_UNTIL_PROMPT = 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 7;

	
	/**
	 * Call this method at the end of your OnCreate method to determine whether to show the rate prompt
	 */
	public static void app_launched(Context context) {
		SharedPreferences prefs = context.getSharedPreferences("apprater", 0);
		if (prefs.getBoolean("dontshowagain", false)) {
			return;
		}

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}

		// Wait for at least the number of launches and the number of days used until prompt
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateDialog(context, editor);
			}
		}

		editor.commit();
	}

	/**
	 * Call this method directly if you want to force a rate prompt, useful for testing purposes
	 */
	public static void showRateDialog(final Context context) {
		showRateDialog(context, null);
	}

	/**
	 * The meat of the library, actually shows the rate prompt dialog
	 */
	private static void showRateDialog(final Context context,
			final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(context);
		dialog.setTitle(String.format(context.getString(R.string.dialog_title),
						context.getString(R.string.app_name)));
		
		LinearLayout ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(16, 16, 16, 16);

		TextView tv = new TextView(context);
		tv.setText(String.format(context.getString(R.string.rate_message),
				context.getString(R.string.app_name)));
		tv.setPadding(16, 16, 16, 16);
		ll.addView(tv);

		Button buttonRateNow = new Button(context);
		buttonRateNow.setText(context.getString(R.string.rate));
		buttonRateNow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id="
								+ context.getPackageName().toString())));
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(buttonRateNow);

		Button buttonRateLater = new Button(context);
		buttonRateLater.setText(context.getString(R.string.later));
		buttonRateLater.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					Long date_firstLaunch = System.currentTimeMillis();
					editor.putLong("date_firstlaunch", date_firstLaunch);
				}
				dialog.dismiss();
			}
		});
		ll.addView(buttonRateLater);

		Button buttonRateNever = new Button(context);
		buttonRateNever.setText(context.getString(R.string.no_thanks));
		buttonRateNever.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(buttonRateNever);

		dialog.setContentView(ll);
		dialog.show();
	}
}
