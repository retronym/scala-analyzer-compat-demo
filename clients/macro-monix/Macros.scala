import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Shape of monix's InlineMacros.stripUnApplyNodes: a Transformer using `global.nme` of the downcast Global. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    import c.universe._
    val t = new Transformer {
      val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
      import global.nme
      override def transform(tree: Tree): Tree = tree match {
        case Select(q, nme.unapply | nme.unapplySeq) => transform(q)
        case t => super.transform(t)
      }
    }
    t.transform(q"x.unapply"); 
    c.info(c.enclosingPosition, "transformer ran", force = true)
    c.Expr[Int](Literal(Constant(1)))
  }
}
