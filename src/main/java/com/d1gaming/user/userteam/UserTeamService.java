package com.d1gaming.user.userteam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.d1gaming.library.team.Team;
import com.d1gaming.library.team.TeamInviteRequest;
import com.d1gaming.library.team.TeamInviteRequestStatus;
import com.d1gaming.library.team.TeamStatus;
import com.d1gaming.library.user.User;
import com.d1gaming.library.user.UserStatus;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;

@Service
public class UserTeamService {

	@Autowired
	private Firestore firestore;
	
	private final String USERS_COLLECTION = "users";
	private final String TEAMS_COLLECTION = "teams";

	
	private CollectionReference getUsersCollection() {
		return firestore.collection(USERS_COLLECTION);
	}
	
	private CollectionReference getTeamsCollection() {
		return firestore.collection(TEAMS_COLLECTION);
	}
	
	private DocumentReference getUserReference(String userId) {
		return getUsersCollection().document(userId);
	}
	
	private DocumentReference getTeamReference(String teamId) {
		return getTeamsCollection().document(teamId);
	}
	
	private DocumentReference getTeamReferenceByName(String teamName) throws InterruptedException, ExecutionException {
		QuerySnapshot query = getTeamsCollection().whereEqualTo("teamName", teamName).get().get();
		if(!query.isEmpty()) {
			List<Team> teamLs = query.toObjects(Team.class);
			for(Team team : teamLs) {
				return getTeamReference(team.getTeamId());
			}
		}
		return null;
	}
	
	
	private boolean isActive(String userId) throws InterruptedException, ExecutionException {
		DocumentReference reference = getUserReference(userId);
		DocumentSnapshot snapshot = reference.get().get();
		if(snapshot.exists() && snapshot.toObject(User.class).getUserStatusCode().equals(UserStatus.ACTIVE)) {
			return true;
		}
		return false;
	}
	
	private boolean isActiveTeam(String teamId) throws InterruptedException, ExecutionException {
		DocumentReference reference = getTeamReference(teamId);
		DocumentSnapshot snapshot = reference.get().get();
		if(snapshot.exists() && snapshot.toObject(Team.class).getTeamStatus().equals(TeamStatus.ACTIVE)) {
			return true;
		}
		return false;
	}
	
	//Get a User's TEAM by its teamId.
	public Optional<Team> getUserTeamByName(String userId, String teamName) throws InterruptedException, ExecutionException {
		DocumentReference teamReference = getTeamReferenceByName(teamName);
		if(isActive(userId) && isActiveTeam(teamReference.getId())) {
			DocumentReference userReference = getUserReference(userId);
			DocumentSnapshot teamSnapshot = teamReference.get().get();
			DocumentSnapshot userSnapshot = userReference.get().get();
			if(userSnapshot.toObject(User.class).getUserTeams().contains(userSnapshot.toObject(Team.class))) {
				return Optional.of(teamSnapshot.toObject(Team.class));				
			}
			return null;
		}
		return null;
	}
	
	//Get a User's TEAM by its teamName.
	public Optional<Team> getUserTeamById(String userId, String teamId) throws InterruptedException, ExecutionException{
		if(isActive(userId) && isActiveTeam(teamId)) {
			DocumentReference userReference = getUserReference(userId);
			DocumentReference teamReference = getTeamReference(teamId);
			DocumentSnapshot userSnapshot = userReference.get().get();
			DocumentSnapshot teamSnapshot = teamReference.get().get();
			if(userSnapshot.toObject(User.class).getUserTeams().contains(teamSnapshot.toObject(Team.class))) {
				return Optional.of(teamSnapshot.toObject(Team.class));
			}
			return null;
		}
		return null;
	}
	
