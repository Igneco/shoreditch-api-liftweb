package im.mange.shoreditch

import im.mange.shoreditch.api.{Action, Check}
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._

//TODO: should be no api.liftwen deps in here
//TODO: see if we can whack summary
//TODO: decide what is mandatory and use Options
case class Shoreditch[Service](base: String, version: String, longName: String = "", alias: String = "",
                               checksEnabled: Boolean, actionsEnabled: Boolean,
                               routes: Seq[Route[Service]])
