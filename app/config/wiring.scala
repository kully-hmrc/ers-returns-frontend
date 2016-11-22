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

import play.api.Play
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.cache.client.{SessionCache, ShortLivedHttpCaching, ShortLivedCache}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.partials.CachedStaticHtmlPartialRetriever
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter
import play.Logger
import play.api.Play.current

import scala.concurrent.duration.Duration

object ERSFileValidatorAuditConnector extends AuditConnector with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

object WSHttp extends WSGet with WSPut with WSPost with WSDelete with WSPatch with AppName with RunMode with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector
}

object WSHttpWithCustomTimeOut extends WSHttp with AppName with RunMode  with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override val auditConnector = ERSFileValidatorAuditConnector

  val ersTimeOut =  (Play.configuration.getInt("ers-timeout-seconds").getOrElse(20)) * 1000

  override def buildRequest[A](url: String)(implicit hc: HeaderCarrier) = {
    super.buildRequest[A](url).withRequestTimeout(Duration(ersTimeOut.toString))
  }
}

object ERSAuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  Logger.info("got the ServiceURL " + serviceUrl)
  lazy val http = WSHttp
}

object ERSAuditConnector extends AuditConnector with AppName with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

object ERSFileValidatorAuthConnector extends AuthConnector with ServicesConfig {
  val serviceUrl = baseUrl("auth")
  lazy val http = WSHttp
}

object FormPartialProvider extends FormPartialRetriever with SessionCookieCryptoFilterWrapper {
  override val httpGet = WSHttp
  override val crypto = encryptCookieString _
}
object CachedStaticHtmlPartialProvider extends CachedStaticHtmlPartialRetriever {
  override val httpGet = WSHttp
}

object ERSHeaderCarrierForPartialsConverter extends   HeaderCarrierForPartialsConverter with SessionCookieCryptoFilterWrapper {
  override val crypto = encryptCookieString _
}
trait SessionCookieCryptoFilterWrapper {

  def encryptCookieString(cookie: String) : String = {
    SessionCookieCryptoFilter.encrypt(cookie)
  }
}

object ERSFileValidatorSessionCache extends SessionCache with AppName with ServicesConfig {
  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("cachable.session-cache")
  override lazy val domain = getConfString("cachable.session-cache.domain", throw new Exception(s"Could not find config 'cachable.session-cache.domain'"))
}

object ShortLivedHttpCaching extends ShortLivedHttpCaching with AppName with ServicesConfig {
  override lazy val http = WSHttp
  override lazy val defaultSource = appName
  override lazy val baseUri = baseUrl("cachable.short-lived-cache")
  override lazy val domain = getConfString("cachable.short-lived-cache.domain", throw new Exception(s"Could not find config 'cachable.short-lived-cache.domain'"))
}

object ShortLivedCache extends ShortLivedCache {
  override implicit lazy val crypto = ApplicationCrypto.JsonCrypto
  override lazy val shortLiveCache = ShortLivedHttpCaching
}
