package scala.tools.partest.scalajs

class ScalaJSPartestOptions private (
  val testFilter: ScalaJSPartestOptions.TestFilter,
  val optimize: Boolean
)

object ScalaJSPartestOptions {

  sealed abstract class TestFilter
  case object UnknownTests extends TestFilter
  case object BlacklistedTests extends TestFilter
  case object WhitelistedTests extends TestFilter
  case object BuglistedTests extends TestFilter
  case class SomeTests(names: List[String]) extends TestFilter {
    override def toString() =
      names.map(x => s""""$x"""").mkString("[", ", ", "]")
  }

  def apply(args: Array[String],
      errorReporter: String => Unit): Option[ScalaJSPartestOptions] = {

    var failed = false

    var filter: Option[TestFilter] = None
    var optimize = false

    def error(msg: String) = {
      failed = true
      errorReporter(msg)
    }

    def setFilter(newFilter: TestFilter) = (filter, newFilter) match {
      case (Some(SomeTests(oldNames)), SomeTests(newNames)) =>
        // Merge test names
        filter = Some(SomeTests(oldNames ++ newNames))
      case (Some(fil), newFilter) =>
        error(s"You cannot specify twice what tests to use (already specified: $fil, new: $newFilter)")
      case (None, newFilter) =>
        filter = Some(newFilter)
    }

    for (arg <- args) arg match {
      case "--fastOpt" =>
        optimize = true
      case "--noOpt" =>
        optimize = false
      case "--blacklisted" =>
        setFilter(BlacklistedTests)
      case "--buglisted" =>
        setFilter(BuglistedTests)
      case "--whitelisted" =>
        setFilter(WhitelistedTests)
      case "--unknown" =>
        setFilter(UnknownTests)
      case _ =>
        setFilter(SomeTests(arg :: Nil))
    }

    if (failed) None
    else Some {
      new ScalaJSPartestOptions(filter.getOrElse(WhitelistedTests), optimize)
    }
  }

}
