package com.z.framesetup.Setup.Configuration

import com.z.framesetup.Setup.Configuration.Middlewares.CustomAuthenticationProvider
import com.z.framesetup.Setup.Configuration.Middlewares.CustomRequestFilter
import com.z.framesetup.Setup.Data.WebRuleDAO
import com.z.framesetup.Setup.Services.JWTService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class Web(private val webRuleDAO: WebRuleDAO) : WebSecurityConfigurerAdapter() {
    //read from application.properties
    @Value("\${jsonWebToken.expiration-time}") private val expirationTime: String? = null
    @Value("\${jsonWebToken.secret}") private val secret: String? = null
    @Value("\${jsonWebToken.token-prefix}") private val tokenPrefix: String? = null
    @Value("\${jsonWebToken.header}") private val headerString: String? = null
    @Value("\${jsonWebToken.issuer}") private val issuer: String? = null

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        var builder = http.csrf().disable()
        webRuleDAO.findAll().forEach {//read rules from db
            val ant = if(it.httpMethod!=null) builder.authorizeRequests().antMatchers(it.httpMethod,it.pattern) else builder.authorizeRequests().antMatchers(it.pattern)
            builder = if(it.roles.isEmpty()) ant.permitAll().and() else ant.hasAnyRole(*it.roles.toTypedArray()).and()
        }
        builder.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)// And filter other requests to check the presence of JWT in header

    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder?) { auth!!.authenticationProvider(userProvider())}//spring custom auth provider

    /*Beans*/
    @Bean @Primary  fun userProvider() = CustomAuthenticationProvider()//auth provider implementation

    @Bean fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean fun tokenAuth()= JWTService(expirationTime!!.toLong(), secret!!, tokenPrefix!!, headerString!!,issuer!!)//init jwt Services

    @Bean fun jwtAuthenticationFilter() = CustomRequestFilter()//filters init
}