import im.mange.shoreditch.Shoreditch
import im.mange.shoreditch.api.liftweb.{ServiceHelper, SimpleRequest}
import org.scalatest.{MustMatchers, WordSpec}
import im.mange.shoreditch.api.Check
import im.mange.shoreditch.api.Action
import im.mange.shoreditch.api.In
import net.liftweb.json.JsonAST.JNothing

import scala.collection.concurrent.TrieMap

//TODO: remove BoxedLiftResponse
//TODO: remove Req
//TODO: repackage and cut lift dependency on that package ......

class MetaDataSpec extends WordSpec with MustMatchers {

  "captures checks and actions" in {
    Example.checks.size mustEqual 2
    Example.checks.head mustEqual "base/check/successful/check" -> SuccessfulCheck

    Example.actions mustEqual TrieMap(
      "base/action/successful/action" -> SuccessfulAction,
      "base/action/successful/action/with/return" -> SuccessfulActionWithReturn
    )
  }

  "handles missing requests" in {
    Example.handler(SimpleRequest("", Seq(""))) mustEqual None
  }

  "handles check requests" in {
    val maybeFunction = Example.handler(SimpleRequest("", Seq("base", "check", "successful", "check")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual """{"failures":[]}"""
  }

  "handles action requests" in {
    val maybeFunction = Example.handler(SimpleRequest("", Seq("base", "action", "successful", "action")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual """{"failures":[]}"""
  }

  "handles metadata requests" in {
    val maybeFunction = Example.handler(SimpleRequest("", Seq("base", "metadata")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual
"""{"name":"Example System","alias":"example","version":"10001","checks":[{"url":"base/check/successful/check"},{"url":"base/check/successful/check/with/arg"}],"actions":[{"url":"base/action/successful/action","in":[]},{"url":"base/action/successful/action/with/return","in":[]}]}"""
  }

  "handles check requests with args" in {
    val maybeFunction = Example.handler(SimpleRequest("", Seq("base", "check", "successful", "check", "with", "args", "arg")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual """{"failures":[]}"""
  }

  //TIP: this is a bug .. it seems to run a check, maybe the first it finds?
  //TODO: making / be the same as /metadata might help ...
  "handles index requests" in {
    val maybeFunction = Example.handler(SimpleRequest("", Seq("base")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual """{"failures":[]}"""
  }

  //TODO: handles check requests with params
  //TODO: handles action requests with params
}

import ServiceHelper._

object Example extends ServiceHelper(
  Shoreditch(
    base = "base",
    version = "10001",
    longName = "Example System",
    alias = "example",
    routes = Seq(
      "successful/check/" check SuccessfulCheck,
      "successful/check/with/arg" check SuccessfulCheckWithArg,
      "successful/action/" action SuccessfulAction,
      "successful/action/with/return" action SuccessfulActionWithReturn
    )
  )
)

case object SuccessfulCheck extends Check {
  override def run = success
}

case class SuccessfulCheckWithArg(arg: String) extends Check {
  override def run = success
}

case object SuccessfulAction extends Action {
  override def run(in: List[In]) = success(None)
}

case object SuccessfulActionWithReturn extends Action {
  override def run(in: List[In]) = success(Some("returnValue"))
}

//TODO: add failure cases ...