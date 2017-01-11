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

package connectors

import java.net.URLEncoder

import config.{ApplicationConfig, WSHttp}
import controllers.routes
import play.api.Logger
import play.api.mvc.Request
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.partials.HeaderCarrierForPartials

import scala.concurrent.Future

trait UploadConfig extends ServicesConfig {

  def apply()(implicit request: Request[_],  hcfp: HeaderCarrierForPartials): String = {
    lazy val url = s"${baseUrl("attachments")}/attachments-internal/uploader"
    val onSuccess = ApplicationConfig.successPageUrl
    val onFailure = ApplicationConfig.failurePageUrl
    val callback = ApplicationConfig.callbackPageUrl

    s"$url?" +
      s"callbackUrl=${encode(callback)}" +
      s"&onSuccess=${encode(onSuccess)}" +
      s"&onFailure=${encode(onFailure)}" +
      s"&accepts=${encode(".ods")}" +
      s"&collection=${encode(ApplicationConfig.uploadCollection)}" +
      s"&decompressedTargetFile=${encode("content.xml")}"

  }

  private def encode(url: String) = URLEncoder.encode(url, "UTF-8")

}

object UploadConfig extends UploadConfig

trait UploadCsvConfig extends ServicesConfig {

  def apply()(implicit request: Request[_],  hcfp: HeaderCarrierForPartials): String = {
    lazy val url = s"${baseUrl("attachments")}/attachments-internal/uploader"
    val onSuccess = ApplicationConfig.successCsvPageUrl
    val onFailure = ApplicationConfig.failureCsvPageUrl
    val callback = ApplicationConfig.callbackCsvPageUrl

    s"$url?" +
      s"callbackUrl=${encode(callback)}" +
      s"&onSuccess=${encode(onSuccess)}" +
      s"&onFailure=${encode(onFailure)}" +
      s"&accepts=${encode(".csv")}" +
      s"&collection=${encode(ApplicationConfig.uploadCollection)}"
  }

  private def encode(url: String) = URLEncoder.encode(url, "UTF-8")

}

object UploadCsvConfig extends UploadCsvConfig


trait AttachmentsConnector {

  import config.ERSHeaderCarrierForPartialsConverter._
  val http: HttpGet with HttpPost = WSHttp

  def getFileUploadPartial()(implicit request: Request[_], hc: HeaderCarrier): Future[HttpResponse] = {
    val uploadurl = UploadConfig()
    Logger.info("Connecting to attachments SessionId before Get /uploader --> " + hc.sessionId)
    http.GET(uploadurl)(handleResponse(uploadurl),hc)
  }

  def getCsvFileUploadPartial()(implicit request: Request[_], hc: HeaderCarrier): Future[HttpResponse] = {
    val uploadurl = UploadCsvConfig()
    Logger.info("Connecting to attachments SessionId before Get /uploader --> " + hc.sessionId)
    http.GET(uploadurl)(handleResponse(uploadurl),hc)
  }

  private def failureMessage(status: Int, url: String): String = s" returned status $status on URL: $url"

  private def handleResponse(url: String): HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    def read(method: String, url: String, response: HttpResponse) = response.status match {
      case status if status >= 400 && status < 500 => Logger.error("Error while getting partial " + failureMessage(status, url)); throw new Upstream4xxResponse(failureMessage(status, url), status, 500)
      case status if status >= 500 && status < 600 => Logger.error("Error while getting partial " + failureMessage(status, url)); throw new Upstream5xxResponse(failureMessage(status, url), status, 502)
      case _ => response
    }
  }

}

object AttachmentsConnector extends AttachmentsConnector
