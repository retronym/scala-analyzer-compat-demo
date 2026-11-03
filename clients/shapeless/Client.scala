import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter

/** Plain Global; the point is the source in source.txt, expanded by the published shapeless_2.13 macros. */
object Client {
  def mkGlobal(s: Settings, r: Reporter): Global = new Global(s, r)
}
