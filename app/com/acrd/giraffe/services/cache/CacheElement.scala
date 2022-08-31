package com.acrd.giraffe.services.cache

object CacheElement {

  val PREFIX_SINGLE_LIVE = "app-live-"
  val PREFIX_SINGLE_PREVIEW = "app-prev-"

  // Regex of incoming cache keys
  val SingleLive = "^app-live-(\\d+)".r
  val SinglePreview = "^app-prev-(\\d+)".r
  val CollectionLive = "^collection-id-live-(.+)-.*".r
  val CollectionPreview = "^collection-id-prev-(.+)-.*".r

  def getDependencies(key: String, isLive: Boolean = true): Vector[String] = {

    val prefix = if (isLive) PREFIX_SINGLE_LIVE else PREFIX_SINGLE_PREVIEW
    key.stripPrefix("(").stripSuffix(")").split(",").map( str => prefix + str ).toVector

  }

  def apply(key: String) = key match {

    case SingleLive(_) => Valid(key)
    case SinglePreview(_) => Valid(key)
    // FIXME: support hierarchy?
    //    case CollectionLive(v) => Valid(key, getDependencies(v))
    //    case CollectionPreview(v) => Valid(key, getDependencies(v, isLive = false))
    case _ => Unknown

  }
}

trait CacheElement
case object Unknown extends CacheElement
case class Valid(key: String, depends: Vector[String] = Vector.empty) extends CacheElement
