## Spring security endpoint tester

Small utility to test authorization on endpoints without the need to run full integration tests for spring-security and spring-mvc.

All done in unit tests - no need for spring integration test ceremony(no SpringRunner, @SpringBootTest, @WebMvcTest etc...).

### Usage in integration tests
```java
public final class MyTest {
    static AuthorizedRequestsProbe probe = AuthorizedRequestsProbe
        .withCustomizer(config ->
            config
                .mvcMatchers("/remember").rememberMe()
                .mvcMatchers("/private/admin/**", "/private/user/**/admin/**").hasRole("ADMIN")
                .mvcMatchers("/private/user/**").hasRole("USER")
                .mvcMatchers("/private/**").authenticated()
                .anyRequest().permitAll()
        );

    @Test
    public void shouldPassUnauthenticated() {
        var hasAccess = probe.checkAccess(
            new FilterInvocation("/", "GET"),
            AuthorizedRequestsProbe.UserMode.UNAUTHENTICATED
        );
        MatcherAssert.assertThat(
            hasAccess,
            Matchers.is(Boolean.TRUE)
        );
    }

    @Test
    public void shouldAllowAdminOnInnerUserPath() {
        var hasAccess = probe.checkAccess(
            new FilterInvocation("/private/user/foo/admin/bar", "GET"),
            "ROLE_ADMIN"
        );
        MatcherAssert.assertThat(
            hasAccess,
            Matchers.is(Boolean.TRUE)
        );
    }

    @Test
    public void shouldAllowRememberMe() {
        var hasAccess = probe.checkAccess(
            new FilterInvocation("/remember", "GET"),
            AuthorizedRequestsProbe.UserMode.REMEMBER_ME
        );
        MatcherAssert.assertThat(
            hasAccess,
            Matchers.is(Boolean.TRUE)
        );
    }
}
```

`AuthorizedRequestsProbe` can use [HttpSecurity#authroizeRequests](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html#authorizeRequests(org.springframework.security.config.Customizer)) customizer but for that it needs a little work because that customizer needs a [HttpSecurity](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/web/builders/HttpSecurity.html) builder while
`AuthorizedRequestsProbe` accepts customizer with any [HttpSecurityBuilder](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/annotation/web/HttpSecurityBuilder.html)

To convey this, a generic customizer is needed.
```java
public final class AuthorizedRequestsCustomizer<H extends HttpSecurityBuilder<H>>
    implements Customizer<ExpressionUrlAuthorizationConfigurer<H>.ExpressionInterceptUrlRegistry> {

    @Override
    public void customize(ExpressionUrlAuthorizationConfigurer<H>.ExpressionInterceptUrlRegistry registry) {
        registry
            .mvcMatchers("/remember").rememberMe()
            .mvcMatchers("/private/admin/**", "/private/user/**/admin/**").hasRole("ADMIN")
            .mvcMatchers("/private/user/**").hasRole("USER")
            .mvcMatchers("/private/**").authenticated()
            .anyRequest().permitAll();
    }

}
```

Now security configuration will look

```java
@EnableWebSecurity
public class SecConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) {
        http
            .authorizeRequests(new AuthorizedRequestsCustomizer<>());
    }
}
```
and in tests:
```java
public final class SecurityTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static AuthorizedRequestsProbe probe = AuthorizedRequestsProbe
        .withCustomizer(new AuthorizedRequestsCustomizer());
    //...
}
```

### Usage in integration tests

`AuthorizedRequestsProbe` can be used in integration tests too.
In this case `AuthorizedRequestsProbe#usingFilterSecurityInterceptor(context)` factory method will
extract [FilterInvocationSecurityMetadataSource](https://docs.spring.io/spring-security/site/docs/4.2.19.RELEASE/apidocs/org/springframework/security/web/access/intercept/FilterInvocationSecurityMetadataSource.html) created by HttpSecurity builder from 
[FilterSecurityInterceptor](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/access/intercept/FilterSecurityInterceptor.html) via
the exposed [FilterChainProxy](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/FilterChainProxy.html) bean.

```java
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SecConfig.class})
@WebAppConfiguration
public final class SecurityTest {
    @Autowired
    ApplicationContext context;

    AuthorizedRequestsProbe probe;

    @Before
    public void before() {
        probe = AuthorizedRequestsProbe.usingFilterSecurityInterceptor(context);
    }
}
```

### Gist
Whole source code is available in this [gist](https://gist.github.com/criske/5960f55614a5801113a3c97e7ed3737f).
