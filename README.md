# keywordstreamer

Find keyword suggestions for web, video, shopping, and wiki sources. Type in your keyword, and watch as thousands of related keyword suggestions stream in.

## Overview

### Technologies Used

* [Clojure](https://clojure.org/)
* [ClojureScript](https://clojurescript.org/)
* [Websockets](https://github.com/ptaoussanis/sente)
* [React](https://reagent-project.github.io/)
* [Data flow programming](https://github.com/Day8/re-frame) (Similar to Redux and Elm)

### Purpose

I originally created this app as an alternative to ubersuggest.io and also to learn Clojurescript with the reagent and re-frame libraries. It ended up growing into something 
pretty neat, but due to abuse by bots and mean-hearted people, I had to take it off of the public internet.

## Setup

### Running the application

In order to run the app, clone the sources and run:

    lein uberjar

This creates a standalone jar file that can be readily run by the `java` command:

    java -jar java -jar target/keywordstreamer-0.1.0-SNAPSHOT-standalone.jar
    
To override the default host ip (127.0.0.1) or the default port (8080), set the `HOST_IP` and/or `PORT` environment variables prior to starting the application.

### Development

To get an interactive development environment run:

    lein figwheel

Then in another terminal run:

    lein run
    
Navigate to http://localhost:8080 in order to see the app in action with live javascript reloading.

To clean all compiled files:

    lein clean

## License

Copyright © 2017 Jason Stewart

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
