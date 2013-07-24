package etsycustomer

@SuppressWarnings(["GroovyAssignabilityCheck", "UnnecessaryQualifiedReference"])
class ProductModel extends Inventory {

    // static belongsTo = [category:Category]
    ShippingItem packaging
    ShippedProduct shipped
    String listingId
    Float shippingPrice = 0.0
    Float price = 0.0

    static hasMany = [collections: ModelCollection, inventory: ProductInventory]

    static constraints = {
        packaging(nullable: true)
        shipped(nullable: true)
        listingId(nullable: true)
        shippingPrice(nullable: true)
    }

    String toString() {
        return this.name
    }

    def order(Map params) {
        // Request the item and create a Workitem for packaging the item in shipping.
        def myRequest = this.request(params.quantity, params.props)
        // Fix this to use the product shipping item.
        return myRequest
    }

    void ship(Map params) {
        // Find the item the params and ship it.
        // Decrease the size of the Product onHand

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

    def request(Integer desired, Map props) {
        // If the requested quantity puts the quantity below the lowLevel then we need to request more Inventory
        def myRequest = ProductRequest.findWhere(state: InventoryRequestState.OPEN, item: this)
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
            myRequest = new ProductRequest(inventory: this, quantity: 0)
            myRequest.props = props
        }
        myRequest.quantity += desired

        if (onHand - desired < lowLevel) {
            myRequest.workItem = this.fulfill(lowLevel - (onHand - desired), props)
        } else {
            onHand -= desired
            this.save(flush: true)
            myRequest.fulfilled()
        }
        myRequest.save(flush: true)
        return myRequest
    }

    Integer toBeBuilt() {
        def wis = WorkItem.findAllWhere(state: WorkItemState.BLOCKED)
        Integer tbb = 0
        wis.each { wi ->
            wi.outputs.each {
                if (it.item == this) {
                    tbb += wi.quantity
                }
            }
        }
        return tbb
    }

    def addItem(Integer quantity, Location location, float cost, Map props) {

        Date now = new Date()
        // Add InventoryItem to inventory in the Inventory.
        def retval
        this.addToInventory(retval = new InventoryItem(location: location, dateCreated: now, quantity: quantity, cost: cost))
        this.save(flush: true)
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
}
