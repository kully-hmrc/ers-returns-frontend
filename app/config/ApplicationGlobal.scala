/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import java.io.File

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.Mode._
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.{Application, Configuration, Play}
import play.twirl.api.Html
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.play.audit.filters.FrontendAuditFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode}
import uk.gov.hmrc.play.frontend.bootstrap.DefaultFrontendGlobal
import uk.gov.hmrc.play.http.logging.filters.FrontendLoggingFilter

object ApplicationGlobal extends DefaultFrontendGlobal with RunMode {

  override val auditConnector = ERSFileValidatorAuditConnector
  override val loggingFilter = ERSFileValidatorLoggingFilter
  override val frontendAuditFilter = ERSFileValidatorAuditFilter

  override def onStart(app: Application) {
    super.onStart(app)
    ApplicationCrypto.verifyConfiguration()
  }

  override def onLoadConfig(config: Configuration, path: File, classLoader: ClassLoader, mode: Mode): Configuration = {
    super.onLoadConfig(config, path, classLoader, mode)
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_], messages: Messages): Html =
    views.html.global_error(pageTitle, heading, message)

  override def microserviceMetricsConfig(implicit app: Application): Option[Configuration] = app.configuration.getConfig(s"$env.microservice.metrics")

}

object ControllerConfiguration extends ControllerConfig {
  lazy val controllerConfigs = Play.current.configuration.underlying.as[Config]("controllers")
}

object ERSFileValidatorLoggingFilter extends FrontendLoggingFilter {
  override def controllerNeedsLogging(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsLogging
}

object ERSFileValidatorAuditFilter extends FrontendAuditFilter with RunMode with AppName {

  override lazy val maskedFormFields = Seq.empty[String]

  override lazy val applicationPort = None

  override lazy val auditConnector = ERSFileValidatorAuditConnector

  override def controllerNeedsAuditing(controllerName: String): Boolean = ControllerConfiguration.paramsForController(controllerName).needsAuditing
}
