/** 
 * software for projectmanagement and documentation
 * 
 * @FeatureDomain                Collaboration 
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     collaboration
 * @copyright                    Copyright (c) 2014, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.services.webshot.client;

import de.yaio.commons.http.HttpUtils;
import de.yaio.commons.io.IOExceptionWithCause;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** 
 * webshot-client
 * 
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 */
public class WebshotClient {
    protected String webshoturl;
    protected String webshotusername;
    protected String webshotpassword;

    protected WebshotClient(final String webshoturl, final String webshotusername,
                                final String webshotpassword) {
        this.webshoturl = webshoturl;
        this.webshotusername = webshotusername;
        this.webshotpassword = webshotpassword;
    }

    public enum FORMAT {
        PNG, PDF
    };

    public static WebshotClient createClient(final String webshoturl, final String webshotusername,
                               final String webshotpassword) {
        if (StringUtils.isEmpty(webshoturl)) {
            throw new IllegalArgumentException("cant create webshotclient: webshoturl must not be empty");
        }
        if (StringUtils.isEmpty(webshotusername)) {
            throw new IllegalArgumentException("cant create webshotclient: webshotusername must not be empty");
        }
        if (StringUtils.isEmpty(webshotpassword)) {
            throw new IllegalArgumentException("cant create webshotclient: webshotpassword must not be empty");
        }
        return new WebshotClient(webshoturl, webshotusername, webshotpassword);
    }

    /**
     * create a webshot of the url
     * @return                       returns the webshot as png-file
     * @param url                    url to make a webshot from
     * @throws IOException           if something went wrong
     */
    public byte[] getWebShotFromUrl(final String url) throws IOExceptionWithCause, IOException {
        return getWebShotFromUrl(url, FORMAT.PNG);
    }

    /**
     * create a webshot of the url
     * @return                       returns the webshot as png-file
     * @param url                    url to make a webshot from
     * @throws IOException           if something went wrong
     */
    public byte[] getWebShotFromUrl(final String url, final FORMAT format) throws IOExceptionWithCause, IOException {
        // get image from url
        Map<String, String> params = new HashMap<String, String>();
        params.put("url", url);

        // call url
        String baseUrl = webshoturl + "/url2" + format;
        HttpEntity entity;
        HttpResponse response;
        try {
            response = HttpUtils.callPostUrlPure(baseUrl,
                    webshotusername, webshotpassword, params, null, null);
            entity = response.getEntity();
        } catch (IOException ex) {
            throw new IOExceptionWithCause("error while calling webshot for url", url, ex);
        }

        // check response
        int retCode = response.getStatusLine().getStatusCode();
        if (retCode < 200 || retCode > 299) {
            throw new IOExceptionWithCause("error while calling webshot for url", url,
                    new IOException("illegal reponse:" + response.getStatusLine()
                            + " for baseurl:" + baseUrl + " with url:" + url
                            + " response:" + EntityUtils.toString(entity)));
        }

        return EntityUtils.toByteArray(entity);
    }
}
