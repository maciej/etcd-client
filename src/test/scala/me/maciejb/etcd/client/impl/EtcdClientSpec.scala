package me.maciejb.etcd.client.impl

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.TestKit
import me.maciejb.etcd.client.{EtcdError, _}
import me.maciejb.etcd.dockermachine.DockerMachineIp
import org.scalatest.Matchers._
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}

import scala.concurrent.duration._

class EtcdClientSpec extends FlatSpec with ScalaFutures with Inside with BeforeAndAfterAll with IntegrationPatience {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  val etcd = EtcdClient(DockerMachineIp.address)

  val baseKey = s"${UUID.randomUUID().toString}/"

  it should "get and set individual keys" in {
    val respFut = for {
      _ ← etcd.set(baseKey + "one", "1")
      resp ← etcd.get(baseKey + "one")
    } yield resp

    whenReady(respFut) { resp ⇒
      resp should matchPattern {
        case EtcdResponse("get", EtcdNode(_, _, _, _, Some("1"), _, None), _) ⇒
      }
    }
  }

  it should "create and delete individual keys" in {
    val respFut = for {
      _ ← etcd.set(baseKey + "simple", "one")
      resp ← etcd.delete(baseKey + "simple")
    } yield resp

    whenReady(respFut) { resp1 ⇒
      resp1 should matchPattern {
        case EtcdResponse("delete", _, _) ⇒
      }

      whenReady(etcd.get(baseKey + "simple").failed) { resp2 ⇒
        resp2 should matchPattern {
          case EtcdError(100, _, _, _) ⇒
        }
      }
    }
  }

  it should "create directories and list their contents" in {
    val respFut = for {
      _ ← etcd.createDir(baseKey + "dir")
      _ ← etcd.set(baseKey + "dir/one", "1")
      _ ← etcd.set(baseKey + "dir/two", "2")
      _ ← etcd.set(baseKey + "dir/three", "3")
      resp ← etcd.get(baseKey + "dir", recursive = true)
    } yield resp

    whenReady(respFut) { resp ⇒
      inside(resp) {
        case EtcdResponse("get", EtcdNode(_, _, _, _, _, Some(true), Some(nodes)), _) ⇒
          nodes collect {
            case EtcdNode(_, _, _, _, Some(v), _, _) ⇒ v
          } should contain allOf("1", "2", "3")
      }
    }
  }

  it should "create directories and delete them recursively" in {
    val respFut = for {
      _ ← etcd.createDir(baseKey + "dir2")
      _ ← etcd.set(baseKey + "dir2/one", "1")
      _ ← etcd.set(baseKey + "dir2/two", "2")
      _ ← etcd.set(baseKey + "dir2/three", "3")
      _ ← etcd.delete(baseKey + "dir2", recursive = true)
      resp ← etcd.get(baseKey + "dir2", recursive = true).failed
    } yield resp

    whenReady(respFut) { case resp ⇒
      resp should matchPattern {
        case EtcdError(100, _, _, _) ⇒
      }
    }
  }

  it should "set keys conditionally, wrt. key's existence" in {
    whenReady(etcd.compareAndSet(baseKey + "atom1", "1", prevExist = Some(false))) { resp1 ⇒
      resp1 should matchPattern {
        case EtcdResponse("create", _, _) ⇒
      }

      whenReady(etcd.compareAndSet(baseKey + "atom1", "1", prevExist = Some(false)).failed) { case resp2 ⇒
        resp2 should matchPattern {
          case EtcdError(105, _, _, _) ⇒
        }
      }
    }
  }

  "compareAndSet" should "set keys conditionally, wrt. key's current value" in {
    val respFut = for {
      _ ← etcd.set(baseKey + "atom2", "1")
      resp1 ← etcd.compareAndSet(baseKey + "atom2", "2", prevValue = Some("1"))
    } yield resp1

    whenReady(respFut) { resp1 ⇒
      resp1 should matchPattern {
        case EtcdResponse("compareAndSwap", _, _) ⇒
      }

      whenReady(etcd.compareAndSet(baseKey + "atom2", "3", prevValue = Some("1")).failed) { case resp2 ⇒
        resp2 should matchPattern {
          case EtcdError(101, _, _, _) ⇒
        }
      }
    }
  }

  it should "set keys conditionally, wrt. key's last write index" in {
    whenReady(etcd.set(baseKey + "atom3", "1")) { resp1 ⇒
      inside(resp1) {
        case EtcdResponse("set", EtcdNode(_, createdIndex, _, _, Some("1"), _, None), _) ⇒

          whenReady(etcd.compareAndSet(baseKey + "atom3", "2", prevIndex = Some(createdIndex))) { resp2 ⇒
            resp2 should matchPattern {
              case EtcdResponse("compareAndSwap", _, _) ⇒
            }

            whenReady(etcd.compareAndSet(baseKey + "atom2", "3", prevIndex = Some(createdIndex)).failed) { case resp3 ⇒
              resp3 should matchPattern {
                case EtcdError(101, _, _, _) ⇒
              }
            }
          }
      }
    }
  }

