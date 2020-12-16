package com.d1gaming.user.user;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.d1gaming.library.user.User;

@RestController
@RequestMapping("/userapi")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {
	
	@Autowired
	UserService userServ;
		
	@GetMapping(value = "/users/search",params="userName")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<Object> getUserByName(@RequestParam(value = "userName", required = true)final String userName) throws InterruptedException, ExecutionException{
		if(userName == null) {
			return new ResponseEntity<>("Invalid Input",HttpStatus.BAD_REQUEST);
		}
		Optional<User> user = userServ.getUserByUserName(userName);
		if(user == null) {
			return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(user,HttpStatus.OK);
	}
	
	@GetMapping("/users")
	@PreAuthorize("hasRole('PLAYER')")
	public ResponseEntity<List<User>> getAllUsers() throws InterruptedException, ExecutionException{
		List<User> ls = userServ.getAllUsers();
		if(ls.isEmpty()) {
			return new ResponseEntity<List<User>>(ls, HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<User>>(ls, HttpStatus.OK);
	}
		
	@DeleteMapping(value = "/users/delete",params="userId")
	@PreAuthorize("hasRole('ADMINISTRATOR')")
	public ResponseEntity<Object> deleteUserById(@RequestParam(value="userId", required = true)String userId, 
												 @RequestParam(required = false, value="userField") String userField) throws InterruptedException, ExecutionException{
		if(userField != null) {
			String response = userServ.deleteUserField(userId, userField);
			if(response.equals("User not found.")) {
				return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);			
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		String response = userServ.deleteUserById(userId);
		if(response.equals("User not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	
	@PutMapping(value = "/users/update")
	@PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('PLAYER')")
	public ResponseEntity<Object> updateUser(@RequestBody User user) throws InterruptedException, ExecutionException{
		String response = userServ.updateUser(user);
		if(response.equals("User not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PutMapping(value = "/users/update",params="userId")
	@PreAuthorize("hasRole('ADMINISTRATOR') or hasRole('PLAYER')")
	public ResponseEntity<Object> updateUserField(@RequestParam(required = true, value="userId")String userId, 
												  @RequestParam(required = true)String userField,
												  @RequestParam(required = true)String replaceValue) throws InterruptedException, ExecutionException{
		String response = userServ.updateUserField(userId, userField, replaceValue);
		if(response.equals("User not found.")) {
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}
		else if(response.equals("This field cannot be updated.")) {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}	
}
