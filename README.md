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

## `display` mode

### Configuration file

Prepare a configuration file in [HOCON][hocon] format:

``` hocon
config {
  userAgent = "choose-a-user-agent"
  timezone = "choose-a-timezone"
}
```

### Option 1: Docker

Run a container with the latest stable image:

``` shell
docker run -it -v /absolute/path/to/application.conf:/application.conf alecigne/somafm-song-history:v0.4.0 "display" "Drone Zone"
```

The Docker image is hosted on [DockerHub][dockerhub].

### Option 2: Jar file

[Download the jar][jar] (or build it from source), then run:

``` shell
java -jar -Dconfig.file=/path/to/application.conf somafm-song-history.jar "display" "Drone Zone"
```

## `save` mode

### Database

If you use `save` mode, you will need a self-hosted PostgreSQL database. This can be achieved using
Docker and the PostgreSQL official image:

``` shell
docker run \
--name somafm-song-history-db \
-e POSTGRES_PASSWORD=mysecretpassword \
-p 5432:5432 \
-d postgres
```

The default user is `postgres`. Maybe this [note][postgres-note] could be of interest for a few people.

Alternatively, use the provided Docker compose file:

``` shell
docker compose up -d db
```

### Configuration file

Prepare a configuration file in [HOCON][hocon] format:

``` hocon
config {
  userAgent = "choose-a-user-agent"
  timezone = "choose-a-timezone"
  db {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "mysecretpassword"
  }
}
```

### Option 1: Docker

Run the container with your credentials of choice:

``` shell
docker run -it -v /absolute/path/to/application.conf:/application.conf --net=host alecigne/somafm-song-history:v0.4.0 "display" "Drone Zone"
```

Note the `--net=host` option.

The Docker image is hosted on [DockerHub][dockerhub].

## Option 2: Jar file

[Download the jar][jar] (or build it from source), then run:

``` shell
java -jar -Dconfig.file=/path/to/application.conf somafm-song-history.jar "display" "Drone Zone"
```

(with Java 17)

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

[jar]: https://github.com/alecigne/somafm-song-history/releases/download/v0.4.0/somafm-song-history-0.4.0-with-dependencies.jar

[postgres-note]: https://lecigne.net/notes/postgres-docker.html
