package demo

import scala.tools.nsc.typechecker.Analyzer

/** For compilers without AbstractAnalyzer (2.13.18 and earlier). */
abstract class AnalyzerBase extends Analyzer
