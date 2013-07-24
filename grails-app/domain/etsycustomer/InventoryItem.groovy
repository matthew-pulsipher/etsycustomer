package etsycustomer

class InventoryItem {
    Location location
    Date dateCreated
    Date lastUpdated
    Map props = [:]
    String propsValue = ""
    float cost = 0.0
    Integer quantity = 0
    static belongsTo = [model: Inventory]

    static constraints = {
        location(nullable: false)
        propsValue(maxSize: 5000)
    }

    static mapping = {
        propsValue type: 'text'
    }

    def beforeInsert() {
        if (props) {
            propsValue = ""
            props.each { k, v -> propsValue += k + ":" + v + "," }
        }
    }

    def beforeUpdate() {
        if (props) {
            propsValue = ""
            props.each { k, v -> propsValue += k + ":" + v + "," }
        }
    }

    def afterLoad() {
        if (propsValue.size() > 0) {
            propsValue.split(/,/).each { value ->
                def values = value.split(/:/)
                if (values.size() > 1)
                    props.putAt(values[0], values[1])
            }
        }
    }
}