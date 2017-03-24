package org.codechimp.apprater;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

public class AppRater {
    // Preference Constants
    private final static String PREF_NAME = "apprater";
    private final static String PREF_LAUNCH_COUNT = "launch_count";
    private final static String PREF_FIRST_LAUNCHED = "date_firstlaunch";
    private final static String PREF_DONT_SHOW_AGAIN = "dontshowagain";
    private final static String PREF_REMIND_LATER = "remindmelater";
    private final static String PREF_APP_VERSION_NAME = "app_version_name";
    private final static String PREF_APP_VERSION_CODE = "app_version_code";

    private int daysUntilPrompt = 3;
    private int launchesUntilPrompt = 7;
    private int daysUntilPromptForRemindLater = 3;
    private int launchesUntilPromptForRemindLater = 7;
    private boolean isDark;
    private boolean themeSet;
    private boolean hideNoButton;
    private boolean isVersionNameCheckEnabled;
    private boolean isVersionCodeCheckEnabled;
    private boolean isCancelable = true;

    private static String packageName;

    private Market market = new GoogleMarket();

    @SuppressLint("NewApi")
    public enum Theme {
        LIGHT(AlertDialog.THEME_HOLO_LIGHT),
        DARK(AlertDialog.THEME_HOLO_DARK);
        private int theme;

        Theme(int theme) {
            this.theme = theme;
        }
    }

    public static class Builder {
        private int daysUntilPrompt = 3;
        private int launchesUntilPrompt = 7;
        private int daysUntilPromptForRemindLater = 3;
        private int launchesUntilPromptForRemindLater = 7;

        private Theme theme = Theme.LIGHT;
        private boolean isThemeSet;
        private boolean hideNoButton;
        private boolean isVersionNameCheckEnabled;
        private boolean isVersionCodeCheckEnabled;
        private boolean isCancelable = true;

        private Market market = new GoogleMarket();

        public Builder theme(Theme theme) {
            this.theme = theme;
            this.isThemeSet = true;
            return this;
        }

        public Builder daysUntilPrompt(int daysUntilPrompt) {
            this.daysUntilPrompt = daysUntilPrompt;
            return this;
        }

        public Builder launchesUntilPrompt(int launchesUntilPrompt) {
            this.launchesUntilPrompt = launchesUntilPrompt;
            return this;
        }

        public Builder daysUntilPromptForRemindLater(int daysUntilPromptForRemindLater) {
            this.daysUntilPromptForRemindLater = daysUntilPromptForRemindLater;
            return this;
        }

        public Builder launchesUntilPromptForRemindLater(int launchesUntilPromptForRemindLater) {
            this.launchesUntilPromptForRemindLater = launchesUntilPromptForRemindLater;
            return this;
        }

        public Builder hideNoButton(boolean hideNoButton) {
            this.hideNoButton = hideNoButton;
            return this;
        }

        public Builder versionNameCheckEnabled(boolean isVersionNameCheckEnabled) {
            this.isVersionNameCheckEnabled = isVersionNameCheckEnabled;
            return this;
        }

        public Builder versionCodeCheckEnabled(boolean isVersionCodeCheckEnabled) {
            this.isVersionCodeCheckEnabled = isVersionCodeCheckEnabled;
            return this;
        }

        public Builder cancelable(boolean isCancelable) {
            this.isCancelable = isCancelable;
            return this;
        }

        public Builder market(Market market) {
            this.market = market;
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
        this.isDark = Theme.DARK.equals(builder.theme);
        this.themeSet = builder.isThemeSet;
        this.hideNoButton = builder.hideNoButton;
        this.isVersionNameCheckEnabled = builder.isVersionNameCheckEnabled;
        this.isVersionCodeCheckEnabled = builder.isVersionCodeCheckEnabled;
        this.isCancelable = builder.isCancelable;
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
                commitOrApply(editor);
            }
        }
        if (isVersionCodeCheckEnabled) {
            if (ratingInfo.getApplicationVersionCode() != (prefs.getInt(PREF_APP_VERSION_CODE, -1))) {
                editor.putInt(PREF_APP_VERSION_CODE, ratingInfo.getApplicationVersionCode());
                resetData(context);
                commitOrApply(editor);
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
        commitOrApply(editor);
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

    public void setPackageName(String packageName) {
        this.market.overridePackageName(packageName);
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
        }
        commitOrApply(editor);
    }

    /**
     * The meat of the library, actually shows the rate prompt dialog
     */
    @SuppressLint("NewApi")
    private void showRateAlertDialog(final Context context, final SharedPreferences.Editor editor) {
        AlertDialog.Builder dialogBuilder;
        if (Build.VERSION.SDK_INT >= 11 && themeSet) {
            dialogBuilder = new AlertDialog.Builder(context, (isDark ? AlertDialog.THEME_HOLO_DARK : AlertDialog.THEME_HOLO_LIGHT));
        } else {
            dialogBuilder = new AlertDialog.Builder(context);
        }
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
                            commitOrApply(editor);
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
                            commitOrApply(editor);
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
                                commitOrApply(editor);
                            }
                            dialog.dismiss();
                        }
                    });
        }

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                try {
                    final Button buttonPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if (buttonPositive == null) {
                        return;
                    }

                    LinearLayout linearLayout = (LinearLayout) buttonPositive.getParent();
                    if (linearLayout == null) {
                        return;
                    }

                    // Check positive button not fits in window
                    boolean shouldUseVerticalLayout = false;
                    if (buttonPositive.getLeft() + buttonPositive.getWidth() > linearLayout.getWidth()) {
                        shouldUseVerticalLayout = true;
                    }

                    // Change layout orientation to vertical
                    if (shouldUseVerticalLayout) {
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.setGravity(Gravity.END);
                    }
                } catch (Exception ignored) {
                }
            }
        });
        alertDialog.show();
    }

    @SuppressLint("NewApi")
    private static void commitOrApply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT > 8) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static void resetData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_DONT_SHOW_AGAIN, false);
        editor.putBoolean(PREF_REMIND_LATER, false);
        editor.putLong(PREF_LAUNCH_COUNT, 0);
        long date_firstLaunch = System.currentTimeMillis();
        editor.putLong(PREF_FIRST_LAUNCHED, date_firstLaunch);
        commitOrApply(editor);
    }
}
