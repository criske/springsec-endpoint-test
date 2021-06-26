/*
 * MIT License
 *
 * Copyright (c) 2021 Pela Cristian
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 *  all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package pcf.crskdev.springspec.endpointest;

import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class to perform test on authorized endpoints isolated
 * from spring security infrastructure.
 */
public final class AuthorizedRequestsProbe {

    private final FilterInvocationSecurityMetadataSource metadataSource;

    public AuthorizedRequestsProbe(FilterInvocationSecurityMetadataSource metadataSource) {
        this.metadataSource = metadataSource;
    }

    public static AuthorizedRequestsProbe withCustomizer(
        Customizer<ExpressionUrlAuthorizationConfigurer<?>.ExpressionInterceptUrlRegistry> customizer
    ) {
        FilterInvocationSecurityMetadataSource metadataSource =
            new SandBoxedUrlAuthorizationConfigurer()
                .authorizeRequests(customizer)
                .getSecurityMetadataSource();
        return new AuthorizedRequestsProbe(metadataSource);
    }

    public static AuthorizedRequestsProbe usingFilterSecurityInterceptor(ApplicationContext context) {
        FilterChainProxy filterChainProxy =
            context.getBean(FilterChainProxy.class);
        FilterSecurityInterceptor filterSecurityInterceptor =
            (FilterSecurityInterceptor)
                filterChainProxy.getFilters("/").stream().filter(
                    f -> f instanceof FilterSecurityInterceptor
                ).findAny().orElseThrow();
        FilterInvocationSecurityMetadataSource metadataSource =
            filterSecurityInterceptor
                .getSecurityMetadataSource();
        return new AuthorizedRequestsProbe(metadataSource);
    }

    public boolean checkAccess(
        FilterInvocation invocation,
        String... authorities
    ) {
        return this.checkAccess(
            invocation,
            UserMode.AUTHENTICATED,
            authorities
        );
    }

    public boolean checkAccess(
        FilterInvocation invocation,
        UserMode mode,
        String... authorities
    ) {

        List<GrantedAuthority> grantedAuthorities =
            Arrays.stream(authorities)
                .map(a -> (GrantedAuthority) () -> a)
                .collect(Collectors.toList());
        Authentication authentication =
            mode.authentication(grantedAuthorities);

        Collection<ConfigAttribute> attributes = this.metadataSource
            .getAttributes(invocation);
        WebExpressionVoter voter = new WebExpressionVoter();
        int decision = voter.vote(authentication, invocation, attributes);

        return decision == AccessDecisionVoter.ACCESS_GRANTED;
    }
}
