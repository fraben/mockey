My first Android app. It is a pretty useless one, and the code is definitely not the best out there (i'm a grunt, not an expert at all!).
I did try to develop something not entirely trivial though, and practice on some of the basics, so, hopefully, absolute beginners might find some parts of it useful. 

[Link on Play Store](https://play.google.com/store/apps/details?id=com.accia77.mockey) (free/no ads/no evil stuff)


####External libraries:
* [ActionBarSherlock](http://actionbarsherlock.com/)


####Some of the basics covered, in random order:
* Database management
* Text to speech, using a service
* Audio recording and playback
* Navigation Drawer
* Shared Preferences
* Custom Application object
* Strings internationalization (only English and Italian so far)
* Action bar support in older devices (obviously thanks to ActionBarSherlock)


####Known issues:
* I just found out that on some older devices the soft keyboard won't reappear: i think it's related to the Navigation Drawer opening while the keyboard is up. I will try to fix it asap
* I am not loading the bitmaps on a worker thread. Shame on me, i left it like that for pure laziness. Gonna fix it asap as well.
* Only the query to retrieve all records is run on a worker thread. Other ones are not!


####Unused stuff
Being mostly a learning exercise for myself, i included stuff that eventually was dropped, or left stubs of potential new features. I hope it doesn't introduce too much clutter.
* Export/import of the quotes, basically letting the user the creation of a zip file with all the quotes in the database, its "export" by email and its import. Since right now it's not used, you can safely ignore the files:
    - Compress.java
    - Decompress.java
