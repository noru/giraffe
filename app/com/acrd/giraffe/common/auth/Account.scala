package com.acrd.giraffe.common.auth

trait AccountRepository {

  def roles(account: Account) : Set[Role]

  def findById(id: String): Option[Account]

}

// TODO, this is a stub!
object StubAccountRepo extends AccountRepository{
  override def roles(account: Account): Set[Role] = Set()

  override def findById(id: String): Option[Account] = {
    Option {
      Account("138602", "Anderson Xiu", "anderson.xiu@autodesk.com")
    }

  }
}

case class Account(id: String, name: String, email:String)