package com.acrd.giraffe.test.init

import com.acrd.giraffe.common.Logging

object Initializer{
  /**
   * Static factory-like method for creating a Initializer instance and call its init() method
   * @param m implicit manifest of A
   * @tparam A type name of a specific initializer that inherits AbstractInitializer
   */
  def apply[A <: Initializer](implicit m: Manifest[A]): Unit = {
    m.runtimeClass.newInstance.asInstanceOf[A].init()
  }

}

/**
 * Abstract initializer for testing. Initialize testing data for each test case
 * Note, subclass of it must only have a constructor without parameters
 */
abstract class Initializer extends Logging{

  def injector = play.api.Play.current.injector

  def ensureSchema: Unit

  def setupData: Unit

  def init(): Unit = {
    try {
      ensureSchema
      setupData
    } catch {
      case e: Throwable => error(e.getMessage)
    }
  }

}

