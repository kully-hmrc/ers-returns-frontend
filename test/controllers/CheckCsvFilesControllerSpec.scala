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

import models._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.mvc.{Result, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.{PageBuilder, CacheUtil}
import scala.concurrent.Future
import play.api.test.Helpers._

class CheckCsvFilesControllerSpec extends UnitSpec with ERSFakeApplication with MockitoSugar {

  "calling checkCsvFilesPage" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
      override def showCheckCsvFilesPage()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = Future.successful(Ok)
    }

    "redirect to company authentication frontend if user is not authenticated" in {
      val result = checkCsvFilesController.checkCsvFilesPage().apply(FakeRequest("GET", ""))
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location").contains("/gg/sign-in") shouldBe true
    }
  }

  "calling showCheckCsvFilesPage" should {

    val mockCacheUtil: CacheUtil = mock[CacheUtil]

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mockCacheUtil
      override val pageBuilder: PageBuilder = mock[PageBuilder]
      override def mergeCsvFilesListWithCsvFilesCallback(csvFilesList: List[CsvFiles], cacheData: CsvFilesCallbackList): List[CsvFiles] = List()
    }

    "show CheckCsvFilesPage if data is successfully extracted from cache" in {
      reset(mockCacheUtil)
      when(
        mockCacheUtil.fetch[CsvFilesCallbackList](anyString(), anyString())(any(), any(), any())
      ).thenReturn(
        Future.successful(mock[CsvFilesCallbackList])
      )
      val result = await(checkCsvFilesController.showCheckCsvFilesPage()(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("GET"), hc))
      status(result) shouldBe OK
    }

    "show CheckCsvFilesPage if there is no data in cache" in {
      reset(mockCacheUtil)
      when(
        mockCacheUtil.fetch[CsvFilesCallbackList](anyString(), anyString())(any(), any(), any())
      ).thenReturn(
        Future.failed(new NoSuchElementException)
      )
      val result = await(checkCsvFilesController.showCheckCsvFilesPage()(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("GET"), hc))
      status(result) shouldBe OK
    }

    "direct to ers errors page if fetching data throws exception" in {
      reset(mockCacheUtil)
      when(
        mockCacheUtil.fetch[CsvFilesCallbackList](anyString(), anyString())(any(), any(), any())
      ).thenReturn(
        Future.failed(new Exception)
      )
      val result = await(checkCsvFilesController.showCheckCsvFilesPage()(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("GET"), hc))
      contentAsString(result) shouldBe contentAsString(checkCsvFilesController.getGlobalErrorPage)
      contentAsString(result) should include("Service unavailable")
    }

  }

  "caling mergeCsvFilesListWithCsvFilesCallback" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
    }

    val csvFilesList: List[CsvFiles] = List(
      CsvFiles("file0", None),
      CsvFiles("file1", None),
      CsvFiles("file2", None),
      CsvFiles("file3", None),
      CsvFiles("file4", None)
    )

    "merge successfully given data with cached one" in {

      val cacheData: CsvFilesCallbackList = CsvFilesCallbackList(
        List(
          CsvFilesCallback("file1", None),
          CsvFilesCallback("file4", None)
        )
      )

      val result = checkCsvFilesController.mergeCsvFilesListWithCsvFilesCallback(csvFilesList, cacheData)
      result shouldBe List(
        CsvFiles("file0", None),
        CsvFiles("file1", Some("1")),
        CsvFiles("file2", None),
        CsvFiles("file3", None),
        CsvFiles("file4", Some("1"))
      )

    }
  }

  "calling checkCsvFilesPage" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
      override def validateCsvFilesPageSelected()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = Future.successful(Ok)
    }

    "redirect to company authentication frontend if user is not authenticated to access checkCsvFilesPage" in {
      val result = checkCsvFilesController.checkCsvFilesPageSelected().apply(FakeRequest("GET", ""))
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location").contains("/gg/sign-in") shouldBe true
    }
  }

  "calling validateCsvFilesPageSelected" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
      override def reloadWithError(): Future[Result] = Future.successful(SeeOther(""))
      override def performCsvFilesPageSelected(formData: CsvFilesList)(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = Future.successful(Ok)
    }

    "return the result of performCsvFilesPageSelected if data is valid" in {

      val csvFilesListData: Map[String, String] = Map(
        ("files[0].fileId", "file0"),
        ("files[0].isSelected", "1"),
        ("files[1].fileId", "file1"),
        ("files[1].isSelected", "2"),
        ("files[2].fileId", "file2"),
        ("files[2].isSelected", "")
      )
      val form = RSformMappings.csvFileCheckForm.bind(csvFilesListData)

      val request = Fixtures.buildFakeRequestWithSessionIdCSOP("POST").withFormUrlEncodedBody(form.data.toSeq: _*)
      val result = await(checkCsvFilesController.validateCsvFilesPageSelected()(Fixtures.buildFakeUser, request, hc))
      status(result) shouldBe OK
    }

    "return the result of reloadWithError if data is invalid" in {

      val csvFilesListData: Map[String, String] = Map(
        ("files[0].fileId", ""),
        ("files[0].isSelected", "5")
      )
      val form = RSformMappings.csvFileCheckForm.bind(csvFilesListData)

      val request = Fixtures.buildFakeRequestWithSessionIdCSOP("POST").withFormUrlEncodedBody(form.data.toSeq: _*)
      val result = await(checkCsvFilesController.validateCsvFilesPageSelected()(Fixtures.buildFakeUser, request, hc))
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") shouldBe ""
    }

  }

  "calling performCsvFilesPageSelected" should {

    val mockListCsvFilesCallback: List[CsvFilesCallback] = mock[List[CsvFilesCallback]]
    val mockCacheUtil: CacheUtil = mock[CacheUtil]

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mockCacheUtil
      override val pageBuilder: PageBuilder = mock[PageBuilder]
      override def createCacheData(csvFilesList: List[CsvFiles]): List[CsvFilesCallback] = mockListCsvFilesCallback
      override def reloadWithError(): Future[Result] = Future.successful(SeeOther(""))
    }

    val formData: CsvFilesList = CsvFilesList(
      List(
        CsvFiles("file0", None),
        CsvFiles("file1", Some("1")),
        CsvFiles("file2", None),
        CsvFiles("file3", None),
        CsvFiles("file4", Some("1"))
      )
    )

    "return the result of reloadWithError if createCacheData returns empty list" in {
      reset(mockListCsvFilesCallback)
      reset(mockCacheUtil)

      when(
        mockListCsvFilesCallback.length
      ).thenReturn(
        0
      )

      val result = await(checkCsvFilesController.performCsvFilesPageSelected(formData)(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("POST"), hc))
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") shouldBe ""
    }

    "redirect to next page if createCacheData returns list with data and caching is successful" in {
      reset(mockListCsvFilesCallback)
      reset(mockCacheUtil)

      when(
        mockListCsvFilesCallback.length
      ).thenReturn(
        1
      )

      when(
        mockCacheUtil.cache(anyString(), any[CsvFilesCallbackList](), anyString())(any(), any(), any())
      ).thenReturn(
        Future.successful(mock[CacheMap])
      )

      val result = await(checkCsvFilesController.performCsvFilesPageSelected(formData)(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("POST"), hc))
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") should not be ""
    }

    "direct to ers errors page if createCacheData returns list with data and caching fails" in {
      reset(mockListCsvFilesCallback)
      reset(mockCacheUtil)

      when(
        mockListCsvFilesCallback.length
      ).thenReturn(
        1
      )

      when(
        mockCacheUtil.cache(anyString(), any[CsvFilesCallbackList](), anyString())(any(), any(), any())
      ).thenReturn(
        Future.failed(new RuntimeException)
      )

      val result = await(checkCsvFilesController.performCsvFilesPageSelected(formData)(Fixtures.buildFakeUser, Fixtures.buildFakeRequestWithSessionIdCSOP("POST"), hc))
      contentAsString(result) shouldBe contentAsString(checkCsvFilesController.getGlobalErrorPage)
      contentAsString(result) should include("Service unavailable")
    }

  }

  "calling createCacheData" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
    }

    val formData: List[CsvFiles] = List(
      CsvFiles("file0", None),
      CsvFiles("file1", Some("1")),
      CsvFiles("file2", None),
      CsvFiles("file3", None),
      CsvFiles("file4", Some("1"))
    )

    "return only selected files" in {
      val result = checkCsvFilesController.createCacheData(formData)
      result shouldBe List(
        CsvFilesCallback("file1", None),
        CsvFilesCallback("file4", None)
      )
    }

  }

  "calling reloadWithError" should {

    val checkCsvFilesController: CheckCsvFilesController = new CheckCsvFilesController {
      override val cacheUtil: CacheUtil = mock[CacheUtil]
      override val pageBuilder: PageBuilder = mock[PageBuilder]
    }

    "reload same page, showing error" in {
      val result = await(checkCsvFilesController.reloadWithError())
      status(result) shouldBe SEE_OTHER
      result.header.headers("Location") shouldBe routes.CheckCsvFilesController.checkCsvFilesPage().toString()
    }
  }

}
