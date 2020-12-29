package com.d1gaming.user.userteam;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.d1gaming.library.team.Team;
import com.d1gaming.library.team.TeamInviteRequest;

@RestController
@CrossOrigin(origins = "localhost:4200")
@RequestMapping(value = "userteamapi")
public class UserTeamController {

	@Autowired
	private UserTeamService userTeamService;
	
	@GetMapping(value = "/userTeamRequests")
	public ResponseEntity<?> getAllUserTeamRequests(@RequestParam(required = true)String userId) throws InterruptedException, ExecutionException{
		List<TeamInviteRequest> userTeamRequests = userTeamService.getAllTeamRequests(userId);
		if(userTeamRequests.isEmpty()) {
			return new ResponseEntity<>(userTeamRequests, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(userTeamRequests, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/userTeams")
	public ResponseEntity<?> getAllUserTeams(@RequestParam(required = true)String userId) throws InterruptedException, ExecutionException{
		List<Team> userTeams = userTeamService.getAllUserTeams(userId);
		if(userTeams.isEmpty()) {
			return new ResponseEntity<>(userTeams, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(userTeams, HttpStatus.OK);
	}
	
	@GetMapping(value = "/userTeams/search", params = "userId")
	public ResponseEntity<?> getUserTeamById(@RequestParam(required = true)String userId,
										     @RequestParam(required = true)String teamId) throws InterruptedException, ExecutionException{
		Optional<Team> userTeam = userTeamService.getUserTeamById(userId, teamId);
		if(userTeam == null) {
			return new ResponseEntity<>("Not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(userTeam.get(), HttpStatus.OK);
	}
	
	@GetMapping(value = "/userTeams/search", params="userName")
	public ResponseEntity<?> getUserTeamByName(@RequestParam(required = true)String userId,
											   @RequestParam(required = true)String teamName) throws InterruptedException, ExecutionException{
		Optional<Team> userTeam = userTeamService.getUserTeamByName(userId, teamName);
		if(userTeam == null) {
			return new ResponseEntity<>("Not found.", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(userTeam.get(), HttpStatus.OK);
	}
	
	@PostMapping(value = "/userTeams/exit")  
	public ResponseEntity<?> exitTeam(@RequestParam(required = true)String userId,
									  @RequestParam(required = true)String teamId) throws InterruptedException, ExecutionException{
		String response = userTeamService.exitTeam(userId, teamId);
		if(response.equals("Not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		if(response.equals("Could not exit team.")) {
			return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping(value = "/userTeamRequests/accept")
	public ResponseEntity<?> acceptUserTeamRequest(@RequestBody(required = true)TeamInviteRequest request) throws InterruptedException, ExecutionException{
		String response = userTeamService.acceptTeamInvite(request);
		switch(response) {
			case "Not found.":
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
			case "User is already a member of this team.":
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			case "Failed.":
				return new ResponseEntity<>(response, HttpStatus.EXPECTATION_FAILED);
			default:
				return new ResponseEntity<>(response, HttpStatus.OK);
		}
	}
	
	@PostMapping(value = "/userTeamRequests/decline")
	public ResponseEntity<?> declineUserTeamRequest(@RequestBody(required = true)TeamInviteRequest request){
		String response = userTeamService.declineTeamInvite(request);
		if(response.equals("Invite not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}	
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
}
