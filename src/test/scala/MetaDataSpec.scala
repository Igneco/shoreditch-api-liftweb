import im.mange.shoreditch.api.liftweb.{ServiceHelper, SimpleRequest}
import org.scalatest.{MustMatchers, WordSpec}
import im.mange.shoreditch.api.Check
import im.mange.shoreditch.api.Action
import im.mange.shoreditch.api.In
import net.liftweb.json.JsonAST.JNothing

import scala.collection.concurrent.TrieMap

//TODO: remove BoxedLiftResponse
//TODO: remove Req

class MetaDataSpec extends WordSpec with MustMatchers {

  "captures checks and actions" in {
    Booking.checks mustEqual TrieMap("booking/check/alive" -> Alive)
    Booking.actions mustEqual TrieMap("booking/action/make/payment" -> MakePayment)
  }

  "handles incoming requests" in {
//    Booking.handler(SimpleRequest(JNothing)) mustEqual ""
  }

}

import ServiceHelper._

object Booking extends ServiceHelper(
  base = "booking",
  version = "10001",
  checksEnabled = true,
  actionsEnabled = true,
  longName = "Booking System",
  alias = "booking"
)(
    "alive/" check Alive,
    "make/payment/" action MakePayment
  )

case object Alive extends Check {
  override def run = success
}

case object MakePayment extends Action {
  override def run(in: List[In]) = success(None)
}