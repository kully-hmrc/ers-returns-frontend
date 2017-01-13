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

import controllers.Fixtures
import models.CallbackData
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{Json, JsObject}
import play.api.test.{FakeHeaders, FakeRequest}
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._
import scala.concurrent.Future

class CsvFileUploadCallbackControllerSpec extends UnitSpec with ERSFakeApplication with MockitoSugar {

  val metaData: JsObject = Json.obj(
    "surname" -> Fixtures.surname,
    "firstForename" -> Fixtures.firstName
  )

  val mockAuthConnector = mock[AuthConnector]

  val mockSessionService: SessionService = mock[SessionService]

  val csvFileUploadCallbackController: CsvFileUploadCallbackController = new CsvFileUploadCallbackController {
    val authConnector = mockAuthConnector

    override val sessionService: SessionService = mockSessionService
  }

  "calling callback" should {

    "return OK if attachments retun valid callback data and storing callback data is successful" in {
      val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some("John"), contentType = Some("content-type"), customMetadata = Some(metaData), sessionId = Some("testId"), noOfRows = None)

      reset(mockSessionService)
      when(
        mockSessionService.storeCallbackData(any[CallbackData]())(any(), any())
      ).thenReturn(
        Future.successful(Some(callbackData))
      )
      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))

      val result = await(
        csvFileUploadCallbackController.callback()(fakeRequest)
      )
      status(result) shouldBe OK
    }

    "return INTERNAL_SERVER_ERROR if attachments retun invalid callback data and storing callback data is successful" in {
      val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some("John"), contentType = Some("content-type"), customMetadata = Some(metaData), sessionId = Some("testId"), noOfRows = None)


      reset(mockSessionService)
      when(
        mockSessionService.storeCallbackData(any[CallbackData]())(any(), any())
      ).thenReturn(
        Future.successful(None)
      )
      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))

      val result = await(
        csvFileUploadCallbackController.callback()(fakeRequest)
      )
      status(result) shouldBe INTERNAL_SERVER_ERROR
      bodyOf(result) shouldBe "Exception"
    }

    "return INTERNAL_SERVER_ERROR if storing callback data fails" in {
      val callbackData = CallbackData(collection = "collection", id = "someid", length = 1000L, name = Some(Fixtures.firstName), contentType = Some("content-type"), customMetadata = Some(metaData), sessionId = Some("testId"), noOfRows = None)

      reset(mockSessionService)
      when(
        mockSessionService.storeCallbackData(any[CallbackData]())(any(), any())
      ).thenReturn(
        Future.failed(new RuntimeException)
      )
      val fakeRequest = FakeRequest(method = "POST", uri = "", headers = FakeHeaders(Seq("Content-type" -> Seq("application/json"))), body = Json.toJson(callbackData))

      val result = await(
        csvFileUploadCallbackController.callback()(fakeRequest)
      )
      status(result) shouldBe INTERNAL_SERVER_ERROR
      bodyOf(result) shouldBe "Exception occurred when attempting to store data"
    }

  }


}
