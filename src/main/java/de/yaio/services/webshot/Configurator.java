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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/** 
 * baseclass for configuration
 */
public class Configurator {

    private static final Logger LOGGER = Logger.getLogger(Configurator.class);

    // must be instantiated after LOGGER because it is used in constructor
    protected static final Configurator instance = new Configurator();
    
    protected Configurator() {
        initConfigurator();
    }
    
    /* 
     ***********************
     ***********************
     * base
     ***********************
     ***********************
     */
    protected void initConfigurator() {
    }
    
    
    /** 
     * return the current static Configurator-instance
     * @return                       the current Configurator-instance
     * @throws Exception             parse/io-Exceptions possible
     */
    public static Configurator getInstance() {
        return instance;
    }
    

    /* 
     ***********************
     ***********************
     * public service-functions
     ***********************
     ***********************
     */
    
    /** 
     * read the properties from the given filepath (first by filesystem, 
     * if failed by classpath)
     * @param filePath               path to the file (filesystem or classressource)
     * @return                       the properties read from propertyfile
     * @throws Exception             parse/io-Exceptions possible
     */
    public static Properties readProperties(final String filePath) throws Exception {
        Properties prop = new Properties();
        
        // first try it from fileystem
        try {
            InputStream in = new FileInputStream(new File(filePath));
            prop.load(in);
            in.close();
            //CHECKSTYLE.OFF: IllegalCatch - Much more readable than catching x exceptions
        } catch (Throwable ex) {
            //CHECKSTYLE.ON: IllegalCatch
            // try it from jar
            try {
                InputStream in = instance.getClass().getResourceAsStream(filePath);
                prop.load(in);
                in.close();
                //CHECKSTYLE.OFF: IllegalCatch - Much more readable than catching x exceptions
            } catch (Throwable ex2) {
                //CHECKSTYLE.ON: IllegalCatch
                throw new Exception("cant read propertiesfile: " + filePath 
                                + " Exception1:" + ex
                                + " Exception2:" + ex2);
            }
        }
        return prop;
    }
}
