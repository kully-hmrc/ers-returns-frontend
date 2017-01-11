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

import java.util.concurrent.TimeUnit

import metrics.Metrics
import models._
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import config.{WSHttp,WSHttpWithCustomTimeOut}
import uk.gov.hmrc.domain.EmpRef
import play.api.mvc.Request
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object ErsConnector extends ErsConnector with ServicesConfig {
  override def ersUrl = baseUrl("ers-returns")

  override def ersRegime = config("ers-returns").getString("regime").get
  override def validatorUrl = baseUrl("ers-file-validator")
  override def httpPost: HttpPost = WSHttpWithCustomTimeOut
  override def httpGet: HttpGet = WSHttp
}

trait ErsConnector {

  val metrics:Metrics = Metrics
  def ersUrl: String

  def ersRegime: String
  def validatorUrl:String
  def httpPost: HttpPost
  def httpGet: HttpGet

  def connectToEtmpSapRequest(schemeRef: String)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[String] = {
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/sapRequest/" + schemeRef
    val startTime = System.currentTimeMillis()
    httpGet.GET[HttpResponse](url).map { response =>
      response.status match {
        case 200 =>
          val sapNumber: String = (response.json \ "SAP Number").as[String]
          sapNumber
        case _ =>
          Logger.error(s"SAP request failed with status ${response.status}, timestamp: ${System.currentTimeMillis()}.")
          throw new Exception
      }
    }.recover {
      case e: Exception =>
        Logger.error(s"connectToEtmpSapRequest failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        metrics.ErsConnector(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
        throw new Exception
    }
  }

  def connectToEtmpSummarySubmit(sap: String, payload: JsValue)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[String] = {
    val empRef: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empRef}/summarySubmit/" + sap
    httpPost.POST(url, payload).map { res =>
      res.status match {
        case 200 =>
          val bundleRef: String = (res.json \ "Form Bundle Number").as[String]
          bundleRef
        case _ =>
          Logger.error(s"Summary submit request failed with status ${res.status}, timestamp: ${System.currentTimeMillis()}.")
          throw new Exception
      }
    }
  }

   def getAuthID(implicit authContext : AuthContext) = {
    authContext.principal.accounts.epaye match {
      case Some(account) => account.empRef
      case None => EmpRef("","")
    }
  }

  def submitReturnToBackend(allData: ErsSummary)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[HttpResponse] = {
    import ErsModelFormats._
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/saveReturnData"
    httpPost.POST(url, allData)
  }


  def validateFileData(callbackData: CallbackData, schemeInfo:SchemeInfo)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[HttpResponse] = {
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${validatorUrl}/ers/${empref}/process-file"
    val startTime = System.currentTimeMillis()
    Logger.info("validateFileData: Call to Validator: " + (System.currentTimeMillis() / 1000))
    httpPost.POST(url, ValidatorData(callbackData, schemeInfo)).map { res =>
      metrics.ErsConnector(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
      res
    }.recover {
      case e: Exception => {
        Logger.error(s"validateFileData: Validate file data failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        metrics.ErsConnector(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
        HttpResponse(400)
      }
    }
  }

  def validateCsvFileData(callbackData: List[CallbackData], schemeInfo: SchemeInfo)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[HttpResponse] = {
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${validatorUrl}/ers/${empref}/process-csv-file"
    val startTime = System.currentTimeMillis()
    Logger.info("validateFileData: Call to Validator: " + (System.currentTimeMillis() / 1000))
    httpPost.POST(url, CsvValidatorData(callbackData, schemeInfo)).map { res =>
      metrics.ErsConnector(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
      res
    }.recover {
      case e: Exception => {
        Logger.error(s"validateCsvFileData: Validate file data failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        metrics.ErsConnector(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS)
        HttpResponse(400)
      }
    }
  }

  def saveMetadata(allData: ErsSummary)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[HttpResponse] = {
    import ErsModelFormats._
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/saveMetadata"
    httpPost.POST(url, allData)
  }

  def checkForPresubmission(schemeInfo: SchemeInfo, validatedSheets: String)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[HttpResponse] = {
    import ErsModelFormats._
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/check-for-presubmission/${validatedSheets}"
    httpPost.POST(url, schemeInfo)
  }

  def removePresubmissionData(schemeInfo: SchemeInfo)(implicit authContext : AuthContext, hc: HeaderCarrier): Future[HttpResponse] = {
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/removePresubmissionData"
    httpPost.POST(url, schemeInfo)
  }

  def retrieveSubmissionData(data: JsObject)(implicit authContext: AuthContext, hc: HeaderCarrier): Future[HttpResponse] = {
    val empref: String = getAuthID(authContext).encodedValue
    val url: String = s"${ersUrl}/ers/${empref}/retrieve-submission-data"
    httpPost.POST(url, data)
  }


}
