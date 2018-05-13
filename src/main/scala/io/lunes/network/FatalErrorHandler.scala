package io.lunes.network

import java.io.IOException

import io.lunes.utils.forceStopApplication
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import scorex.utils.ScorexLogging

import scala.util.control.NonFatal

/**
  *
  */
@Sharable
class FatalErrorHandler extends ChannelInboundHandlerAdapter with ScorexLogging {
  /**
    *
    * @param ctx
    * @param cause
    */
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = cause match {
    case ioe: IOException if ioe.getMessage == "Connection reset by peer" =>
      // https://stackoverflow.com/q/9829531
      // https://stackoverflow.com/q/1434451
      log.trace(s"${id(ctx)} Connection reset by peer")
    case NonFatal(_) =>
      log.debug(s"${id(ctx)} Exception caught", cause)
    case _ =>
      log.error(s"${id(ctx)} Fatal error in channel, terminating application", cause)
      forceStopApplication()
  }
}
