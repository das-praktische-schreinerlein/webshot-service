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
package de.yaio.services.webshot.controller;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/** 
 * controller with Webshot-Services to create and download a webshot of a webpage
 *  
 * @FeatureDomain                Webservice
 * @package                      de.yaio.services.plantuml.controller
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     diagram-services
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
@Controller
@RequestMapping("${yaio-webshot-service.baseurl}")
public class WebshotController {

    private static final Logger LOGGER = Logger.getLogger(WebshotController.class);

    @Autowired
    protected WebshotProvider converterUtils;

    /** 
     * Request to generate pdf-webshot for url
     * @FeatureDomain                Webservice
     * @FeatureResult                send image of the webpage via HttpServletResponse
     * @FeatureKeywords              Webservice
     * @param url                    the url to shot
     * @param request                the request-obj to get the servlet-context 
     * @param response               the response-Obj to set contenttype and headers
     * @throws IOException           possible
     */
    @RequestMapping(method = RequestMethod.POST, 
                    value = "/url2pdf",
                    produces = "application/pdf")
    public @ResponseBody void shotUrl2Pdf(@RequestParam(value="url", required=true) String url,
                                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            File tmpFile = converterUtils.shotUrl2Pdf(url);
            converterUtils.downloadResultFile(request, response, tmpFile);
        } catch (Exception e) {
            LOGGER.warn("exception start for url:" + url, e);
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
        }
    }
    
    /** 
     * Request to generate png-webshot for url
     * @FeatureDomain                Webservice
     * @FeatureResult                send image of the webpage via HttpServletResponse
     * @FeatureKeywords              Webservice
     * @param url                    the url to shot
     * @param request                the request-obj to get the servlet-context 
     * @param response               the response-Obj to set contenttype and headers
     * @throws IOException           possible
     */
    @RequestMapping(method = RequestMethod.POST, 
                    value = "/url2png",
                    produces = "image/png")
    public @ResponseBody void shotUrl2Png(@RequestParam(value="url", required=true) String url,
                                          HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            File tmpFile = converterUtils.shotUrl2Png(url);
            converterUtils.downloadResultFile(request, response, tmpFile);
        } catch (Exception e) {
            LOGGER.warn("exception start for url:" + url, e);
            response.setStatus(404);
            response.getWriter().append("error while reading:" + e.getMessage());
        }
    }
    
}