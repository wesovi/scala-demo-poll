package com.wesovi.demo.poll

import com.twitter.finagle.Service
import com.twitter.util.Future

import io.finch._

/**
 * @author Ivan
 */
object service {
  
  import model._  
   
  object CreatePoll extends Service[AuthRequest,Poll]{
    def apply(req:AuthRequest):Future[Poll] = for{
      in <- poll(req.http)
      out <-Db.insert(in.id,in)
    } yield out
  }  
  
  case class GetPoll(pollId:Long) extends Service[AuthRequest,Poll]{
    def apply(req: AuthRequest): Future[Poll] = Db.select(pollId) flatMap {
      case Some(user) => user.toFuture
      case None => UnexistingPollException(pollId).toFutureException[Poll]
    }
  } 
  
  case class AddPollOption(pollId:Long) extends Service[AuthRequest,PollOption]{
    def apply(req: AuthRequest): Future[Poll] = for {
      pollOption <- pollOption(req.http)
      poll <- GetPoll(pollId)(req) // fetch exist user
      updatedU = poll.copy(options = poll.options :+ pollOption)
      _ <- Db.insert(poll, updatedU)
    } yield poll
  }
  
  case class DeletePollOption(pollId:Long,pollOptionId:Long) extends Service[AuthRequest,Poll]{
    def apply(req: AuthRequest): Future[Poll] = for {
      pollOption <- pollOption(req.http)
      poll <- GetPoll(pollId)(req) // fetch exist user
      updatedU = poll.copy(options = poll.options :- pollOption)
      _ <- Db.insert(poll, updatedU)
    } yield poll
  }
}