package im.mange.shoreditch.api

trait Request {
  val json: String
  //TODO: ultimately this needs to be a String
  val inboundPathParts: Seq[String]
}
