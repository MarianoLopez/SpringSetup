package com.z.framesetup.Setup.Services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.z.framesetup.Setup.Data.LoginResponse
import com.z.framesetup.Setup.Util.asLocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*
import javax.servlet.http.HttpServletRequest


class JWTService(private val EXPIRATIONTIME: Long, SECRET: String, private val TOKEN_PREFIX: String, private val HEADER_STRING: String,private val ISSUER:String){
    @Qualifier("UserService")
    @Autowired private lateinit var userDetails: UserDetailsService
    private var algorithm: Algorithm? = null

    init{ algorithm = Algorithm.HMAC512(SECRET)}


    //create token
    @Throws(JWTCreationException::class)
    fun createToken(auth: Authentication,req: HttpServletRequest,ipAsAudience:Boolean = true): LoginResponse {
        val expires = Date(System.currentTimeMillis() + EXPIRATIONTIME)
        val token = JWT.create().apply {
            withSubject(auth.name)
            withIssuer(ISSUER)
            withExpiresAt(expires)
            if(ipAsAudience) withAudience(getIp(req))
        }.sign(algorithm)
        return LoginResponse(token=token,expires = expires.asLocalDateTime(),
                username = auth.name,audience = if(ipAsAudience) getIp(req) else "",roles = auth.authorities.map { it.authority })

    }

    //get auth from token
    @Throws(UsernameNotFoundException::class,JWTVerificationException::class)
    fun getAuthentication(request: HttpServletRequest): Authentication? {
        val token = request.getHeader(HEADER_STRING)?:request.getParameter("token")//token from header-parameter
        val verifier = JWT.require(algorithm).withIssuer(ISSUER).withAudience(getIp(request)).build() //config decoder
        val jwt = verifier.verify(token.replace(TOKEN_PREFIX, "").trim({ it <= ' ' }))//decode token
        userDetails.loadUserByUsername(jwt.subject).let {
            return UsernamePasswordAuthenticationToken(it.username, it.password, it.authorities)
        }
    }

    private fun getIp(req:HttpServletRequest)=req.remoteAddr?.replace("0:0:0:0:0:0:0:1","127.0.0.1")?.replace("localhost","127.0.0.1") ?: "unknown"
}