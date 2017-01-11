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

class AlterationsAmendsDecorator(map: Map[String, String], headingFontSize: Float, answerFontSize: Float, lineSpacer: Float, blockSize: Float) extends Decorator {

  private def addTextToPdf(streamer: ErsContentsStreamer, text: String, fontSize: Float, lineSpacer: Float): Unit = {
    streamer.drawText(text, fontSize)
    streamer.drawText("", lineSpacer)
  }

  def decorate(streamer: ErsContentsStreamer): Unit = {

    if(map.isEmpty)
      return

    addTextToPdf(streamer, map("title"), headingFontSize, lineSpacer)

    Array("option1", "option2", "option3", "option4", "option5").map { key =>
      if(map.contains(key))
        addTextToPdf(streamer, s"${map(key)}.", answerFontSize, lineSpacer)
    }

    streamer.drawText("", blockSize)
    streamer.drawLine()
    streamer.drawText("", blockSize)
  }
}
