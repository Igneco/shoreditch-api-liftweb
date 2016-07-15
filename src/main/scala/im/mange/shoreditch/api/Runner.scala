package im.mange.shoreditch.api

object Runner {
  import net.liftweb.json._

  //TODO: this needs the post'ed input
  def run(a: Action, req: Request) = {
    //TODO: we should definitely validate the json in our regular way here ...
    val r = try {
      val in = Json.deserialiseIn(parse(req.json))
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
