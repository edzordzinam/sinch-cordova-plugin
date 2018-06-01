# Cordova Hello World Plugin

Simple plugin that returns your string prefixed with hello.

Greeting a user with "Hello, world" is something that could be done in JavaScript. This plugin provides a simple example demonstrating how Cordova plugins work.

## Using

Create a new Cordova Project

    $ cordova create hello com.example.helloapp Hello
    
Install the plugin

    $ cd hello
    $ cordova plugin add https://github.com/don/cordova-plugin-hello.git
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling Hello Plugin");
    }

    hello.greet("World", success, failure);
```

Install iOS or Android platform

    cordova platform add ios
    cordova platform add android
    
Run the code

    cordova run 

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/latest/guide/cli/index.html)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/latest/guide/hybrid/plugins/index.html)




        <source-file src="src/android/libs/sinch-android-rtc-3.12.4.aar" target-dir="libs" framework="true"/>
        <source-file src="src/android/CallPlugin.java" target-dir="src/prexition/plugin/voip/"/>
        <source-file src="src/android/AudioPlayer.java" target-dir="src/prexition/plugin/voip/"/>
        <source-file src="src/android/SinchService.java" target-dir="src/prexition/plugin/voip/"/>
        <source-file src="src/android/JSONErrorFactory.java" target-dir="src/prexition/plugin/voip/"/>
        <source-file src="src/android/PermissionHelper.java" target-dir="src/prexition/plugin/voip/"/>
        <source-file src="src/android/CallScreenActivity.java" target-dir="src/prexition/plugin/voip"/>

        <resource-file src="src/android/res/raw/progress_tone.wav" target="res/raw/progress_tone.wav"/>

        <resource-file src="src/android/res/mipmap-hdpi/loudspeaker.png" target="res/mipmap-hdpi/loudspeaker.png"/>
        <resource-file src="src/android/res/mipmap-mdpi/loudspeaker.png" target="res/mipmap-mdpi/loudspeaker.png"/>
        <resource-file src="src/android/res/mipmap-xhdpi/loudspeaker.png" target="res/mipmap-xhdpi/loudspeaker.png"/>
        <resource-file src="src/android/res/mipmap-xxhdpi/loudspeaker.png" target="res/mipmap-xxhdpi/loudspeaker.png"/>
        <resource-file src="src/android/res/mipmap-xxxhdpi/loudspeaker.png" target="res/mipmap-xxxhdpi/loudspeaker.png"/>

        <resource-file src="src/android/res/mipmap-hdpi/loudspeaker_on.png" target="res/mipmap-hdpi/loudspeaker_on.png"/>
        <resource-file src="src/android/res/mipmap-mdpi/loudspeaker_on.png" target="res/mipmap-mdpi/loudspeaker_on.png"/>
        <resource-file src="src/android/res/mipmap-xhdpi/loudspeaker_on.png" target="res/mipmap-xhdpi/loudspeaker_on.png"/>
        <resource-file src="src/android/res/mipmap-xxhdpi/loudspeaker_on.png" target="res/mipmap-xxhdpi/loudspeaker_on.png"/>
        <resource-file src="src/android/res/mipmap-xxxhdpi/loudspeaker_on.png" target="res/mipmap-xxxhdpi/loudspeaker_on.png"/>

        <resource-file src="src/android/res/mipmap-hdpi/hangup.png" target="res/mipmap-hdpi/hangup.png"/>
        <resource-file src="src/android/res/mipmap-mdpi/hangup.png" target="res/mipmap-mdpi/hangup.png"/>
        <resource-file src="src/android/res/mipmap-xhdpi/hangup.png" target="res/mipmap-xhdpi/hangup.png"/>
        <resource-file src="src/android/res/mipmap-xxhdpi/hangup.png" target="res/mipmap-xxhdpi/hangup.png"/>
        <resource-file src="src/android/res/mipmap-xxxhdpi/hangup.png" target="res/mipmap-xxxhdpi/hangup.png"/>


        <resource-file src="src/android/res/drawable/roundcorner.xml" target="res/drawable/roundcorner.xml"/>
        <resource-file src="src/android/res/drawable/roundcorner_black.xml" target="res/drawable/roundcorner_black.xml"/>

        <resource-file src="src/android/res/layout/activity_call_screen.xml" target="res/layout/activity_call_screen.xml" />
        <resource-file src="src/android/res/values/colors.xml" target="res/values/colors.xml" />
        <resource-file src="src/android/res/values/styles.xml" target="res/values/styles.xml" />