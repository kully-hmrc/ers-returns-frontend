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

package controllers

import _root_.models._
import connectors.ErsConnector
import metrics.Metrics
import org.joda.time.DateTime
import play.Logger
import play.api.Play
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.play.frontend.auth.{AllowAll, AuthContext}
import uk.gov.hmrc.play.http.HeaderCarrier
import utils._
import config._
import scala.concurrent.Future

object ReturnServiceController extends ReturnServiceController {
  override val cacheUtil: CacheUtil = CacheUtil

  override val accessThreshold = Play.current.configuration.getInt("accessThreshold").getOrElse(100)
  override val accessDeniedUrl = "/outage-ers-frontend/index.html"
  override val metrics = Metrics
}

trait ErsConstants{
  val screenSchemeInfo = "screenSchemeInfo"
  val schemeId = "schemeId"
  val schemeRef  = "schemeRef"
  implicit val context: ErsContext = ErsContextImpl
}

trait ReturnServiceController extends ERSReturnBaseController with Authenticator with ErsConstants {

  val accessDeniedUrl: String

  val cacheUtil: CacheUtil
  val messages = applicationMessages

  val accessThreshold: Int
  val metrics: Metrics

  def getSchemeId(schemeType: String) =
  {
      schemeType.toUpperCase match {
        case PageBuilder.CSOP => PageBuilder.SCHEME_CSOP
        case PageBuilder.EMI => PageBuilder.SCHEME_EMI
        case PageBuilder.SAYE => PageBuilder.SCHEME_SAYE
        case PageBuilder.SIP => PageBuilder.SCHEME_SIP
        case PageBuilder.OTHER => PageBuilder.SCHEME_OTHER
        case _ => PageBuilder.DEFAULT
      }
  }
  def cacheParams(ersRequestObject: RequestObject)(implicit authContext:AuthContext, request: Request[AnyRef], hc: HeaderCarrier) = {

    implicit val formatRSParams = Json.format[ErsMetaData]
    val sr = ersRequestObject.getSchemeReference()
    val schemeInfo = SchemeInfo(ersRequestObject.ersSchemeRef.get,DateTime.now,getSchemeId(ersRequestObject.getSchemeType()),ersRequestObject.taxYear.get,ersRequestObject.schemeName.get,ersRequestObject.schemeType.get)

      val metaData: ErsMetaData = ErsMetaData(schemeInfo, request.remoteAddress, ersRequestObject.aoRef, ersRequestObject.getEmpRef, ersRequestObject.agentRef, None)
      Logger.debug("metaData created --> " + metaData)
      cacheUtil.cache(CacheUtil.ersMetaData, metaData, sr).map {
        cacheResult =>  {Logger.debug("metaData after cache --> " + metaData); showInitialStartPage(metaData)(authContext, request, hc)}
      } recover { case e: Exception =>
        Logger.warn(s"Caught exception ${e.getMessage}", e)
        getGlobalErrorPage
      }
  }

  def getRequestParameters(request: Request[AnyContent]) =
  {
    val aoRef: Option[String] = request.getQueryString("aoRef")
    val taxYear: Option[String] = request.getQueryString("taxYear")
    val ersSchemeRef: Option[String] = request.getQueryString("ersSchemeRef")
    val schemeType: Option[String] = request.getQueryString("schemeType")
    val schemeName: Option[String] = request.getQueryString("schemeName")
    val agentRef: Option[String] = request.getQueryString("agentRef")
    val empRef: Option[String] = request.getQueryString("empRef")
    val ts: Option[String] = request.getQueryString("ts")
    val hmac: Option[String] = request.getQueryString("hmac")
    RequestObject(aoRef, taxYear, ersSchemeRef, schemeName, schemeType, agentRef, empRef, ts, hmac)
  }

  def hmacCheck() = AuthenticatedBy(ERSGovernmentGateway, pageVisibility = AllowAll).async {
    implicit user =>
      implicit request =>
        Logger.warn("HMAC Check Authenticated")
        if (request.getQueryString("ersSchemeRef").getOrElse("") == "") {
          Logger.warn("Missing SchemeRef in URL")
          Future(getGlobalErrorPage)
        } else {
          HMACUtil.isHmacAndTimestampValid(getRequestParameters(request)) match {
            case true => Logger.warn("HMAC Check Valid")
              try {cacheParams(getRequestParameters(request))
              } catch {
                case e: Throwable => Logger.warn(s"Caught exception ${e.getMessage}", e)
                  Future(getGlobalErrorPage)
              }
            case _ => Logger.warn("HMAC Check Invalid")
              showUnauthPage(request)
          }
        }
  }

  def showInitialStartPage(metaData: ErsMetaData)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier) = {
    Ok(views.html.start(ErsMetaDataHelper.getScreenSchemeInfo(metaData))).withSession(request.session + (screenSchemeInfo -> ErsMetaDataHelper.getScreenSchemeInfo(metaData)) - "bundelRef" - "dateTimeSubmitted")
  }

  def startPage() = AuthenticatedBy(ERSGovernmentGateway, pageVisibility = AllowAll).async {
    implicit user =>
      implicit request =>
        Future(Ok(views.html.start(request.session.get(screenSchemeInfo).get)).withSession(request.session - "bundelRef" - "dateTimeSubmitted"))
  }


  def showUnauthPage(request: Request[AnyRef]): Future[Result] = {
    Future.successful(Ok(views.html.unauthorised()(request, context)))
  }

    def getGlobalErrorPage = Ok(views.html.global_error(messages("ers.global_errors.title"), messages("ers.global_errors.heading"), messages("ers.global_errors.message")))


}
