package me.maciejb.etcd.client

import akka.actor.{ActorSystem, Cancellable}
import akka.http.scaladsl.settings.ClientConnectionSettings
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import me.maciejb.etcd.client.impl.EtcdClientImpl

import scala.concurrent.{ExecutionContext, Future}

/**
  * `etcd` client API
  *
  * @see [[https://coreos.com/etcd/docs/latest/api.html etcd API documentation]]
  */
trait EtcdClient {

  def get(key: String, recursive: Boolean = false, sorted: Boolean = false): Future[EtcdResponse]

  def wait(key: String,
           waitIndex: Option[Int] = None,
           recursive: Boolean = false,
           sorted: Boolean = false,
           quorum: Boolean = false): Future[EtcdResponse]

  def set(key: String, value: String, ttl: Option[Int] = None): Future[EtcdResponse]

  def compareAndSet(key: String, value: String,
                    ttl: Option[Int] = None,
                    prevValue: Option[String] = None,
                    prevIndex: Option[Int] = None,
                    prevExist: Option[Boolean] = None): Future[EtcdResponse]

  def refreshTtl(key: String, ttl: Int): Future[EtcdResponse]

  def clearTtl(key: String): Future[EtcdResponse]

  def create(parentKey: String, value: String): Future[EtcdResponse]

  def createDir(key: String, ttl: Option[Int] = None): Future[EtcdResponse]

  def refreshDirTtl(key: String, ttl: Int): Future[EtcdResponse]

  def delete(key: String, recursive: Boolean = false): Future[EtcdResponse]

  def compareAndDelete(key: String,
                       prevValue: Option[String] = None,
                       prevIndex: Option[Int] = None): Future[EtcdResponse]

  def watch(key: String,
            waitIndex: Option[Int] = None,
            recursive: Boolean = false,
            quorum: Boolean = false): Source[EtcdResponse, Cancellable]

}

/**
  * `etcd` client factory.
  */
object EtcdClient {

  /**
    * Creates a new instance of `etcd` client.
    *
    * @param host               host to connect to.
    * @param port               port to connect to, 4001 by default.
    * @param httpClientSettings optional client options for Akka HTTP.
    */
  def apply(host: String, port: Int = 4001,
            httpClientSettings: Option[ClientConnectionSettings] = None)
           (implicit ec: ExecutionContext,
            system: ActorSystem,
            mat: Materializer): EtcdClient =
    new EtcdClientImpl(host, port, httpClientSettings)

}
