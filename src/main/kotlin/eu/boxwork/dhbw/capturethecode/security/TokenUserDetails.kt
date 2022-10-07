package eu.boxwork.dhbw.capturethecode.security

import eu.boxwork.dhbw.capturethecode.dto.TeamDto
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

class TokenUserDetails(
    private val id: UUID,
    private val token: String,
    private val authorities: MutableCollection<out GrantedAuthority>,
    val team : TeamDto?
):UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    override fun getPassword() = token

    override fun getUsername() = id.toString()

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}