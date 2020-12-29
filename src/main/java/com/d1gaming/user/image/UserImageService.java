package com.d1gaming.user.image;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.d1gaming.library.image.ImageModel;
import com.d1gaming.library.image.ImageUtils;
import com.d1gaming.library.user.User;
import com.d1gaming.library.user.UserStatus;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;

@Service
public class UserImageService {

	@Autowired
	private Firestore firestore;
	
	private CollectionReference getUsersCollection() {
		return firestore.collection("users");
	}
	
	private DocumentReference getUserReference(String userId) {
		return getUsersCollection().document(userId);
	}
	
	private boolean isActive(String userId) throws InterruptedException, ExecutionException{
		DocumentReference reference = getUsersCollection().document(userId);
		DocumentSnapshot snapshot = reference.get().get();
		if(snapshot.exists() && snapshot.toObject(User.class).getUserStatusCode().equals(UserStatus.ACTIVE)) {
			return true;
		}
		return false;
	}
	
	public String saveUserImage(String userId, ImageModel image) throws InterruptedException, ExecutionException {
		if(isActive(userId)) {
			DocumentReference reference = getUserReference(userId);
			ImageModel imageModel = new ImageModel(image.getImageName(), image.getImageType(),
													ImageUtils.compressBytes(image.getImageByte()));
			WriteBatch batch = firestore.batch();
			batch.update(reference, "userImage", imageModel);
			List<WriteResult> results = batch.commit().get();
			results.forEach(result -> System.out.println("Update Time: " + result.getUpdateTime()));
			return "Image saved successfully.";
		}
		return "User not found.";
	}
	
	public Optional<ImageModel> getUserImage(String userId) throws InterruptedException, ExecutionException {
		if(isActive(userId)) {
			DocumentReference reference = getUserReference(userId);
			DocumentSnapshot snapshot = reference.get().get();
			ImageModel compressedImage = snapshot.toObject(User.class).getUserImage();
			ImageModel decompressedImage = new ImageModel(compressedImage.getImageName(), compressedImage.getImageType(),
															ImageUtils.decompressBytes(compressedImage.getImageByte()));
			return Optional.of(decompressedImage);
		}
		return null;
	}	
}
