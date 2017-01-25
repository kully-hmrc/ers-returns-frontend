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

import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Fixtures

import scala.concurrent.Future

trait ERSUsers {
  implicit val mockAuthConnector: AuthConnector
  def withAuthorisedUser(test: FakeRequest[AnyContentAsEmpty.type] => Any) {
    when(mockAuthConnector.currentAuthority(Matchers.any[HeaderCarrier]())) thenReturn Future.successful(Some(Fixtures.authority))
    test(Fixtures.buildFakeRequestWithSessionIdEMI("POST"))
  }
}
