package com.d1gaming.user.security;


import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.d1gaming.user.user.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
//			securedEnabled = true,
//			jsr250Enabled = true,
			prePostEnabled = true
		)
public class UserSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	UserServiceDetailsImpl userDetailsService;

	@Autowired
	private UserService userService;
	
	private final Logger logger = LoggerFactory.getLogger(UserSecurityConfiguration.class);
	
	@Bean
	GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults("");
	}
	
	@Bean
	public JwtTokenFilter authenticationJwtTokenFilter() {
		return new JwtTokenFilter();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
		authManagerBuilder.userDetailsService(username -> {
			try {
				return userService
						.getUserDetailsByUserName(username)
						.orElseThrow(
								() -> new UsernameNotFoundException("User: '" + username + "', not found.")
						);
			} 
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			return null;
		});
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
			
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		http = http.cors().and();
		
		http = http.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and();
		
		http = http.exceptionHandling()
				.authenticationEntryPoint(
						(request, response, ex) -> {
							logger.error("Unauthorized request - {}", ex.getMessage());
							response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
						}).and();
		
		http.authorizeRequests()
			.antMatchers("/userapi/**").permitAll()
			.antMatchers("/auth/register").permitAll()
			.antMatchers("/auth/login").permitAll()
			.antMatchers("/billing/**").permitAll()
			.antMatchers("/userimagesapi/**").permitAll()
			.antMatchers("/userteamapi").permitAll()
			.anyRequest().authenticated();
			
		http.addFilterBefore(authenticationJwtTokenFilter(),
				UsernamePasswordAuthenticationFilter.class);	
	}	
	
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}