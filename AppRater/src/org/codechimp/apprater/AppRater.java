package org.codechimp.apprater;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;

    private static Market market = new GoogleMarket();

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the default day and launch count values
     *
     * @param context
     */
    public static void app_launched(Context context) {
        app_launched(context, DAYS_UNTIL_PROMPT, LAUNCHES_UNTIL_PROMPT);
    }


    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param context
     * @param daysUntilPrompt
     * @param launchesUntilPrompt
     */
    public static void app_launched(Context context, int daysUntilPrompt, int launchesUntilPrompt) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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

        commitOrApply(editor);
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     * @param context
     */
    public static void showRateDialog(final Context context) {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     * @param context
     */
    public static void rateNow(final Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
    }

    /**
     * Set an alternate Market, defaults to Google Play
     *
     * @param market
     */
    public static void setMarket(Market market) {
        AppRater.market = market;
    }

    /**
     * Get the currently set Market
     *
     * @return market
     */
    public static Market getMarket() {
        return market;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format(
                context.getString(R.string.dialog_title),
                getAppName(context)));

        builder.setMessage(String.format(
                context.getString(R.string.rate_message),
                getAppName(context)));

        builder.setPositiveButton(context.getString(R.string.rate),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            commitOrApply(editor);
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
                            commitOrApply(editor);
                        }
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(context.getString(R.string.no_thanks),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            commitOrApply(editor);
                        }
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private static String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "");
    }
}