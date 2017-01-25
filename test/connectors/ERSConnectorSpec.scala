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

import models.{CallbackData, SchemeInfo, ValidatorData}
import metrics.Metrics
import org.joda.time.DateTime
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpPost, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.Fixtures

import scala.concurrent.Future

class ErsConnectorSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  implicit val hc = new HeaderCarrier
  implicit val authContext = mock[AuthContext]
  implicit val request = FakeRequest()

  val schemeInfo =  SchemeInfo("XA1100000000000", DateTime.now,"1" ,"2016","EMI", "EMI")

  "calling sendData" should {

    val mockHttp = mock[HttpPost]

    val schemeInfo1 =  SchemeInfo("XA1100000000000", DateTime.now,"1" ,"2016","QQQ", "QQQ")
    val schemeType = "EMI"

    val callbackData = CallbackData(
      collection = "collection",
      id = "someid",
      length = 1000L,
      name = Some(Fixtures.firstName),
      contentType = Some("content-type"),
      sessionId = Some("testId"),
      customMetadata = Some(Json.obj("sessionId" -> "testId")), noOfRows = None)

    val ersConnector:  ErsConnector = new ErsConnector {
      override val metrics:Metrics = mock[Metrics]
      override def httpPost: HttpPost = mockHttp
      override def httpGet: HttpGet = mock[HttpGet]
      override def ersUrl = "ers-returns"
      override def ersRegime = "epaye"
      override def validatorUrl = "ers-file-validator"
      override def getAuthID(implicit authContext : AuthContext) = EmpRef("","")
    }

    "successful validation" in {
      reset(mockHttp)
      when(
        mockHttp.POST[ValidatorData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
      ).thenReturn(
        Future.successful(HttpResponse(OK))
      )

      val result = await(ersConnector.validateFileData(callbackData, schemeInfo))
      result.status shouldBe OK
    }

    "validation fails" in {
      reset(mockHttp)
      when(
        mockHttp.POST[ValidatorData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
      ).thenReturn(
        Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
      )

      val result = await(ersConnector.validateFileData(callbackData, schemeInfo))
      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "validator throw Exception" in {
      reset(mockHttp)
      doThrow(
        new RuntimeException
      ).when(mockHttp).POST[ValidatorData, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())

      intercept[Exception]{
        ersConnector.validateFileData(callbackData,  schemeInfo1)
      }
    }
  }

  "calling retrieveSubmissionData" should {

    val mockHttp = mock[HttpPost]

    val ersConnector:  ErsConnector = new ErsConnector {
      override val metrics:Metrics = mock[Metrics]
      override def httpPost: HttpPost = mockHttp
      override def httpGet: HttpGet = mock[HttpGet]
      override def ersUrl = "ers-returns"
      override def ersRegime = "epaye"
      override def validatorUrl = "ers-file-validator"
      override def getAuthID(implicit authContext : AuthContext) = EmpRef("","")
    }

    val data: JsObject = Json.obj(
      "schemeRef" -> "XA1100000000000",
      "confTime" -> "2016-08-05T11:14:43"
    )

    "successful retrieving" in {
      reset(mockHttp)
      when(
        mockHttp.POST[SchemeInfo, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
      ).thenReturn(
        Future.successful(HttpResponse(OK))
      )

      val result = await(ersConnector.retrieveSubmissionData(data))
      result.status shouldBe OK
    }

    "failed retrieving" in {
      reset(mockHttp)
      when(
        mockHttp.POST[SchemeInfo, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
      ).thenReturn(
        Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
      )

      val result = await(ersConnector.retrieveSubmissionData(data))
      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "throws exception" in {
      reset(mockHttp)
      when(
        mockHttp.POST[SchemeInfo, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())
      ).thenReturn(
        Future.failed(new RuntimeException)
      )

      intercept[Exception] {
        await(ersConnector.retrieveSubmissionData(data))
      }
    }

  }
}
