package com.z.framesetup.Setup.Data

import java.time.LocalDateTime


data class Message(val title:String, val date: LocalDateTime = LocalDateTime.now(), val message: Any?= null)
data class AccountCredentials(val username: String, val password: String)
data class LoginResponse(val token:String, val expires:LocalDateTime,val roles:List<String>,val username:String, val audience:String = "")