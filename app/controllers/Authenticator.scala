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

import models.ErsMetaData
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{EpayeAccount, Accounts}
import uk.gov.hmrc.play.frontend.auth._
import utils.CacheUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Authenticator extends Actions  with ErsConstants{


  private val cacheUtil: CacheUtil = CacheUtil
  private type AsyncUserRequest = AuthContext => Request[AnyContent] => Future[Result]
  private type UserRequest = AuthContext => Request[AnyContent] => Result

  def AuthorisedForAsync()(body: AsyncUserRequest): Action[AnyContent] = {
    AuthorisedFor(ERSRegime, pageVisibility = GGConfidence).async {
      implicit user => implicit request => {
        implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
        FilterAgentsWrapperAsync(user, body)
      }
    }
  }

  def AuthorisedFor(body: UserRequest): Action[AnyContent] = {
    AuthorisedFor(ERSRegime, pageVisibility = GGConfidence).async {
      implicit user => implicit request =>
        implicit val hc = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
        FilterAgentsWrapper(user, body)
    }
  }

  /**
   * stil to do:
   * refactor the sso endpoint to allow for passing an url
   * @param authContext
   * @param request
   * @param body
   * @return
   */
  def FilterAgentsWrapper(authContext: AuthContext, body: UserRequest)(implicit hc: HeaderCarrier, request: Request[AnyContent]): Future[Result] = {

    implicit val formatRSParams = Json.format[ErsMetaData]

    val defined = authContext.principal.accounts.agent.isDefined

    if (defined) {
      val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
      cacheUtil.fetch[ErsMetaData](CacheUtil.ersMetaData, schemeRef).map { all =>
        body(delegationModelUser(all, authContext: AuthContext))(request)
      }
    } else {

      Future {
        body(authContext)(request)
      }
    }
  }

  /**
   * look at FilterAgentsWrapper
   * @param authContext
   * @param request
   * @param body
   * @return
   */
  def FilterAgentsWrapperAsync(authContext:AuthContext, body: AsyncUserRequest)(implicit hc: HeaderCarrier,
                                  request: Request[AnyContent]): Future[Result] = {

    implicit val formatRSParams = Json.format[ErsMetaData]

    val defined = authContext.principal.accounts.agent.isDefined

    if (defined) {
      val schemeRef = cacheUtil.getSchemeRefFromScreenSchemeInfo(request.session.get(screenSchemeInfo).get)
      cacheUtil.fetch[ErsMetaData](CacheUtil.ersMetaData, schemeRef).flatMap { all =>
        body(delegationModelUser(all, authContext: AuthContext))(request)
      }
    } else {

      body(authContext)(request)
    }
  }

  def delegationModelUser(metaData:ErsMetaData, authContext: AuthContext): AuthContext =
  {
    val empRef: String = metaData.empRef
    val twoPartKey = empRef.split('/')
    val accounts =  Accounts(agent = authContext.principal.accounts.agent,
      epaye = Some(EpayeAccount(s"/epaye/$empRef", EmpRef(twoPartKey(0), twoPartKey(1)))))


    AuthContext(authContext.user,Principal(authContext.principal.name, accounts),authContext.attorney, None, None, None)
  }

}
