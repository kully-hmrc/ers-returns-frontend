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

package services.pdf

import controllers.Fixtures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.internal.verification.VerificationModeFactory

class GroupSummaryDecoratorSpec extends UnitSpec with MockitoSugar{
  val decorator = new GroupSummaryDecorator("title", Fixtures.ersSummary.companies, 1.0F, 2.0F, 3.0F, 4.0F)

  "GroupSummary Decorator" should {

    "not add anything if companies is not defined" in {
      val decorator = new GroupSummaryDecorator("title", None, 1.0F, 2.0F, 3.0F, 4.0F)
      val streamer = mock[ErsContentsStreamer]
      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(0)).drawText(org.mockito.Matchers.eq("title": String), org.mockito.Matchers.eq(1.0F: Float))
      verify(streamer, VerificationModeFactory.times(0)).drawText(org.mockito.Matchers.eq("": String), org.mockito.Matchers.eq(3.0F: Float))

    }
    "add title to section" in {
      val streamer = mock[ErsContentsStreamer]
      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq("title": String), org.mockito.Matchers.eq(1.0F: Float))
     }

    "add company name to section" in {
      val streamer = mock[ErsContentsStreamer]
      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq("testCompany": String), org.mockito.Matchers.eq(2.0F: Float))
     }

    "add block spacer at the end of the section" in {
      val streamer = mock[ErsContentsStreamer]
      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(3)).drawText(org.mockito.Matchers.eq("": String), org.mockito.Matchers.eq(3.0F: Float))
      verify(streamer, VerificationModeFactory.times(2)).drawText(org.mockito.Matchers.eq("": String), org.mockito.Matchers.eq(4.0F: Float))
      verify(streamer, VerificationModeFactory.times(1)).drawLine()

    }
   }
}
