package im.mange.shoreditch

import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._

//TODO: should be no api.liftweb deps in here
case class Shoreditch[Service](base: String,
                               version: String,
                               longName: String,
                               alias: String,
                               checksEnabled: Boolean = true,
                               actionsEnabled: Boolean = true,
                               routes: Seq[Route[Service]])
