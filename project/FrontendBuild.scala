import sbt._

object FrontendBuild extends Build with MicroService {
  val appName = "ers-returns-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"
  private val frontendBootstrapVersion = "6.7.0"
  private val govukTemplateVersion = "4.0.0"
  private val playUiVersion = "4.16.0"
  private val playAuthFrontendVersion = "5.5.0"
  private val playConfigVersion = "2.1.0"
  private val metricsPlayVersion = "0.2.1"
  private val metricsGraphiteVersion = "3.0.2"
  private val domainVersion = "3.7.0"
  private val httpCachingVersion = "5.6.0"
  private val playPartialVersion = "4.6.0"
  private val jsonLoggerVersion = "2.1.1"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-partials" % playPartialVersion,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthFrontendVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "com.kenshoo" %% "metrics-play" % metricsPlayVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphiteVersion,
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
    "uk.gov.hmrc" %% "play-json-logger" % jsonLoggerVersion,
    "org.apache.pdfbox" % "pdfbox" % "1.8.11",
    "org.apache.pdfbox" % "xmpbox" % "1.8.11"

  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  private val scalatestVersion = "2.2.5"
  private val scalatestPlusPlayVersion = "1.2.0"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.8.3"

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  private val hmrcTestVersion = "1.8.0"

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
