package im.mange.shoreditch.api

object Runner {
  import net.liftweb.json._

  def run(a: Action, req: Request) =
    try { a.run(Json.deserialiseIn(parse(req.json))) }
    catch { case e: Throwable ⇒ a.failure(List(e.getMessage)) }

  def run(c: Check) =
    try { c.run }
    catch { case e: Throwable ⇒ c.failure(List(e.getMessage)) }
}
