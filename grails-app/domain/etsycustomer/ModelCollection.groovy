package etsycustomer

class ModelCollection {
    String name
    String description
    Date beginDate
    Date endDate

    static belongsTo = ProductModel

    static hasMany = [models:ProductModel]

    static constraints = {
        name blank:false
        description blank:false
        beginDate nullable:true
        endDate nullable:true
    }

    String toString() {
        return name
    }
}