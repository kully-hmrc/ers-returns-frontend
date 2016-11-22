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
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.{CacheUtil, PageBuilder}

import scala.concurrent._

object AltAmendsController extends AltAmendsController {
  override val cacheUtil: CacheUtil = CacheUtil
  override val ersConnector: ErsConnector = ErsConnector
}

trait AltAmendsController extends ERSReturnBaseController with Authenticator with ErsConstants {

  val messages = applicationMessages
  val cacheUtil: CacheUtil
  val ersConnector: ErsConnector

  def altActivityPage() = AuthorisedForAsync() {
    implicit user =>
      implicit request =>
        showAltActivityPage()(user, request, hc)
  }

  def showAltActivityPage()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val schemeRef:String = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[GroupSchemeInfo](CacheUtil.GROUP_SCHEME_CACHE_CONTROLLER, schemeRef).flatMap { groupSchemeActivity =>
      cacheUtil.fetch[AltAmendsActivity](CacheUtil.altAmendsActivity, schemeRef).map { altAmendsActivity =>
        Ok(views.html.alterations_activity(altAmendsActivity.altActivity, groupSchemeActivity.groupScheme.getOrElse(PageBuilder.DEFAULT), RSformMappings.altActivityForm.fill(altAmendsActivity)))
      } recover {
        case e: NoSuchElementException => {
          val form = AltAmendsActivity("")
          Ok(views.html.alterations_activity("", groupSchemeActivity.groupScheme.getOrElse(PageBuilder.DEFAULT), RSformMappings.altActivityForm.fill(form)))
        }
      }
    } recover {
      case e: Throwable => {
        Logger.error(s"showAltActivityPage: Get data from cache failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
        getGlobalErrorPage
      }
    }
  }

  def altActivitySelected() = AuthorisedForAsync() {
    implicit user =>
      implicit request =>
        showAltActivitySelected()(user, request, hc)
  }

  def showAltActivitySelected()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val schemeId = try {
      request.session.get(screenSchemeInfo).get.split(" - ").head
    } catch {
      case _:Throwable => {
        return Future(getGlobalErrorPage)
      }
    }
    RSformMappings.altActivityForm.bindFromRequest.fold(
      errors => {
        Future.successful(Ok(views.html.alterations_activity("", "", errors)))
      },
      formData => {
        val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
        cacheUtil.cache(CacheUtil.altAmendsActivity, formData, schemeRef).map { all =>
            formData.altActivity match {
              case PageBuilder.OPTION_NO => Redirect(routes.SummaryDeclarationController.summaryDeclarationPage())
              case PageBuilder.OPTION_YES => Redirect(routes.AltAmendsController.altAmendsPage())
            }
        } recover {
          case e: Throwable => {
            Logger.error(s"showAltActivitySelected: Save data to cache failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
            getGlobalErrorPage
          }
        }
      }
    )
  }

  def altAmendsPage() = AuthorisedForAsync() {
    implicit user =>
      implicit request =>
        showAltAmendsPage()(user, request, hc)
  }

  def showAltAmendsPage()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val schemeRef:String = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
    cacheUtil.fetch[AltAmends](CacheUtil.ALT_AMENDS_CACHE_CONTROLLER, schemeRef).map { altAmends =>
      Ok(views.html.alterations_amends(altAmends))
    } recover {
      case e: NoSuchElementException => Ok(views.html.alterations_amends(AltAmends(None, None, None, None, None)))
    }
  }


  def altAmendsSelected() = AuthorisedForAsync() {
    implicit user =>
      implicit request =>
        showAltAmendsSelected()(user, request, hc)
  }

  def showAltAmendsSelected()(implicit authContext : AuthContext, request: Request[AnyRef], hc: HeaderCarrier): Future[Result] = {
    val schemeId = request.session.get(screenSchemeInfo).get.split(" - ").head
    RSformMappings.altAmendsForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Redirect(routes.AltAmendsController.altAmendsPage()).flashing("alt-amends-not-selected-error" -> PageBuilder.getPageElement(schemeId, PageBuilder.PAGE_ALT_AMENDS, "err.message")))
      },
      formData => {
        val altAmends = AltAmends(
          if (formData.altAmendsTerms != None) formData.altAmendsTerms else Option("0"),
          if (formData.altAmendsEligibility != None) formData.altAmendsEligibility else Option("0"),
          if (formData.altAmendsExchange != None) formData.altAmendsExchange else Option("0"),
          if (formData.altAmendsVariations != None) formData.altAmendsVariations else Option("0"),
          if (formData.altAmendsOther != None) formData.altAmendsOther else Option("0")
        )
        val schemeId = request.session.get(screenSchemeInfo).get.split(" - ").head
        val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
        cacheUtil.cache(CacheUtil.ALT_AMENDS_CACHE_CONTROLLER, altAmends, schemeRef).flatMap { all =>
          if (formData.altAmendsTerms == None
            && formData.altAmendsEligibility == None
            && formData.altAmendsExchange == None
            && formData.altAmendsVariations == None
            && formData.altAmendsOther == None) {
            Future.successful(Redirect(routes.AltAmendsController.altAmendsPage()).flashing("alt-amends-not-selected-error" -> PageBuilder.getPageElement(schemeId, PageBuilder.PAGE_ALT_AMENDS, "err.message")))
          } else {
            Future.successful(Redirect(routes.SummaryDeclarationController.summaryDeclarationPage()))
          }
        } recover {
          case e: Throwable => {
            Logger.error(s"showAltAmendsSelected: Save data to cache failed with exception ${e.getMessage}, timestamp: ${System.currentTimeMillis()}.")
            getGlobalErrorPage
          }
        }
      }
    )
  }

    def getGlobalErrorPage = Ok(views.html.global_error(messages("ers.global_errors.title"), messages("ers.global_errors.heading"), messages("ers.global_errors.message")))

}
