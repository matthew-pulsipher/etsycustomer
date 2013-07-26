package com.yoly.merced

import grails.plugin.spock.IntegrationSpec

class UseCaseSpec extends IntegrationSpec {
    def customerService

    def setup() {
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