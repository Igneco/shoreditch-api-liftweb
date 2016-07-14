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
    Example.handler(SimpleRequest(JNothing, Seq(""))) mustEqual None
  }

  "handles check requests" in {
    val maybeFunction = Example.handler(SimpleRequest(JNothing, Seq("base", "check", "successful", "check")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual None
  }

  "handles action requests" in {
    val maybeFunction = Example.handler(SimpleRequest(JNothing, Seq("base", "action", "successful", "action")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual None
  }

  "handles metadata requests" in {
    val maybeFunction = Example.handler(SimpleRequest(JNothing, Seq("base", "metadata")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual None
  }

  "handles check requests with args" in {
    val maybeFunction = Example.handler(SimpleRequest(JNothing, Seq("base", "check", "successful", "check", "with", "args", "arg")))
    maybeFunction.isDefined mustEqual true
    maybeFunction.get() mustEqual None
  }

  //TODO: handles check requests with params
  //TODO: handles action requests with params
}

import ServiceHelper._

//TODO: ultimately rename me ...
object Example extends ServiceHelper(
  base = "base",
  version = "10001",
  checksEnabled = true,
  actionsEnabled = true,
  longName = "Example System",
  alias = "example"
)(
    "successful/check/" check SuccessfulCheck,
    "successful/check/with/arg" check SuccessfulCheckWithArg,
    "successful/action/" action SuccessfulAction,
    "successful/action/with/return" action SuccessfulActionWithReturn
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