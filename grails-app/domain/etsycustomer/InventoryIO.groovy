package etsycustomer

@SuppressWarnings("GroovyAssignabilityCheck")
class InventoryIO {
    Inventory item
    Integer quantity = 0
    Integer taken = 0
    Map props = [:]
    String propsValue = ""

    static transients = ['props']

    static constraints = {
        item(nullable: true)
        propsValue(maxsize: 5000)
    }

    static mapping = {
        propsValue type: 'text'
    }

    String toString() {
        if (this.quantity > 1) {
            return "${this.item} [${this.quantity}]"
        } else {
            return this.item
        }
    }

    String variations() {
        def retVal = []
        if (props.size() > 0) {
            props.each { k, v ->
                retVal << "${k}=${v}"
            }
        }
        return retVal.join(',')
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
