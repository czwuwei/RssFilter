package app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn
import com.typesafe.config.ConfigFactory

object Boot extends App {
  println("Start Web Server...")
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val server = new WebServer()

  val config = ConfigFactory.load
  val port: Int = config.getInt("http.port")

  val bindingFuture = Http().bindAndHandle(server.routes, "0.0.0.0", port)

  println(s"Server online at http://localhost:${port}/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
