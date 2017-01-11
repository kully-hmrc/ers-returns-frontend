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

package utils

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object ContentUtil extends ContentUtil

trait ContentUtil {

  implicit val messages = applicationMessages

  def getSchemeName(schemeType: String): String = {
    schemeType match {
      case "1" => messages("ers_pdf_error_report.csop")
      case "2" => messages("ers_pdf_error_report.emi")
      case "4" => messages("ers_pdf_error_report.saye")
      case "5" => messages("ers_pdf_error_report.sip")
      case "3" => messages("ers_pdf_error_report.other")
      case _ => ""
    }
  }

  def getSchemeAbbreviation(schemeType: String): String = {
    schemeType.toLowerCase match {
      case "1" => messages("ers.csop")
      case "2" => messages("ers.emi")
      case "4" => messages("ers.saye")
      case "5" => messages("ers.sip")
      case "3" => messages("ers.other")
      case "csop" => messages("ers.csop")
      case "emi" => messages("ers.emi")
      case "saye" => messages("ers.saye")
      case "sip" => messages("ers.sip")
      case "other" => messages("ers.other")
      case _ => ""
    }
  }

  def geAcknowledgementRef(schemeRef: String): String = {
    System.currentTimeMillis().toString
  }
}
