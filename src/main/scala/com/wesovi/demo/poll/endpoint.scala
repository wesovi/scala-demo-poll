package com.wesovi.demo.poll
  

import com.wesovi.demo.poll.model.{Poll,PollOption}
import com.wesovi.demo.poll.service._
import io.finch.route._


/**
 * @author Ivan 
 */ 
object endpoint {
  val createPoll: Endpoint[AuthRequest,Poll] = post("polls") /> CreatePoll
  val getPoll: Endpoint[AuthRequest,Poll] = get("polls" / long) /> GetPoll
  val addPollOption: Endpoint[AuthRequest,PollOption]= post("polls" / long / "options") /> AddPollOption
  val deletePollOption: Endpoint[AuthRequest,PollOption]= delete("polls" / long / "options" / long) /> DeletePollOption
} 