import im.mange.shoreditch.api.liftweb.ServiceHelper
import org.scalatest.{MustMatchers, WordSpec}
import im.mange.shoreditch.api.Action
import im.mange.shoreditch.api.In

import scala.collection.concurrent.TrieMap

class MetaDataSpec extends WordSpec with MustMatchers {

  "simple" in {
    Booking.checks mustEqual TrieMap.empty
    Booking.actions mustEqual TrieMap("booking/action/make/payment" -> MakePayment)
  }

}

import ServiceHelper._

//http://localhost:4253/booking/metadata
object Booking extends ServiceHelper(
  base = "booking",
  version = "10001",
  checksEnabled = true,
  actionsEnabled = true,
  longName = "Booking System",
  alias = "booking"
)(
    "make/payment/" action MakePayment
  )

case object MakePayment extends Action {
  override def run(in: List[In]) = success(None)
}