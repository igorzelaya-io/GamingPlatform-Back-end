package com.d1gaming.user.userteam;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.d1gaming.library.team.Team;
import com.d1gaming.library.team.TeamInviteRequest;
import com.d1gaming.library.team.TeamInviteRequestStatus;
import com.d1gaming.library.user.User;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;

@Service
public class UserTeamService {

	private final String USERS_COLLECTION = "users";
	private final String TEAMS_COLLECTION = "teams";

	@Autowired
	private Firestore firestore;
	
	private CollectionReference getUsersCollection() {
		return firestore.collection(USERS_COLLECTION);
	}
	
	private CollectionReference getTeamsCollection() {
		return firestore.collection(TEAMS_COLLECTION);
	}
	
	//Get a User's TEAM by its teamId.
	public Optional<Team> getUserTeamById(String userId, String teamId) throws InterruptedException, ExecutionException {
		DocumentReference userReference = getUsersCollection().document(userId);
		DocumentReference teamReference = firestore.collection("teams").document(teamId);
		if(!userReference.get().get().exists() || !teamReference.get().get().exists()) {
			return null;
		}
		DocumentSnapshot userSnapshot = userReference.get().get();
		Team team = teamReference.get().get().toObject(Team.class);
		List<Team> userTeams = userSnapshot.toObject(User.class).getUserTeams();
		Team userTeam = null;
		if(userTeams.contains(team)) {
			int teamIndex = userTeams.indexOf(team);
			userTeam = userTeams.get(teamIndex);
		}
		return Optional.of(userTeam);
	}
	
	//Get a User's TEAM by its teamName.
	public Optional<Team> getUserTeamByName(String userId, String teamName) throws InterruptedException, ExecutionException{
		DocumentReference reference = getUsersCollection().document(userId);
		Query query = getTeamsCollection().whereEqualTo("teamName", teamName);
		if(!query.get().get().isEmpty() || !reference.get().get().exists()) {
			return null;
		}
		DocumentSnapshot userSnapshot = reference.get().get();
		Team team = query.get().get().toObjects(Team.class).get(0);
		List<Team> userTeams = userSnapshot.toObject(User.class).getUserTeams();
		Team userTeam = null;
		if(userTeams.contains(team)) {
			int teamIndex = userTeams.indexOf(team);
			userTeam = userTeams.get(teamIndex);
		}
		return Optional.of(userTeam);
	}
	
	//Remove player from team player List, same for userTeamList.
	public String exitTeam(String userId, String teamId) throws InterruptedException, ExecutionException {
		DocumentReference userReference = getUsersCollection().document(userId);
		DocumentReference teamReference = getTeamsCollection().document(teamId);
		if(!userReference.get().get().exists() || !userReference.get().get().exists()) {
			return "Not found.";
		}
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
	
	public List<TeamInviteRequest> getAllTeamRequests(String userId) throws InterruptedException, ExecutionException{
		DocumentReference reference = getUsersCollection().document(userId);
		if(!reference.get().get().exists()) {
			return null;
		}
		return reference.get().get().toObject(User.class).getUserTeamRequests();
	}
	
	public String acceptTeamInvite(TeamInviteRequest teamInviteRequest) throws InterruptedException, ExecutionException {
		DocumentReference userReference = getUsersCollection().document(teamInviteRequest.getRequestedUser().getUserId());
		DocumentReference teamReference = getTeamsCollection().document(teamInviteRequest.getTeamRequest().getTeamId());
		if(!userReference.get().get().exists() || !teamReference.get().get().exists()) {
			return "Not found.";
		}
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
			teamInviteRequest.setRequestStatus(TeamInviteRequestStatus.ACCEPTED);
			List<TeamInviteRequest> userInviteRequests = user.getUserTeamRequests();
			int requestIndex = userInviteRequests.indexOf(teamInviteRequest);
			if(requestIndex != -1) {
				userInviteRequests.remove(requestIndex);
				return "Invite accepted successfully.";
			}
			return "Invite not found.";
		}
		return "Invite could not be accepted.";
	}
	
	public String declineTeamInvite(TeamInviteRequest teamInviteRequest) {
		teamInviteRequest.setRequestStatus(TeamInviteRequestStatus.DECLINED); 
		List<TeamInviteRequest> userRequests = teamInviteRequest.getRequestedUser().getUserTeamRequests();
		int requestIndex = userRequests.indexOf(teamInviteRequest);
		if(requestIndex != -1) {
			userRequests.remove(requestIndex);
			return "Invite declined.";
		}
		return "Invite not found.";
	}
	
}