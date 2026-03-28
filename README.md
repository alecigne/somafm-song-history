# `somafm-song-history`

`somafm-song-history` is a Java application that retrieves and prints [SomaFM][soma]'s recently
played songs in the console, or save it to a database.

Current version is v0.6.0.

Please support SomaFM's awesome work [here][soma-support].

# About

This project is developed for my personal use, but I'll be glad to help if you encounter any
[issue][issues].

My goal is to build and browse a personal database of songs played by SomaFM, and as an ambient fan,
especially Drone Zone.

This project uses [somafm-recentlib][lib] through [JitPack][jitpack].

# Usage

## Modes

`somafm-song-history` can be used in 3 different modes.

### API mode

This mode runs a Javalin server that exposes a REST API. The application runs continuously and
update its database regularly for a given set of channels.

This is the main mode; the other two modes below are historical. I kept them... because I can! :)

### Display mode

Prints recently played songs in the console for a given channel.

### Save mode

Saves recently played songs to a database for a given channel.

## Using Docker

The Docker image is hosted on [DockerHub][dockerhub].

If you want to run everything with [the default config][config], simply run this for `display` mode:

``` shell
docker run --rm -it alecigne/somafm-song-history:v0.6.0 "display" "Drone Zone"
```

For `save` mode, you will need a PostgreSQL database. You can spawn it using the provided Docker
Compose file:

``` shell
docker compose up -d db
```

It will create a database matching the default config above. Then:

``` shell
docker run --rm -it alecigne/somafm-song-history:v0.6.0 "save" "Drone Zone"
```

For `api` mode:

TODO

If you need your own config, typically to run the application as a service on your local network,
prepare a file in [HOCON][hocon] format:

``` hocon
userAgent = "my-own-user-agent"
timezone = "my-own-timezone"
// This part is optional in display mode
db {
  url = "jdbc:postgresql://[my-host]:[my-port]/my_own_db"
  user = "user"
  password = "password"
}
```

Then pass the config to Docker with your chosen mode:

``` shell
docker run --rm -it --net=host -v /path/to/application.conf:/application.conf alecigne/somafm-song-history:v0.5.0 "display" "Drone Zone"
```

## Using a JAR file (Java 21)

Grab a Jar from the [releases][releases] section or build it from source. Use the commands above
(Docker section) if you need a Postgres instance and/or a custom config file. Then:

``` shell
java -jar somafm-song-history.jar "display" "Drone Zone"
```

or

``` shell
java -jar -Dconfig.file=/path/to/application.conf somafm-song-history.jar "save" "Drone Zone"
```


[soma]:
https://somafm.com

[soma-support]:
https://somafm.com/support/

[issues]:
https://github.com/alecigne/somafm-song-history/issues

[lib]:
https://github.com/alecigne/somafm-recentlib

[soma-channels]:
https://somafm.com/#alpha

[hocon]:
https://github.com/lightbend/config/blob/main/HOCON.md

[dockerhub]:
https://hub.docker.com/r/alecigne/somafm-song-history

[releases]:
https://github.com/alecigne/somafm-song-history/releases

[postgres-note]:
https://lecigne.net/notes/postgres-docker.html

[config]:
https://github.com/alecigne/somafm-song-history/blob/master/src/main/resources/application.conf

[jitpack]:
https://jitpack.io/
