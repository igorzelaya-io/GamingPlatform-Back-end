package com.d1gaming.user.image;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.d1gaming.library.image.ImageModel;

@RestController
@CrossOrigin( origins = "localhost:4200")
@RequestMapping(value = "/userimagesapi")
public class UserImageController {

	@Autowired
	private UserImageService userImageService;

	@PostMapping(value = "/images/save", params = "userId, file")
	public ResponseEntity<?> saveImage(@RequestParam(required = true)String userId, 
									   @RequestParam(required = true)MultipartFile file) throws IOException, InterruptedException, ExecutionException{
		ImageModel model = new ImageModel(file.getName(), file.getContentType(), file.getBytes());
		String response = userImageService.saveUserImage(userId, model);
		if(response.equals("User not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(value = "/image/search", params = "userId")
	public ResponseEntity<?> getUserImage(@RequestParam(required = true) String userId) throws InterruptedException, ExecutionException{
		Optional<ImageModel> userImage = userImageService.getUserImage(userId);
		if(userImage == null ) {
			return new ResponseEntity<>(userImage, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(userImage.get(), HttpStatus.OK);
	}
}
