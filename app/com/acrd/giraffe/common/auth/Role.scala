package com.acrd.giraffe.common.auth

case class Role(name: String)

object Role {
  val admin = Role("admin")
  val normal = Role("normal")
}


