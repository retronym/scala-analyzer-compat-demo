import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Shape of jrudolph/upickle-pprint Derive: callsiteTyper.asInstanceOf[global.analyzer.Typer].context.isAccessible, then treeBuild. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    import c.universe._
    val tpe = c.typeOf[Option[Int]]
    val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val typer = c.asInstanceOf[scala.reflect.macros.runtime.Context].callsiteTyper.asInstanceOf[global.analyzer.Typer]
    val acc = typer.context.isAccessible(tpe.typeSymbol.asInstanceOf[global.Symbol], tpe.asInstanceOf[global.Type].prefix)
    val symTab = c.universe.asInstanceOf[scala.reflect.internal.SymbolTable]
    val pre = tpe.asInstanceOf[symTab.Type].prefix.asInstanceOf[Type]
    val ref = c.universe.treeBuild.mkAttributedRef(pre, tpe.typeSymbol.companion)
    c.info(c.enclosingPosition, s"accessible=$acc ref=$ref", force = true)
    c.Expr[Int](Literal(Constant(1)))
  }
}
