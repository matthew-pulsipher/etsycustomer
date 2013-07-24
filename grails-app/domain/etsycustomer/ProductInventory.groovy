package etsycustomer

class ProductInventory {
    Location location
    Float cost // Calculated from the generation of the BOM.
    Float price
    Integer quantity

    static belongsTo = [model:ProductModel]
    static constraints = {
    }
}