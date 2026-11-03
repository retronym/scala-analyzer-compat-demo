import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.typechecker.{Analyzer, MacroAnnotationNamers}

/** Ammonite 3.0.9's CompilerCompatibility.analyzer, verbatim in shape. */
object Client {
  def analyzer(g: Global): Analyzer { val global: g.type } =
    if (g.settings.YmacroAnnotations.value)
      new { val global: g.type = g } with Analyzer with MacroAnnotationNamers {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }
    else
      new { val global: g.type = g } with Analyzer {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }

  def mkGlobal(s: Settings, r: Reporter): Global =
    new Global(s, r) { g =>
      override lazy val analyzer = Client.analyzer(g)
    }
}
