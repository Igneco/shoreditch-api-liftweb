package im.mange.shoreditch.api.liftweb

import net.liftweb.http.Req

protected object Any {
  def unapply(r: Req): Option[(List[String], Req)] = Some(r.path.partPath -> r)
}
