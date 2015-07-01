package com.wesovi.demo.poll

import com.twitter.finagle.{Filter, SimpleFilter, Service, Httpx}
import com.twitter.util.{Future, Await}
import _root_.argonaut._
import io.finch.{Endpoint => _, _}
import io.finch.argonaut._
import io.finch.request._
import io.finch.request.items._
import io.finch.response._
import io.finch.route._ 
import com.wesovi.demo.poll.model.UnexistingPollException

/**
 * @author Ivan
 */
class Application extends App {
  import model._

  // Serve the backend using the Httpx protocol.
  val _ = Await.ready(Httpx.serve(":8080", Demo.backend))
}

object Demo {

  import model._
  import endpoint._

  val Secret = "open sesame"

  // A Finagle filter that authorizes a request: performs conversion `HttpRequest` => `AuthRequest`.
  val authorize = new Filter[HttpRequest, HttpResponse, AuthRequest, HttpResponse] {
    def apply(req: HttpRequest, service: Service[AuthRequest, HttpResponse]): Future[HttpResponse] = for {
      `X-Secret` <- headerOption("X-Secret")(req)
      rep <- `X-Secret` collect {
        case Secret => service(AuthRequest(req))
      } getOrElse Unauthorized().toFuture
    } yield rep
  }

  val handleDomainErrors: PartialFunction[Throwable, HttpResponse] = {
    case UnexistingPollException(id) => BadRequest(Json("error" := "poll_not_found", "id" := id))
  }

  val handleRequestReaderErrors: PartialFunction[Throwable, HttpResponse] = {
    case NotPresent(ParamItem(p)) => BadRequest(
      Json("error" := "param_not_present", "param" := p)
    )

    case NotPresent(BodyItem) => BadRequest(Json("error" := "body_not_present"))

    case NotParsed(ParamItem(p), _, _) => BadRequest(
      Json("error" := "param_not_parsed", "param" := p)
    ) 

    case NotParsed(BodyItem, _, _) => BadRequest(Json("error" := "body_not_parsed"))

    case NotValid(ParamItem(p), rule) => BadRequest(
      Json("error" := "param_not_valid", "param" := p, "rule" := rule)
    )
  }

  val handleRouterErrors: PartialFunction[Throwable, HttpResponse] = {
    case RouteNotFound(route) => NotFound(Json("error" := "route_not_found", "route" := route))
  }

  // A simple Finagle filter that handles all the exceptions, which might be thrown by
  // a request reader of one of the REST services.
  val handleExceptions = new SimpleFilter[HttpRequest, HttpResponse] {
    def apply(req: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = service(req) handle
      (handleDomainErrors orElse handleRequestReaderErrors orElse handleRouterErrors orElse {
        case _ => InternalServerError() 
      })
  }

  // An API endpoint.
  val api: Service[AuthRequest, HttpResponse] =
    (createPoll :+: getPoll :+: addPollOption :+: deletePollOption).toService

  // An HTTP endpoint with exception handler and Auth filter.
  val backend: Service[HttpRequest, HttpResponse] =
    handleExceptions ! authorize ! api
}