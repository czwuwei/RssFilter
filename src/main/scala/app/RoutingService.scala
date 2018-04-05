package app

import akka.actor.ActorSystem
import akka.event.{LogSource, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Elem, Node, NodeSeq}

/**
  * Created by z00066 on 2017/01/13.
  */
trait RoutingService {

  implicit val system: ActorSystem
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val myLogSourceType: LogSource[RoutingService] = new LogSource[RoutingService] {
    override def genString(a: RoutingService) = a.name

    override def genString(a: RoutingService, s: ActorSystem) = a.name + "," + s
  }

  val name = "Routing Service"
  val logger = Logging(system, this)

  def filtering(text:String, keywords:Seq[String]):Boolean = {
    logger.debug(s"filter $keywords in \n  $text")
    keywords.foldLeft(false) { case (filtered, keyword) =>
        filtered || (text.contains(keyword))
    }
  }

  val routes: Route = {
    pathSingleSlash {
      get {
        complete("<h1>RSS Filter</h1>")
      }
    } ~
    path("filter") {
      get {
        parameters('feed, 'keywords) { (feed, keywords) =>

          val keywordList: Seq[String] = keywords.split(",").toSeq

          logger.debug(s"input origin feed: $feed & keywords:[${keywordList.mkString(",")}]")


          //          val connectionFlow = Http().outgoingConnection("japanese.engadget.com")
          //          val futureFilteredRss: Future[String] = Source.single(HttpRequest.apply(uri = "/rss.xml"))
          //            .via(connectionFlow)
          //            .runWith(Sink.head)
          //            .flatMap { response =>
          //              Unmarshal(response.entity).to[NodeSeq].map { node =>
          //                logger.debug(s" find node [${node}]")
          //                val fRemove = new RewriteRule {
          //                  override def transform(n: Node): Seq[Node] = n match {
          //                    case item: Elem if item.label == "item" => item match {
          //                      case want: Elem if item.text.contains("NFC") => want
          //                      case _ => NodeSeq.Empty
          //                    }
          //                    case other => other
          //                  }
          //                }
          //                new RuleTransformer(fRemove).transform(node)(0).toString()
          //              }
          //            }


          //          val futureFilteredRss:Future[String] = Http().singleRequest(HttpRequest(uri = feed)).flatMap { response =>
          //            logger.debug(s" response status [${response.status}]")
          //            response.entity.toStrict(3.second).flatMap { strictEntity =>
          //              val node: Unmarshal[Strict] = Unmarshal(strictEntity)
          //
          //              node.to[Elem].map { node =>
          //                logger.debug(s" find node [${node}]")
          //                val fRemove = new RewriteRule {
          //                  override def transform(n: Node): Seq[Node] = n match {
          //                    case item: Elem if item.label == "item" => item match {
          //                      case want: Elem if item.text.contains("NFC") => want
          //                      case _ => NodeSeq.Empty
          //                    }
          //                    case other => other
          //                  }
          //                }
          //              }
          //
          //              new RuleTransformer(fRemove).transform(node)(0).toString()
          //
          //            }
          //          }

          val futureFilteredRss = for {
            response <- Http().singleRequest(HttpRequest(uri = feed))
            _ <- Future.successful(logger.debug(s" response status [${response.status}]"))
            node <- Unmarshal(response.entity).to[NodeSeq]
          } yield {
            if (response.status != StatusCodes.OK) {
              logger.debug("not OK response: " + node.toString())
            }

            val fRemove = new RewriteRule {
              override def transform(n: Node): Seq[Node] = n match {
                case item: Elem if item.label == "item" => item match {
                  case want: Elem if filtering(item.text, keywordList) => want
                  case unwant => NodeSeq.Empty
                }
                case other => other
              }
            }

            val filteredNode = new RuleTransformer(fRemove).transform(node)
            logger.debug(s"filteredNode size : ${(filteredNode \\ "item").size}")
            filteredNode(0).toString()
          }

          logger.debug("onComplete")

          onComplete(futureFilteredRss) {
            case Success(frss) =>
              logger.debug(s"success: $frss")
              complete(HttpEntity(ContentTypes.`text/xml(UTF-8)`, frss))
            case Failure(ex) =>
              logger.debug(s"failure: $ex")
              complete(ex.toString)
          }
        }
      }
    }
  }

}
