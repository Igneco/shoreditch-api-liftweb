package im.mange.shoreditch.example

import im.mange.shoreditch.api.Request

case class SimpleRequest(inboundPathParts: Seq[String], json: String = "") extends Request