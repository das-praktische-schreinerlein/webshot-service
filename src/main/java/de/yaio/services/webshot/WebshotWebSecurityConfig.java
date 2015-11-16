/** 
 * software for webshots
 * 
 * @FeatureDomain                Converter
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     webshot-services
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.services.webshot;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

/** 
 * secure the webshot-service
 *  
 * @FeatureDomain                Config
 * @package                      de.yaio.services.webshot
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     WebSecurityConfig
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
@EnableWebSecurity
@Component
@Order(30)
public class WebshotWebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final Logger logger = Logger.getLogger(WebshotWebSecurityConfig.class);
    
    /**
     * configure API-Configuration for webshot-service
     */
    @EnableWebSecurity
    @Configuration
    @Order(8)
    public static class WebshotServiceSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Value("${yaio-webshot-service.baseurl}")
        protected String webshotBaseUrl;

        @Value("${yaio-webshot-service.security.apiusers.filelocation}")
        protected String usersFile;

        @Value("${yaio-webshot-service.security.useown}")
        protected Boolean flgSecureByMyOwn;

        private CsrfTokenRepository csrfTokenRepository() {
            HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
            repository.setHeaderName("X-XSRF-TOKEN");
            return repository;
        }

        @Autowired
        public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
            if (! flgSecureByMyOwn) {
                return;
            }
            
            // secure it by my own
            Properties users = Configurator.readProperties(usersFile);
            InMemoryUserDetailsManager im = new InMemoryUserDetailsManager(users);
            auth.userDetailsService(im);
        }

        protected void configure(final HttpSecurity http) throws Exception {
            if (! flgSecureByMyOwn) {
                return;
            }
            
            // secure it by my own
            http
                    // authentification
                    .httpBasic()
                .and()
                    // secure path
                    .requestMatcher(new AntPathRequestMatcher(webshotBaseUrl + "/**", "POST"))
                        .authorizeRequests()
                    // secure API webservice
                    .anyRequest()
                        .hasRole("WEBSHOT")
                .and()
                   // disable csrf-protection
                   .csrf().disable();
        }
    }    
}
