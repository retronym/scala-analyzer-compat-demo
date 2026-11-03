import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.Reporter

/** The Ammonite fix (retronym/Ammonite#1): keep the Global's own analyzer, steer macros via findMacroClassLoader. */
object Client {
  def mkGlobal(s: Settings, r: Reporter): Global =
    new Global(s, r) {
      override def findMacroClassLoader(): ClassLoader = getClass.getClassLoader
    }
}
