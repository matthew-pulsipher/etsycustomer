package etsycustomer

import uk.co.desirableobjects.oauth.scribe.OauthService

class AdminController {
    OauthService oauthService

    def index() {
        Token gtoken = Token.findByServiceName("google")
        Token etoken = Token.findByServiceName("etsy")
        println etoken
//        if(etoken) {
//
//            def resp =  oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/shops/DaisyBlossomCreation/transactions?limit=100")
//            def js = JSON.parse(resp.parseBodyContents())
//            println js.count
//        }
        [google: gtoken, etsy: etoken]
    }

    def connectEtsy() {
        String sessionKey = oauthService.findSessionKeyForAccessToken('etsy')
        org.scribe.model.Token stoken = session[sessionKey] as org.scribe.model.Token
        Token token = new Token(serviceName: "etsy")
        token.token = stoken
        token.save(flush: true)
        redirect(url: "/")
    }

    def connectGoogle() {
        String sessionKey = oauthService.findSessionKeyForAccessToken('google')
        Token token = new Token(serviceName: "google")
        token.token = session[sessionKey] as org.scribe.model.Token
        token.save(flush: true)
        redirect(url: "/")
    }
}

