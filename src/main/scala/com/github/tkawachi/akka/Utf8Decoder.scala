package com.github.tkawachi.akka

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.stream.{ Attributes, FlowShape, Inlet, Outlet }
import akka.util.ByteString

class Utf8Decoder extends GraphStage[FlowShape[ByteString, String]] {
  import Utf8Decoder._

  private val in: Inlet[ByteString] = Inlet("ByteStringInlet")

  private val out: Outlet[String] = Outlet("StringOutlet")

  override def shape: FlowShape[ByteString, String] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {

      var incompleteBytes: ByteString = ByteString.empty

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          val bs = incompleteBytes ++ grab(in)
          val n = numberOfIncompleteBytes(bs)
          val (completeCharBytes, rest) = bs.splitAt(bs.length - n)
          push(out, completeCharBytes.utf8String)
          incompleteBytes = rest
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          pull(in)
        }
      })
    }
  }

}

object Utf8Decoder {

  val maxBytesPerChar: Int = 6

  def numberOfIncompleteBytes(bs: ByteString): Int = {
    bs.takeRight(maxBytesPerChar).reverseIterator.map(followingBytes).zipWithIndex
      .find { case (fb, _) => fb >= 0 }
      .map { case (fb, i) => if (fb == i) 0 else i + 1 }
      .getOrElse(0)
  }

  def followingBytes(b: Byte): Int = {
    if ((b & 0x80) == 0x00) {
      0 // ASCII char
    } else if ((b & 0xE0) == 0xC0) {
      1 // head of 2 bytes char
    } else if ((b & 0xF0) == 0xE0) {
      2 // head of 3 bytes char
    } else if ((b & 0xF8) == 0xF0) {
      3 // head of 4 bytes char
    } else if ((b & 0xFC) == 0xF8) {
      4 // head of 5 bytes char
    } else if ((b & 0xFE) == 0xFC) {
      5 // head of 6 bytes char
    } else {
      -1 // non-head or invalid byte
    }
  }

  def flow: Flow[ByteString, String, NotUsed] = Flow.fromGraph(new Utf8Decoder)
}
