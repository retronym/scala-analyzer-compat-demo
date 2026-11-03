import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.interactive.{InteractiveAnalyzer, Global => IGlobal}
import scala.tools.nsc.reporters.Reporter

/** Ammonite's Pressy shape: the presentation compiler with a custom InteractiveAnalyzer. */
object Client {
  def mkGlobal(s: Settings, r: Reporter): Global =
    new IGlobal(s, r, "demo") { g =>
      override lazy val analyzer = new { val global: g.type = g } with InteractiveAnalyzer {}
    }
}
