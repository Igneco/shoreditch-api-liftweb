package im.mange.shoreditch

import im.mange.shoreditch.api.{Request, _}
import im.mange.shoreditch.api.liftweb.Route
import im.mange.shoreditch.handler.HttpMethodPartialFunctions._
import im.mange.shoreditch.handler.ShoreditchHandler

//TODO: should be no api.liftweb deps in here
case class Shoreditch(base: String,
                               version: String,
                               longName: String,
                               alias: String,
                               checksEnabled: Boolean = true,
                               actionsEnabled: Boolean = true,
                               routes: Seq[Route[Service]]) {

  private val handler = new ShoreditchHandler(this)

  def handle(request: Request) = handler.handler(request).map(_())

  val actions = handler.actions
  val checks = handler.checks
}

object Shoreditch {
  implicit class CheckRouteBuildingString(val path: String) extends AnyVal {
    def action(f:                ⇒ Action): Route[Service] = POST0("action/" + path)(f)
    def check(f:                 ⇒ Check): Route[Service]  = GET0("check/" + path)(f)
    def check(f: (String)        ⇒ Check): Route[Service]  = GET1("check/" + path)(f)
    def check(f: (String,String) ⇒ Check): Route[Service]  = GET2("check/" + path)(f)
  }
}
