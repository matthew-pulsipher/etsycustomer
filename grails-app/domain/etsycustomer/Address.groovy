package etsycustomer

class Address {
    String addressName;
    String firstLine;
    String secondLine;
    String city;
    String state;
    String zip;
    String country_name;

    static constraints = {
        addressName(blank: false)
        firstLine(blank: false)
        secondLine(blank: true)
        city(blank: false)
        state(blank: true)
        zip(blank: true)
        country_name(blank: false)
    }
}