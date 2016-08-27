# Akka HTTP based etcd client
[![Maven Central][maven-central-badge]][maven-central-link]
[![Build Status][travis-ci-badge]][travis-ci-link]

This module provides a fully asynchronous client for etcd 2.x based on [Akka Streams / HTTP](http://akka.io).

## Getting started
In your `build.sbt` add the following:
```scala
libraryDependencies += "me.maciejb.etcd-client" %% "etcd-client" % "0.1.1"
```

_etcd-client_ is available for Scala 2.11 and Akka 2.4.

## Usage
```scala
import me.maciejb.etcd.client._

/* ActorSystem, ExecutionContext and Materializer have to available implicitly in scope */

val client = EtcdClient(host, port)

val responseFut: Future[EtcdResponse] = client.get("/some/key")
/* ... */

```
See [EtcdClient.scala] source code to get started.

## Running tests

The integration tests require an instance of etcd running. Please use _Docker Compose_
to start it:

```
docker-compose up
```

## Resources
* [etcd project website](https://coreos.com/etcd/)
* [etcd-client at bintray](https://bintray.com/maciej/maven/etcd-client/)

[maven-central-badge]: https://maven-badges.herokuapp.com/maven-central/me.maciejb.etcd-client/etcd-client_2.11/badge.svg
[maven-central-link]: https://maven-badges.herokuapp.com/maven-central/me.maciejb.etcd-client/etcd-client_2.11
[travis-ci-badge]: https://travis-ci.org/maciej/etcd-client.svg
[travis-ci-link]: https://travis-ci.org/maciej/etcd-client
[EtcdClient.scala]: https://github.com/maciej/etcd-client/blob/master/src/main/scala/me/maciejb/etcd/client/EtcdClient.scala
