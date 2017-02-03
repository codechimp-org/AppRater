package org.codechimp.apprater;

import android.content.Context;
import android.net.Uri;

public class AmazonMarket extends Market {
    private static String marketLink = "http://www.amazon.com/gp/mas/dl/android?p=";

    @Override
    public Uri getMarketURI(Context context) {
        return Uri.parse(marketLink + Market.getPackageName(context));
    }
}

