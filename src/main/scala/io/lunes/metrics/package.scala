package io.lunes

import kamon.metric.Histogram

/**
  *
  */
package object metrics {

  /**
    *
    * @param h
    */
  final implicit class HistogramExt(val h: Histogram) extends AnyVal {
    def safeRecord(value: Long): Unit = h.record(Math.max(value, 0))
  }
}
