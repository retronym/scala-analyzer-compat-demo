import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** A naughty macro: downcasts the macro universe to the compiler's Global and pokes at its analyzer. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    val g = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val ctx = g.analyzer.rootContext(g.NoCompilationUnit)
    c.info(c.enclosingPosition, s"macro saw analyzer ${g.analyzer.getClass.getName.split("[.]").last} loader=${g.analyzer.defaultMacroClassloader != null} ctx=${ctx != null}", force = true)
    c.Expr[Int](c.universe.Literal(c.universe.Constant(1)))
  }
}

object Client {
  def mkGlobal(s: scala.tools.nsc.Settings, r: scala.tools.nsc.reporters.Reporter): scala.tools.nsc.Global =
    new scala.tools.nsc.Global(s, r)
}
