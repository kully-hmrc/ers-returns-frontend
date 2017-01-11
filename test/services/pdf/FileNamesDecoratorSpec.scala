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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.internal.verification.VerificationModeFactory
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import scala.collection.mutable.ListBuffer

class FileNamesDecoratorSpec extends UnitSpec with MockitoSugar {

  "file name decortor" should {
    "not show file names when nil reuturn is false" in {
      val decorator = new FileNamesDecorator("2", Some(ListBuffer[String]()), 0.0F, 0.0F, 0.0F, 0.0F)
      val streamer = mock[ErsContentsStreamer]

      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(0)).drawText(any(), any())
     }

    "show ods files names when nil return is true" in {
      val streamer = mock[ErsContentsStreamer]
      val decorator = new FileNamesDecorator("1", Some(ListBuffer[String]("odsFile")), 1.0F, 2.0F, 3.0F, 4.0F)

      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq(Messages("ers_summary_declaration.file_name"): String), org.mockito.Matchers.eq(1.0F: Float))
      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq("odsFile": String), org.mockito.Matchers.eq(2.0F: Float))
    }

    "show csv files names when nil return is true" in {
      val streamer = mock[ErsContentsStreamer]
      val decorator = new FileNamesDecorator("1", Some(ListBuffer[String]("csvFile0", "csvFile1")), 1.0F, 2.0F, 3.0F, 4.0F)

      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq(Messages("ers_summary_declaration.file_names"): String), org.mockito.Matchers.eq(1.0F: Float))
      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq("csvFile0": String), org.mockito.Matchers.eq(2.0F: Float))
      verify(streamer, VerificationModeFactory.times(1)).drawText(org.mockito.Matchers.eq("csvFile1": String), org.mockito.Matchers.eq(2.0F: Float))
    }

    "show block spacer at the end" in {
      val streamer = mock[ErsContentsStreamer]
      val decorator = new FileNamesDecorator("1", Some(ListBuffer[String]("odsFile")), 1.0F, 2.0F, 3.0F, 4.0F)

      decorator.decorate(streamer)

      verify(streamer, VerificationModeFactory.times(2)).drawText(org.mockito.Matchers.eq("": String), org.mockito.Matchers.eq(4.0F: Float))
      verify(streamer, VerificationModeFactory.times(1)).drawLine()
    }
   }
}
