package com.z.framesetup.Setup.Configuration

import com.z.framesetup.Setup.Configuration.Middlewares.CustomAuthenticationProvider
import com.z.framesetup.Setup.Configuration.Middlewares.CustomRequestFilter
import com.z.framesetup.Setup.Data.WebRuleDAO
import com.z.framesetup.Setup.Services.JWTService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
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
class Web(private val webRuleDAO: WebRuleDAO) : WebSecurityConfigurerAdapter() {
    //read from application.properties
    @Value("\${jsonWebToken.expiration-time}") private val EXPIRATIONTIME: String? = null
    @Value("\${jsonWebToken.secret}") private val SECRET: String? = null
    @Value("\${jsonWebToken.token-prefix}") private val TOKEN_PREFIX: String? = null
    @Value("\${jsonWebToken.header}") private val HEADER_STRING: String? = null
    @Value("\${jsonWebToken.issuer}") private val ISSUER: String? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        var builder = http.authorizeRequests().and()
        webRuleDAO.findAll().forEach {
            val ant = if(it.httpMethod!=null) builder.authorizeRequests().antMatchers(it.httpMethod,it.pattern) else builder.authorizeRequests().antMatchers(it.pattern)
            builder = if(it.roles.isEmpty()) ant.permitAll().and() else ant.hasAnyRole(*it.roles.toTypedArray()).and()
        }
        http.csrf().disable()
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