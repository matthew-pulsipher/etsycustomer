package com.yoly.merced

import groovy.xml.MarkupBuilder


class CustomerService {
    def etsyService
    def googleService
    def registerEtsy() {

    }

    def registerGoogle() {

    }

    def customersFromEtsy() {
        def retVal = [:]
        return retVal ;
    }

    def populateGoogleContacts(def customers) {
        customers.firstName = "Fred"
        customers.lastName = "Stonemeyer"
        customers.email = "fstonemeyer@yahoo.com"
        customers.username = "fstonemeyer"
        customers.address.city = "provo"
        customers.address.street = "500 N 900 S"
        customers.address.state = "UT"
        customers.address.zip = "84604"
        customers.address.country = "United States"

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.'atom:entry'('xmlns:atom': 'http://www.w3.org/2005/Atom', 'xmlns:gd': 'http://schemas.google.com/g/2005')
        {
            'atom:category'(scheme:'http://schemas.google.com/g/2005#kind', term:'http://schemas.google.com/contact/2008#contact')
            'gd:name'(){
                  'gd:givenName'('Fred')
                  'gd:familyName'('Stonemeyer')
                  'gd:fullName'('Fred Stonemeyer')
              }

            'gd:email'(rel:'http://schemas.google.com/g/2005#home', primary:'true',address:'fstonemeyer@yahoo.com',displayName:'fstonemeyer')

              'gd.structuredPostalAddress'(rel:'http://schemas.google.com/g/2005#home',primary:'true'){
                  'gd:city'('Provo')
                  'gd:street'('500 N 900 S')
                  'gd:region'('Utah')
                  'gd:postcode'('84604')
                  'gd:country'('United States')
              }

        }


        println(writer.toString());

    }

}
