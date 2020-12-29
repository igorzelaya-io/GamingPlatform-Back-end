package com.d1gaming.user.userchallenge;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;

import com.d1gaming.library.challenge.Challenge;
import com.d1gaming.library.challenge.ChallengeStatus;
import com.d1gaming.library.user.User;
import com.d1gaming.library.user.UserStatus;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
public class UserChallengeService {

	private final String USERCHALLENGE_SUBCOLLECTION = "userChallenges";
	private final String USERS_COLLECTION = "users";
	
	@Autowired
	private Firestore firestore;
	
	private CollectionReference getUsersCollection() {
		return firestore.collection(USERS_COLLECTION);
	}
	
	private CollectionReference getUserChallengeSubcollection(String userId) {
		return getUserReference(userId).collection(USERCHALLENGE_SUBCOLLECTION);
	}
	
	private boolean isActive(String userId) throws InterruptedException, ExecutionException {
		DocumentReference reference = getUserReference(userId);
		DocumentSnapshot snapshot = reference.get().get();
		if(snapshot.exists() && snapshot.toObject(User.class).getUserStatusCode().equals(UserStatus.ACTIVE)) {
			return true;
		}
		return false;
	}
	
	private boolean isActiveChallenge(String userId, String challengeId) throws InterruptedException, ExecutionException {
		DocumentReference reference = getUserChallengeReference(userId, challengeId);
		DocumentSnapshot snapshot = reference.get().get();
		if(snapshot.exists() && snapshot.toObject(Challenge.class).getChallengeStatus().equals(ChallengeStatus.ACTIVE)) {
			return true;
		}
		return false;
	}
	
	private DocumentReference getUserReference(String userId) {
		return getUsersCollection().document(userId);
	}
	
	private DocumentReference getUserChallengeReference(String userId, String challengeId) {
		return getUserReference(userId).collection(USERCHALLENGE_SUBCOLLECTION).document(challengeId);
	}
	
	public List<Challenge> getAllUsersChallenges(String userId) throws InterruptedException, ExecutionException{
		//Asynchronously retrieve all documents.
		QuerySnapshot userChallengesRef = getUserChallengeSubcollection(userId).get().get();
		if(!isActive(userId)) {
			return null;
		}
		//If Query snapshot is empty, no challenges found, return empty list.
		return userChallengesRef.toObjects(Challenge.class);
	}
	
	public Optional<Challenge> getChallengeById(String userId, String challengeId) throws InterruptedException, ExecutionException{
		if(isActive(userId)) {
			if(isActiveChallenge(userId, challengeId)) {
				DocumentSnapshot snapshot = getUserChallengeReference(userId, challengeId).get().get();
				return Optional.of(snapshot.toObject(Challenge.class));
			}
			return null;
		}
		return null;
	}
	//TODO
	public String postChallenge(Challenge challenge, String userId) throws InterruptedException, ExecutionException {
		return "";
	}
	
	public String joinChallenge(Challenge challenge) {
		return " ";
	}

	//Add challenge moderator role to user.
	private String addChallengeModeratorRole(User user) throws InterruptedException, ExecutionException {
		String userId = user.getUserId();
		DocumentReference reference = firestore.collection("users").document(userId);
		//Validating if user exists before making changes.
		if(!reference.get().get().exists()) {
			return "User not found.";
		}
		WriteBatch batch = firestore.batch();
//		batch.update(reference, "", value, moreFieldsAndValues)
//TODO
		return " ";
	}
}
