package com.github.tkawachi.akka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.ByteString
import org.scalacheck.Gen
import org.scalatest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ BeforeAndAfter, FunSuite }

class Utf8DecoderTest extends FunSuite with PropertyChecks with ScalaFutures with BeforeAndAfter {

  implicit var actorSystem: ActorSystem = _

  implicit def materializer(implicit actorSystem: ActorSystem): Materializer = ActorMaterializer()

  before {
    actorSystem = ActorSystem("system")
  }

  after {
    actorSystem.terminate()
  }

  test("abcde") {
    splitTest(ByteString("abcde"))
  }

  test("日本語") {
    splitTest(ByteString("日本語"))
  }

  test("😺👽💩🐴💛") {
    splitTest(ByteString("😺👽💩🐴💛"))
  }

  def splitTest(bs: ByteString): scalatest.Assertion = {
    forAll((Gen.chooseNum(0, bs.length), "splitAt")) { splitAt =>
      val (bs1, bs2) = bs.splitAt(splitAt)
      val source = Source.single(bs1) ++ Source.single(bs2)
      val actual = source.via(Utf8Decoder.flow).runReduce(_ ++ _)
      val expected = bs.utf8String
      assert(actual.futureValue == expected)
    }
  }
}
