package com.cg.bookmymovie.websiteService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//@EnableOAuth2Sso
public class WebsiteServiceApplication  /*extends WebSecurityConfigurerAdapter*/ {

/*	  @Override
	  protected void configure(HttpSecurity http) throws Exception {
	   
	    
	    http.antMatcher("/**")
	      .authorizeRequests()
	        .antMatchers("/", "/login**", "/webjars/**", "/error**")
	        .permitAll()
	      .anyRequest()
	        .authenticated()
	      .and().logout().logoutSuccessUrl("/").permitAll()
	      .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());;
	  }
	 */
	
	public static void main(String[] args) {
		SpringApplication.run(WebsiteServiceApplication.class, args);
	}
	
	@Bean
	public RestTemplate getTemplate() {
		return new RestTemplate();
	}

}

