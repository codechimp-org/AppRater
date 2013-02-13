package org.codechimp.apprater;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {
	private final static int DAYS_UNTIL_PROMPT = 3;
	private final static int LAUNCHES_UNTIL_PROMPT = 7;

	/**
	 * Call this method at the end of your OnCreate method to determine whether
	 * to show the rate prompt
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

		// Wait for at least the number of launches and the number of days used
		// until prompt
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch
					+ (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				showRateAlertDialog(context, editor);
			}
		}

		editor.commit();
	}

	/**
	 * Call this method directly if you want to force a rate prompt, useful for
	 * testing purposes
	 */
	public static void showRateDialog(final Context context) {
		showRateAlertDialog(context, null);
	}

	/**
	 * The meat of the library, actually shows the rate prompt dialog
	 */
	private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
		Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(String.format(
				context.getString(R.string.dialog_title),
				context.getString(R.string.app_name)));

		builder.setMessage(String.format(
				context.getString(R.string.rate_message),
				context.getString(R.string.app_name)));

		builder.setPositiveButton(context.getString(R.string.rate),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
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

		builder.setNeutralButton(context.getString(R.string.later),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (editor != null) {
							Long date_firstLaunch = System.currentTimeMillis();
							editor.putLong("date_firstlaunch", date_firstLaunch);
							editor.commit();
						}
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(context.getString(R.string.no_thanks),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (editor != null) {
							editor.putBoolean("dontshowagain", true);
							editor.commit();
						}
						dialog.dismiss();					}
				});
		
		builder.show();
	}
}
