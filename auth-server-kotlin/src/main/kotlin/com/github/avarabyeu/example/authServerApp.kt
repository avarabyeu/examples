package com.github.avarabyeu.example

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.health.RedisHealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


fun main(args: Array<String>) {
    SpringApplication.run(AuthServerApp::class.java, *args)
}

@SpringBootApplication
open class AuthServerApp {}

@Configuration
open class GlobalAuthConfig : GlobalAuthenticationConfigurerAdapter() {

    val USERS = listOf<UserDetails>(User("user", "password", listOf(SimpleGrantedAuthority("USER"))))


    @Bean
    open fun userDetailsService(): UserDetailsService {
        return InMemoryUserDetailsManager(USERS)
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

    @Autowired
    lateinit var tokenStore: TokenStore

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
        val exceptionTranslator = object : DefaultWebResponseExceptionTranslator() {
            override fun translate(e: Exception?): ResponseEntity<OAuth2Exception> {
                if (e is RedisConnectionFailureException) {
                    return ResponseEntity(object : OAuth2Exception("Horrible exception!!! You are not lucky man") {
                        override fun getOAuth2ErrorCode(): String {
                            return "server_error"
                        }
                    }, HttpStatus.SERVICE_UNAVAILABLE)
                } else {
                    return super.translate(e)
                }
            }
        }

        endpoints.authenticationManager(authManager)
                .exceptionTranslator(exceptionTranslator)
                .tokenStore(tokenStore)
    }

    /* default token store. Use it if nothing else in app context */
    @Bean
    open fun tokenStore(): TokenStore {
        return InMemoryTokenStore()
    }

}

/**
 * If redis specified explicitly, then use it as token store
 */
@Configuration
@ConditionalOnProperty("spring.redis.host")
open class RedisConfiguration {

    @Autowired
    lateinit var redisConnectionFactory: RedisConnectionFactory

    @Bean
    @Primary
    open fun redisTokenStore(): TokenStore {
        return RedisTokenStore(redisConnectionFactory);
    }

    @Bean
    open fun redisHealth(): RedisHealthIndicator {
        return RedisHealthIndicator(redisConnectionFactory);
    }
}

@Configuration
@EnableResourceServer
open class ResourceServerConfig : ResourceServerConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http!!.authorizeRequests().antMatchers("/api").authenticated()
    }
}


@RestController
class UserController {

    @GetMapping("/api/me")
    fun getMyself(authentication: Authentication) = authentication
}


