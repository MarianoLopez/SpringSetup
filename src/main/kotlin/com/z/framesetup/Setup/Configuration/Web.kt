package com.z.framesetup.Setup.Configuration

import com.z.framesetup.Setup.Configuration.Middlewares.CustomAuthenticationProvider
import com.z.framesetup.Setup.Configuration.Middlewares.CustomRequestFilter
import com.z.framesetup.Setup.Services.JWTService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class Web : WebSecurityConfigurerAdapter() {
    //read from application.properties
    @Value("\${jsonWebToken.expiration-time}") private val EXPIRATIONTIME: String? = null
    @Value("\${jsonWebToken.secret}") private val SECRET: String? = null
    @Value("\${jsonWebToken.token-prefix}") private val TOKEN_PREFIX: String? = null
    @Value("\${jsonWebToken.header}") private val HEADER_STRING: String? = null
    @Value("\${jsonWebToken.issuer}") private val ISSUER: String? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf().disable()//no csrf
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()//no session
                .authorizeRequests().antMatchers(HttpMethod.POST, "/login").permitAll().and()

                .authorizeRequests().antMatchers(HttpMethod.GET,"/user/**").hasAnyRole("TESTER","ADMIN").and()
                .authorizeRequests().antMatchers("/user/**").hasAnyRole("ADMIN").and()
                .authorizeRequests().antMatchers(HttpMethod.GET,"/role/**").hasAnyRole("TESTER","ADMIN").and()
                .authorizeRequests().antMatchers("/role/**").hasAnyRole("ADMIN").and()
                //api
                .authorizeRequests().antMatchers(HttpMethod.GET,"/api/**").hasAnyRole("ADMIN","TESTER").and()
                .authorizeRequests().antMatchers(HttpMethod.POST,"/api/**").hasAnyRole("ADMIN").and()
                .authorizeRequests().antMatchers(HttpMethod.PUT,"/api/**").hasAnyRole("ADMIN").and()
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)// And filter other requests to check the presence of JWT in header
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder?) { auth!!.authenticationProvider(userProvider())}//spring custom auth provider

    /*Beans*/
    @Bean @Primary  fun userProvider() = CustomAuthenticationProvider()//auth provider implementation

    @Bean fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean fun tokenAuth()= JWTService(EXPIRATIONTIME!!.toLong(), SECRET!!, TOKEN_PREFIX!!, HEADER_STRING!!,ISSUER!!)//init jwt Services

    @Bean fun jwtAuthenticationFilter() = CustomRequestFilter()//filters init
}