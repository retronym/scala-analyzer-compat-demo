import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Shape of cvogt/scala-extensions EnumerateSingletons: a deferred ref-check TypeTree built from the downcast Global. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    import c.universe._
    val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val tt = new global.TypeTreeWithDeferredRefCheck(global.TypeTree(global.NoType))(() => global.TypeTree(global.NoType)).asInstanceOf[TypTree]
    val name = TypeName(c.freshName("Verify"))
    c.info(c.enclosingPosition, "built " + tt.getClass.getSimpleName, force = true)
    c.Expr[Int](q"{ type $name = $tt; 1 }")
  }
}
