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


import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import models.RequestObject
import org.apache.commons.codec.binary.Base64
import org.joda.time.{Seconds, DateTime}

object HMACUtil extends HMACUtil {
}

trait HMACUtil {

  val TIME_RANGE: Int = 300 // seconds

  def verifyHMAC(urlParams: RequestObject): Boolean = {

    val generatedHMAC: Array[Byte] = sha1Bytes(urlParams.concatenateParameters())
    val urlParamsHMAC: Array[Byte] = Base64.decodeBase64(urlParams.getHMAC)

    val hmacIsEqual: Boolean = java.util.Arrays.equals(generatedHMAC, urlParamsHMAC)

    if (hmacIsEqual) {
      return true
    }

    return false
  }

  def decodeSecretKey(s: String): SecretKey = {

    val encodedKey = Base64.decodeBase64(s)
    val originalKey: SecretKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "HmacSHA1")
    originalKey
  }

  def sha1Bytes(s: String): Array[Byte] = {

    val mac: Mac = Mac.getInstance("HmacSHA1")
    mac.init(decodeSecretKey(ExternalUrls.hmacToken))
    val bytes = mac.doFinal(s.getBytes("UTF-8"))
    bytes
  }

  def timeIsValid(urlParams: RequestObject): Boolean = {
    try {
      val longTime: Long = urlParams.getTS().toLong * 1000
      val urlTime: DateTime = new DateTime(longTime)
      val now: DateTime = DateTime.now()
      val diff: Int = Seconds.secondsBetween(urlTime, now).getSeconds

      if (diff <= TIME_RANGE) {
        return true
      }
    }
    catch {
      case ne: NumberFormatException => {}
    }

    false
  }

  def isHmacAndTimestampValid(requestObject: RequestObject): Boolean = {

    if (ExternalUrls.hmacOnSwitch.toBoolean) {

      if (HMACUtil.timeIsValid(requestObject) && HMACUtil.verifyHMAC(requestObject)) {
        true
      }
      else {
        false
      }
    }
    else {
      true
    }
  }

}
