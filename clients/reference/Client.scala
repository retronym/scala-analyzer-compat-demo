import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter

/** Merely uses `global.analyzer`, like the sbt compiler bridge and plugins registering AnalyzerPlugins. */
object Client {
  def mkGlobal(s: Settings, r: Reporter): Global = {
    val g = new Global(s, r)
    import g._
    analyzer.addAnalyzerPlugin(new analyzer.AnalyzerPlugin {
      override def pluginsTyped(tpe: Type, typer: analyzer.Typer, tree: Tree, mode: scala.reflect.internal.Mode, pt: Type): Type = tpe
    })
    g
  }
}
