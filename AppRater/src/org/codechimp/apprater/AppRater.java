package org.codechimp.apprater;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER="remindmelater";
    private final static String PREF_APP_VERSION="app_version";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    private  static int DAYS_UNTIL_PROMPT_FOR_REMIND_LATER = 3;
    private  static int LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER = 7;
    private static boolean isDark;
	private static boolean themeSetted;

    private static Market market = new GoogleMarket();
    /**
     * sets number of day until rating dialog pops up for next time when remind me later option is chosen 
     * @param daysUntilPromt
     */
 	public static void setNumDaysForRemindLater(int daysUntilPromt){
 		DAYS_UNTIL_PROMPT_FOR_REMIND_LATER=daysUntilPromt;
 	}
 	/**
 	 * sets the number of launches until the rating dialog pops up for next time when remind me later option is chosen
 	 * @param launchesUntilPrompt
 	 */
 	public static void setNumLaunchesForRemindLater(int launchesUntilPrompt){

 		LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER=launchesUntilPrompt;
 	}
    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt using the specified or default  day, launch count values and checking if
     * the version is changed or not
     *
     * @param context
     */
    public static void app_launched(Context context) {
    	SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        try {
			int versionCode = context.getPackageManager()
				    .getPackageInfo(context.getPackageName(), 0).versionCode;
			if(versionCode!=(prefs.getInt(PREF_APP_VERSION,-1))){
				editor.putInt(PREF_APP_VERSION, versionCode);
				editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
				editor.putBoolean(PREF_REMIND_LATER, false);
				editor.putLong(PREF_LAUNCH_COUNT, 0);
				Long date_firstLaunch = System.currentTimeMillis();
	            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
	            commitOrApply(editor);
			}
		} catch (NameNotFoundException e) {
			// ignored
		}
    	if(prefs.getBoolean(PREF_REMIND_LATER, false))
            app_launched(context, DAYS_UNTIL_PROMPT_FOR_REMIND_LATER, LAUNCHES_UNTIL_PROMPT_FOR_REMIND_LATER);
        	 else
        	app_launched(context,DAYS_UNTIL_PROMPT,LAUNCHES_UNTIL_PROMPT);    }


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
     * Sets dialog theme to dark
     */
    @TargetApi(11)
    public static void setDarkTheme(){
    	isDark = true;
    	themeSetted = true;
    }
    /**
     * Sets dialog theme to light
     */
    @TargetApi(11)
    public static void setLigthTheme(){
    	isDark = false;
    	themeSetted = true;
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi") 
    private static void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
    	Builder builder = new AlertDialog.Builder(context);
    	if(themeSetted && isDark && Build.VERSION.SDK_INT >= 11 ){
    		builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
    	}else
    	if(themeSetted && !isDark && Build.VERSION.SDK_INT >= 11){
    		builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
    	}else{
    		builder = new AlertDialog.Builder(context);
    	}
        builder.setTitle(String.format(
                context.getString(R.string.dialog_title),
                getApplicationName(context)));

        builder.setMessage(String.format(
                context.getString(R.string.rate_message),
                getApplicationName(context)));

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
    @SuppressLint("NewApi") 
    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    private static String getApplicationName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "");
    }
}