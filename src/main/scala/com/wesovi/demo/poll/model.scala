package com.wesovi.demo.poll

import argonaut._
import argonaut.Argonaut._
/**
 * @author Ivan 
 */
object model { 
  
  object PollType extends Enumeration{
    val Anonymous,Public,Private = Value
    
    implicit def pollTypeEncoding: EncodeJson[PollType.Value] = jencode1L(
      (value:PollType.Value)=>(value)
    )("value")
  } 

  case class Poll(title:String,pollType:PollType.Value,description:Option[String],options:List[PollOption])
  
  object Poll {
    implicit def pollEncoding: EncodeJson[Poll] = jencode4L(
      (poll:Poll) => (poll.title,poll.pollType, poll.description.get,poll.options) 
    )("title","type","description","options")
  }
  
  case class PollOption(value:String)
  
  object PollOption{
    implicit def pollOptionEncoding: EncodeJson[PollOption] = jencode1L(
      (pollOption:PollOption) => (pollOption.value)
    )("value")
  } 
  
  case class UnexistingPollException(pollId: Long) extends Exception(s"Poll with id pollId is not found.")
  
 
}