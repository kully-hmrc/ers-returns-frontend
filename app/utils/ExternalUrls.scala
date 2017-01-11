/*
 * Copyright 2017 HM Revenue & Customs
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

package utils

import controllers.routes
import play.api.Play
import uk.gov.hmrc.play.config.RunMode
import play.api.Play.current

object ExternalUrls extends RunMode {

  val companyAuthHost = s"${Play.configuration.getString(s"$env.microservice.services.auth.company-auth.host").getOrElse("")}"
  val loginCallback = Play.configuration.getString(s"$env.microservice.services.auth.login-callback.url").getOrElse(routes.ReturnServiceController.hmacCheck().url)
  val loginPath = s"${Play.configuration.getString(s"$env.microservice.services.auth.login_path").getOrElse("sign-in")}"
  
  
  val signIn = s"$companyAuthHost/gg/$loginPath" //?continue=$loginCallback"
  val ytaUrl = s"${Play.configuration.getString(s"govuk-tax.$env.yta.url").getOrElse("/gg")}"
  val signOut = s"$companyAuthHost/gg/sign-out"
  val ersServiceUrl = s"${Play.configuration.getString(s"govuk-tax.$env.services.ers.url").getOrElse("")}"
  val hmacToken = s"${Play.configuration.getString(s"hmac.hmac_token").getOrElse("")}"
  val hmacOnSwitch = s"${Play.configuration.getString(s"hmac.hmac_switch").getOrElse("true")}"
  val portalDomain = s"${Play.configuration.getString(s"portal.domain").getOrElse("")}"
  val fileTransferSwitch = s"${Play.configuration.getString(s"filetransfer.toggle").getOrElse("true")}"
  
  val betaFeedbackUrl = s"/contact/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"/contact/beta-feedback-unauthenticated"
 
}
