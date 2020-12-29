package com.d1gaming.user.role;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.d1gaming.library.role.ERole;
import com.d1gaming.library.role.Role;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;

@Service
public class RoleService {

	private final String ROLES_COLLECTION = "roles";
	
	@Autowired
	private Firestore firestore;
	
	private CollectionReference getRolesCollection() {
		return firestore.collection(ROLES_COLLECTION);
	}
	
	//Get a Role given its name.
	public Optional<Role> getRoleByType(ERole roleName) throws InterruptedException, ExecutionException {
		Query query = getRolesCollection().whereEqualTo("roleType",roleName.toString());
		QuerySnapshot snapshot = query.get().get();
		//Evaluate if Role exists given its Name.
		if(!snapshot.isEmpty()) {
			List<Role> roles = snapshot.toObjects(Role.class);
			//Since there is a unique name for each role object, we will retrieve the first one on List.
			for(Role currRole : roles) {
				return Optional.of(currRole);
			}
		}
		return null;
	}
	
	//Get a Role given its ID.
	public Role getRoleById(String roleId) throws InterruptedException, ExecutionException {
		DocumentReference reference = getRolesCollection().document(roleId);
		DocumentSnapshot snapshot = reference.get().get();
		//Evaluate if Role with given ID exists in collection.
		if(snapshot.exists()) {
			return snapshot.toObject(Role.class);
		}
		return null;
	}
	
	
	public String saveRole(Role role) throws InterruptedException, ExecutionException {
		//Create a new Document with provided body.
		ApiFuture<DocumentReference> document = getRolesCollection().add(role);
		DocumentReference reference = document.get();
		//Retrieve Role's auto-assigned ID by firestore.
		String roleId = reference.getId();
		WriteBatch batch = firestore.batch();
		//Assign auto-generated Id into roleId field for future querying.
		batch.update(reference, "roleId",roleId);
		List<WriteResult> results = batch.commit().get();
		results.forEach(result -> 
						System.out.println("Time: " + result.getUpdateTime()));
		if(reference.get().get().exists()) {
			return "Role created successfully";
		}
		return "Role could not be created.";
	}

}
