package eu.boxwork.dhbw.capturethecode.security

import eu.boxwork.dhbw.capturethecode.enums.RoleType
import eu.boxwork.dhbw.capturethecode.service.repo.TeamRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.core.context.SecurityContextHolder

import java.io.IOException

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.util.matcher.RequestMatcher
import java.util.*
import javax.naming.AuthenticationException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TokenAuthenticationFilter(
    urlsMatcher: RequestMatcher,
    private val adminToken: String?,
    private val teamRepository: TeamRepository
) : AbstractAuthenticationProcessingFilter (urlsMatcher) {

    var log: Logger = LoggerFactory.getLogger("TokenAuthenticationFilter")
    private val AUTHORIZATION = "Authorization"

    @Throws(AuthenticationException::class, IOException::class, ServletException::class)
    override fun attemptAuthentication(
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse?,
    ): Authentication? {
        val urlString = "${httpServletRequest.method} ${httpServletRequest.requestURI}"
        if (log.isDebugEnabled) log.debug("attempt token authorization ($urlString)")

        var requestAuthentication = UsernamePasswordAuthenticationToken("", "")
        var token: String? = httpServletRequest.getHeader(AUTHORIZATION)
        if (token!=null)
        {
            token = token.replace("token", "", ignoreCase = true).trim()

            if (!adminToken.isNullOrEmpty() && token == adminToken)
            {
                val authorities: MutableList<GrantedAuthority> = ArrayList()
                authorities.add(SimpleGrantedAuthority("ROLE_"+RoleType.ADMIN))

                requestAuthentication = UsernamePasswordAuthenticationToken(
                    UUID.randomUUID(),
                    token,
                    authorities)
                requestAuthentication.details = null
            }
            else
            {
                try {
                    val team = teamRepository.findByTeamToken(token)
                    if (team!=null)
                    {
                        // user is part of the team
                        val authorities: MutableList<GrantedAuthority> = ArrayList()
                        authorities.add(SimpleGrantedAuthority("ROLE_" + RoleType.PLAYER))

                        requestAuthentication = UsernamePasswordAuthenticationToken(
                            team.uuid,
                            token,
                            authorities)
                        requestAuthentication.details = team.dto()
                    }
                    else{
                        log.error("team not found for token $token")
                    }


                }
                catch (e: Exception)
                {
                    log.error("unable to parse UUID: $e")
                }

            }

        }
        else
        {
            log.error("no token in header. ($urlString)")
        }

        return authenticationManager.authenticate(requestAuthentication)
    }

    @Throws(IOException::class, ServletException::class)
    override fun successfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        chain: FilterChain,
        authResult: Authentication?,
    ) {
        if (log.isDebugEnabled) {
            log.debug("Authentication success using device token authentication." +
                    " Updating SecurityContextHolder to contain: {}",
                authResult)
        }

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authResult
        SecurityContextHolder.setContext(context)

        try {
            // Fire event
            if (eventPublisher != null) {
                eventPublisher.publishEvent(InteractiveAuthenticationSuccessEvent(authResult, this.javaClass))
            }
            chain.doFilter(request, response)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }
}