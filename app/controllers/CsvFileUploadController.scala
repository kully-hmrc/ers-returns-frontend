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

import config.ERSFileValidatorAuthConnector
import connectors.{ErsConnector, AttachmentsConnector}
import play.api.mvc.{Result, Request}
import play.api.Logger
import play.twirl.api.Html
import services.SessionService
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import utils._
import _root_.models._
import scala.concurrent.Future
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

trait CsvFileUploadController extends FrontendController with Authenticator {

  val messages = applicationMessages
  val attachmentsConnector: AttachmentsConnector
  val sessionService: SessionService
  val cacheUtil: CacheUtil
  val ersConnector: ErsConnector

  def uploadFilePage() = AuthorisedForAsync()  {
    implicit user =>
      implicit request =>
        showUploadFilePage()(user, request, hc)
  }

  def showUploadFilePage()(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[CsvFilesCallbackList](CacheUtil.CHECK_CSV_FILES, scRef).flatMap { csvFilesCallbackList =>
      showAttachmentsPartial(csvFilesCallbackList.files)
    } recover {
      case e: Throwable => {
        Logger.error(s"Fetch csvFilesCallback failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def showAttachmentsPartial(csvFilesList: List[CsvFilesCallback])(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    attachmentsConnector.getCsvFileUploadPartial().map {
      Logger.info(s"uploadFilePage: Response received from Attachments for partial, timestamp: ${System.currentTimeMillis()}.")
      partial => Ok(views.html.csv_file_upload(Html(partial.body), csvFilesList))
    }.recover {
      case ex: Exception => {
        Logger.error(s"Failing retrieving attachments partial. Error: ${ex.getMessage}, timestamp: ${System.currentTimeMillis()}")
        getGlobalErrorPage
      }
    }
  }

  def success() = AuthorisedForAsync()  {
    implicit user =>
      implicit request =>
        showSuccess()
  }

  def showSuccess()(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    Logger.info("success: Attachments Success: " + (System.currentTimeMillis() / 1000))
    sessionService.retrieveCallbackData().flatMap { callbackData =>
      proceedCallbackData(callbackData)
    }
  }

  def proceedCallbackData(callbackData: Option[CallbackData])(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[CsvFilesCallbackList](CacheUtil.CHECK_CSV_FILES, scRef).flatMap { csvFilesCallbackList =>
      val newCsvFilesCallbackList: List[CsvFilesCallback] = updateCallbackData(callbackData, csvFilesCallbackList.files)
      modifyCachedCallbackData(newCsvFilesCallbackList)
    } recover {
      case e: Exception => {
        Logger.error(s"success: failed to fetch callback data with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def modifyCachedCallbackData(newCsvFilesCallbackList: List[CsvFilesCallback])(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.cache[CsvFilesCallbackList](CacheUtil.CHECK_CSV_FILES, CsvFilesCallbackList(newCsvFilesCallbackList), scRef).map { cached =>
      if (newCsvFilesCallbackList.count(_.callbackData.isEmpty) > 0) {
        Redirect(routes.CsvFileUploadController.uploadFilePage())
      } else {
        Ok(views.html.success(Some(CsvFilesCallbackList(newCsvFilesCallbackList)), None))
      }
    } recover {
      case e: Exception => {
        Logger.error(s"success: failed to save callback data list with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def updateCallbackData(callbackData: Option[CallbackData], csvFilesCallbackList: List[CsvFilesCallback])(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): List[CsvFilesCallback] = {
    val schemeId = request.session.get("screenSchemeInfo").get.split(" - ").head
    for(csvFileCallback <- csvFilesCallbackList) yield {
      val filename = messages(PageBuilder.getPageElement(schemeId, PageBuilder.PAGE_CHECK_CSV_FILE, csvFileCallback.fileId+".file_name"))
      if (filename == callbackData.get.name.get) {
        CsvFilesCallback(csvFileCallback.fileId, callbackData)
      } else {
        csvFileCallback
      }
    }
  }

  def validationResults() = AuthorisedFor(ERSRegime, pageVisibility = GGConfidence).async {
    implicit user =>
      implicit request =>
        processValidationResults()
  }

  def processValidationResults()(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[ErsMetaData](CacheUtil.ersMetaData, scRef).flatMap { all =>
      removePresubmissionData(all.schemeInfo)
    } recover {
      case e: Exception => {
        Logger.error(s"Failed to fetch metadata data with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def removePresubmissionData(schemeInfo: SchemeInfo)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    ersConnector.removePresubmissionData(schemeInfo).flatMap { result =>
      result.status match {
        case 200 => extractCsvCallbackData(schemeInfo)
        case _ => {
          Logger.error(s"validationResults: removePresubmissionData failed with status ${result.status}, timestamp: ${System.currentTimeMillis()}.")
          Future(getGlobalErrorPage)
        }
      }
    } recover {
      case e: Exception => {
        Logger.error(s"Failed to remove presubmission data with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def extractCsvCallbackData(schemeInfo: SchemeInfo)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[CsvFilesCallbackList](CacheUtil.CHECK_CSV_FILES, scRef).flatMap { csvFilesCallbackList =>
      val csvCallbackValidatorData: List[CallbackData] = for (csvCallback <- csvFilesCallbackList.files) yield {
        csvCallback.callbackData.get
      }
      validateCsv(csvCallbackValidatorData, schemeInfo)
    } recover {
      case e: Exception => {
        Logger.error(s"Failed to fetch CsvFilesCallbackList with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def validateCsv(csvCallbackValidatorData: List[CallbackData], schemeInfo: SchemeInfo)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    ersConnector.validateCsvFileData(csvCallbackValidatorData, schemeInfo).map { res =>
      Logger.info(s"validateCsv: Response from validator: ${res.status}, timestamp: ${System.currentTimeMillis()}.")
      res.status match {
        case 200 => {
          Logger.warn(s"validateCsv: Validation is successful for schemeRef: ${scRef}, callback: ${csvCallbackValidatorData.toString}, timestamp: ${System.currentTimeMillis()}.")
          cacheUtil.cache(cacheUtil.VALIDATED_SHEEETS, res.body, scRef)
          Redirect(routes.SchemeOrganiserController.schemeOrganiserPage())
        }
        case 202 => {
          Logger.warn(s"validateCsv: Validation is not successful for schemeRef: ${scRef}, callback: ${csvCallbackValidatorData.toString}, timestamp: ${System.currentTimeMillis()}.")
          Redirect(routes.CsvFileUploadController.validationFailure())
        }
        case _ => Logger.error(s"validateCsv: Validate file data failed with Status ${res.status}, timestamp: ${System.currentTimeMillis()}.")
          getGlobalErrorPage
      }
    } recover {
      case e: Exception => {
        Logger.error(s"validateCsv: Failed to fetch CsvFilesCallbackList with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def validationFailure() = AuthorisedFor(ERSRegime, pageVisibility = GGConfidence).async {
    implicit user =>
      implicit request =>
        processValidationFailure()(user, request, hc)
  }

  def processValidationFailure()(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    Logger.info("validationFailure: Validation Failure: " + (System.currentTimeMillis() / 1000))
    val scRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[CheckFileType](CacheUtil.FILE_TYPE_CACHE, scRef).flatMap { fileType =>
      cacheUtil.fetch[ErsMetaData](CacheUtil.ersMetaData, scRef).map { all =>
        val scheme: String = all.schemeInfo.schemeId
        val schemeName: String = all.schemeInfo.schemeName
        Ok(views.html.file_upload_errors(scheme, schemeName, scRef, fileType.checkFileType.get))
      }.recover {
        case e: Exception => {
          Logger.error(s"processValidationFailure: failed to save callback data list with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
          getGlobalErrorPage
        }
      }
    }
  }

  def failure() = AuthorisedFor(ERSRegime, pageVisibility = GGConfidence).async {
    implicit user =>
      implicit request =>
        Logger.error("failure: Attachments Failure: " + (System.currentTimeMillis() / 1000))
        Future(getGlobalErrorPage)
  }

    def getGlobalErrorPage = Ok(views.html.global_error(messages("ers.global_errors.title"), messages("ers.global_errors.heading"), messages("ers.global_errors.message")))

}

object CsvFileUploadController extends CsvFileUploadController {
  val attachmentsConnector = AttachmentsConnector
  val authConnector = ERSFileValidatorAuthConnector
  val sessionService = SessionService
  val ersConnector: ErsConnector = ErsConnector
  override val cacheUtil: CacheUtil = CacheUtil
}
