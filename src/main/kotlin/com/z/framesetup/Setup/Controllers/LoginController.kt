package com.z.framesetup.Setup.Controllers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.z.framesetup.Setup.Configuration.Middlewares.CustomAuthenticationProvider
import com.z.framesetup.Setup.Data.AccountCredentials
import com.z.framesetup.Setup.Data.LoginResponse
import com.z.framesetup.Setup.Services.JWTService
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController("/")
class LoginController(val tokenAuth: JWTService, val authProvider: CustomAuthenticationProvider) {
    @PostMapping("login") fun login(@RequestBody credentials: AccountCredentials, req: HttpServletRequest): LoginResponse {
        val auth = authProvider.authenticate(UsernamePasswordAuthenticationToken(credentials.username, credentials.password, Collections.emptyList()))
        return tokenAuth.createToken(auth,req)
    }
}