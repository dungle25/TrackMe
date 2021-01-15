# TrackMe

TrackMe is an Activity tracker with GPS: track distance & record route

## Architecture overview

TrackMe app follows the [MVVM](https://www.journaldev.com/20292/android-mvvm-design-pattern#:~:text=MVVM%20stands%20for%20Model%2C%20View%2C%20ViewModel.&text=Generally%2C%20it%27s%20recommended%20to%20expose,It%20observes%20the%20ViewModel.) architecture and fully in Kotlin language.

Tools/ Libraries/ Patterns Used:

```
* MVVM architecture 
* Coroutine (use to handle multithread)
* RecyclerView (use to load list)
* Room Persistence (use to store data to local database)
* Gson (use to parse json to object)
* Git (source control)
* Eventbus (observer data)
* Koin (dependency injection)
* Navigation
* Google Map, Google API
```

## Project structure

```
* "data" package contains all classes that related to data handling (get, set data, etc...)
* "di" dependency injection 
* "model" contains models classes
* "service" package contains location tracking service class
* "ui" package contains all classes that related to UI & viewmodel
* "util" package contains helper classes & handling other stuff (calculation, parse value...)
```