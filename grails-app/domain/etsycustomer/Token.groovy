package etsycustomer


class Token {
    static transients = ['token']
    org.scribe.model.Token token

    String serviceName
    String accessToken
    String secret
    String rawResponse

    static constraints = {
        serviceName(nullable: false)
        accessToken(nullable: true)
        secret(nullable: true)
        rawResponse(nullable: true)
        token(nullable: true)
    }

    def beforeInsert() {
        if (token) {
            accessToken = token.getToken()
            secret = token.getSecret()
            rawResponse = token.getRawResponse()
        }
    }

    def beforeUpdate() {
        if (token) {
            accessToken = token.getToken()
            secret = token.getSecret()
            rawResponse = token.getRawResponse()
        }
    }

    def afterLoad() {
        token = new org.scribe.model.Token(accessToken, secret, rawResponse)
    }
}
