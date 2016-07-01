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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.yaio.commons.net.NetFirewall;
import de.yaio.commons.net.NetFirewallFactory;

/** 
 * services to create webshots
 */
@Service
class WebshotProvider {
    private static final Logger LOGGER = Logger.getLogger(WebshotProvider.class);

    @Value("${yaio-webshot-service.timeout}")
    private int timeout;

    @Value("${yaio-webshot-service.buffersize}")
    private int BUFFER_SIZE;

    @Value("${yaio-webshot-service.html2pdf.bin}")
    private String HTML2PDF;
    @Value("${yaio-webshot-service.html2pdf.defaultoptions}")
    private String HTML2PDF_DEFAULTOPTIONS; 

    @Value("${yaio-webshot-service.html2png.bin}")
    private String HTML2PNG;
    @Value("${yaio-webshot-service.html2png.defaultoptions}")
    private String HTML2PNG_DEFAULTOPTIONS; 

    @Autowired
    protected WebShotFirewallConfig firewallConfig;
    
    protected NetFirewall netFirewall;
    
    public File shotUrl2Pdf(String url) throws IOException {
        LOGGER.info("start shotUrl2Pdf for url:" + url);
        // check url
        if (!getNetFirewall().isUrlAllowed(url)) {
            LOGGER.warn("illegal request for url:" + url 
                            + " disallowed by NetFirewall:" 
                            + new ReflectionToStringBuilder(getNetFirewall(), ToStringStyle.SHORT_PREFIX_STYLE).toString()
                            + " with config:" 
                            + new ReflectionToStringBuilder(firewallConfig, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            throw new IOException("illegal request for url:" + url 
                            + " disallowed by NetFirewall");
        }

        File tmpFile = File.createTempFile("yaio-webshot-service", ".pdf");
        tmpFile.deleteOnExit();
        String fileName = tmpFile.getAbsolutePath();
        String[] baseCommand = HTML2PDF_DEFAULTOPTIONS.split(" ");
        String[] params = concatenate(baseCommand, new String[]{url, fileName});
        
        runCommand(HTML2PDF, params, timeout * 1000);
        return tmpFile;
    }

    public File shotUrl2Png(String url) throws IOException {
        LOGGER.info("start shotUrl2Png for url:" + url);
        // check url
        if (!getNetFirewall().isUrlAllowed(url)) {
            LOGGER.warn("illegal request for url:" + url 
                            + " disallowed by NetFirewall:" 
                            + new ReflectionToStringBuilder(getNetFirewall(), ToStringStyle.SHORT_PREFIX_STYLE).toString()
                            + " with config:" 
                            + new ReflectionToStringBuilder(firewallConfig, ToStringStyle.SHORT_PREFIX_STYLE).toString());
            throw new IOException("illegal request for url:" + url 
                            + " disallowed by NetFirewall");
        }
        

        File tmpFile = File.createTempFile("yaio-webshot-service", ".png");
        tmpFile.deleteOnExit();
        String fileName = tmpFile.getAbsolutePath();
        String[] baseCommand = HTML2PNG_DEFAULTOPTIONS.split(" ");
        String[] params = concatenate(baseCommand, new String[]{url, fileName});

        runCommand(HTML2PNG, params, timeout * 1000);
        return tmpFile;
    }

    public void downloadResultFile(HttpServletRequest request,
                                   HttpServletResponse response, 
                                   File downloadFile) throws IOException {
        // construct the complete absolute path of the file
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        ServletContext context = request.getServletContext();
        String mimeType = context.getMimeType(downloadFile.getAbsolutePath());

        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MIME type: " + mimeType);
        }

        // set content attributes for the response
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());

        // set headers for the response
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                        downloadFile.getName());
        response.setHeader(headerKey, headerValue);

        // get output stream of the response
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;

        // write bytes read from the input stream into the output stream
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outStream.close();
    }

    protected WebShotResultHandler runCommand(final String command, 
                                              final String[] params, 
                                              final long jobTimeout) throws IOException {
        int exitValue;
        boolean inBackground = false;
        ExecuteWatchdog watchdog = null;
        WebShotResultHandler resultHandler;

        // build up the command line to using a 'java.io.File'
        final CommandLine commandLine = new CommandLine(command);
        commandLine.addArguments(params);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("start command " + command 
                        + " with params" + new ReflectionToStringBuilder(
                                        params, ToStringStyle.SHORT_PREFIX_STYLE).toString());
        }

        // create the executor and consider the exitValue '1' as success
        final Executor executor = new DefaultExecutor();
        executor.setExitValue(0);

        // create a watchdog if requested
        if (jobTimeout > 0) {
            watchdog = new ExecuteWatchdog(jobTimeout);
            executor.setWatchdog(watchdog);
        }

        if (inBackground) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[WebShot] Executing non-blocking WebShot job  ...");
            }
            resultHandler = new WebShotResultHandler(watchdog);
            executor.execute(commandLine, resultHandler);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[WebShot] Executing blocking WebShot job  ...");
            }
            exitValue = executor.execute(commandLine);
            resultHandler = new WebShotResultHandler(exitValue);
        }

        return resultHandler;
    }

    private class WebShotResultHandler extends DefaultExecuteResultHandler {

        private ExecuteWatchdog watchdog;

        public WebShotResultHandler(final ExecuteWatchdog watchdog) {
            this.watchdog = watchdog;
        }

        public WebShotResultHandler(final int exitValue) {
            super.onProcessComplete(exitValue);
        }

        @Override
        public void onProcessComplete(final int exitValue) {
            super.onProcessComplete(exitValue);
            LOGGER.info("[WebShotResultHandler] The document was successfully shot ...");
        }

        @Override
        public void onProcessFailed(final ExecuteException e) {
            super.onProcessFailed(e);
            if (watchdog != null && watchdog.killedProcess()) {
                LOGGER.warn("[WebShotResultHandler] The shot process timed out");
            } else {
                LOGGER.warn("[WebShotResultHandler] The shot process failed to do : " + e.getMessage());
            }
        }
    }

    protected NetFirewall getNetFirewall() {
        if (netFirewall == null) {
            netFirewall = NetFirewallFactory.creatNetFirewall(firewallConfig);
        }
        return netFirewall;
    }

    private static <T> T[] concatenate(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
