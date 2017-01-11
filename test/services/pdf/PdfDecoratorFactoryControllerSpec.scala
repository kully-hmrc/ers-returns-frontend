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
import models.{AltAmendsActivity, AlterationAmends, ErsSummary}
import org.joda.time.DateTime
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class PdfDecoratorFactoryControllerSpec extends UnitSpec with MockitoSugar {

  val altAmends = AlterationAmends(altAmendsTerms = Some("1"),
    altAmendsEligibility = Some("1"),
    altAmendsExchange = Some("1"),
    altAmendsVariations = Some("1"),
    altAmendsOther = Some("1")
  )
  val ersSummary = ErsSummary(
    bundleRef = "",
    isNilReturn = "",
    fileType = None,
    confirmationDateTime = new DateTime(2016, 6, 8, 11, 45),
    metaData = Fixtures.EMIMetaData,
    altAmendsActivity = Some(AltAmendsActivity(altActivity = "1")),
    alterationAmends = Some(altAmends),
    groupService = Some(Fixtures.groupScheme),
    schemeOrganiser = Some(Fixtures.schemeOrganiserDetails),
    companies = None,
    trustees = None,
    nofOfRows = None,
    transferStatus = None
  )

  // a function to get matching an instance to be of certain type
  def anInstanceOf[T](implicit manifest: Manifest[T]) = {
    val clazz = manifest.runtimeClass.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) =
        BePropertyMatchResult(clazz.isAssignableFrom(left.getClass), "an instance of " + clazz.getName)
    }
  }

  "when scheme is not defiend pdf decorator factory" should {

    "throw invalid argument" in {
      intercept[IllegalArgumentException] {
        PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("blah", Fixtures.ersSummary, None)
      }
    }
  }

  "When scheme is emi, pdf decorator controller factory" should {

    "add 5 decorators" in {

      val ersSummary = ErsSummary("testbundle", "1", None, new DateTime(2016, 6, 8, 11, 45), metaData = Fixtures.EMIMetaData, None, None,
        Some(Fixtures.groupScheme), Some(Fixtures.schemeOrganiserDetails), Some(Fixtures.companiesList), None, None,None)
      val decoratorController = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("emi", ersSummary, None)

      val decorators = decoratorController.getDecorators

      decoratorController.getNumberOfDecorator shouldEqual 5
      decorators(0) should be(anInstanceOf[YesNoDecorator])
      decorators(1) should be(anInstanceOf[FileNamesDecorator])
      decorators(2) should be(anInstanceOf[SchemeOrganiserDetailseDecorator])
      decorators(3) should be(anInstanceOf[YesNoDecorator])
      decorators(4) should be(anInstanceOf[GroupSummaryDecorator])
    }
  }

  "when scheme is csop, pdf decorator controller factory" should {

    "add 7 decorators" in {

      val decoratorController = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("csop", ersSummary, None)
      val decorators = decoratorController.getDecorators

      decoratorController.getNumberOfDecorator shouldEqual 7
      decorators(0) should be(anInstanceOf[YesNoDecorator])
      decorators(1) should be(anInstanceOf[FileNamesDecorator])
      decorators(2) should be(anInstanceOf[SchemeOrganiserDetailseDecorator])
      decorators(3) should be(anInstanceOf[YesNoDecorator])
      decorators(4) should be(anInstanceOf[GroupSummaryDecorator])
      decorators(5) should be(anInstanceOf[YesNoDecorator])
      decorators(6) should be(anInstanceOf[AlterationsAmendsDecorator])
    }
  }

  "when scheme is sip, pdf decorator controller factory" should {

    "add 8 decorators" in {

      val decoratorController = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("sip", ersSummary, None)
      val decorators = decoratorController.getDecorators

      decoratorController.getNumberOfDecorator shouldEqual 8
      decorators(0) should be(anInstanceOf[YesNoDecorator])
      decorators(1) should be(anInstanceOf[FileNamesDecorator])
      decorators(2) should be(anInstanceOf[SchemeOrganiserDetailseDecorator])
      decorators(3) should be(anInstanceOf[YesNoDecorator])
      decorators(4) should be(anInstanceOf[GroupSummaryDecorator])
      decorators(5) should be(anInstanceOf[TrusteesDecorator])
      decorators(6) should be(anInstanceOf[YesNoDecorator])
      decorators(7) should be(anInstanceOf[AlterationsAmendsDecorator])
    }
  }

  "when scheme is saye, pdf decorator controller factory" should {

    "add 6 decorators" in {

      val decoratorController = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("saye", ersSummary, None)
      val decorators = decoratorController.getDecorators

      decoratorController.getNumberOfDecorator shouldEqual 6
      decorators(0) should be(anInstanceOf[YesNoDecorator])
      decorators(1) should be(anInstanceOf[FileNamesDecorator])
      decorators(2) should be(anInstanceOf[SchemeOrganiserDetailseDecorator])
      decorators(3) should be(anInstanceOf[YesNoDecorator])
      decorators(4) should be(anInstanceOf[YesNoDecorator])
      decorators(5) should be(anInstanceOf[AlterationsAmendsDecorator])
    }
  }

  "when scheme is other, pdf decorator controller factory" should {

    "add 5 decorators" in {

      val decoratorController = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("other", ersSummary, None)
      val decorators = decoratorController.getDecorators

      decoratorController.getNumberOfDecorator shouldEqual 5
      decorators(0) should be(anInstanceOf[YesNoDecorator])
      decorators(1) should be(anInstanceOf[FileNamesDecorator])
      decorators(2) should be(anInstanceOf[SchemeOrganiserDetailseDecorator])
      decorators(3) should be(anInstanceOf[YesNoDecorator])
      decorators(4) should be(anInstanceOf[GroupSummaryDecorator])
     }
  }

    "pdf decorator controller factory" should {
    "map given alt amends for given schemes" in {

      Array("csop", "sip", "saye").map { scheme =>
        val mappedAltAmends = PdfDecoratorControllerFactory.createAltAmendOptionsFor(ersSummary, scheme)

        mappedAltAmends("title") shouldEqual (Messages("ers_trustee_summary.altamends.section"))
        mappedAltAmends("option1") shouldEqual (Messages(s"ers_alt_amends.${scheme}.option_1"))
        mappedAltAmends("option2") shouldEqual (Messages(s"ers_alt_amends.${scheme}.option_2"))
        mappedAltAmends("option3") shouldEqual (Messages(s"ers_alt_amends.${scheme}.option_3"))
        mappedAltAmends("option4") shouldEqual (Messages(s"ers_alt_amends.${scheme}.option_4"))
        mappedAltAmends("option5") shouldEqual (Messages(s"ers_alt_amends.${scheme}.option_5"))
      }
    }
  }
}
