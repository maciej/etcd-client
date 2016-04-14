package me.maciejb.etcd.dockermachine

import scala.util.Try
import sys.process._


object DockerMachineIp {

  /**
    * Determines the address of docker-machine if installed.
    *
    * We assume the etcd instance used in tests is ran out of provided docker-compose.yml.
    *
    * On OSes other than Linux Docker Machine has to be used to support docker and this method will
    * try determining the ip address of the Docker Machine VM falling back to 127.0.0.1.
    */
  lazy val address: String = Try("docker-machine ip".!!.trim).getOrElse("127.0.0.1")

}