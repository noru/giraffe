package com.acrd.giraffe.modules

import com.acrd.giraffe.base.{AppContext, BaseContext, TestingContext}
import com.google.inject._
import play.api.{Configuration, Environment}

class ContextModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    if (configuration.getString("app.mode").contains("test")) {

      bind(classOf[BaseContext])
          .to(classOf[TestingContext])

    } else {
      bind(classOf[BaseContext])
          .to(classOf[AppContext])
    }

  }

}