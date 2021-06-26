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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * Wrapper for {@link ExpressionUrlAuthorizationConfigurer} based on
 * {@link SandBoxedSecurityHttpBuilder}.
 */
final class SandBoxedUrlAuthorizationConfigurer {

    private final ExpressionUrlAuthorizationConfigurer<SandBoxedSecurityHttpBuilder>
        configurer;

    private final SandBoxedSecurityHttpBuilder builder;

    SandBoxedUrlAuthorizationConfigurer(ApplicationContext applicationContext) {
        this.configurer =
            new ExpressionUrlAuthorizationConfigurer<>(applicationContext);
        this.configurer.withObjectPostProcessor(new ObjectPostProcessor<>() {
            @Override
            public <O> O postProcess(O object) {
                return object;
            }
        });
        builder = new SandBoxedSecurityHttpBuilder();
    }

    SandBoxedUrlAuthorizationConfigurer() {
       this(new SandBoxedApplicationContext());
    }

    public SandBoxedUrlAuthorizationConfigurer authorizeRequests(
        Customizer<ExpressionUrlAuthorizationConfigurer<?>.ExpressionInterceptUrlRegistry> customizer
    ) {
        customizer.customize(this.configurer.getRegistry());
        return this;
    }

    public FilterInvocationSecurityMetadataSource getSecurityMetadataSource() {
        try {
            this.configurer.configure(this.builder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.builder
            .getSharedObject(FilterSecurityInterceptor.class)
            .getSecurityMetadataSource();
    }

}
