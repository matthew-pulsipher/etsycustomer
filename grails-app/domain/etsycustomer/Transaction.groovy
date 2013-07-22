package etsycustomer

class Transaction {
    Integer listing_id;
    Integer quantity;
    String url;
    Float price;
    static constraints = {
        listing_id(blank: false)
        quantity(blank: false)
        url(blank: false)
        price(blank: false)
    }
}