package com.acrd.giraffe.common.utils

import java.util.concurrent.TimeUnit
import play.api.libs.concurrent.Akka
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent._
import scala.util.Try
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object TimeoutPromise {
  def apply[T](message: => T, duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) = new TimeoutPromise(message, duration, unit)
  def apply[T](message: => T, duration: Duration): TimeoutPromise[T] = apply(message, duration.toMillis)
}

class TimeoutPromise[T] (message: => T, duration: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) extends Promise[T] {

  val promise = Promise[T]()
  import play.api.Play.current
  val cancellable = Akka.system.scheduler.scheduleOnce(FiniteDuration(duration, unit)) {
    promise.complete(Try(message))
  }

  /** Make this promise "cancelable"
    */
  def cancel = {
    cancellable.cancel()
    promise.tryComplete(Try(message))
  }

  /** obligated delegation
    */
  def future: Future[T] = promise.future
  def tryComplete(result: Try[T]) = promise.tryComplete(result)
  def isCompleted = promise.isCompleted

}