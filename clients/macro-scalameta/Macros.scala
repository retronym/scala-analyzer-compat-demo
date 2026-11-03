import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Shape of scalameta's ConversionMacros.typeMismatchMessage: calls a method directly on `global.analyzer`. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    val g = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val found = c.typeOf[String]; val req = c.typeOf[Int]
    val msg = g.analyzer.foundReqMsg(found.asInstanceOf[g.Type], req.asInstanceOf[g.Type])
    c.info(c.enclosingPosition, "foundReqMsg: " + msg.trim.replace("\n", " "), force = true)
    c.Expr[Int](c.universe.Literal(c.universe.Constant(1)))
  }
}
