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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Array;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** 
 * services to create webshots
 *  
 * @FeatureDomain                service
 * @package                      de.yaio.services.plantuml.controller
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     diagram-services
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 */
@Service
class WebshotUtils {
    
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

    public File shotUrl2Pdf(String url) throws IOException {
        File tmpFile = File.createTempFile("yaio-webshot-service", ".pdf");
        tmpFile.deleteOnExit();
        String fileName = tmpFile.getAbsolutePath();
        String[] baseCommand = concatenate(new String[] {HTML2PDF}, HTML2PDF_DEFAULTOPTIONS.split(" "));
        String[] command = concatenate(baseCommand, new String[]{url, fileName});
        System.err.println("start for url:" + url);
        runCommand(command);
        return tmpFile;
    }

    public File shotUrl2Png(String url) throws IOException {
        File tmpFile = File.createTempFile("yaio-webshot-service", ".png");
        tmpFile.deleteOnExit();
        String fileName = tmpFile.getAbsolutePath();
        String[] baseCommand = concatenate(new String[] {HTML2PNG}, HTML2PNG_DEFAULTOPTIONS.split(" "));
        String[] command = concatenate(baseCommand, new String[]{url, fileName});
        System.err.println("start for url:" + url);
        runCommand(command);
        return tmpFile;
    }

    public void downloadResultFile(HttpServletRequest request,
                                   HttpServletResponse response, File downloadFile) throws IOException {
        // construct the complete absolute path of the file
        FileInputStream inputStream = new FileInputStream(downloadFile);

        // get MIME type of the file
        ServletContext context = request.getServletContext();
        String mimeType = context.getMimeType(downloadFile.getAbsolutePath());

        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        System.err.println("MIME type: " + mimeType);

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

    private Integer runCommand(String[] command) throws IOException {
        if (isWindowsSystem()) {
            System.err.println("Windows System");
            runWindowsCommand(command);
        }
        else if (isLinuxSystem()) { 
            System.err.println("Linux System");
            // exec linux commands ...
        }
        else {
            System.err.println("Unknown System");
            System.exit(1);
        }
        return 1;
    }

    static void runWindowsCommand(String[] command) throws IOException {
        System.err.println("Windows command: " + command);
        String line;
        Process process = Runtime.getRuntime().exec(command);
        Reader r = new InputStreamReader(process.getInputStream());
        BufferedReader in = new BufferedReader(r);
        while((line = in.readLine()) != null) System.out.println(line);
        in.close();
        r.close();
    }

    static boolean isWindowsSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("windows") >= 0;
    }

    static boolean isLinuxSystem() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("linux") >= 0;
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
