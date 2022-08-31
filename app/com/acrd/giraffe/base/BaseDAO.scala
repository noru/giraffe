package com.acrd.giraffe.base

import com.acrd.giraffe.common.Logging
import com.acrd.giraffe.common.implicits.Timestamps
import com.acrd.giraffe.common.utils.{StopWatch, IdGenerator}
import slick.dbio.DBIO
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext => EC, Await, Awaitable, Future}

abstract class BaseDAO(context: BaseContext) extends Logging with DAOUtils{

  val dbConfig = context.dbConfig
  val db = context.db
  val tables = context.tables
  import tables.profile.api._

  def createSchema(implicit ec: EC): Future[Unit] =
      throw new NotImplementedError("This table cannot be created alone, use createAllSchema instead")

  def createAllSchema(implicit ec: EC) = db.run(tables.schema.create)


}

trait DAOUtils {

  val DummyDBIO = FakeDBIO(())
  def FakeDBIO[T](a: T = ()) = if (a != null && a.isInstanceOf[Throwable]) DBIO.failed(a.asInstanceOf[Throwable])
                                   else DBIO.successful(a)

  import com.acrd.giraffe.common.Consts.QueryLimit
  def topLimit(top: Int = 0): Int = if (top <= 0 || top > QueryLimit) QueryLimit else top

  def getUID = IdGenerator.getUID

  def now = Timestamps.now

  def performanceTest(cases: Seq[() => Awaitable[_]], repeat: Int = 10, interval: Int = 0)(implicit ec: EC) = {

    val timer = new StopWatch

    def runner(caseToRun: () => Awaitable[_]): (Long, Double, Long, Long) = {

      timer.reset
      timer.start
      (1 to repeat).map( _ =>{
        Await.result(caseToRun(), Duration.Inf)
        timer.pause
        Thread.sleep(interval)
        timer.resume
        timer.lap
      })
      val total = timer.stop
      (total, total / repeat, timer.max, timer.min)
    }

    cases.zipWithIndex.map{ case (c, i) => (i, runner(c)) }.map{
      case (i, (t, a, max, min)) => s"Case $i: $a/$max/$min/$t(average/max/min/total)}"
    }

  }

}