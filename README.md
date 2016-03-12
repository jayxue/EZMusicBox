# EZMusicBox - Android library for eazily creating music box apps

An Android library for easily creating music box applications for various topics, events or occasions.

![Demo Screenshot 1](https://github.com/jayxue/EZMusicBox/blob/master/EZMusicBoxSDK/src/main/res/raw/screenshot_1.png)
![Demo Screenshot 2](https://github.com/jayxue/EZMusicBox/blob/master/EZMusicBoxSDK/src/main/res/raw/screenshot_2.png)

Details
-------
This Android library facilitates developers to create music box applications running on Android. No matter what topic, event or occasion you are facing, as long as you assemble a list of MP3 music links, you can always build an Android app that retrieves and plays music on the fly.

The music apps can be used for various situations to create atmosphere that matches the themes and properly express your feelings.

The major features include:
* Press a music to retrieve from the Internet.
* Start playing music before retrieval is completed.
* Directly play cached music.
* Play all cached music in sequential order.
* Play animation on screen for better experience. The animation can be stopped.

Usage
-----

In order to utilize this library, you just need to do some configurations without writing any code.

* Import the EZMusicBoxSDK module into your Android Studio project. Add dependency to the module to your app project.
* In your Android app's ```AndroidManifest.xml```, make sure that you have the following permissions:
  * ```android.permission.INTERNET```
  * ```android.permission.WRITE_EXTERNAL_STORAGE```
  * ```com.android.launcher.permission.INSTALL_SHORTCUT```
  * ```com.android.launcher.permission.READ_SETTINGS```
* In your app's ```AndroidManifest.xml```, include the activities:
  * ```com.wms.ezmusicbox.sdk.activity.MusicListActivity```
  * ```com.wms.ezmusicbox.sdk.activity.AboutAppActivity```
* In your app's ```res/values/strings.xml```,
  * Set ```app_name``` (name of your application).
  * Set ```aboutAppText``` (introduction to your application)
* Replace your app's ic_launcher icons. You may also replace about_app.png, home_header.png or menu_icon.png to customize your app.
* In your app's ```assets/music_list.json```, add information for the music to include in your app. For each music, you'll need to provide:
  * auther
  * name
  * link to mp3 file
  * size in KBs
  * duration in seconds
Note: please pay attention to your links. Many music are copyright-protected material and it is not allowed to download from the Internet for free. You may want to make sure that you have permission of using the material.
For more information, see https://play.google.com/about/spam.html#impersonation-intellectual-property.
  
Of course you can modify any components of the library or add new components to customize your Android app's functionality.

Acknowledgement
---------------

This library utilizes the following libraries:
* Daniele Palombo: https://github.com/DanielePalombo/105RadioAlarm
* "Java Source Code Warehouse" project from DevDaily.com: http://alvinalexander.com/java/jwarehouse/android-examples/platforms/android-2/samples

Developer
---------
* Jay Xue <yxue24@gmail.com>, Waterloo Mobile Studio

License
-------

    Copyright 2016 Waterloo Mobile Studio

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


