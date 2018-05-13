package io.lunes.metrics

import java.net.URI
import java.util.concurrent.TimeUnit

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.SchedulerService
import org.influxdb.dto.Point
import org.influxdb.{InfluxDB, InfluxDBFactory}
import scorex.utils.{NTP, ScorexLogging}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

/**
  *
  */
object Metrics extends ScorexLogging {

  /**
    *
    * @param uri
    * @param db
    * @param username
    * @param password
    * @param batchActions
    * @param batchFlashDuration
    */
  case class InfluxDbSettings(uri: URI,
                              db: String,
                              username: Option[String],
                              password: Option[String],
                              batchActions: Int,
                              batchFlashDuration: FiniteDuration)

  /**
    *
    * @param enable
    * @param nodeId
    * @param influxDb
    */
  case class Settings(enable: Boolean,
                      nodeId: Int,
                      influxDb: InfluxDbSettings)

  private implicit val scheduler: SchedulerService = Scheduler.singleThread("metrics")

  private var settings: Settings = _
  private var db: Option[InfluxDB] = None

  /**
    *
    * @param config
    * @return
    */
  def start(config: Settings): Future[Boolean] = Task {
    shutdown()
    settings = config
    if (settings.enable) {
      import config.{influxDb => dbSettings}

      log.info(s"Metrics are enabled and will be sent to ${dbSettings.uri}/${dbSettings.db}")
      val x = if (dbSettings.username.nonEmpty && dbSettings.password.nonEmpty) {
        InfluxDBFactory.connect(
          dbSettings.uri.toString,
          dbSettings.username.getOrElse(""),
          dbSettings.password.getOrElse("")
        )
      } else {
        InfluxDBFactory.connect(dbSettings.uri.toString)
      }
      x.setDatabase(dbSettings.db)
      x.enableBatch(dbSettings.batchActions, dbSettings.batchFlashDuration.toSeconds.toInt, TimeUnit.SECONDS)

      try {
        val pong = x.ping()
        log.info(s"Metrics will be sent to ${dbSettings.uri}/${dbSettings.db}. Connected in ${pong.getResponseTime}ms.")
        db = Some(x)
      } catch {
        case NonFatal(e) =>
          log.warn("Can't connect to InfluxDB", e)
      }
    }

    db.nonEmpty
  }.runAsyncLogErr

  /**
    *
    */
  def shutdown(): Unit = Task {
    db.foreach(_.close())
  }.runAsyncLogErr

  /**
    *
    * @param b
    */
  def write(b: Point.Builder): Unit = {
    val time = NTP.getTimestamp()
    Task {
      db.foreach(_.write(b
        // Should be a tag, but tags are the strings now
        // https://docs.influxdata.com/influxdb/v1.3/concepts/glossary/#tag-value
        .addField("node", settings.nodeId)
        .tag("node", settings.nodeId.toString)
        .time(time, TimeUnit.MILLISECONDS)
        .build()
      ))
    }.runAsyncLogErr
  }

  /**
    *
    * @param name
    */
  def writeEvent(name: String): Unit = write(Point.measurement(name))

}
