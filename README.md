## AndroidNotifier
_Desktop event notifier for Android devices_

This is forked from [android-notifier](http://code.google.com/p/android-notifier/).

# Introduction

This project sends notifications to a desktop computer when certain events happen on an Android device, such as the phone ringing, an SMS being received, or the battery running low. The notifications can be sent over Wifi, Bluetooth, or (in the future) USB.

It runs as a service on android, consuming little resources while no events are happening, and the desktop application notifies the user about the event in some way (Growl on Mac, Gnome dbus notifications on Linux, Growl for Windows or System tray alert on Windows), including information such as the number that's calling.

This is useful for people (like the developer) who wear noise-cancelling headphones, keep their cell phone in their bags, or don't want to be interrupted to look at a vibrating cell phone in a meeting.

There are currently two different desktop applications - one called MacDroidNotifier for MacOS X, and another called MultiDroidNotifier which works on all operating systems. The former is in the process of being deprecated, so please download the later.

# Next Step

This project will be a whole new one sperate from the orginal.

* Code refactoring is the important target at first.

* Remove words of `Android` from project name, because the `Android` is a trademark of Google.

* Then, will keep on adding USB support, all App notification support (Pass all notifications on notification center to other device).

* Refactoring for desktop and make some tablet device can also receive notifications.

# License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

And the icons and images which not specially mentioned are under [Creative Commons 3.0 BY-SA](http://creativecommons.org/licenses/by-sa/3.0/)

[Android](http://www.android.com/) is a trademark of [Google Inc.](http://www.google.com/)
