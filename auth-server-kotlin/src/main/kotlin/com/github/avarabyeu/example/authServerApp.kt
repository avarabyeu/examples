package com.github.avarabyeu.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


fun main(args: Array<String>) {
    SpringApplication.run(AuthServerApp::class.java, *args)
}

@SpringBootApplication
open class AuthServerApp {

    companion object {
        val USERS = listOf<UserDetails>(User("user", "password",
                listOf(SimpleGrantedAuthority("USER"))))
    }
}

@Configuration
open class GlobalAuthConfig : GlobalAuthenticationConfigurerAdapter() {

    @Bean
    open fun userDetailsService(): UserDetailsService {
        return InMemoryUserDetailsManager(AuthServerApp.USERS)
    }

    override fun init(auth: AuthenticationManagerBuilder) {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService())
        auth.authenticationProvider(authenticationProvider)
    }
}

@Configuration
@EnableAuthorizationServer
open class AuthorizationServerConfig : AuthorizationServerConfigurerAdapter() {

    @Autowired
    lateinit var authManager: AuthenticationManager

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        //@formatter:off
        clients.inMemory()
                .withClient("oauth")
                .secret("oauth")
                .authorizedGrantTypes("refresh_token", "password")
                .scopes("oauth")
                .accessTokenValiditySeconds(60 * 5)
                .authorities("USER")
        //@formatter:on
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints.authenticationManager(authManager).tokenStore(InMemoryTokenStore())
    }

}

@Configuration
@EnableResourceServer
open class ResourceServerConfig : ResourceServerConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests().anyRequest().authenticated()
    }
}


@RestController
class UserController {

    @GetMapping("/api/me")
    fun getMyself(authentication: Authentication) = authentication
}


