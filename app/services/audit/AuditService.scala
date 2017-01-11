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

package services.audit


import org.joda.time.DateTime
import play.api.mvc.{Request, Session}
import uk.gov.hmrc.play.audit.http.config.LoadAuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent


import scala.concurrent.ExecutionContext.Implicits.global

trait AuditServiceConnector {
  def auditData(dataEvent : DataEvent)(implicit hc : HeaderCarrier) : Unit
}

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

object AuditServiceConnector extends AuditServiceConnector {


  lazy val auditConnector = MicroserviceAuditConnector

  override def auditData(dataEvent : DataEvent)(implicit hc : HeaderCarrier) : Unit = {
    auditConnector.sendEvent(dataEvent)
  }

}

object AuditService extends AuditService {
  override def auditConnector : AuditServiceConnector = AuditServiceConnector
}

trait AuditService {
  val auditSource = "ers-returns-frontend"

  def auditConnector : AuditServiceConnector

  def sendEvent(transactionName : String, details: Map[String, String])(implicit request: Request[_], hc: HeaderCarrier) =
    auditConnector.auditData(buildEvent(transactionName, details))

  private def buildEvent( transactionName: String,  details: Map[String, String])(implicit request: Request[_], hc: HeaderCarrier) =
    DataEvent(
      auditSource = auditSource,
      auditType = transactionName,
      tags = generateTags(request.session, hc),
      detail = details
    )


  private def generateTags(session: Session, hc: HeaderCarrier): Map[String, String] =
    hc.headers.toMap ++
      hc.headers.toMap ++
      Map("dateTime" ->  getDateTime.toString)



  private def getDateTime = new DateTime

}
