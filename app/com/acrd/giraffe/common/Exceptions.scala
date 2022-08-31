package com.acrd.giraffe.common

import com.acrd.giraffe.base.BaseException
import com.acrd.giraffe.models.AppStoreTables.Platform.Platform
import com.wix.accord.Violation
import play.api.i18n.Lang

object exceptions {

  type BE = BaseException
  type CVE = ConstraintViolationException

  class CheckedException(msg: String = "") extends BE(msg)

  class UncheckedException(msg: String = "") extends BE(msg)

  final class TimeoutException(timeout: Any) extends UncheckedException(s"Request timeout in $timeout milliseconds")

  final class NotImplementedException(msg: String = "") extends CheckedException(msg)

  class ConstraintViolationException(msg: String = "Constrain violated") extends CheckedException(msg)

  class IdAlreadyExistsException(id: Any, content: String = "Content") extends CVE(s"$content with id '$id' already exists")

  class IdNotExistsException(id: Any, content: String = "Content") extends CVE(s"$content with id '$id' does not exist")

  class DuplicateEntryException(id: Any, content: String = "Content") extends CVE(s"$content with id '$id' already exists")

  class PayloadIdNotExistsException(id: Long, lang: Lang, os: Platform)
      extends IdNotExistsException(s"$id, $lang, $os", "Payload")

  class InvalidParameterException(msg: String) extends CVE(s"Invalid Parameter: $msg")

  class InvalidEnumException(name: String, valid: Seq[_])
      extends CVE(s"'$name' is an invalid enum, valid options are: ${valid.mkString(", ")}")

  class InvalidFormatException(msg: String = "") extends CVE("Invalid format. " + msg)

  class ValidationFailedException(e: Set[Violation]) extends CVE("Validation Failed: " + e.map(violation => violation.description.getOrElse("Unknown") + " " + violation.constraint).mkString(", "))

}

