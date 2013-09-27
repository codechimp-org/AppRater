package org.codechimp.apprater;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the default day and launch count values
     */
    public static void app_launched(Context context) {
        app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT);
    }


    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, 0);
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }

        // Wait for at least the number of launches and the number of days used
        // until prompt
        if (launch_count >= launchesUntilPrompt) {
            if (System.currentTimeMillis() >= date_firstLaunch
                    + (daysUntilPrompt * 24 * 60 * 60 * 1000)) {
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
     * Call this method directly to go straight to play store listing for rating
     */
    public static void rateNow(final Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                .parse("market://details?id="
                        + context.getPackageName().toString())));
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
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
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
                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(context.getString(R.string.no_thanks),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.commit();
                        }
                        dialog.dismiss();
                    }
                });

        builder.show();
    }
}
