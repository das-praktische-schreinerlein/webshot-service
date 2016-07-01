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
package de.yaio.services.webshot.server;

import de.yaio.services.webshot.server.configuration.WebshotServerConfig;
import org.springframework.boot.SpringApplication;

/** 
 * the webshot-service as spring boot application
 */
public class WebshotApplication {

    /** 
     * Main-method to start the application
     * @param args                   the command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WebshotServerConfig.class, args);
    }
}