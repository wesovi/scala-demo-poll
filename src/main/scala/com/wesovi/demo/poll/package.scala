package com.wesovi.demo

import io.finch.request.ToRequest

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._

import com.twitter.util.Future

/**
 * @author Ivan
 */
package object poll {
  import model._

  // A custom request type that wraps an `HttpRequest`.
  // We prefer composition over inheritance. 
  case class AuthRequest(http: HttpRequest)

  object AuthRequest {
    implicit val toRequest: ToRequest[AuthRequest] =
      ToRequest[AuthRequest](_.http)
  }
}