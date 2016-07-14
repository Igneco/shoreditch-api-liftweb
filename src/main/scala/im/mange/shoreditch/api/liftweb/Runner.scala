package im.mange.shoreditch.api.liftweb

import net.liftweb.http._
import im.mange.shoreditch.api.{Check, Action}

//TODO: kill println's or use a real logger (like reprobate)
//TODO: this needs to be made Lift-neutral ... removing Req and LiftResponse etc

object Runner {
  //TODO: this needs the post'ed input
  def run(a: Action, req: Request) = {
    //TODO: we should definitely validate the json in our regular way here ...
    //TODO: we should definitely fold or map the json
    val r = try {
      val in = Json.deserialiseIn(req.json)
      a.run(in)
    }
    catch { case e: Throwable ⇒ a.failure(List(e.getMessage)) }
    r
  }

  def run(c: Check) = {
    val r = try { c.run }
    catch { case e: Throwable ⇒ c.failure(List(e.getMessage)) }
    r
  }

}
