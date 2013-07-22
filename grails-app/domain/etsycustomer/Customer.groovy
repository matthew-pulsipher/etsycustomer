package etsycustomer

class Customer {
    Integer userID;
    String userName;
    String email;
    Integer referredByUser;
    static hasMany = [transactions: Transaction, addresses: Address]
    static constraints = {
        userID(blank: false)
        userName(blank: false)
        email(blank: false, email: true)
        referredByUser(blank: true, nullable: true)
    }
}
