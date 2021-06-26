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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

/**
 * SecurityHttpBuilder isolated from spring security infrastructure.
 */
final class SandBoxedSecurityHttpBuilder implements HttpSecurityBuilder<SandBoxedSecurityHttpBuilder> {

    private final Map<Class<?>, Object> sharedObjects = new HashMap<>();

    SandBoxedSecurityHttpBuilder() {
        sharedObjects.put(
            AuthenticationManager.class,
            (AuthenticationManager) authentication -> null
        );
    }

    @Override
    public <C extends SecurityConfigurer<DefaultSecurityFilterChain,
        SandBoxedSecurityHttpBuilder>> C getConfigurer(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C extends SecurityConfigurer<DefaultSecurityFilterChain,
        SandBoxedSecurityHttpBuilder>> C removeConfigurer(Class<C> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <C> void setSharedObject(Class<C> sharedType, C object) {
        this.sharedObjects.put(sharedType, object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C> C getSharedObject(Class<C> sharedType) {
        return (C) this.sharedObjects.get(sharedType);
    }

    @Override
    public SandBoxedSecurityHttpBuilder authenticationProvider(AuthenticationProvider authenticationProvider) {
        return this;
    }

    @Override
    public SandBoxedSecurityHttpBuilder userDetailsService(UserDetailsService userDetailsService)
        throws Exception {
        return this;
    }

    @Override
    public SandBoxedSecurityHttpBuilder addFilterAfter(
        Filter filter, Class<?
        extends Filter> afterFilter
    ) {
        return this;
    }

    @Override
    public SandBoxedSecurityHttpBuilder addFilterBefore(
        Filter filter, Class<
        ? extends Filter> beforeFilter
    ) {
        return this;
    }

    @Override
    public SandBoxedSecurityHttpBuilder addFilter(Filter filter) {
        return this;
    }

    @Override
    public DefaultSecurityFilterChain build() throws Exception {
        throw new UnsupportedOperationException();
    }
}
