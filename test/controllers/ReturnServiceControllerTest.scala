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

package controllers

import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

import config.ApplicationConfig
import connectors.{CustomAuditConnector, ErsConnector}
import metrics.Metrics
import models.{ErsMetaData, _}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.libs.json
import play.api.libs.json.{JsValue, _}
import play.api.mvc.{Action, Request, Results}
import play.api.test.{FakeRequest, Helpers}
import services.SessionService
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.{CacheUtil, ExternalUrls}

import scala.concurrent.{Await, ExecutionContext, Future}
import org.jsoup.Jsoup
import play.api.test.Helpers._
import utils.ContentUtil._
import play.api.i18n.Messages


class ReturnServiceControllerTest extends UnitSpec with ERSFakeApplication with MockitoSugar {

  val mockHttp = mock[HttpPost]
  val mockHttpGet = mock[HttpGet]
  val mockSessionCache = mock[SessionService]
  val ExpectedRedirectionUrlIfNotSignedIn = "/gg/sign-in?continue=/submit-your-ers-return"
  val schemeInfo =  SchemeInfo("XA1100000000000", DateTime.now,"1" ,"2016","EMI", "EMI")
  val rsc: ErsMetaData = new ErsMetaData(schemeInfo, "ipRef", Some("aoRef"), "empRef",Some("agentRef"),Some("sapNumber"))

  def buildFakeReturnServiceController(accessThresholdValue: Int = 100) = new ReturnServiceController {

    val accessDeniedUrl = "/denied.html"
    var fetchMapVal = "e"


    when(
      mockHttp.POST[ValidatorData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
    ).thenReturn(
      Future.successful(HttpResponse(200))
    )

    override val cacheUtil: CacheUtil = new CacheUtil {
      override val sessionService: SessionService = mockSessionCache
      override def cache[T](key: String, body: T, cacheId: String)(implicit hc: HeaderCarrier, formats: json.Format[T], request: Request[AnyRef]) = {
        Future.successful(null)
      }

      @throws(classOf[NoSuchElementException])
      override def fetch[T](key: String, cacheId: String)(implicit hc: HeaderCarrier, formats: json.Format[T], request: Request[AnyRef]): Future[T] = {

        fetchMapVal match {
          case "e" => {
            Future(throw new NoSuchElementException)
          }
          case "withSchemeType" => {
            Future.successful(rsc.asInstanceOf[T])
          }
          case "withZeroErrorCount" => {
            Future.successful(rsc.asInstanceOf[T])
          }
          case "withErrorCountSchemeTypeFileNameFileType" => {
            Future.successful(rsc.asInstanceOf[T])
          }
          case "withSchemeAndFileType" => {
          Future.successful(rsc.asInstanceOf[T])
          }
          case "withMatchingSchemeRef" => {
            Future.successful(rsc.asInstanceOf[T])
          }
          case "withNonMatchingSchemeRef" => {
            Future.successful(rsc.asInstanceOf[T])
          }
        }
      }

      override def shortLivedCache: ShortLivedCache = ???
    }
    override val accessThreshold: Int = accessThresholdValue
    override val metrics: Metrics = mock[Metrics]
  }

  val schemeRef = Fixtures.schemeRef

  
  "Calling ReturnServiceController.cacheParams with existing cache storage for the given schemeId and schemeRef" should {
  	"retrieve the stored cache and redirect to the initial start page" in {
      val controllerUnderTest = buildFakeReturnServiceController()
      controllerUnderTest.fetchMapVal = "withMatchingSchemeRef"     	
      val aoRef: Option[String] = Option("123AA12345678")
      val taxYear: Option[String] = Option("2014/15")
      val ersSchemeRef: Option[String] = Option("AA0000000000000")
      val schemeType: Option[String] = Option("CSOP")
      val schemeName: Option[String] = Option("MyScheme")
      val agentRef: Option[String] = None
      val empRef: Option[String] = Option("empRef")
      val ts: Option[String] = None
      val hmac: Option[String] = Option("hmac")
      val ersRequestObject = new RequestObject(aoRef, taxYear, ersSchemeRef, schemeName, schemeType, agentRef, empRef, ts, hmac)
      val result = controllerUnderTest.cacheParams(ersRequestObject)(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionId("GET"), hc)
      status(result) shouldBe Status.OK  
      val document = Jsoup.parse(contentAsString(result))
      val scheme = "1"
      document.getElementsByTag("h1").text() should include(Messages("ers_start.page_title", getSchemeName(scheme)))            
  	} 
  }  
  
  "Calling ReturnServiceController.cacheParams with no matching cache storage for the given schemeId and schemeRef" should {
  	"create a new cache object and redirect to the initial start page" in {
      val controllerUnderTest = buildFakeReturnServiceController()
      controllerUnderTest.fetchMapVal = "withNonMatchingSchemeRef"     	
      val aoRef: Option[String] = Option("123AA12345678")
      val taxYear: Option[String] = Option("2014/15")
      val ersSchemeRef: Option[String] = Option("AA0000000000000")
      val schemeType: Option[String] = Option("CSOP")
      val schemeName: Option[String] = Option("MyScheme")
      val agentRef: Option[String] = None
      val empRef: Option[String] = Option("empRef")
      val ts: Option[String] = None
      val hmac: Option[String] = Option("hmac")
      val ersRequestObject = new RequestObject(aoRef, taxYear, ersSchemeRef, schemeName, schemeType, agentRef, empRef, ts, hmac)
      val result = controllerUnderTest.cacheParams(ersRequestObject)(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionId("GET"), hc)
      status(result) shouldBe Status.OK  		
  	} 
  }
  
  //Start Page
  "Calling ReturnServiceController.startPage (GET) without authentication" should {
    "give a redirect status (to company authentication frontend)" in {
      val controllerUnderTest = buildFakeReturnServiceController()
      val result = controllerUnderTest.startPage().apply(FakeRequest("GET", ""))
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "Calling ReturnServiceController.hmacCheck" should {
    "without authentication should redirect to to company authentication frontend" in {
      implicit val fakeRequest = Fixtures.buildFakeRequestWithSessionId("?")
      val controllerUnderTest = buildFakeReturnServiceController(accessThresholdValue = 0)
      val result = await(controllerUnderTest.hmacCheck()(fakeRequest))
      Helpers.redirectLocation(result).get.startsWith(ApplicationConfig.ggSignInUrl) shouldBe true
    }
  }

  "Calling ReturnServiceController.startPage" should {
    "without authentication should redirect to to company authentication frontend" in {
      implicit val fakeRequest = Fixtures.buildFakeRequestWithSessionId("?")
      val controllerUnderTest = buildFakeReturnServiceController(accessThresholdValue = 0)
      val result = await(controllerUnderTest.startPage()(fakeRequest))
      Helpers.redirectLocation(result).get.startsWith(ApplicationConfig.ggSignInUrl) shouldBe true
    }
  }

}
