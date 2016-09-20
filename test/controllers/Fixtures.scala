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

import models._
import org.joda.time.DateTime
import org.scalatest.Suite
import play.api.libs.json._
import play.api.test.{FakeApplication, FakeRequest, WithApplication}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.play.frontend.auth._
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel.L500
import uk.gov.hmrc.play.frontend.auth.connectors.domain._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.WithFakeApplication
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.concurrent.duration._
import uk.gov.hmrc.domain.Generator

object Fixtures {
	val firstName = "FirstName"
	val middleName = "MiddleName"
	val surname = "Surname"
	val nino = new Generator().nextNino.nino
	val companyName = "Company Name"

	def getAwaitDuration(): Duration = {
		60 seconds
	}

	val accounts =  Accounts(None,		epaye = Some(EpayeAccount(s"/epaye/empRef", EmpRef("ABC", "1234"))))
	val loggedInuser = LoggedInUser("userId", Some(DateTime.now), Some(DateTime.now), Some("governmentGatewayToken"), CredentialStrength.Strong, L500)
	def buildFakeUser = AuthContext(loggedInuser,Principal(Some("name"), accounts),Some(Attorney("name", Link("url", "text"))))

	def buildFakeRequestWithSessionId(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "2 - EMI - MYScheme - XX12345678 - 2016"))
	def buildFakeRequestWithSessionIdCSOP(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "1 - CSOP - MYScheme - XX12345678 - 2016"))
	def buildFakeRequestWithSessionIdSAYE(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "4 - SAYE - MYScheme - XX12345678 - 2016"))
	def buildFakeRequestWithSessionIdSIP(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "5 - SIP - MYScheme - XX12345678 - 2016"))
	def buildFakeRequestWithSessionIdEMI(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "2 - EMI - MYScheme - XX12345678 - 2016"))
	def buildFakeRequestWithSessionIdOTHER(method: String) = FakeRequest().withSession(("sessionId" -> "FAKE_SESSION_ID"),("screenSchemeInfo" -> "3 - OTHER - MYScheme - XX12345678 - 2016"))
	def buildFakeRequest(method: String) = FakeRequest()

	def fetchAndGetMockScheme(): Future[Option[String]] = {
		Future.successful(Option("1"))
	}

	def schemeRef : String = {"XYZ12345"}

	val timestamp: DateTime = DateTime.now

	val schemeType = "EMI"

	val EMISchemeInfo: SchemeInfo = SchemeInfo (
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

	val scheetName: String = "EMI40_Adjustments_V3"
	val data: Option[ListBuffer[Seq[String]]] = Some(
		ListBuffer(
			Seq("no", "no", "yes", "3", "2015-12-09", firstName, "", surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234"),
			Seq("no", "no", "no", "", "2015-12-09", firstName, "", surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234"),
			Seq("yes", "", "", "", "2015-12-09", firstName, middleName, surname, nino, "123/XZ55555555", "10.1234", "100.12", "10.1234", "10.1234")
		)
	)

	val invalidJson: JsObject = Json.obj(
		"metafield1" -> "metavalue1",
		"metafield2" -> "metavalue2",
		"metafield3" -> "metavalue3"
	)

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

	val groupScheme  = GroupSchemeInfo(Some("1"), Some("emi"))

  val companiesList = CompanyDetailsList(List(companyDetails))
	val ersSummary = ErsSummary("testbundle", "1", None, new DateTime(2016,6,8,11,5), metaData = EMIMetaData, None, None, Some(groupScheme), Some(schemeOrganiserDetails),
		Some(companiesList), None,None,None)

	val metadataJson: JsObject = Json.toJson(EMIMetaData).as[JsObject]

}

abstract class WithErsSetup extends WithApplication(FakeApplication(additionalConfiguration = Map(
	"application.secret" -> "test",
	"govuk-tax.Test.login-callback.url" -> "test"))) {
	implicit val hc = HeaderCarrier()
}

trait ERSFakeApplication extends WithFakeApplication {
	this: Suite =>

	implicit val hc = HeaderCarrier()

	val config = Map("application.secret" -> "test",
		"govuk-tax.Test.login-callback.url" -> "test",
		"govuk-tax.Test.services.contact-frontend.host" -> "test",
		"govuk-tax.Test.services.contact-frontend.port" -> "9250",
		"govuk-tax.Test.assets.url" -> "test",
		"govuk-tax.Test.assets.version" -> "version",
		"Test.external-url.contact-frontend.host" -> "test",
		"Test.external-url.contact-frontend.host" -> "test",
		"Test.external-url.tai-frontend.host" -> "test",
		"govuk-tax.Test.login-callback.url" -> "test",
		"govuk-tax.Test.services.contact-frontend.host" -> "localhost",
		"govuk-tax.Test.services.contact-frontend.port" -> "9250",
		"metrics.enabled" -> "false")

	override lazy val fakeApplication = FakeApplication(additionalConfiguration = config)

}
