Yaio - Webshot-Service
=====================

# Desc
A webservice to create screenshots of a url with help of [wkhtmltopdf](https://github.com/wkhtmltopdf/wkhtmltopdf).

# Build and run
- configure path to Wkhtmltopdf in config/webshot-application.properties
- test it

        mvn install
        java -Xmx768m -Xms128m -Dspring.config.location=file:config/webshot-application.properties -Dlog4j.configuration=file:config/log4j.properties -cp "dist/yaio-webshot-service-full.jar" de.yaio.services.webshot.server.WebshotApplication --config config/webshot-application.properties
        curl --user webshot:secret -X POST http://localhost:8081/services/webshot/url2pdf -s --data 'url=http://www.google.de' > /cygdrive/d/tmp/testresult.pdf
        curl --user webshot:secret -X POST http://localhost:8081/services/webshot/url2png -s --data 'url=http://www.google.de' > /cygdrive/d/tmp/testresult.png

# Thanks to
- **Build-Tools**
    - [Apache Maven](https://github.com/apache/maven)
    - [Eclipse](http://eclipse.org/)
- **Java-Core-Frameworks**
    - [Spring-Framework](https://github.com/spring-projects/spring-framework)
    - [Spring-boot](https://github.com/spring-projects/spring-boot)
    - [Spring Security](https://github.com/spring-projects/spring-security)
- **Wkhtmltopdf**
    - [Wkhtmltopdf](https://github.com/wkhtmltopdf/wkhtmltopdf)

# License
    /**
     * @author Michael Schreiner <michael.schreiner@your-it-fellow.de>
     * @category collaboration
     * @copyright Copyright (c) 2010-2014, Michael Schreiner
     * @license http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
     *
     * This Source Code Form is subject to the terms of the Mozilla Public
     * License, v. 2.0. If a copy of the MPL was not distributed with this
     * file, You can obtain one at http://mozilla.org/MPL/2.0/.
     */
