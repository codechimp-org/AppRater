package org.codechimp.apprater;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private final static int DEFAULT_DAYS_UNTIL_PROMPT = 3;
    private final static int DEFAULT_LAUNCHES_UNTIL_PROMPT = 7;


    private int daysUntilPrompt = DEFAULT_DAYS_UNTIL_PROMPT;
    private int launchesUntilPrompt = DEFAULT_LAUNCHES_UNTIL_PROMPT;
    private int daysUntilPromptForRemindLater = DEFAULT_DAYS_UNTIL_PROMPT;
    private int launchesUntilPromptForRemindLater = DEFAULT_LAUNCHES_UNTIL_PROMPT;
    private boolean hideNoButton;
    private boolean isVersionNameCheckEnabled;
    private boolean isVersionCodeCheckEnabled;
    private boolean isCancelable = true;
    private String packageName;
    private Market market = new GoogleMarket();

    public static class Builder {
        private int daysUntilPrompt = DEFAULT_DAYS_UNTIL_PROMPT;
        private int launchesUntilPrompt = DEFAULT_LAUNCHES_UNTIL_PROMPT;
        private int daysUntilPromptForRemindLater = DEFAULT_DAYS_UNTIL_PROMPT;
        private int launchesUntilPromptForRemindLater = DEFAULT_LAUNCHES_UNTIL_PROMPT;
        private boolean hideNoButton;
        private boolean isVersionNameCheckEnabled;
        private boolean isVersionCodeCheckEnabled;
        private boolean isCancelable = true;
        private String packageName;
        private Market market = new GoogleMarket();


        /**
         * Sets the number of days from the first launch at which the rate prompt will be shown
         *
         * @param daysUntilPrompt the number of days at with to prompt
         * @return builder
         */
        public Builder daysUntilPrompt(int daysUntilPrompt) {
            this.daysUntilPrompt = daysUntilPrompt;
            return this;
        }

        /**
         * Sets the number of launches from the first launch at which the rate prompt will be shown
         *
         * @param launchesUntilPrompt the number of launches at with to prompt
         * @return builder
         */
        public Builder launchesUntilPrompt(int launchesUntilPrompt) {
            this.launchesUntilPrompt = launchesUntilPrompt;
            return this;
        }

        /**
         * Sets the number of days from the remind later button being pressed at which the rate prompt will be shown again
         *
         * @param daysUntilPromptForRemindLater the number of days at with to prompt
         * @return builder
         */
        public Builder daysUntilPromptForRemindLater(int daysUntilPromptForRemindLater) {
            this.daysUntilPromptForRemindLater = daysUntilPromptForRemindLater;
            return this;
        }

        /**
         * Sets the number of launches from the remind later button being pressed at which the rate prompt will be shown again
         *
         * @param launchesUntilPromptForRemindLater the number of launches at with to prompt
         * @return builder
         */
        public Builder launchesUntilPromptForRemindLater(int launchesUntilPromptForRemindLater) {
            this.launchesUntilPromptForRemindLater = launchesUntilPromptForRemindLater;
            return this;
        }

        /**
         * Sets whether to display the No Thanks button on the rate prompt.
         *
         * @param hideNoButton true to hide the No Thanks button
         * @return builder
         */
        public Builder hideNoButton(boolean hideNoButton) {
            this.hideNoButton = hideNoButton;
            return this;
        }

        /**
         * If enabled this will reset the day/launch counts when a new version name is detected
         *
         * @param isVersionNameCheckEnabled true to enable version name checking
         * @return builder
         */
        public Builder versionNameCheckEnabled(boolean isVersionNameCheckEnabled) {
            this.isVersionNameCheckEnabled = isVersionNameCheckEnabled;
            return this;
        }

        /**
         * If enabled this will reset the day/launch counts when a new version code is detected
         *
         * @param isVersionCodeCheckEnabled true to enable version code checking
         * @return builder
         */
        public Builder versionCodeCheckEnabled(boolean isVersionCodeCheckEnabled) {
            this.isVersionCodeCheckEnabled = isVersionCodeCheckEnabled;
            return this;
        }

        /**
         * If enabled will allow the dialog to be cancelled rather than specifically chose an option
         *
         * @param isCancelable true to set that the dialog is cancelable
         * @return builder
         */
        public Builder cancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * Sets the market to use, different markets have different URI's to trigger the app store rating
         * Market classes are responsible for generating URI's combining the package name with the base market URI.
         *
         * @param market the market URL generator to use
         * @return builder
         */
        public Builder market(Market market) {
            this.market = market;
            this.market.setPackageName(packageName);
            return this;
        }

        /**
         * When compiling a debug version of your app this allows you to change the package name for testing purposes.
         *
         * @param packageName the package name that should be set and used via the market URI
         * @return builder
         */
        public Builder packageName(String packageName) {
            this.packageName = packageName;
            this.market.setPackageName(packageName);
            return this;
        }

        public AppRater build() {
            return new AppRater(this);
        }
    }

    public AppRater() {

    }

    public AppRater(Builder builder) {
        this.daysUntilPrompt = builder.daysUntilPrompt;
        this.launchesUntilPrompt = builder.launchesUntilPrompt;
        this.daysUntilPromptForRemindLater = builder.daysUntilPromptForRemindLater;
        this.launchesUntilPromptForRemindLater = builder.launchesUntilPromptForRemindLater;
        this.hideNoButton = builder.hideNoButton;
        this.isVersionNameCheckEnabled = builder.isVersionNameCheckEnabled;
        this.isVersionCodeCheckEnabled = builder.isVersionCodeCheckEnabled;
        this.isCancelable = builder.isCancelable;
        this.packageName = builder.packageName;
        this.market = builder.market;
    }

    /**
     * Call this method at the end of your OnCreate method to determine whether
     * to show the rate prompt
     *
     * @param context {@link Context}
     */
    public void appLaunched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        int days;
        int launches;
        if (isVersionNameCheckEnabled) {
            if (!ratingInfo.getApplicationVersionName().equals(prefs.getString(PREF_APP_VERSION_NAME, "none"))) {
                editor.putString(PREF_APP_VERSION_NAME, ratingInfo.getApplicationVersionName());
                resetData(context);
                editor.apply();
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                editor.apply();
            }
        }
        if (prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false)) {
            return;
        } else if (prefs.getBoolean(PREF_REMIND_LATER, false)) {
            days = daysUntilPromptForRemindLater;
            launches = launchesUntilPromptForRemindLater;
        } else {
            days = daysUntilPrompt;
            launches = launchesUntilPrompt;
        }

        // Increment launch counter
        long launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0) + 1;
        editor.putLong(PREF_LAUNCH_COUNT, launch_count);
        // Get date of first launch
        Long date_firstLaunch = prefs.getLong(PREF_FIRST_LAUNCHED, 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        }
        // Wait for at least the number of launches or the number of days used
        // until prompt
        if (launch_count >= launches || (System.currentTimeMillis() >= date_firstLaunch + (days * 24 * 60 * 60 * 1000))) {
            showRateAlertDialog(context, editor);
        }
        editor.apply();
    }

    /**
     * Call this method directly if you want to force a rate prompt, useful for
     * testing purposes
     *
     * @param context {@link Context}
     */
    public void showRateDialog(final Context context) {
        showRateAlertDialog(context, null);
    }

    /**
     * Call this method directly to go straight to play store listing for rating
     *
     * @param context {@link Context}
     */
    public void rateNow(final Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, market.getMarketURI(context)));
        } catch (ActivityNotFoundException activityNotFoundException1) {
            Log.e(AppRater.class.getSimpleName(), "Market Intent not found", activityNotFoundException1);
        }
    }

    /**
     * Returns whether the Rate Now button has been pressed in the past
     *
     * @param context {@link Context}
     * @return true if the Rate Now button has previously been pressed
     */
    public boolean getIsRated(final Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_DONT_SHOW_AGAIN, false);
    }

    /**
     * Sets the rated flag so the dialog will/will not be shown in future
     *
     * @param context {@link Context}
     * @param rated   sets whether the Rate Now button has previously been pressed
     */
    public void setIsRated(final Context context, final boolean rated) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (editor != null) {
            editor.putBoolean(PREF_DONT_SHOW_AGAIN, rated);
            editor.apply();
        }
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        ApplicationRatingInfo ratingInfo = ApplicationRatingInfo.createApplicationInfo(context);
        dialogBuilder.setTitle(String.format(context.getString(R.string.apprater_dialog_title), ratingInfo.getApplicationName()));

        dialogBuilder.setMessage(context.getString(R.string.apprater_rate_message));

        dialogBuilder.setCancelable(isCancelable);

        dialogBuilder.setPositiveButton(context.getString(R.string.apprater_rate),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        rateNow(context);
                        if (editor != null) {
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                            editor.apply();
                        }
                        dialog.dismiss();
                    }
                });

        dialogBuilder.setNeutralButton(context.getString(R.string.apprater_later),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editor != null) {
                            Long date_firstLaunch = System.currentTimeMillis();
                            editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                            editor.putLong(PREF_LAUNCH_COUNT, 0);
                            editor.putBoolean(PREF_REMIND_LATER, true);
                            editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
                            editor.apply();
                        }
                        dialog.dismiss();
                    }
                });
        if (!hideNoButton) {
            dialogBuilder.setNegativeButton(context.getString(R.string.apprater_no_thanks),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (editor != null) {
                                editor.putBoolean(PREF_DONT_SHOW_AGAIN, true);
                                editor.putBoolean(PREF_REMIND_LATER, false);
                                long date_firstLaunch = System.currentTimeMillis();
                                editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
                                editor.putLong(PREF_LAUNCH_COUNT, 0);
                                editor.apply();
                            }
                            dialog.dismiss();
                        }
                    });
        }
        dialogBuilder.show();
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        editor.apply();
    }
}
