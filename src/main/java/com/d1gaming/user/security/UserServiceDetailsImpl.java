package com.d1gaming.user.security;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.d1gaming.library.user.User;
import com.d1gaming.user.user.UserService;

@Component
public class UserServiceDetailsImpl implements UserDetailsService {

	@Autowired
	UserService userService;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = null;
		try {
			user = userService.getUserByName(username);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (ExecutionException e) {		
			e.printStackTrace();
		}
		if(user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		return UserDetailsImpl.build(user);
	}
}
