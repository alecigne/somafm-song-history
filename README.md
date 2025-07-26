# `somafm-song-history`

Application that retrieves and prints [SomaFM][1]'s recently played songs in the console, or save it
to a database.

Please support SomaFM's awesome work [here][2].

# About

This project is developed for my personal use, but I'll be glad to help if you encounter
any [issue][3].

My goal is to build and browse a personal database of songs played by SomaFM, and as an ambient fan,
especially Drone Zone.

This project uses [somafm-recentlib][4].

# Usage

Two arguments must be provided to the application: an *action* (`display` or `save`) and a
*channel* (e.g. `Groove Salad`). The channel name is its public name, available
on [this page][5].

## `display` mode

### Configuration file

Prepare a configuration file in [HOCON][6] format:

``` hocon
config {
  userAgent = "choose-a-user-agent"
  timezone = "choose-a-timezone"
}
```

### Option 1: Docker

Run a container with the latest image:

``` shell
docker run -it -v /absolute/path/to/application.conf:/application.conf alecigne/somafm-song-history:v0.4.0 "display" "Drone Zone"
```

The Docker image is hosted on [DockerHub][7].

### Option 2: Jar file

[Download the jar][8] (or build it from source), then run:

``` shell
java -jar -Dconfig.file=/path/to/application.conf somafm-song-history.jar "display" "Drone Zone"
```

## `save` mode

### Database

If you use `save` mode, you will need a PostgreSQL database. This can be achieved using Docker and
the PostgreSQL official image:

``` shell
docker run \
--name somafm-song-history-db \
-e POSTGRES_PASSWORD=mysecretpassword \
-p 5432:5432 \
-d postgres
```

The default user is `postgres`.

Alternatively, use the provided Docker compose file:

``` shell
docker compose up -d db
```

### Configuration file

Prepare a configuration file in [HOCON][7] format:

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

The Docker image is hosted on [DockerHub][8].

## Option 2: Jar file

[Download the jar][9] (or build it from source), then run:

``` shell
java -jar -Dconfig.file=/path/to/application.conf somafm-song-history.jar "display" "Drone Zone"
```

(with Java 17)

# Known bugs

Check them [here][9].

[1]: https://somafm.com

[2]: https://somafm.com/support/

[3]: https://github.com/alecigne/somafm-song-history/issues

[4]: https://github.com/alecigne/somafm-recentlib

[5]: https://somafm.com/#alpha

[6]: https://github.com/lightbend/config/blob/main/HOCON.md

[7]: https://hub.docker.com/r/alecigne/somafm-song-history

[8]: https://github.com/alecigne/somafm-song-history/releases/download/0.2.0/somafm-song-history-0.2.0.jar

[9]: https://github.com/alecigne/somafm-song-history/issues?q=is%3Aopen+is%3Aissue+label%3Abug
