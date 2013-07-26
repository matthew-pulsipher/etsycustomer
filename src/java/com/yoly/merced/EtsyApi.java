/**
 * Created with IntelliJ IDEA.
 * User: dwpulsip
 * Date: 5/21/13
 * Time: 8:13 AM
 *
 */
package com.yoly.merced;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Verb;

public class EtsyApi extends DefaultApi10a {
    private static final String AUTHORIZATION_URL = "https://www.etsy.com/oauth/signin?oauth_token=%s";

    @Override
    public String getAccessTokenEndpoint() {
        return "https://openapi.etsy.com/v2/oauth/access_token";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "https://openapi.etsy.com/v2/oauth/request_token";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.GET;
    }

    @Override
    public Verb getRequestTokenVerb() {
        return Verb.GET;
    }

    @Override
    public String getAuthorizationUrl(org.scribe.model.Token token) {
        // Get the consumer key
        return String.format(AUTHORIZATION_URL, token.getToken());
    }
}
