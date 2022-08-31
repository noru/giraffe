package com.acrd.giraffe.common.utils

import java.util.{UUID, Date}
import java.util.concurrent.atomic.AtomicLong
import scala.math.abs

object IdGenerator {

  private lazy val atomicLong: AtomicLong = new AtomicLong(new Date().getTime)

  /**
   * Mimic of uuid_short() in mysql
   * A long type, thread-safe ID generator using AtomicLong and a start-up time as com.acrd.giraffe.base timestamp
   * The generated id is less strict than UUID(also shorter and more index-friendly), but sufficient for most cases
   * @return id
   */
  def getIncrementUID = {
    atomicLong.getAndIncrement()
  }

  /**
   * Cut the least significant bits from a UUID and get its absolute value
   * Has higher chance of collision (2^61 to 2^29) than UUID, but sufficient form most cases
   * @return id
   */
  def getUID = {
    abs(UUID.randomUUID().getMostSignificantBits)
  }


}
