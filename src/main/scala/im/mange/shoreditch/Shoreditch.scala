package im.mange.shoreditch

import scala.collection.concurrent
import im.mange.shoreditch.api.{Action, Check}
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._

//TODO: should be no api.liftwen deps in here

case class Shoreditch[Service](base: String, routes: Seq[Route[Service]]) {
  var actions = concurrent.TrieMap[String, Action]()
  var checks = concurrent.TrieMap[String, Check]()

  //TODO: should be foreach
  routes.map(r =>
    r.service match {
      case a:Action => actions.update(base + "/" + r.pathStr, a)
      case c:Check => checks.update(base + "/" + r.pathStr, c)
      case x => //???
    })
}
