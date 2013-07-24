package etsycustomer

enum InventoryRequestState {
    PENDING, OPEN, INPROCESS, FULFILLED
}
@SuppressWarnings("GroovyAssignabilityCheck")
class InventoryRequest {
    Inventory inventory
    InventoryItem item
    Integer quantity
    InventoryRequestState state = InventoryRequestState.OPEN
    Location location
    Map props = [:]
    String propsValue = ""


    static transients = ['props']

    static constraints = {
        item(nullable: true)
        inventory(nullable: false)
        location(nullable: true)
        propsValue(maxSize: 5000)
    }

    static mapping = {
        propsValue type: 'text'
    }

    void fulfilled() {
        this.state = InventoryRequestState.FULFILLED
        this.save()
    }

    void inprocess() {
        this.state = InventoryRequestState.INPROCESS
        this.save()
    }

    String toString() {
        return inventory.toString() + "[${quantity}]"
    }

    String variations() {
        def retVal = []
        if (props.size() > 0) {
            props.each { k, v ->
                retVal << "${k}=${v}"
            }
        }
        return retVal
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
