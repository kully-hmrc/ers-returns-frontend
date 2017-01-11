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
import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}
import uk.gov.hmrc.play.test.UnitSpec

class PdfDecoratorContorllerFactorySpec extends UnitSpec {

  // a function to get matching an instance to be of certain type
  def anInstanceOf[T](implicit manifest: Manifest[T]) = {
    val clazz = manifest.runtimeClass.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) =
        BePropertyMatchResult(clazz.isAssignableFrom(left.getClass), "an instance of " + clazz.getName)
    }
  }

  "extended pdf scheme decortator factory" should {
    "create new emi scheme decorator when scheme is EMI" in {
     val decorator = PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("emi", Fixtures.ersSummary, None)
      decorator should be(anInstanceOf[DecoratorController])
    }

    "throw invalid argument exception if scheme is not supported" in {
      intercept[IllegalArgumentException] {
        PdfDecoratorControllerFactory.createPdfDecoratorControllerForScheme("blah", Fixtures.ersSummary, None)
      }
    }
 }
}
