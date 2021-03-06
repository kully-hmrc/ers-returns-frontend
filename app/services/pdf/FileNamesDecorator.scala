/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.LegacyI18nSupport
import utils.PageBuilder

import scala.collection.mutable.ListBuffer

class FileNamesDecorator(reportableEvents: String, filesUploaded: Option[ListBuffer[String]], headingFontSize: Float, answerFontSize: Float, lineSpacer: Float, blockSpacer: Float) extends Decorator with LegacyI18nSupport {

  def decorate(streamer: ErsContentsStreamer): Unit = {
    if (reportableEvents == PageBuilder.OPTION_NIL_RETURN)
      return


    if (filesUploaded.get.length == 1) {
      streamer.drawText(Messages("ers_summary_declaration.file_name"), headingFontSize)
    } else {
      streamer.drawText(Messages("ers_summary_declaration.file_names"), headingFontSize)
    }

    streamer.drawText("", lineSpacer)

    filesUploaded.get.foreach { filename =>
      streamer.drawText(filename, answerFontSize)
      streamer.drawText("", lineSpacer)
    }

    streamer.drawText("", blockSpacer)
    streamer.drawLine()
    streamer.drawText("", blockSpacer)

  }
}