	//Remove player from team player List, same for userTeamList.
	public String exitTeam(String userId, String teamId) throws InterruptedException, ExecutionException {
		if(isActive(userId) && isActiveTeam(teamId)) {
			DocumentReference userReference = getUserReference(userId);
			DocumentReference teamReference = getTeamReference(userId);
			User user = userReference.get().get().toObject(User.class);
			Team userTeam = teamReference.get().get().toObject(Team.class);
			List<User> teamUsers = userTeam.getTeamUsers();
			List<Team> userTeams = user.getUserTeams();
			if(teamUsers.contains(user) && userTeams.contains(userTeam)) {
				int teamIndex = userTeams.indexOf(userTeam);
				int userIndex = teamUsers.indexOf(user);
				teamUsers.remove(userIndex);
				userTeams.remove(teamIndex);
			}
			WriteBatch batch = firestore.batch();
			batch.update(teamReference, "teamUsers",teamUsers);
			batch.update(userReference, "userTeams", userTeams);
			List<WriteResult> results = batch.commit().get();
			results.forEach(result -> 
					System.out.println("Update Time: " + result.getUpdateTime()));
			if(!userTeams.contains(userTeam) && !teamUsers.contains(user)) {
				return "Team exited successfully.";
			}
			return "Could not exit team.";
		}
		return "Not found.";
	}
	
	public List<Team> getAllUserTeams(String userId) throws InterruptedException, ExecutionException{
		List<Team> userTeams = new ArrayList<>();
		if(isActive(userId)) {
			DocumentReference userReference = getUserReference(userId);
			DocumentSnapshot userSnapshot = userReference.get().get();
			userTeams = userSnapshot.toObject(User.class).getUserTeams();
		}
		return userTeams;
	}
	
	public List<TeamInviteRequest> getAllTeamRequests(String userId) throws InterruptedException, ExecutionException{
		List<TeamInviteRequest> teamInviteRequests = new ArrayList<>();
		if(isActive(userId)) {
			DocumentReference userReference = getUserReference(userId);
			DocumentSnapshot userSnapshot = userReference.get().get();
			teamInviteRequests = userSnapshot.toObject(User.class).getUserTeamRequests();
		}
		return teamInviteRequests;
	}
	
	public String acceptTeamInvite(TeamInviteRequest teamInviteRequest) throws InterruptedException, ExecutionException {
		String response = "Not found.";
		if(isActive(teamInviteRequest.getRequestedUser().getUserId())) {
			DocumentReference userReference = getUserReference(teamInviteRequest.getRequestedUser().getUserId());
			DocumentReference teamReference = getTeamReference(teamInviteRequest.getTeamRequest().getTeamId());
			User user = userReference.get().get().toObject(User.class);
			Team userTeam = teamReference.get().get().toObject(Team.class);
			List<User> teamUsers = userTeam.getTeamUsers();
			List<Team> userTeams = user.getUserTeams();
			WriteBatch batch = firestore.batch();
			if(userTeams.contains(userTeam) && teamUsers.contains(user)) {
				teamInviteRequest.setRequestStatus(TeamInviteRequestStatus.INVALID);
				return "User is already a member of this team.";
			}
			userTeams.add(userTeam);
			teamUsers.add(user);
			batch.update(teamReference, "teamUsers", teamUsers);
			batch.update(userReference, "userTeams", userTeams);
			List<WriteResult> results = batch.commit().get();
			results.forEach(result -> 
					System.out.println("Update Time: " + result.getUpdateTime()));
			if(userTeams.contains(userTeam) && teamUsers.contains(user)) {
				List<TeamInviteRequest> userInviteRequests = user.getUserTeamRequests();
				int requestIndex = userInviteRequests.indexOf(teamInviteRequest);
				if(requestIndex != -1) {
					teamInviteRequest.setRequestStatus(TeamInviteRequestStatus.ACCEPTED);
					userInviteRequests.remove(requestIndex);
					return "Invite accepted successfully.";
				}
				return "Not found.";
			}
			return "Failed.";
		}
		return response;
	}
	
	public String declineTeamInvite(TeamInviteRequest teamInviteRequest) {
		teamInviteRequest.setRequestStatus(TeamInviteRequestStatus.DECLINED); 
		List<TeamInviteRequest> userRequests = teamInviteRequest.getRequestedUser().getUserTeamRequests();
		int requestIndex = userRequests.indexOf(teamInviteRequest);
		String response = "Invite not found.";
		if(requestIndex != -1) {
			userRequests.remove(requestIndex);
			response = "Invite declined.";
		}
		return response;
	}
	
	
}