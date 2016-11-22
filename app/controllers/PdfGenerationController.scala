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

import connectors.{AttachmentsConnector, ErsConnector}
import models._
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.{Logger, Play}
import services.SessionService
import services.pdf.{ApachePdfContentsStreamer, ErsReceiptPdfBuilderService}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.{CacheUtil, PageBuilder}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.collection.mutable.ListBuffer

trait PdfGenerationController extends ERSReturnBaseController with Authenticator {
  val cacheUtil: CacheUtil
  val pdfBuilderService : ErsReceiptPdfBuilderService
  val messages = applicationMessages


  def buildPdfForBundle(bundle: String, dateSubmitted: String) = AuthorisedForAsync() {
    implicit user =>
      implicit request =>
        generatePdf(bundle, dateSubmitted)
 }

  def generatePdf(bundle: String, dateSubmitted: String)(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier) = {

    Logger.debug("ers returns frontend getting into the controller to generate the pdf")
    cacheUtil.fetch[ErsMetaData](CacheUtil.ersMetaData, cacheUtil.getSchemeRefFromScreenSchemeInfo(
      request.session.get(screenSchemeInfo).get)).flatMap { all =>
      Logger.debug("ers returns fronend pdf generation: got the metadata")
      cacheUtil.getAllData(bundle, all).flatMap { alldata =>
        Logger.debug("ers returns fronend generation: got the cache map")
        val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)

        cacheUtil.fetchAll(schemeRef).map { all =>
          val filesUploaded: ListBuffer[String] = ListBuffer()
          val schemeId = request.session.get("screenSchemeInfo").get.split(" - ").head
          if (all.getEntry[ReportableEvents](CacheUtil.reportableEvents).get.isNilReturn.get == PageBuilder.OPTION_UPLOAD_SPREEDSHEET) {
            val fileType = all.getEntry[CheckFileType](CacheUtil.FILE_TYPE_CACHE).get.checkFileType.get
            if (fileType == PageBuilder.OPTION_CSV) {
              val csvFilesCallback: List[CsvFilesCallback] = all.getEntry[CsvFilesCallbackList](CacheUtil.CHECK_CSV_FILES).get.files
              for (file <- csvFilesCallback if (file.callbackData.isDefined)) {
                filesUploaded += messages(PageBuilder.getPageElement(schemeId, PageBuilder.PAGE_CHECK_CSV_FILE, file.fileId + ".file_name"))
              }
            } else {
              filesUploaded += all.getEntry[String](CacheUtil.FILE_NAME_CACHE).get
            }
          }
          Ok(pdfBuilderService.createPdf(new ApachePdfContentsStreamer(alldata), alldata, Some(filesUploaded), dateSubmitted).
            toByteArray).as("application/pdf").
            withHeaders(CONTENT_DISPOSITION -> s"inline; filename=${bundle}-confirmation.pdf")
        } recover {
          case e: Throwable => {
            Logger.error(s"Problem fetching file list from cache ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
            getGlobalErrorPage
          }
        }
      }.recover {
        case e: Throwable => {
          Logger.error(s"Problem saving Pdf Receipt ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
          getGlobalErrorPage
        }
      }
    }
  }

  def getGlobalErrorPage = Ok(views.html.global_error(messages("ers.global_errors.title"),
    messages("ers.global_errors.heading"), messages("ers.global_errors.message")))
}

object PdfGenerationController extends PdfGenerationController {
  val attachmentsConnector = AttachmentsConnector
  val currentConfig = Play.current.configuration
  val sessionService = SessionService
  val ersConnector: ErsConnector = ErsConnector
  override val cacheUtil: CacheUtil = CacheUtil
  override val pdfBuilderService: ErsReceiptPdfBuilderService = ErsReceiptPdfBuilderService
}
