# `somafm-song-history`

`somafm-song-history` is a Java application that retrieves and prints [SomaFM][soma]'s recently
played songs in the console, or save it to a database.

Please support SomaFM's awesome work [here][soma-support].

# About

This project is developed for my personal use, but I'll be glad to help if you encounter any
[issue][issues].

My goal is to build and browse a personal database of songs played by SomaFM, and as an ambient fan,
especially Drone Zone.

This project uses [somafm-recentlib][lib].

# Usage

Two arguments must be provided to the application: an *action* (`display` or `save`) and a *channel*
(e.g. `Groove Salad`). The channel name is its public name, available on [this page][soma-channels].

## Using Docker

The Docker image is hosted on [DockerHub][dockerhub].

If you want to run everything with [the default config][config], simply run this for `display` mode:

``` shell
docker run --rm -it alecigne/somafm-song-history:v0.4.1 "display" "Drone Zone"
```

For `save` mode, you will need a PostgreSQL database. You can spawn it using the provided Docker
Compose file:

``` shell
docker compose up -d db
```

It will create a database matching the default config above. Then:

``` shell
docker run --rm -it --net=host alecigne/somafm-song-history:v0.4.1 "save" "Drone Zone"
```

`--net=host` is necessary to reach a local database.

If you need your own config, typically to run the application as a service on your local network,
prepare a file in [HOCON][hocon] format:

``` hocon
config {
  userAgent = "my-own-user-agent"
  timezone = "my-own-timezone"
  // This part is optional in display mode
  db {
    url = "jdbc:postgresql://[my-host]:[my-port]/my_own_db"
    user = "user"
    password = "password"
  }
}
```

Then pass the config to Docker with your chosen mode:

``` shell
docker run --rm -it -v /path/to/application.conf:/application.conf alecigne/somafm-song-history:v0.4.1 "display" "Drone Zone"
```

## Using a JAR file (Java 17)

[Download the jar][jar] or build it from source. Use the commands above (Docker section) if you need
a Postgres instance and/or a custom config file. Then:

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

[jar]:
https://github.com/alecigne/somafm-song-history/releases/download/v0.4.1/somafm-song-history-with-dependencies.jar

[postgres-note]:
https://lecigne.net/notes/postgres-docker.html

[config]:
https://github.com/alecigne/somafm-song-history/blob/master/src/main/resources/application.conf
