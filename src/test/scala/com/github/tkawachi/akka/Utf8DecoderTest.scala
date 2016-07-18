package com.github.tkawachi.akka

import java.nio.charset.StandardCharsets

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ ActorMaterializer, Materializer }
import akka.util.ByteString
import org.scalacheck.Gen
import org.scalatest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ BeforeAndAfterAll, FunSuite }

class Utf8DecoderTest extends FunSuite with PropertyChecks with ScalaFutures with BeforeAndAfterAll {

  implicit var actorSystem: ActorSystem = _

  implicit def materializer(implicit actorSystem: ActorSystem): Materializer = ActorMaterializer()

  override def beforeAll(): Unit = {
    actorSystem = ActorSystem("system")
  }

  override def afterAll(): Unit = {
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
      val expected = bs.utf8String

      val (bs1, bs2) = bs.splitAt(splitAt)
      val source = Source.single(bs1) ++ Source.single(bs2)
      val actual = source.via(Utf8Decoder.flow).runReduce(_ ++ _)
      assert(actual.futureValue == expected)
    }
  }

  test("followingBytes") {
    forAll("char", minSuccessful(10000)) { (char: Char) =>
      val bytes = String.valueOf(char).getBytes(StandardCharsets.UTF_8)
      val fbs = bytes.map(Utf8Decoder.followingBytes)
      assert(fbs.head == bytes.length - 1)
      assert(fbs.tail.forall(_ == -1))
    }
  }
}
