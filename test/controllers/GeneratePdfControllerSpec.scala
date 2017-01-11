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

import java.io.ByteArrayOutputStream
import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.pdf.{ErsContentsStreamer, ErsReceiptPdfBuilderService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.{CacheUtil, PageBuilder}
import scala.concurrent.Future

class GeneratePdfControllerSpec extends UnitSpec with ERSFakeApplication with MockitoSugar{
  val pdfBuilderMock = mock[ErsReceiptPdfBuilderService]
  val cache = mock[CacheUtil]
  val schemeInfo = SchemeInfo("XA1100000000000", DateTime.now, "1", "2016", "EMI", "EMI")
  val rsc = ErsMetaData(schemeInfo, "ipRef", Some("aoRef"), "empRef", Some("agentRef"), Some("sapNumber"))
  val ersSummary = ErsSummary("testbundle", "2", None, DateTime.now, rsc, None, None, None, None, None, None, None, None)
  val cacheMap = mock[CacheMap]

  def createController(fetchAllRes: Boolean = true, getAllDataRes : Boolean = true, isNilReturn: Boolean = true, fileTypeCSV: Boolean = true): PdfGenerationController = new PdfGenerationController {

    override val cacheUtil: CacheUtil = cache
    override val pdfBuilderService: ErsReceiptPdfBuilderService = pdfBuilderMock

    val callbackData: CallbackData = new CallbackData("","",0,None,None,None,None,None)
    val csvFilesCallBack = new CsvFilesCallback("file0", Some(callbackData))
    val csvFilesCallbackList: CsvFilesCallbackList = new CsvFilesCallbackList(List(csvFilesCallBack))

    when(pdfBuilderMock.createPdf(any[ErsContentsStreamer], any[ErsSummary], any(), any())).thenReturn(mock[ByteArrayOutputStream])
    when(cache.fetch[ErsMetaData](refEq(CacheUtil.ersMetaData), anyString())(any(), any(), any())).thenReturn(Future.successful(rsc))
    when(cacheMap.getEntry[CsvFilesCallbackList](refEq(CacheUtil.CHECK_CSV_FILES))(any())).thenReturn(Future.successful(Some(csvFilesCallbackList)))
    when(cacheMap.getEntry[String](refEq(CacheUtil.FILE_NAME_CACHE))(any())).thenReturn(Future.successful(Some("test.ods")))

    if(fileTypeCSV)
      when(cacheMap.getEntry[CheckFileType](refEq(CacheUtil.FILE_TYPE_CACHE))(any())).thenReturn(Future.successful(Some(new CheckFileType(Some(PageBuilder.OPTION_CSV)))))
    else
      when(cacheMap.getEntry[CheckFileType](refEq(CacheUtil.FILE_TYPE_CACHE))(any())).thenReturn(Future.successful(Some(new CheckFileType(Some(PageBuilder.OPTION_ODS)))))

    if(isNilReturn)
      when(cacheMap.getEntry[ReportableEvents](refEq(CacheUtil.reportableEvents))(any())).thenReturn(Future.successful(Some(new ReportableEvents(Some(PageBuilder.OPTION_NIL_RETURN)))))
    else
      when(cacheMap.getEntry[ReportableEvents](refEq(CacheUtil.reportableEvents))(any())).thenReturn(Future.successful(Some(new ReportableEvents(Some(PageBuilder.OPTION_UPLOAD_SPREEDSHEET)))))

    if(fetchAllRes)
      when(cache.fetchAll(anyString())(any(), any())).thenReturn(Future.successful(cacheMap))
    else
      when(cache.fetchAll(anyString())(any(), any())).thenReturn(Future.failed(new Exception))

    if(getAllDataRes)
      when(cache.getAllData(anyString(), any[ErsMetaData]())(any(), any(), any())).thenReturn(Future.successful(ersSummary))
    else
      when(cache.getAllData(anyString(), any[ErsMetaData]())(any(), any(), any())).thenReturn(Future.failed(new Exception))
  }

  "pdf generation conroller" should {

    "give a redirect status (to company authentication frontend) on GET if user is not authenticated" in {
      val controller = createController()
      val result = controller.buildPdfForBundle("", "").apply(FakeRequest("GET", ""))
      status(result) shouldBe Status.SEE_OTHER
    }

    "give a status OK on GET if user is authenticated" in {
      val controller = createController()
      val result = controller.buildPdfForBundle("", "").apply(Fixtures.buildFakeRequestWithSessionIdCSOP("GET"))
      status(result) shouldBe Status.SEE_OTHER
    }

    "direct to errors page if fetch all res pdf throws exception" in {
      val controller = createController(fetchAllRes = false)
      val result = await(controller.generatePdf("", "")(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("GET"), hc))
      contentAsString(result) should include("Service unavailable")
      contentAsString(result) shouldBe contentAsString(createController().getGlobalErrorPage)
    }

    "direct to errors page if get all data res pdf throws exception" in {
      val controller = createController(getAllDataRes = false)
      val result = await(controller.generatePdf("", "")(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("GET"), hc))
      contentAsString(result) should include("Service unavailable")
      contentAsString(result) shouldBe contentAsString(createController().getGlobalErrorPage)
    }

    "use bundle ref to generate the confirmation pdf filename (NilReturn)" in {
      val controller = createController()
      val res = await(controller.generatePdf("123456", "8 August 2016, 4:28pm")(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionId("GET"), new HeaderCarrier()))
      res.header.headers("Content-Disposition") should include("123456-confirmation.pdf")
    }

    "use bundle ref to generate the confirmation pdf filename (CSV File submission)" in {
      val controller = createController(isNilReturn = false)
      val res = await(controller.generatePdf("123456", "8 August 2016, 4:28pm")(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionId("GET"), new HeaderCarrier()))
      res.header.headers("Content-Disposition") should include("123456-confirmation.pdf")
    }

    "use bundle ref to generate the confirmation pdf filename (ODS File submission)" in {
      val controller = createController(isNilReturn = false, fileTypeCSV = false)
      val res = await(controller.generatePdf("123456", "8 August 2016, 4:28pm")(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionId("GET"), new HeaderCarrier()))
      res.header.headers("Content-Disposition") should include("123456-confirmation.pdf")
    }

  }

}
