package com.acrd.giraffe.services

import com.acrd.giraffe.common.auth._

class AccountService(accountRepo: AccountRepository) {

  def isAdmin(account: Account) : Boolean = {
    accountRepo.roles(account).contains(Role.admin)
  }

  def findById(id: String): Option[Account] = {
    accountRepo.findById(id)
  }

  def hasRole(account: Account, role: Role): Boolean = {
    accountRepo.roles(account).contains(role)
  }

}