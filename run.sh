!#/bin/sh
brew service start mongodb/brew/mongodb-community
./gradlew run
brew service stop mongodb/brew/mongodb-community