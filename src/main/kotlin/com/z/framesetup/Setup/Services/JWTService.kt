package com.z.framesetup.Setup.Services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.z.framesetup.Setup.Util.asLocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.format.DateTimeFormatter
import java.util.*
import javax.servlet.http.HttpServletRequest


class JWTService(private val EXPIRATIONTIME: Long, SECRET: String, private val TOKEN_PREFIX: String, private val HEADER_STRING: String,private val ISSUER:String){
    @Qualifier("UserService")
    @Autowired private lateinit var userDetails: UserDetailsService
    @Autowired private lateinit var mapper: ObjectMapper
    private var algorithm: Algorithm? = null
    private val formatterTime = DateTimeFormatter.ofPattern("dd-MM-YYYY HH:mm:ss")

    init{ algorithm = Algorithm.HMAC512(SECRET)}


    //create token
    @Throws(JWTCreationException::class)
    fun createToken(auth: Authentication,req: HttpServletRequest):ObjectNode{
        val expires = Date(System.currentTimeMillis() + EXPIRATIONTIME)
        val token = JWT.create()
                .withSubject(auth.name)
                .withAudience(getIp(req))
                .withIssuer(ISSUER)
                .withExpiresAt(expires)
                .sign(algorithm)
        return mapper.createObjectNode().apply {
            put("token",token)
            put("expires",formatterTime.format(expires.asLocalDateTime()))
            putPOJO("roles",auth.authorities.map { it.authority })
        }
    }

    //get auth from token
    @Throws(UsernameNotFoundException::class,JWTVerificationException::class)
    fun getAuthentication(request: HttpServletRequest): Authentication? {
        val token = request.getHeader(HEADER_STRING)?:request.getParameter("token")//token from header-parameter
        if (token != null) {
            val verifier = JWT.require(algorithm).withIssuer(ISSUER).withAudience(getIp(request)).build() //decode token
            val jwt = verifier.verify(token.replace(TOKEN_PREFIX, "").trim({ it <= ' ' }))//check token
            userDetails.loadUserByUsername(jwt.subject).let {
                return UsernamePasswordAuthenticationToken(it.username, it.password, it.authorities)
            }
        }
        return null
    }

    private fun getIp(req:HttpServletRequest)=req.remoteAddr?.replace("0:0:0:0:0:0:0:1","127.0.0.1")?.replace("localhost","127.0.0.1")
}