package etsycustomer

class ShippedProduct extends Inventory {

    static constraints = {

    }

    def addItem(Integer quantity, Location location, float cost, Map props) {
        def retval = super.addItem(quantity, location, cost, props)

        // Remove the quantity because it has been shipped.
        this.onHand -= quantity
        this.save()
        return retval
    }
}

