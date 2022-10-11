package eu.boxwork.dhbw.capturethecode.security

import eu.boxwork.dhbw.capturethecode.enums.RoleType
import eu.boxwork.dhbw.capturethecode.service.repo.TeamRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import java.lang.Exception
import java.util.*


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class TokenSecurityConfiguration(
    @Autowired val provider: TokenAuthenticationProvider,
    @Value("\${admin.token}") private val adminToken: String,
    @Autowired private val teamRepository: TeamRepository
)
    : WebSecurityConfigurerAdapter() {
    private val PROTECTED_URLS: RequestMatcher = OrRequestMatcher(
        AntPathRequestMatcher("/admin/*"),
        AntPathRequestMatcher("/team"),
        AntPathRequestMatcher("/team/*"),
        AntPathRequestMatcher("/competition/**"),
        AntPathRequestMatcher("/training/**"),
        AntPathRequestMatcher("/player"),
        AntPathRequestMatcher("/player/*"),
        AntPathRequestMatcher("/player/**")
    )

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(provider)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .and()
            .authenticationProvider(provider)
            .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter::class.java)
            .authorizeRequests()
            //.requestMatchers(PROTECTED_URLS).authenticated() // allows all roles
            // OPEN INTERFACES
            .antMatchers(HttpMethod.GET,"/error").permitAll()
            .antMatchers(HttpMethod.GET,"/spectator/**").permitAll()
            .antMatchers(HttpMethod.GET,"/team").permitAll()
            .antMatchers(HttpMethod.GET,"/team/*").permitAll()

            // TRAINING
            .antMatchers(HttpMethod.GET, "/training/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.PUT, "/training/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.POST, "/training/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.DELETE, "/training/**").hasAnyRole(RoleType.PLAYER.name)

            // COMPETITION
            .antMatchers(HttpMethod.GET, "/competition/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.PUT, "/competition/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.POST, "/competition/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.DELETE, "/competition/**").hasAnyRole(RoleType.PLAYER.name)

            // ADMIN
            .antMatchers(HttpMethod.GET, "/admin/**").hasRole(RoleType.ADMIN.name)
            .antMatchers(HttpMethod.POST, "/admin/**").hasRole(RoleType.ADMIN.name)
            .antMatchers(HttpMethod.PUT, "/admin/**").hasRole(RoleType.ADMIN.name)
            .antMatchers(HttpMethod.DELETE, "/admin/**").hasRole(RoleType.ADMIN.name)

            // TEAM
            .antMatchers(HttpMethod.POST, "/team/**").hasAnyRole(RoleType.ADMIN.name,RoleType.PLAYER.name)
            .antMatchers(HttpMethod.PUT, "/team/**").hasAnyRole(RoleType.ADMIN.name)
            .antMatchers(HttpMethod.DELETE, "/team/members").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.DELETE, "/team/**").hasAnyRole(RoleType.ADMIN.name)

            // PLAYER
            .antMatchers(HttpMethod.GET, "/player/**").hasAnyRole(RoleType.ADMIN.name,RoleType.PLAYER.name)
            .antMatchers(HttpMethod.PUT, "/player/**").hasAnyRole(RoleType.PLAYER.name)
            .antMatchers(HttpMethod.POST, "/player/**").hasAnyRole(RoleType.ADMIN.name,RoleType.PLAYER.name)
            .antMatchers(HttpMethod.DELETE, "/player/**").hasAnyRole(RoleType.ADMIN.name,RoleType.PLAYER.name)

            .anyRequest().denyAll()
            .and()
            .csrf().disable()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
    }

    //@Bean
    //@Throws(Exception::class)
    fun authenticationFilter(): TokenAuthenticationFilter? {
        val filter = TokenAuthenticationFilter(PROTECTED_URLS,adminToken,teamRepository)
        filter.setAuthenticationManager(authenticationManager())
        //filter.setAuthenticationSuccessHandler(successHandler());
        return filter
    }

    @Bean
    fun forbiddenEntryPoint(): AuthenticationEntryPoint? {
        return HttpStatusEntryPoint(HttpStatus.FORBIDDEN)
    }
}