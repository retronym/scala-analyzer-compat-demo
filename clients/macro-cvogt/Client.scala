object Client {
  def mkGlobal(s: scala.tools.nsc.Settings, r: scala.tools.nsc.reporters.Reporter): scala.tools.nsc.Global =
    new scala.tools.nsc.Global(s, r)
}
