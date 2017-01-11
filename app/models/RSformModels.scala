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

package models

import play.api.libs.json.Json

case class RS_scheme(scheme: String)

case class ReportableEvents(isNilReturn: Option[String])
object ReportableEvents {
  implicit val format = Json.format[ReportableEvents]
}

case class CheckFileType(checkFileType: Option[String])
object CheckFileType {
  implicit val format = Json.format[CheckFileType]
}

case class RS_schemeType (schemeType: String)

case class RS_groupSchemeType(groupSchemeType: String)

case class RS_groupScheme(groupScheme: Option[String])
object RS_groupScheme {
  implicit val format = Json.format[RS_groupScheme]
}

case class AltAmendsActivity(altActivity: String)
object AltAmendsActivity {
  implicit val format = Json.format[AltAmendsActivity]
}

case class AltAmends(
                         altAmendsTerms: Option[String],
                         altAmendsEligibility: Option[String],
                         altAmendsExchange: Option[String],
                         altAmendsVariations: Option[String],
                         altAmendsOther: Option[String]
                         )
object AltAmends {
  implicit val format = Json.format[AltAmends]
}

case class SchemeOrganiserDetails(
                                      companyName: String,
                                      addressLine1: String,
                                      addressLine2: Option[String],
                                      addressLine3: Option[String],
                                      addressLine4: Option[String],
                                      country: Option[String],
                                      postcode: Option[String],
                                      companyReg: Option[String],
                                      corporationRef: Option[String]
                                      )
object SchemeOrganiserDetails {
  implicit val format = Json.format[SchemeOrganiserDetails]
}

case class TrusteeDetails(
                              name: String,
                              addressLine1: String,
                              addressLine2: Option[String],
                              addressLine3: Option[String],
                              addressLine4: Option[String],
                              country: Option[String],
                              postcode: Option[String]
                              )
object TrusteeDetails {
  implicit val format = Json.format[TrusteeDetails]
}


case class TrusteeDetailsList(trustees: List[TrusteeDetails])
object TrusteeDetailsList {
  implicit val format = Json.format[TrusteeDetailsList]
}

case class CsvFiles(fileId: String, isSelected: Option[String])
object CsvFiles {
  implicit val format = Json.format[CsvFiles]
}

case class CsvFilesList(files: List[CsvFiles])
object CsvFilesList {
  implicit val format = Json.format[CsvFilesList]
}

case class RequestObject(
                          aoRef: Option[String],
                          taxYear: Option[String],
                          ersSchemeRef: Option[String],
                          schemeName: Option[String],
                          schemeType: Option[String],
                          agentRef: Option[String],
                          empRef: Option[String],
                          ts: Option[String],
                          hmac: Option[String]
                          ) {

  def getAORef() = aoRef.get

  def getTaxYear() = taxYear.get

  def getSchemeReference() = ersSchemeRef.get

  def getSchemeName() = schemeName.get

  def getSchemeType() = schemeType.get

  def getAgentRef() = agentRef.get

  def getEmpRef() = empRef.getOrElse("")

  def getTS() = ts.getOrElse("")

  def getHMAC() = hmac.getOrElse("")

  def concatenateParameters() = {
    getNVPair("agentRef", agentRef) +
      getNVPair("aoRef", aoRef) +
      getNVPair("empRef", empRef) +
      getNVPair("ersSchemeRef", ersSchemeRef) +
      getNVPair("schemeName", schemeName) +
      getNVPair("schemeType", schemeType) +
      getNVPair("taxYear", taxYear) +
      getNVPair("ts", ts)
  }

  private def getNVPair(paramName: String, value: Option[String]): String = {

    try {
      val tempValue = value.get
      (paramName + "=" + value.get + ";")
    } catch {
      case ne: java.util.NoSuchElementException => {
        ""
      }
    }
  }
}
