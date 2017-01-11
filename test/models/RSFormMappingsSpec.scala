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

import controllers.Fixtures
import models.RSformMappings._
import play.api.libs.json.Json
import org.scalatestplus.play.PlaySpec
import play.api.data.FormError

class rsformMappingsSpec extends PlaySpec {

  "companyDetailsForm" must {
    "return no errors with valid data" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1"
      )
      val validateForm = companyDetailsForm.bind(postData)
      assert(validateForm.errors.isEmpty)
    }
  }

  "companyName" must {
    "return an error if companyName missing" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> "",
        companyDetailsFields.addressLine1 -> "Address Line 1"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.companyName, List("ers_manual_company_details.err.summary.company_name_required"))))
    }

    "return an error if companyName size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> randomString(121),
        companyDetailsFields.addressLine1 -> "Address Line 1"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.companyName, List("ers_manual_company_details.err.company_name"))))
    }

    "return an error if companyName contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> "<script>rm *.*</script>",
        companyDetailsFields.addressLine1 -> "Address Line 1"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.companyName, List("ers_manual_company_details.err.invalidChars.company_name"))))
    }
  }

  "addressLine1" must {
    "return an error if addressLine1 missing" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> ""
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine1, List("ers_manual_company_details.err.summary.address_line1_required"))))
    }

    "return an error if addressLine1 size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> randomString(28)
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine1, List("ers_manual_company_details.err.address_line1"))))
    }

    "return an error if addressLine1 contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "<script>rm *.*</script>"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine1, List("ers_manual_company_details.err.invalidChars.address_line1"))))
    }
  }

  "addressLine2" must {
    "return an error if addressLine2 size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> randomString(28)
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine2, List("ers_manual_company_details.err.address_line2"))))
    }

    "return an error if addressLine2 contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "<script>rm *.*</script>"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine2, List("ers_manual_company_details.err.invalidChars.address_line2"))))
    }
  }

  "addressLine3" must {
    "return an error if addressLine3 size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> randomString(28)
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine3, List("ers_manual_company_details.err.address_line3"))))
    }

    "return an error if addressLine3 contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> "<script>rm *.*</script>"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine3, List("ers_manual_company_details.err.invalidChars.address_line3"))))
    }
  }

  "addressLine4" must {
    "return an error if addressLine4 size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> "Address Line 3",
        companyDetailsFields.addressLine4 -> randomString(19)
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine4, List("ers_manual_company_details.err.address_line4"))))
    }

    "return an error if addressLine4 contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> "Address Line 3",
        companyDetailsFields.addressLine4 -> "<script>rm *.*</script>"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.addressLine4, List("ers_manual_company_details.err.invalidChars.address_line4"))))
    }
  }

  "postCode" must {
    "return an error if postCode size too large" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> "Address Line 3",
        companyDetailsFields.addressLine4 -> "Address Line 4",
        companyDetailsFields.postcode -> randomString(9)
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.postcode, List("ers_manual_company_details.err.postcode"))))
    }

    "return an error if postCode contains invalid chars" in {
      val postData = Json.obj(
        companyDetailsFields.companyName -> Fixtures.companyName,
        companyDetailsFields.addressLine1 -> "Address Line 1",
        companyDetailsFields.addressLine2 -> "Address Line 2",
        companyDetailsFields.addressLine3 -> "Address Line 3",
        companyDetailsFields.addressLine4 -> "Address Line 4",
        companyDetailsFields.postcode -> "??&&$$"
      )
      val validatedForm = companyDetailsForm.bind(postData)
      assert(validatedForm.errors.contains(FormError(companyDetailsFields.postcode, List("ers_manual_company_details.err.postcode"))))
    }
  }

  def randomString(length: Int) = scala.util.Random.alphanumeric.take(length).mkString
}
