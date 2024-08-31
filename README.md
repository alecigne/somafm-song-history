# `somafm-song-history`

Application that retrieves and prints [SomaFM](https://somafm.com)'s recently played songs in the
console, or save it to a database.

Please support SomaFM's awesome work [here](https://somafm.com/support/).

# About

This project is developed for my personal use, but I'll be glad to help if you encounter
any [issue](https://github.com/alecigne/somafm-song-history/issues).

My goal is to build and browse a personal database of songs played by SomaFM, and as an ambient fan,
especially Drone Zone.

As stated on [this page](https://somafm.com/linktous/api.html):

> SomaFM API is no longer available to third parties

Thus this application works by parsing SomaFM's "Recently Played Songs" page
(example [here](https://somafm.com/dronezone/songhistory.html)).

# Usage

Two arguments must be provided to the application: an *action* (`display` or `save`) and a
*channel* (e.g. `Groove Salad`). The channel name is its public name, available
on [this page](https://somafm.com/#alpha).

## Setting up a database

You will need a PostgreSQL database running. This can be achieved using Docker and the PostgreSQL
official image:

``` shell
docker run \
--name postgres-db \
-e POSTGRES_PASSWORD=mysecretpassword \
-p 5432:5432 \
-d postgres
```

The default user is `postgres`.

## Option 1: Docker

Run the container with your credentials of choice:

``` shell
docker run \
-e DB_URL="jdbc:postgresql://localhost:5432/postgres" \
-e DB_USER="postgres" \
-e DB_PASSWORD="mysecretpassword" \
--net=host \
alecigne/somafm-song-history "display" "Drone Zone"
```

The Docker image is hosted
on [DockerHub](https://hub.docker.com/r/alecigne/somafm-song-history).

## Option 2: Jar file

[Download the jar](https://github.com/alecigne/somafm-song-history/releases/download/0.2.0/somafm-song-history-0.2.0.jar)
or build it from source), then run:

``` shell
DB_URL="jdbc:postgresql://localhost:5432/postgres" \
DB_USER="postgres" \
DB_PASSWORD="mysecretpassword" \
java -jar somafm-song-history-0.2.0.jar "display" "Drone Zone"
```

(with Java 17)

# Known bugs

Check them [here](https://github.com/alecigne/somafm-song-history/issues?q=is%3Aopen+is%3Aissue+label%3Abug).
