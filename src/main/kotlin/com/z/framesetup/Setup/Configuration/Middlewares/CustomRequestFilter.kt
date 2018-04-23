package com.z.framesetup.Setup.Configuration.Middlewares

import com.fasterxml.jackson.databind.ObjectMapper
import com.z.framesetup.Setup.Controllers.ErrorHandler
import com.z.framesetup.Setup.Data.Message
import com.z.framesetup.Setup.Services.JWTService
import org.slf4j.LoggerFactory
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
    @Autowired private lateinit var tokenService: JWTService
    @Autowired private lateinit var mapper: ObjectMapper

    @Value("\${jsonWebToken.header}") private val headerString: String = "Authorization"
    @Value("\${request.filter.jsonWebToken.do-not-eval}") private val doNotEval:Array<String> = arrayOf("/","/login","/swagger-ui.html")
    @Value("\${request.filter.resources-regex}") private val resourcesRegex:String = "^(/js|/css|/fonts|/webjars)/.*\$"
    @Value("\${request.filter.cors.allow-origin}") private val allowOrigin:String=""
    @Value("\${request.filter.cors.allow-methods}") private val allowMethods:String=""
    @Value("\${request.filter.cors.max-age}") private val maxAge:String=""
    @Value("\${request.filter.cors.allow-headers}") private val allowHeaders:String=""
    @Value("\${request.filter.swagger-redirect}") private val swaggerRedirect:Array<String> = arrayOf("/api","/swagger")
    private val _logger = LoggerFactory.getLogger(CustomRequestFilter::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, filterChain: FilterChain) {
        _logger.debug(req.requestURI)
        if (swaggerRedirect.contains(req.requestURI)){//check for redirect to swagger
            _logger.debug("swagger redirect")
            res.sendRedirect(req.contextPath+ "/swagger-ui.html")
        }else{
            if(doNotEval.contains(req.requestURI)||isResource(req.requestURI)){//check forward
                _logger.debug("do not eval || isResource")
                filterChain.doFilter(req, res)//continue request
            }else{
                _logger.debug("setCORS - token check")
                setCrossOriginResourceSharing(res)//add cors headers
                val token = req.getHeader(headerString)?:req.getParameter("token")
                token.let {
                    try{
                        val authentication = tokenService.getAuthentication(req)//get auth from token
                        SecurityContextHolder.getContext().authentication = authentication//set auth on spring security context
                        filterChain.doFilter(req, res)//continue request
                    }catch(exception: RuntimeException ){
                        _logger.debug("auth: ${exception.message?:exception.toString()}")
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