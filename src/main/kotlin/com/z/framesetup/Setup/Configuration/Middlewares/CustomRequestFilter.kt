package com.z.framesetup.Setup.Configuration.Middlewares

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.framesetup.Setup.Data.Message
import com.z.framesetup.Setup.Services.JWTService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.util.regex.Pattern
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class CustomRequestFilter : OncePerRequestFilter(){
    @Autowired
    private lateinit var tokenService: JWTService
    @Autowired
    private lateinit var mapper: ObjectMapper

    @Value("\${jsonWebToken.header}") private val HEADER_STRING: String = "Authorization"
    @Value("\${request.filter.jsonWebToken.do-not-eval}") private val doNotEval:Array<String> = arrayOf("/","/login","/swagger-ui.html")
    @Value("\${request.filter.resources-regex}") private val resourcesRegex:String = "^(/js|/css|/fonts)/.*\$"
    @Value("\${request.filter.cors.allow-origin}") private val allowOrigin:String=""
    @Value("\${request.filter.cors.allow-methods}") private val allowMethods:String=""
    @Value("\${request.filter.cors.max-age}") private val maxAge:String=""
    @Value("\${request.filter.cors.allow-headers}") private val allowHeaders:String=""
    private val swaggerRedirect = listOf("/","/api","/swagger")

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, filterChain: FilterChain) {
        if (swaggerRedirect.contains(req.requestURI)){//check for redirect to swagger
            res.sendRedirect(req.contextPath+ "/swagger-ui.html")
        }else{
            if(doNotEval.contains(req.requestURI)||isResource(req.requestURI)){//check forward
                filterChain.doFilter(req, res)//continue request
            }else{
                setCrossOriginResourceSharing(res)//add cors headers
                val token = req.getHeader(HEADER_STRING)?:req.getParameter("token")
                token.let {
                    try{
                        val authentication = tokenService.getAuthentication(req)//get auth from token
                        SecurityContextHolder.getContext().authentication = authentication//set auth on spring security context
                        filterChain.doFilter(req, res)//continue request
                    }catch(exception: RuntimeException ){
                        res.status = HttpServletResponse.SC_BAD_REQUEST
                        res.addHeader("Content-Type", "application/json")
                        res.writer.write(mapper.writeValueAsString(Message("auth error", message = exception.message?:exception.toString())))
                    }
                }
            }
        }
    }
    private fun isResource(uri:String) = Pattern.compile(resourcesRegex).matcher(uri).matches()

    private fun setCrossOriginResourceSharing(response: HttpServletResponse){
        response.setHeader("Access-Control-Allow-Origin", allowOrigin)
        response.setHeader("Access-Control-Allow-Methods", allowMethods)
        response.setHeader("Access-Control-Max-Age", maxAge)
        response.setHeader("Access-Control-Allow-Headers", allowHeaders)
    }
}