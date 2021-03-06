import sbt._

object FrontendBuild extends Build with MicroService {
  val appName = "ers-returns-frontend"
  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._


  private val playPartialVersion = "6.1.0"
  private val httpCachingVersion = "7.0.0"
  private val domainVersion = "4.1.0"
  private val frontendBootstrapVersion = "8.24.0"
  private val hmrcTestVersion = "2.4.0"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"
  private val pdfboxVersion = "1.8.11"
  private val xmpboxVersion = "1.8.11"
  private val scalaParserCombinatorsVersion = "1.0.3"
  private val scalatestVersion = "2.2.5"
  private val scalatestPlusPlayVersion = "1.5.1"
  private val jsoupVersion = "1.9.2"
  private val mockitoCoreVersion = "1.9.5"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-partials" % playPartialVersion,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "org.scala-lang.modules" %% "scala-parser-combinators" % scalaParserCombinatorsVersion,
    "org.apache.pdfbox" % "pdfbox" % pdfboxVersion,
    "org.apache.pdfbox" % "xmpbox" % xmpboxVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % mockitoCoreVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val scope: String = "it"
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
