import demo.AnalyzerBase
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.typechecker.{Analyzer, MacroAnnotationNamers}

/** One source for every compiler: the instantiation names the shim class, only the shim differs per version. */
object Client {
  def analyzer(g: Global): Analyzer { val global: g.type } =
    if (g.settings.YmacroAnnotations.value)
      new { val global: g.type = g } with AnalyzerBase with MacroAnnotationNamers {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }
    else
      new { val global: g.type = g } with AnalyzerBase {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }

  def mkGlobal(s: Settings, r: Reporter): Global =
    new Global(s, r) { g =>
      override lazy val analyzer = Client.analyzer(g)
    }
}
