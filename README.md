# AppRater

AppRater is a library for Android designed to facilitate easy prompting of users to rate your app within the Google Play store.
It won't prompt until at least 3 days and 7 uses of the app has passed and if the user chooses to rate later the count will start again.

AppRater inherits your themeing so can be used with light or dark variants as seen here;

![Example Image Dark][1] ![Example Image Light][2]

To use simply add the library to your app and make one call within your onCreate method as follows;

`AppRater.app_launched(this);`

Optionally you can use the overriden method to specify your own day and launch count parameters.

If you want to have a "Rate Now" menu option to go straight to your play store listing call `AppRater.rateNow(this);` within your menu code.

Try out the demo within this repository.

## Gradle

AppRater is now pushed to Maven Central as an AAR, so you just need to add the following dependency to your `build.gradle`.
    
    dependencies {
        compile 'com.github.codechimp-org.apprater:library:1.0.+'
    }

## Translations

If you would like to help localise this library please contribute to the GetLocalization project located here
[http://www.getlocalization.com/AppRater/](http://www.getlocalization.com/AppRater/)

## Contributing Code

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## Developed By

Andrew Jackson <andrew@codechimp.org>

Google+ profile: 
[https://plus.google.com/109496301003141993140](https://plus.google.com/109496301003141993140)

Adapted from a snippet originally posted [here](http://www.androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater)

## License

    Copyright 2013 Andrew Jackson

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.





 [1]: https://raw.github.com/codechimp-org/AppRater/master/Screenshots/demo-dark.png
 [2]: https://raw.github.com/codechimp-org/AppRater/master/Screenshots/demo-light.png
