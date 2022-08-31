package com.acrd.giraffe.common.utils

import scala.collection.mutable.ArrayBuffer

class StopWatch {

  private var startAt: Long = 0L
  private var endAt: Long = 0L
  private var lastLap: Long = 0L
  private var pauseAt: Long = 0L
  private var pauseTime: Long = 0L
  private val lapRecord: ArrayBuffer[Long] = ArrayBuffer.empty

  def start = {
    if (!isStarted){
      startAt = now
      lastLap = startAt
    }
  }
  def stop: Long = {
    if (isStarted) {
      endAt = now
      lastLap = endAt
    }
    endAt - startAt - pauseTime
  }

  def pause = {
    pauseTime = 0L
    pauseAt = now
  }

  def resume = {
    if (isPaused && pauseTime == 0L) {
      pauseTime += (now - pauseAt)
      pauseAt = 0L
    }
  }

  def lap: Long = {
    var lapTime = 0L
    if (isStarted && !isPaused){
      lapTime = now - lastLap - pauseTime
      lastLap += (lapTime + pauseTime)
      lapRecord += lapTime
    }
    lapTime
  }
  def reset = {
    startAt = 0L
    endAt = 0L
    lastLap = 0L
    pauseAt = 0L
    pauseTime = 0L
    lapRecord.clear
  }

  def isStarted = startAt > 0
  def isPaused = pauseAt > 0

  def max = lapRecord.max
  def min = lapRecord.min

  private def now = System.currentTimeMillis

}