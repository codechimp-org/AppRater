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


        // When compiling a debug version of your app this allows you to change the package name meaning QA can test this feature correctly.
        // To change package name, just call...
        // appRater.setPackageName("com.mytestpackagename");
        // If no package is set, your default behaviour is used.


        //TODO - change this comment once builder pattern finalised
        // Optionally you can set the Market you want to use prior to calling app_launched
        // If setMarket not called it will default to Google Play
        // Current implementations are Google Play and Amazon App Store, you can add your own by implementing Market
        // AppRater.setMarket(new GoogleMarket());
        // AppRater.setMarket(new AmazonMarket());

        // This will keep a track of when the app was first used and whether to show a prompt
        // It should be the default implementation of AppRater

        // It should be the default implementation of AppRater
        appRater = new AppRater.Builder().build();
        appRater.appLaunched(this);
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
