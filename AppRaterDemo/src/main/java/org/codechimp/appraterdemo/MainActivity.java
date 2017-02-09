package org.codechimp.appraterdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.codechimp.apprater.AppRater;
import org.codechimp.apprater.GoogleMarket;

public class MainActivity extends Activity {

    private AppRater appRater;
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
                appRater.showRateDialog(v.getContext());
            }
        });

        // Basic implementation using all defaults
        // =======================================
        appRater = new AppRater.Builder().build();
        appRater.appLaunched(this);

        // Advanced Configuration
        // ======================
        // Always start with a builder then set custom values
        AppRater.Builder builder = new AppRater.Builder();

        //TODO - change this comment once builder pattern finalised

        // Override the default day/launch counts
//        builder.daysUntilPrompt(11);
//        builder.launchesUntilPrompt(100);

        // Set the Market you want to use prior to calling app_launched, if not set it will default to Google Play
        // Current implementations are Google Play and Amazon App Store, you can add your own by implementing Market
//         builder.market(new GoogleMarket());
//         builder.packageName("com.mytestpackage");
//         appRater = builder.build();


        // When compiling a debug version of your app this allows you to change the package name meaning QA can test this feature correctly.
        // If no package is set, your default behaviour is used.
//        builder.packageName("com.mytestpackage");

        // Finally build the apprater and launch
//        appRater = builder.build();
//        appRater.appLaunched(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        // This example will conditionally hide a menu item based on whether the rate now button has been pressed.
        menu.findItem(R.id.menu_ratenow).setVisible(appRater.getIsRated(this));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.menu_ratenow): {
                appRater.rateNow(this);
                return true;
            }
        }
        return false;
    }
}
