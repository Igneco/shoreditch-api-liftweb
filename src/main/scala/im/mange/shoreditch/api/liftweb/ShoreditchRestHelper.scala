package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.Shoreditch
import net.liftweb.http.rest.RestHelper

case class ShoreditchRestHelper(shoreditch: Shoreditch) extends RestHelper {
  serve {
    case req @ (shoreditch.base :: extras) Any _ => LiftwebToShoreditchRequestAdaptor(req).handle(shoreditch)
  }
}
