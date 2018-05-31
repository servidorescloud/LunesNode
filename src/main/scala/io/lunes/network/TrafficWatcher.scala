package io.lunes.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelDuplexHandler, ChannelHandlerContext, ChannelPromise}
import kamon.Kamon
//import kamon.metric.instrument.{Histogram, Memory}
import kamon.metric.{Histogram, MeasurementUnit}
import scorex.network.message.{Message => ScorexMessage}

/**
  *
  */
@Sharable
class TrafficWatcher extends ChannelDuplexHandler {

  import BasicMessagesRepo.specsByCodes

  private val outgoing: Map[ScorexMessage.MessageCode, Histogram] = specsByCodes.map { case (code, spec) =>
    code -> createHistogram("outgoing", spec)
  }

  private val incoming: Map[ScorexMessage.MessageCode, Histogram] = specsByCodes.map { case (code, spec) =>
    code -> createHistogram("incoming", spec)
  }
/*
  private def createHistogram(dir: String, spec: BasicMessagesRepo.Spec) = Kamon.metrics.histogram("traffic", Map(
    "type" -> spec.messageName,
    "dir" -> dir
  ), Memory.Bytes)
*/
  private def createHistogram(dir: String, spec: BasicMessagesRepo.Spec) =
    Kamon
      .histogram("traffic", MeasurementUnit.information.bytes)
      .refine(
        "type" -> spec.messageName,
        "dir" -> dir
      )

  /**
    *
    * @param ctx
    * @param msg
    * @param promise
    */
  override def write(ctx: ChannelHandlerContext, msg: AnyRef, promise: ChannelPromise): Unit = {
    msg match {
      case x: RawBytes => outgoing.get(x.code).foreach(_.record(x.data.length))
      case _ =>
    }

    super.write(ctx, msg, promise)
  }

  /**
    *
    * @param ctx
    * @param msg
    */
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    msg match {
      case x: RawBytes => incoming.get(x.code).foreach(_.record(x.data.length))
      case _ =>
    }

    super.channelRead(ctx, msg)
  }

}
