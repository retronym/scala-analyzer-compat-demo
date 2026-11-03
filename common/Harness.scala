import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.Settings
import scala.tools.nsc.reporters.StoreReporter

/** Runs one in-memory compilation with the Global that `Client.mkGlobal` builds, and prints a one-line verdict. */
object Main {
  def main(args: Array[String]): Unit = {
    val s = new Settings
    s.classpath.value = sys.props("java.class.path")
    s.outputDirs.setSingleOutput(new VirtualDirectory("(mem)", None))
    val rep = new StoreReporter(s)
    val g = Client.mkGlobal(s, rep)
    def compile(): Unit = {
      val run = new g.Run
      run.compileSources(List(new BatchSourceFile("T.scala", sys.props.getOrElse("demo.source", "object T { def f: Int = 1 + 1; class C { def g = f } }"))))
    }
    g match {
      case ig: scala.tools.nsc.interactive.Global => ig.ask(() => compile()) // must run on the presentation compiler thread
      case _ => compile()
    }
    if (rep.hasErrors) println("FAIL " + rep.infos.mkString("; "))
    else println("ok analyzer=" + g.analyzer.getClass.getName + rep.infos.map(i => " [" + i.msg + "]").mkString)
  }
}
