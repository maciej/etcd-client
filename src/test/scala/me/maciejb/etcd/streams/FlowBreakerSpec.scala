package me.maciejb.etcd.streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestKitBase
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class FlowBreakerSpec extends FlatSpec with Matchers with TestKitBase with BeforeAndAfterAll {

  override implicit lazy val system: ActorSystem = ActorSystem("FlowBreakerSpec")
  implicit val mat: Materializer = ActorMaterializer()

  val testFlow = TestSource.probe[Int]
    .viaMat(FlowBreaker[Int])(Keep.both)
    .toMat(TestSink.probe[Int]) {
      case ((sourceProbe, cancellable), sinkProbe) ⇒ (sourceProbe, sinkProbe, cancellable)
    }

  "FlowBreaker" should "cancel a stream without outstanding demand downstream" in {
    val (sourceProbe, sinkProbe, cancellable) = testFlow.run()

    sinkProbe.ensureSubscription()

    cancellable.cancel()

    sourceProbe.expectCancellation()

    sinkProbe.expectComplete()
  }

  it should "cancel a stream with outstanding demand downstream" in {
    val (sourceProbe, sinkProbe, cancellable) = testFlow.run()

    sinkProbe
      .ensureSubscription()
      .request(1)

    cancellable.cancel()

    sourceProbe.expectCancellation()

    sinkProbe.expectComplete()
  }

  it should "cancel a stream in progress" in {
    val (sourceProbe, sinkProbe, cancellable) = testFlow.run()

    sinkProbe
      .ensureSubscription()
      .request(1)

    sourceProbe.sendNext(0)

    sinkProbe
      .expectNext(0)
      .request(1)

    cancellable.cancel()

    sourceProbe.expectCancellation()

    sinkProbe.expectComplete()
  }

}
