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

import models.{SchemeInfo, CallbackData, ErsMetaData}
import org.joda.time.DateTime
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import play.api.test.{FakeHeaders, FakeRequest}
import services.SessionService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future
import utils.CacheUtil

class FileUploadCallbackControllerSpec extends PlaySpec with MockitoSugar{

  val mockAuthConnector = mock[AuthConnector]
  val mockCurrentConfig = mock[Configuration]
  val mockSessionService = mock[SessionService]
  val mockCacheUtil = mock[CacheUtil]

  object TestFileUploadCallbackController extends FileUploadCallbackController {
    val authConnector = mockAuthConnector
    val currentConfig = mockCurrentConfig
    val sessionService = mockSessionService
    val cacheUtil = mockCacheUtil
  }

  val metaData: JsObject = Json.obj(
    "surname" -> Fixtures.surname,
    "firstForename" -> Fixtures.firstName
  )

  val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some(Fixtures.firstName), contentType = Some("content-type"), customMetadata = Some(metaData), sessionId = Some("testId"), noOfRows = None)

  val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))
  
  val sr = "XA1100000000000"
  val schemeInfo =  SchemeInfo("XA1100000000000", DateTime.now,"1" ,"2016","EMI", "EMI")
  val rsc: ErsMetaData = new ErsMetaData(schemeInfo, "ipRef", Some("aoRef"), "empRef",Some("agentRef"),Some("sapNumber"))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "callback" must {
    "successfully store and validates callback data" in {

      when(mockSessionService.storeCallbackData(Matchers.any[CallbackData]())(Matchers.any[Request[_]](),Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(Some(callbackData)))
      when(mockCacheUtil.fetch[ErsMetaData](Matchers.any[String](), Matchers.any[String]())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(rsc))
      when(mockCacheUtil.cache(Matchers.anyString(), Matchers.any(), Matchers.anyString())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(mock[CacheMap]))
      val result = TestFileUploadCallbackController.callback().apply(fakeRequest)

      status(result) must be(OK)
    }

    "successfully store and validates callback data when callbackData doesn't have session id" in {
      val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some(Fixtures.firstName), contentType = Some("content-type"),
        customMetadata = None, sessionId = None, noOfRows = None)

      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))
      when(mockSessionService.storeCallbackData(Matchers.any[CallbackData]())(Matchers.any[Request[_]](),Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(Some(callbackData)))
      when(mockCacheUtil.fetch[ErsMetaData](Matchers.any[String](), Matchers.any[String]())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(rsc))
      when(mockCacheUtil.cache(Matchers.anyString(), Matchers.any(), Matchers.anyString())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(mock[CacheMap]))
      val result = TestFileUploadCallbackController.callback().apply(fakeRequest)

      status(result) must be(OK)

    }

    "fail storing data with missing session id" in {
      val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some(Fixtures.firstName), contentType = Some("content-type"),
        customMetadata = None, sessionId = Some("testId"), noOfRows = None)

      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))
      when(mockSessionService.storeCallbackData(Matchers.any[CallbackData]())(Matchers.any[Request[_]](),Matchers.any[HeaderCarrier]())).thenReturn(Future.successful(None))
      val result = TestFileUploadCallbackController.callback().apply(fakeRequest)
      status(result) must be(INTERNAL_SERVER_ERROR)

    }

    "fail storing data when an exception occurs" in {

      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))
      when(mockSessionService.storeCallbackData(Matchers.any[CallbackData]())(Matchers.any[Request[_]](),Matchers.any[HeaderCarrier]())).thenReturn(Future.failed(new RuntimeException))
      val result = TestFileUploadCallbackController.callback().apply(fakeRequest)
      status(result) must be(INTERNAL_SERVER_ERROR)

    }
  }

}
