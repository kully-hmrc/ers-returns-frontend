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

import models.{RSformMappings, _}
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc._
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils._
import scala.concurrent.Future


object CheckFileTypeController extends CheckFileTypeController {
  override val cacheUtil: CacheUtil = CacheUtil
}

trait CheckFileTypeController extends ERSReturnBaseController with Authenticator {

  val messages = applicationMessages
  val jsonParser = JsonParser
  val contentUtil = ContentUtil
  val cacheUtil: CacheUtil

  def checkFileTypePage() = AuthenticatedBy(ERSGovernmentGateway, pageVisibilityPredicate).async {
    implicit authContext =>
      implicit request =>
        showCheckFileTypePage(authContext, request, hc)
  }

  def showCheckFileTypePage(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[CheckFileType](CacheUtil.FILE_TYPE_CACHE, schemeRef).map { fileType =>
      Ok(views.html.check_file_type(fileType.checkFileType, RSformMappings.checkFileTypeForm.fill(fileType)))
    } recover {
      case e: NoSuchElementException => {
        val form = CheckFileType(Some(""))
        Ok(views.html.check_file_type(Some(""), RSformMappings.checkFileTypeForm.fill(form)))
      }
    }
  }

  def checkFileTypeSelected() = AuthenticatedBy(ERSGovernmentGateway, pageVisibilityPredicate).async {
    implicit authContext =>
      implicit request =>
        showCheckFileTypeSelected(authContext, request, hc)
  }

  def showCheckFileTypeSelected(implicit authContext: AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    RSformMappings.checkFileTypeForm.bindFromRequest.fold(
      errors => {
        Future.successful(Ok(views.html.check_file_type(Some(""), errors)))
      },
      formData => {
        val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
        cacheUtil.cache(CacheUtil.FILE_TYPE_CACHE, formData, schemeRef).map { res =>
          if (formData.checkFileType.get == PageBuilder.OPTION_ODS) {
            Redirect(routes.FileUploadController.uploadFilePage())
          } else {
            Redirect(routes.CheckCsvFilesController.checkCsvFilesPage())
          }
        }.recover {
          case e: Exception => {
            Logger.error("showCheckFileTypeSelected: Unable to save file type. Error: " + e.getMessage)
            getGlobalErrorPage
          }
        }
      }
    )
  }

    def getGlobalErrorPage = Ok(views.html.global_error(messages("ers.global_errors.title"), messages("ers.global_errors.heading"), messages("ers.global_errors.message")))

}
