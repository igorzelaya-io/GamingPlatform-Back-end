package com.d1gaming.user.userTournament;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;

public class UserTournamentService {

	private final String USER_TOURNAMENT_SUBCOLLECTION = "userTournaments";
	
	@Autowired
	private Firestore firestore;
	
	private CollectionReference getUserTournamentsSubcollection() {
		return firestore.collection(this.USER_TOURNAMENT_SUBCOLLECTION);
	}
	
	//TODO
	public void updateUserKillsOnTournament(String tournamentId) {
		DocumentReference reference = getUserTournamentsSubcollection().document(tournamentId);
		reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
			@Override
			public void onEvent(@Nullable DocumentSnapshot snapshot,
								@Nullable FirestoreException e) {
				if(e != null) {
					throw new RuntimeException(e.getMessage());
				}
				if(snapshot != null && snapshot.exists()) {
					System.out.println("Data: " + snapshot.getData());
				}
			}
		});
	}
	
	
}