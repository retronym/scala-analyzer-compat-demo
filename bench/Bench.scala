import demo.AnalyzerBase
import scala.reflect.internal.util.BatchSourceFile
import scala.reflect.io.VirtualDirectory
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.typechecker.Analyzer

/** Synthetic version of the PR's benchmark: compile a generated corpus repeatedly, each iteration with one of K
 *  Global instances, each of which has an analyzer of its own class. With K > 2 the typechecker's calls to its
 *  analyzer see more receiver classes than HotSpot profiles, which is where invokeinterface gets slow.
 *  Prints one `iter <n> <ms>` line per iteration.
 *
 *  args: K warmup measured
 */
object Bench {
  // Each instantiation site is its own anonymous class, so these are K distinct analyzer classes.
  def a0(g: Global): Analyzer { val global: g.type } = new { val global: g.type = g } with AnalyzerBase { val tag = 0 }
  def a1(g: Global): Analyzer { val global: g.type } = new { val global: g.type = g } with AnalyzerBase { val tag = 1 }
  def a2(g: Global): Analyzer { val global: g.type } = new { val global: g.type = g } with AnalyzerBase { val tag = 2 }
  def a3(g: Global): Analyzer { val global: g.type } = new { val global: g.type = g } with AnalyzerBase { val tag = 3 }

  def mk(k: Int): Global = {
    val s = new Settings
    s.classpath.value = sys.props("java.class.path")
    s.outputDirs.setSingleOutput(new VirtualDirectory("(mem)", None))
    val r = new StoreReporter(s)
    k match {
      case 0 => new Global(s, r) { g => override lazy val analyzer = a0(g) }
      case 1 => new Global(s, r) { g => override lazy val analyzer = a1(g) }
      case 2 => new Global(s, r) { g => override lazy val analyzer = a2(g) }
      case 3 => new Global(s, r) { g => override lazy val analyzer = a3(g) }
    }
  }

  def corpus: String = (0 until 120).map { i =>
    s"""case class C$i(a: Int, b: String, c: List[Int]) {
       |  implicit val ord: Ordering[C$i] = Ordering.by(_.a)
       |  def f(x: Seq[Int]) = x.map(_ + a).filter(_ > $i).foldLeft(0)(_ + _)
       |  def g = c.zip(List.fill(3)(b)).toMap.map { case (k, v) => v + k }
       |  def h = List(this, this).sorted.map(_.f(c)).groupBy(identity).view.mapValues(_.size).toMap
       |  def m(o: Option[C$i]) = o.map(_.a).getOrElse(0) + c.sum + (if (b.isEmpty) 1 else 2)
       |}
       |""".stripMargin
  }.mkString

  def main(args: Array[String]): Unit = {
    val Array(k, warm, meas) = args.map(_.toInt)
    val globals = Array.tabulate(k)(mk)
    val src = corpus
    for (i <- 0 until warm + meas) {
      val g = globals(i % k)
      val t0 = System.nanoTime()
      val run = new g.Run
      run.compileSources(List(new BatchSourceFile("Corpus.scala", src)))
      val ms = (System.nanoTime() - t0) / 1e6
      if (g.reporter.hasErrors) { println("FAIL " + g.reporter.asInstanceOf[StoreReporter].infos.take(3).mkString("; ")); sys.exit(1) }
      if (i >= warm) println(f"iter ${i - warm} $ms%.1f")
    }
    println("classes " + globals.map(_.analyzer.getClass.getName).distinct.size + " analyzers")
  }
}
