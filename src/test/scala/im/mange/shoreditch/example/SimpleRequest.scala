package im.mange.shoreditch.example

import im.mange.shoreditch.api.Request

case class SimpleRequest(json: String, inboundPathParts: Seq[String]) extends Request