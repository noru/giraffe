package com.acrd.giraffe.common

import play.api.Logger

trait Logging {

  val logger = Logger(this.getClass)

  def debug(message: => String) = logger.debug(message)
  def debug(message: => String, error: => Throwable) = logger.debug(message, error)

  def info(message: => String) = logger.info(message)
  def info(message: => String, error: => Throwable) = logger.info(message, error)

  def warn(message: => String) = logger.warn(message)
  def warn(message: => String, error: => Throwable) = logger.warn(message, error)

  def error(message: => String) = logger.error(message)
  def error(message: => String, error: => Throwable) = logger.error(message, error)

  def trace(message: => String) = logger.trace(message)
  def trace(message: => String, error: => Throwable) = logger.trace(message, error)


}
