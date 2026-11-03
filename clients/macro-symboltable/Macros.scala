import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** A naughty macro: downcasts the macro universe to scala.reflect.internal.SymbolTable, below Global, no analyzer. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    val st = c.universe.asInstanceOf[scala.reflect.internal.SymbolTable]
    c.info(c.enclosingPosition, s"macro saw ${st.definitions.IntClass.fullName} in ${st.getClass.getSuperclass.getSimpleName}", force = true)
    c.Expr[Int](c.universe.Literal(c.universe.Constant(1)))
  }
}

object Client {
  def mkGlobal(s: scala.tools.nsc.Settings, r: scala.tools.nsc.reporters.Reporter): scala.tools.nsc.Global =
    new scala.tools.nsc.Global(s, r)
}
