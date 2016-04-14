# Akka HTTP based etcd client
[![Build Status][travis-ci-badge]][travis-ci-link]


This module provides a fully asynchronous client for etcd 2.x implemented using Akka HTTP.

## Running tests

The integration tests require an instance of etcd running. Please use _Docker Compose_
to start it:

```
docker-compose up
```

## Resources
* [etcd-client at bintray](https://bintray.com/maciej/maven/etcd-client/)

[travis-ci-badge]: https://travis-ci.org/maciej/etcd-client.svg
[travis-ci-link]: https://travis-ci.org/maciej/etcd-client
