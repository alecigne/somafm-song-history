set shell := ["bash", "-euo", "pipefail", "-c"]

maven := "./mvnw -B --no-transfer-progress"
local_image := "localhost/somafm-song-history:local"

unit-test:
    {{maven}} test

integration-test:
    {{maven}} test-compile failsafe:integration-test failsafe:verify

package:
    {{maven}} -Dmaven.test.skip=true package

build: unit-test integration-test package

db-up:
    podman-compose up -d

db-down:
    podman-compose down

db-reset:
    podman-compose down -v

image tag=local_image: package
    podman build -f Containerfile -t "{{tag}}" .

run-api: db-up package
    java -jar target/somafm-song-history-with-dependencies.jar api

run-api-container tag=local_image: db-up (image tag)
    podman run --rm -it --name somafm-song-history-api --network=host "{{tag}}" api
