package etsycustomer

@SuppressWarnings(["GroovyAssignabilityCheck", "UnnecessaryQualifiedReference"])
class Inventory {
    def workService

    static transients = ['workService', 'props']

    BOM bom
    ImageContainer images
    String name
    String description
    Integer onHand = 0
    Integer lowLevel = 0
    float cost = 0 // average cost of all inventory per item.
    Integer total = 0 // Total of all items created.
    Map props = [:]
    String propsValue = ""
    Boolean material = false

    static hasMany = [inventory: InventoryItem]
    static constraints = {
        onHand(min: 0)
        lowLevel(min: 0)
        name(blank: false, maxSize: 254)
        description(blank: true, nullable: true, maxSize: 65000)
        bom(nullable: true)
        propsValue(maxSize: 5000)
        images(nullable: true)
    }
    static mapping = {
        description type: 'text'
        propsValue type: 'text'
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

    String toString() {
        return this.name
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
            props.each { k, v ->
                propsValue += k + ":" + v + "," }
        }
    }

    def afterLoad() {
        if (propsValue.size() > 0) {
            for (value in propsValue.split(/,/)) {
                def values = value.split(/:/)
                if (values.size() > 1) {
                    props.putAt(values[0], values[1])
                }
            }
        }
    }


    Integer consume(Integer desired, Map props) {
        if (this.request(desired, props)) {
            return desired
        } else {
            //if(fulfill(desired)) {
            //	return desired
        }
        return 0
    }

    // Does Not request to get more or make more.
    boolean canTake(Integer desired, Map props) {
        log.debug "Props : ${props} ignored"
        if (onHand - desired < lowLevel) {
            return false
        } else {
            onHand -= desired
            Integer needed = desired
            // Find the InventoryItems and remove the number required By changing the quantity.
            inventory.each { item ->
                if (needed > 0) {
                    if (needed >= item.quantity) {
                        needed -= item.quantity
                        item.quantity = 0
                    } else {
                        item.quantity -= needed
                        needed = 0
                    }
                    item.save()
                }
            }
            this.save(flush: true)
        }
        return true
    }

    // If there is not inventory then ask for more to be created.
    def request(Integer desired, Map props) {
        // If the requested quantity puts the quantity below the lowLevel then we need to request more Inventory
        if (!canTake(desired, props)) {
            def wi = this.fulfill(desired, props)
            return wi?.irequest
        }
        return null
    }
    // This handles old non-property calls. good for Raw Materials
    def addItem(Integer quantity, def location, float cost) {
        return addItem(quantity, location, cost, [:])
    }

    def addItem(Integer quantity, def location, float cost, Map props) {

        Date now = new Date()
        // Add InventoryItem to inventory in the Inventory.
        def retval = new InventoryItem(location: location, dateCreated: now, quantity: quantity, cost: cost, model: this, props: props)
        retval.save()
        this.addToInventory(retval)

        // Add the quantity to the amount onHand
        this.onHand += quantity

        // Average out the cost of the inventory item based on what the new cost coming in is.
        float spent = (this.total * this.cost) + (quantity * cost)
        this.total += quantity
        this.cost = spent / this.total

        this.save(flush: true)

        workService.itemAdded(this, quantity, props)
        return retval
    }

    String _rawName(Map props) {
        String rawname = ""
        props.each { k, v ->
            rawname += " ${v}"
        }
        rawname += " ${name}"
        return rawname
    }

    def _createSpecificRawMaterial(Map props) {
        def rawname = this._rawName(props)
        def propString = ""
        props.each { k, v ->
            propString += "${k}= ${v}, "
        }
        def raw = RawMaterial.findOrCreateWhere(name: rawname, description: "Raw Material created from variations: " + propString)
        raw.props = props
        raw.save()
        this.bom.firstStep.addToInputs(item: raw, quantity: 1, props: props)
        return raw
    }
    // This should be overridden to handle raw materials.
    def fulfill(Integer pQuantity, Map props) {

        if (material) {
            // Check and see if the variation already exists if it does then call fulfill
            // if it does not then create it and call fulfill
            String rawname = this._rawName(props)
            def raw = RawMaterial.findWhere(name: rawname)
            if (!raw) {
                this._createSpecificRawMaterial(props)
            }
        }
        // Raw Materials do not have a bom.
        if (!bom) {
            // Raw Materials with properties defined should be broken down more.
            // Example Paint(Color=White) should be made from "White Paint"
            // So if someone is trying to fulfill a inventory item with a property we should
            // Automatically create a BOM and BOMStep that has a new inventory item in.

            log.info "Parameterized Inventory found with no bom. ${this}"
            if (props?.size() > 0) {
                // Set the flag so that when another variation of the material
                // comes through it goes to the proper location.
                this.material = true
                def store = Location.findOrCreateWhere(name: "Store")
                store.save()
                this.bom = new BOM(inventory: this)
                this.save(flush: true)
                this.bom.firstStep = new BOMMaterialStep(steps: "Buy the Raw Material", location: store, automatic: true)
                this.bom.firstStep.save()
                this.bom.firstStep.addToOutputs(item: this, quantity: 1, props: props)
                this.save()
                // Call fulfill again with the new connections.
                // This only works for the first prop file through we need to make the fulfill smarter.
                // It needs to handle looking at properties being handled below it.
                // We need to flag this so it can be handled properly.
                log.info "Reset expectations Calling fulfill for ${this} again"
                this.fulfill(pQuantity, props)
            } else {
                def myRequest = InventoryRequest.findWhere(state: InventoryRequestState.OPEN, item: this)

                boolean match = true
                if (myRequest) {
                    // Check that all of the props are the same for the inventory request.
                    props.each { key, value ->
                        if (myRequest.props.containsKey(key)) {
                            if (myRequest.props.get(key) != value) {
                                match = false
                            }
                        } else {
                            match = false
                        }
                    }
                }
                if (!match || !myRequest) {
                    log.info "Create Inventory Request for ${this}"
                    myRequest = new InventoryRequest(inventory: this, quantity: 0)
                    myRequest.props = props
                }
                myRequest.quantity += pQuantity
                myRequest.save(flush: true)

                return null
            }
        } else {
            log.info "Process Step for ${this}"
            def results = this.bom.firstStep.process(pQuantity, props)
            return results
        }
    }
}
