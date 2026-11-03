import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.typechecker.{AbstractAnalyzer, Analyzer, MacroAnnotationNamers}

/** The shape scala/scala#11306 asks instantiators to move to. Does not exist on 2.13.18. */
object Client {
  def analyzer(g: Global): Analyzer { val global: g.type } =
    if (g.settings.YmacroAnnotations.value)
      new { val global: g.type = g } with AbstractAnalyzer with MacroAnnotationNamers {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }
    else
      new { val global: g.type = g } with AbstractAnalyzer {
        override def defaultMacroClassloader = global.findMacroClassLoader
      }

  def mkGlobal(s: Settings, r: Reporter): Global =
    new Global(s, r) { g =>
      override lazy val analyzer = Client.analyzer(g)
    }
}
