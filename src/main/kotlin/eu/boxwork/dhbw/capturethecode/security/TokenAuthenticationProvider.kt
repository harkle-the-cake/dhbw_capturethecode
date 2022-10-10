package eu.boxwork.dhbw.capturethecode.security

import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import eu.boxwork.dhbw.capturethecode.enums.RoleType
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import java.util.*
import javax.naming.AuthenticationException

@Component
class TokenAuthenticationProvider(
) : AbstractUserDetailsAuthenticationProvider() {

    @Throws(AuthenticationException::class)
    override fun additionalAuthenticationChecks(
        userDetails: UserDetails?,
        usernamePasswordAuthenticationToken: UsernamePasswordAuthenticationToken?,
    ) {
        //
    }

    @Throws(AuthenticationException::class)
    override fun retrieveUser(
        userName: String?,
        usernamePasswordAuthenticationToken: UsernamePasswordAuthenticationToken,
    ): UserDetails {
        if (userName.isNullOrEmpty()) throw UsernameNotFoundException("user name not set")
        val token = usernamePasswordAuthenticationToken.credentials as String
        if (token.isEmpty()) throw UsernameNotFoundException("login token not set")
        try {
            if (isAdmin(usernamePasswordAuthenticationToken))
            {
                return TokenUserDetails(
                    UUID.randomUUID(),
                    token,
                    usernamePasswordAuthenticationToken.authorities,
                    null
                )
            }
            else
            {
                if (usernamePasswordAuthenticationToken.details == null) throw UsernameNotFoundException("login token details not set")
                return TokenUserDetails(
                    UUID.fromString(userName),
                    token,
                    usernamePasswordAuthenticationToken.authorities,
                    usernamePasswordAuthenticationToken.details as TeamDto
                )
            }
        } catch (e: Exception)
        {
            throw UsernameNotFoundException("token not parsed: $e")
        }

    }

    private fun isAdmin(usernamePasswordAuthenticationToken: UsernamePasswordAuthenticationToken): Boolean {
        return usernamePasswordAuthenticationToken.authorities.any {
            it.authority=="ROLE_"+RoleType.ADMIN
        }
    }
}