import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/** Shape of phantom's HListHelpers.prefix / unpackHListTpe: `Type.prefix` via the downcast Global, compared with `=:=`. */
object Macros {
  def hack: Int = macro impl
  def impl(c: blackbox.Context): c.Expr[Int] = {
    import c.universe._
    def prefix(tpe: Type): Type = {
      val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
      val gTpe = tpe.asInstanceOf[global.Type]
      gTpe.prefix.asInstanceOf[Type]
    }
    val cons = typeOf[scala.collection.immutable.::[Int]]
    val pre = prefix(cons)
    c.info(c.enclosingPosition, s"prefix=$pre same=${pre =:= prefix(typeOf[scala.collection.immutable.::[String]])}", force = true)
    c.Expr[Int](Literal(Constant(1)))
  }
}
