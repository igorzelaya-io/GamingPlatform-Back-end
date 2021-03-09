package com.d1gaming.user.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.d1gaming.library.request.UserLoginRequest;
import com.d1gaming.library.request.UserRegisterRequest;
import com.d1gaming.library.response.JwtResponse;
import com.d1gaming.library.response.MessageResponse;
import com.d1gaming.library.role.ERole;
import com.d1gaming.library.role.Role;
import com.d1gaming.library.user.User;
import com.d1gaming.library.user.UserStatus;
import com.d1gaming.user.role.RoleService;
import com.d1gaming.user.security.JwtTokenUtil;
import com.d1gaming.user.security.UserDetailsImpl;

import io.jsonwebtoken.impl.DefaultClaims;

@CrossOrigin(origins = "localhost:4200")
@RestController
@RequestMapping(path = "/auth")
public class UserAuthenticationController {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	RoleService roleService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	JwtTokenUtil jwtUtils;
		
	@PostMapping("/login")
	public ResponseEntity<Object> login(@Valid @RequestBody UserLoginRequest request){
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUserName(),request.getUserPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		return new ResponseEntity<>(new JwtResponse(jwt, userDetails.getUserId()), HttpStatus.OK);
		
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest registerRequest) throws InterruptedException, ExecutionException {
		if(userService.getUserByUserName(registerRequest.getUserName()) != null) {
			return ResponseEntity.badRequest().body(new MessageResponse("Username is already taken."));
		}
		if(userService.getUserByEmail(registerRequest.getUserEmail()) != null) {
			return ResponseEntity.badRequest().body(new MessageResponse("Email is already taken."));
		}
		User user = new User(registerRequest.getUserRealName(),registerRequest.getUserName()
						,	registerRequest.getUserPassword(),registerRequest.getUserEmail(),UserStatus.ACTIVE,
						registerRequest.getUserCountry(),registerRequest.getUserBirthDate()
						);
		List<String> strRoles = registerRequest.getUserRoles();
		List<Role> roles = new ArrayList<>();
		if(strRoles == null) {
			Role userRole = roleService.getRoleByType(ERole.ROLE_PLAYER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		}
		else {
			strRoles.forEach(role -> {
				switch(role) {
					case "Administrator":
						try {
							Role adminRole = roleService.getRoleByType(ERole.ROLE_ADMIN)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
							roles.add(adminRole);
							break;
						} 
						catch (RuntimeException | InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					case "ChallengeModerator":
						try {
							Role moderator = roleService.getRoleByType(ERole.ROLE_CHALLENGE_MODERATOR)
									.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
							roles.add(moderator);
							break;
						}
						catch(RuntimeException | InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					case "TourneyModerator":
						try {
							Role moderator = roleService.getRoleByType(ERole.ROLE_TOURNEY_MODERATOR)
									.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
							roles.add(moderator);
							break;
						}
						catch(RuntimeException | ExecutionException | InterruptedException e) {
							e.printStackTrace();
						}
					default:
						try {
							Role userRole = roleService.getRoleByType(ERole.ROLE_PLAYER)
								.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
							roles.add(userRole);
							break;
						}
						catch(RuntimeException | ExecutionException | InterruptedException e) {
							e.printStackTrace();
						}
					}
			});
		}
		user.setUserRoles(roles);
		try {
			userService.saveUser(user);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(new MessageResponse("User created Successfully."), HttpStatus.OK);
	}
	
	@GetMapping(value = "/refreshtoken")
	public ResponseEntity<?> refreshToken(HttpServletRequest request){
		DefaultClaims claims = (DefaultClaims) request.getAttribute("claims");
		Map<String, Object> expectedMap = getMapFromJWTClaims(claims);
		String token = jwtUtils.generateRefreshJwtToken(expectedMap, expectedMap.get("sub").toString());
		return new ResponseEntity<>(new JwtResponse(token), HttpStatus.OK);
	}
		
	public Map<String, Object> getMapFromJWTClaims(DefaultClaims claims){
		Map<String, Object> expectedMap =  new HashMap<String, Object>();
		for(Entry<String, Object> entry : claims.entrySet()) {
			expectedMap.put(entry.getKey(), entry.getValue());
		}
		return expectedMap;
	}
	
}