  it should "delete keys conditionally, wrt. key's current value" in {
    whenReady(for {
      _ ← etcd.set(baseKey + "atom4", "1")
      resp1 ← etcd.compareAndDelete(baseKey + "atom4", prevValue = Some("2")).failed
    } yield resp1) { case resp1 ⇒
      resp1 should matchPattern {
        case EtcdError(101, _, _, _) ⇒
      }

      whenReady(etcd.compareAndDelete(baseKey + "atom4", prevValue = Some("1"))) { resp2 ⇒
        resp2 should matchPattern {
          case EtcdResponse("compareAndDelete", _, _) ⇒
        }
      }
    }
  }

  it should "delete keys conditionally, wrt. key's last write index" in {
    whenReady(etcd.set(baseKey + "atom5", "1")) { resp1 ⇒
      inside(resp1) {
        case EtcdResponse("set", EtcdNode(_, createdIndex, _, _, Some("1"), _, None), _) ⇒

          whenReady(etcd.compareAndDelete(baseKey + "atom5", prevIndex = Some(createdIndex - 1)).failed) { case resp2 ⇒
            resp2 should matchPattern {
              case EtcdError(101, _, _, _) ⇒
            }

            whenReady(etcd.compareAndDelete(baseKey + "atom5", prevIndex = Some(createdIndex))) { resp3 ⇒
              resp3 should matchPattern {
                case EtcdResponse("compareAndDelete", _, _) ⇒
              }
            }
          }
      }
    }
  }

  it should "create new unique keys and retrieve them in order" in {
    val respFut = for {
      _ ← etcd.createDir(baseKey + "dir3")
      _ ← etcd.create(baseKey + "dir3", "1")
      _ ← etcd.create(baseKey + "dir3", "2")
      _ ← etcd.create(baseKey + "dir3", "3")
      resp ← etcd.get(baseKey + "dir3", recursive = true, sorted = true)
    } yield resp

    whenReady(respFut) { resp ⇒
      inside(resp) {
        case EtcdResponse("get", EtcdNode(_, _, _, _, _, Some(true), Some(nodes)), _) ⇒
          val Key = s"/${baseKey}dir3/(\\d+)".r
          val (keys, values) = (nodes collect {
            case EtcdNode(Key(seq), _, _, _, Some(v), _, _) ⇒ (seq.toInt, v)
          }).unzip
          keys shouldBe sorted
          values should contain inOrderOnly("1", "2", "3")
      }
    }
  }

  it should "wait for key updates" in {
    whenReady(etcd.set(baseKey + "wait1", "1")) { resp1 ⇒
      val index = resp1.node.modifiedIndex
      etcd.set(baseKey + "wait1", "2")
      whenReady(for {
        resp2 ← etcd.wait(baseKey + "wait1", waitIndex = Some(index + 1))
      } yield resp2) { resp2 ⇒
        resp2 should matchPattern {
          case EtcdResponse("set", EtcdNode(_, _, _, _, Some("2"), _, None), Some(EtcdNode(_, _, _, _, Some("1"), _, None))) ⇒
        }
      }
    }
  }

  it should "replay key updates that happened in the past" in {
    whenReady(etcd.set(baseKey + "wait2", "1")) { resp1 ⇒
      val index = resp1.node.modifiedIndex
      whenReady(for {
        resp2 ← etcd.wait(baseKey + "wait2", waitIndex = Some(index))
      } yield resp2) { resp2 ⇒
        resp2 should matchPattern {
          case EtcdResponse("set", EtcdNode(_, _, _, _, Some("1"), _, None), None) ⇒
        }
      }
    }
  }

  it should "provide a stream of updates to a key" in {
    val respFut = for {
      _ ← etcd.createDir(baseKey + "watch1")
      resp ← etcd.create(baseKey + "watch1", "1")
      _ ← etcd.create(baseKey + "watch1", "2")
      _ ← etcd.create(baseKey + "watch1", "3")
      _ ← etcd.get(baseKey + "watch1", recursive = true, sorted = true)
    } yield resp

    whenReady(respFut) { resp ⇒
      val createdIndex = resp.node.createdIndex
      whenReady(etcd.watch(baseKey + "watch1", Some(createdIndex), true).take(3).runFold(Seq[EtcdResponse]()) {
        case (resps, r) ⇒ r +: resps
      }) { resps ⇒
        resps.map(_.node.value.get).reverse should contain inOrderOnly("1", "2", "3")
      }
    }
  }

  it should "allow cancelling the stream of updates" in {
    val respFut = for {
      _ ← etcd.createDir(baseKey + "watch2")
      resp ← etcd.create(baseKey + "watch2", "1")
      _ ← etcd.create(baseKey + "watch2", "2")
      _ ← etcd.create(baseKey + "watch2", "3")
    } yield resp

    whenReady(respFut) { resp ⇒
      val createdIndex = resp.node.createdIndex
      val source = etcd.watch(baseKey + "watch2", Some(createdIndex), recursive = true)
      val sink = TestSink.probe[EtcdResponse]
      val (cancellable, probe) = source.toMat(sink)(Keep.both).run()
      probe.within(1.second) {
        probe.request(1)
        probe.expectNext()
        cancellable.cancel()
        probe.request(1)
        // streams should complete immediately after cancelation regardless of pending demand
        probe.expectComplete()
      }
    }
  }

  override protected def afterAll() = TestKit.shutdownActorSystem(system)

}
