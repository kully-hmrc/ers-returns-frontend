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

import models._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{EmpRef, Generator, SaUtr, Vrn}
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.domain._

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration._

object Fixtures {


  val firstName = "FirstName"
  val middleName = "MiddleName"
  val surname = "Surname"
  val userId = "userId"
  val nino: String = new Generator().nextNino.nino
  val companyName = "Company Name"
  val legacyOid = "legacyOid"
  val sixMonths = 6

  def getAwaitDuration: Duration = 60 seconds

  val saAccount = Some(SaAccount("", SaUtr("1234567890")))
  val vatAccount = Some(VatAccount("", Vrn("123456789")))
  val epayeAccount = Some(EpayeAccount("", EmpRef("000", "AA00000")))

  val accounts1 = Accounts(epaye = Some(EpayeAccount(s"/epaye/empRef", EmpRef("ABC", "1234"))))
  val accounts2 = Accounts(sa = saAccount, vat = vatAccount, epaye = epayeAccount)

  val governmentGatewayToken = "governmentGatewayToken"

  val loggedInUser = LoggedInUser(userId,
    Some(DateTime.now),
    Some(DateTime.now),
    Some(governmentGatewayToken),
    CredentialStrength.Strong,
    ConfidenceLevel.L500,
    legacyOid)

  val principal = Principal(Some("name"), accounts1)

  val authority = Authority(userId, accounts2, None, None, CredentialStrength.Strong, ConfidenceLevel.L50, None, None, None, legacyOid)

  val attorney = Attorney("name", Link("url", "text"))

  def buildFakeUser = AuthContext(loggedInUser, principal, Some(attorney), None, None, None)

  val baseSession = List("sessionId" -> "FAKE_SESSION_ID")
  val sessionWithId: List[(String, String)] = baseSession
  val sessionWithIdAndCsop: List[(String, String)] = ("screenSchemeInfo" -> "1 - CSOP - MYScheme - XX12345678 - 2016") :: baseSession
  val sessionWithIdAndSaye: List[(String, String)] = ("screenSchemeInfo" -> "4 - SAYE - MYScheme - XX12345678 - 2016") :: baseSession
  val sessionWithIdAndSip: List[(String, String)] = ("screenSchemeInfo" -> "5 - SIP - MYScheme - XX12345678 - 2016") :: baseSession
  val sessionWithIdAndEmi: List[(String, String)] = ("screenSchemeInfo" -> "2 - EMI - MYScheme - XX12345678 - 2016") :: baseSession

  val sessionWithIdAndOther: List[(String, String)] = ("screenSchemeInfo" -> "3 - OTHER - MYScheme - XX12345678 - 2016") :: baseSession

  def buildFakeRequestWithSessionId(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithId: _*)

  def buildFakeRequestWithSessionIdCSOP(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithIdAndCsop: _*)

  def buildFakeRequestWithSessionIdSAYE(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithIdAndSaye: _*)

  def buildFakeRequestWithSessionIdSIP(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithIdAndSip: _*)

  def buildFakeRequestWithSessionIdEMI(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithIdAndEmi: _*)

  def buildFakeRequestWithSessionIdOTHER(method: String): FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(sessionWithIdAndOther: _*)

  def buildFakeRequest(method: String) = FakeRequest()

  def fetchAndGetMockScheme(): Future[Option[String]] = Future.successful(Option("1"))

  def schemeRef: String = {
    "XYZ12345"
  }

  val timestamp: DateTime = DateTime.now

  val schemeType = "EMI"

  val EMISchemeInfo: SchemeInfo = SchemeInfo(
    schemeRef = "XA1100000000000",
    timestamp = timestamp,
    schemeId = "123AA12345678",
    taxYear = "2014/15",
    schemeName = "My scheme",
    schemeType = schemeType
  )

  val EMIMetaData = ErsMetaData(
    schemeInfo = EMISchemeInfo,
    ipRef = "127.0.0.0",
    aoRef = Some("123AA12345678"),
    empRef = "EMI - MyScheme - XA1100000000000 - 2014/15",
    agentRef = None,
    sapNumber = Some("sap-123456")
  )

  val data: Option[ListBuffer[Seq[String]]] = Some(
    ListBuffer(
      Seq("no", "no", "yes", "3", "2015-12-09", firstName, "", surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234"),
      Seq("no", "no", "no", "", "2015-12-09", firstName, "", surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234"),
      Seq("yes", "", "", "", "2015-12-09", firstName, middleName, surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234")
    )
  )

  val invalidJson: JsObject = Json.obj("metafield1" -> "metavalue1", "metafield2" -> "metavalue2", "metafield3" -> "metavalue3")

  val schemeOrganiserDetails: SchemeOrganiserDetails = SchemeOrganiserDetails(
    "companyName",
    "addressLine1",
    None,
    None,
    None,
    None,
    None,
    None,
    Some("corporationRef")
  )

  val companyDetails: CompanyDetails = CompanyDetails(
    "testCompany",
    "testAddress1",
    Some("testAddress2"),
    Some("testAddress3"),
    Some("testAddress4"),
    Some("AA1 1AA"),
    Some("United Kingdom"),
    Some("1234567890"),
    Some("1234567890")
  )

  val groupScheme = GroupSchemeInfo(Some("1"), Some("emi"))

  val companiesList = CompanyDetailsList(List(companyDetails))

  val ersSummary = ErsSummary("testbundle",
    "1",
    None,
    timestamp.minusMonths(sixMonths),
    metaData = EMIMetaData,
    None,
    None,
    Some(groupScheme),
    Some(schemeOrganiserDetails),
    Some(companiesList),
    None,
    None,
    None)

  val metadataJson: JsObject = Json.toJson(EMIMetaData).as[JsObject]
}


