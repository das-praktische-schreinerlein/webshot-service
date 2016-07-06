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
package de.yaio.services.webshot.server.controller;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.yaio.commons.io.IOExceptionWithCause;
import de.yaio.commons.net.PermissionException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

/** 
 * controller with Webshot-Services to create and download a webshot of a webpage
 */
@Controller
@RequestMapping("${yaio-webshot-service.baseurl}")
public class WebshotController {

    @Autowired
    protected WebshotProvider converterUtils;

    private static final Logger LOGGER = Logger.getLogger(WebshotController.class);

    /**
     * Request to generate pdf-webshot for url
     * @param url                    the url to shot
     * @param request                the request-obj to get the servlet-context 
     * @param response               the response-Obj to set contenttype and headers
     * @throws IOException           possible
     */
    @RequestMapping(method = RequestMethod.POST, 
                    value = "/url2pdf",
                    produces = "application/pdf")
    public @ResponseBody void shotUrl2Pdf(@RequestParam(value="url", required=true) String url,
                                          HttpServletRequest request, HttpServletResponse response)
            throws IOException, IOExceptionWithCause, PermissionException {
        File tmpFile = converterUtils.shotUrl2Pdf(url);
        converterUtils.downloadResultFile(request, response, tmpFile);
    }
    
    /** 
     * Request to generate png-webshot for url
     * @param url                    the url to shot
     * @param request                the request-obj to get the servlet-context 
     * @param response               the response-Obj to set contenttype and headers
     * @throws IOException           possible
     */
    @RequestMapping(method = RequestMethod.POST, 
                    value = "/url2png",
                    produces = "image/png")
    public @ResponseBody void shotUrl2Png(@RequestParam(value="url", required=true) String url,
                                          HttpServletRequest request, HttpServletResponse response)
            throws IOException, IOExceptionWithCause, PermissionException {
        File tmpFile = converterUtils.shotUrl2Png(url);
        converterUtils.downloadResultFile(request, response, tmpFile);
    }

    @ExceptionHandler(PermissionException.class)
    public void handleCustomException(final HttpServletRequest request, final PermissionException e,
                                      final HttpServletResponse response) throws IOException {
        LOGGER.info("Exception while running request:" + createRequestLogMessage(request), e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().append("permission denied (firewall...) while webshoting resource");
    }

    @ExceptionHandler(IOExceptionWithCause.class)
    public void handleCustomException(final HttpServletRequest request, final IOExceptionWithCause e,
                                      final HttpServletResponse response) throws IOException {
        LOGGER.info("Exception while running request:" + createRequestLogMessage(request), e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().append("url-get failed while webshoting resource:");
        response.getWriter().append(e.getCause().getMessage());
    }

    @ExceptionHandler(value = {Exception.class, RuntimeException.class, IOException.class})
    public void handleAllException(final HttpServletRequest request, final Exception e,
                                   final HttpServletResponse response) {
        LOGGER.info("Exception while running request:" + createRequestLogMessage(request), e);
        response.setStatus(SC_INTERNAL_SERVER_ERROR);
        try {
            response.getWriter().append("exception while webshoting for requested resource");
        } catch (IOException ex) {
            LOGGER.warn("exception while exceptionhandling", ex);
        }
    }

    protected String createRequestLogMessage(HttpServletRequest request) {
        return new StringBuilder("REST Request - ")
                .append("[HTTP METHOD:")
                .append(request.getMethod())
                .append("] [URL:")
                .append(request.getRequestURL())
                .append("] [REQUEST PARAMETERS:")
                .append(getRequestMap(request))
                .append("] [REMOTE ADDRESS:")
                .append(request.getRemoteAddr())
                .append("]").toString();
    }

    private Map<String, String> getRequestMap(HttpServletRequest request) {
        Map<String, String> typesafeRequestMap = new HashMap<>();
        Enumeration<?> requestParamNames = request.getParameterNames();
        while (requestParamNames.hasMoreElements()) {
            String requestParamName = (String)requestParamNames.nextElement();
            String requestParamValue = request.getParameter(requestParamName);
            typesafeRequestMap.put(requestParamName, requestParamValue);
        }
        return typesafeRequestMap;
    }
}