package com.yoly.merced

import etsycustomer.Token
import grails.plugin.spock.IntegrationSpec

class UseCaseSpec extends IntegrationSpec {
    def customerService

    def setup() {
        def gToken = new Token(serviceName:"google", accessToken:"1/vXOkhZMN_QxseMkA8ivqZbwqqCE5CYeXUC_MRMjJMmk", secret:"uq2Yttu0QsfKfNaWtN_q961d", rawResponse:"oauth_token=1%2FvXOkhZMN_QxseMkA8ivqZbwqqCE5CYeXUC_MRMjJMmk&oauth_token_secret=uq2Yttu0QsfKfNaWtN_q961d")
        gToken.save(flush:true)
        def eToken = new Token(serviceName:"etsy", accessToken:"c091f941777b92d2789951dd58378d", secret:"7a4ea6e828", rawResponse:"oauth_token=c091f941777b92d2789951dd58378d&oauth_token_secret=7a4ea6e828")
        eToken.save(flush:true)
    }

    def cleanup() {
    }

    void "User wants to select customers based on criteria and email them"() {

        given:
        customerService.registerEtsy()
        customerService.registerGoogle()
        def customers = customerService.customersFromEtsy()
        customerService.populateGoogleContacts(customers)

        expect:

        assert true
    }

    void "User wants to send postal mail to customers based on criteria"() {
        expect:
        assert true
    }

    void "User wants to see stats on customer location"() {
        expect:
        assert true
    }

    void "User would like to rank the customers based on the number of products purchased"() {
        expect:
        assert true
    }

    void "Download customers information email, phone, address to google contacts"() {
        expect:
        assert true
    }
}