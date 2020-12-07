package com.d1gaming.user.userchallenge;
import com.d1gaming.library.challenge.*;
import com.d1gaming.library.user.*;
import com.d1gaming.user.user.*;
import com.d1gaming.user.firebaseconfig.*;
import com.google.cloud.firestore.Firestore;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
public class UserChallengeService {

	private final String USERCHALLENGE_COLLECTION = "usersChallenges";
	
	@Autowired
	private Firestore firestore;
	
	private CollectionReference getUserChallengesCollection() {
		return firestore.collection(USERCHALLENGE_COLLECTION);
	}
	
	public List<Challenge> getAllUsersChallenges() throws InterruptedException, ExecutionException{
		//Asynchronously retrieve all documents.
		QuerySnapshot userChallengesRef = getUserChallengesCollection().get().get();
		//If Query snapshot is empty, no challenges found, return empty list.
		if(userChallengesRef.isEmpty()) {
			return new ArrayList<>();
		}
		return userChallengesRef.toObjects(Challenge.class);
	}
	
	public Optional<Challenge> getChallengeById(String challengeId) throws InterruptedException, ExecutionException{
		DocumentReference reference = getUserChallengesCollection().document(challengeId);
		//Evaluate if document with provided id exists in collection, else, return null.
		if(!reference.get().get().exists()) {
			return null;
		}
		DocumentSnapshot challengeSnapshot = reference.get().get();
		return Optional.of(challengeSnapshot.toObject(Challenge.class));
	}
	
	public String postChallenge(Challenge challenge, User user) throws InterruptedException, ExecutionException {
		//Add a document onto challenges subcollection with given body and auto-generated ID.
		DocumentReference reference = getUserChallengesCollection().add(challenge).get();
		String challengeId = reference.getId();
		WriteBatch batch = firestore.batch();
		//Assign auto-generated id to challengeId field.
		batch.update(reference, "challengeId", challengeId);
		//Assign given user to challengeUserAdmin 
		batch.update(reference, "challengeUserAdmin", user);
		return " ";
		
		
	
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
