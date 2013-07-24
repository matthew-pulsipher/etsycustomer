package etsycustomer


enum WorkItemState {
    INIT, OPEN, CLOSED, POSTPONED, BLOCKED
}
@SuppressWarnings("GroovyAssignabilityCheck")
class WorkItem {
    def workService

    static transients = ['workService', 'props']
    InventoryRequest irequest
    BOMStep step
    Date dateCreated
    Date lastUpdated
    Date created
    Location location
    WorkItemState state = WorkItemState.INIT
    Integer quantity = 0
    Map props = [:]
    String propsValue = ""

    static hasMany = [inputs: InventoryIO, outputs: InventoryIO]

    static constraints = {
        irequest(nullable: true)
        step(nullable: true)
        location(nullable: true)
        created(nullable: true)
        dateCreated(nullable: true)
        lastUpdated(nullable: true)
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

    String toString() {
        return "${id}>[${state}]@${location}(${quantity})>> " + this.inputs?.join(",") + " --> " + this.outputs?.join(",")
    }

    Integer process() {
        // Only process Steps that are in the blocked state.
        boolean ok = false
        Integer retval = 0
        // Check to see if we have everything we need to satisfy the work item

        if (state == WorkItemState.INIT) {
            ok = true
            this.inputs.each { io ->
                // Check and see if there is enough inventory available for each input
                // When the item is requested it is attached to a workItem below. The inventory number is decreased.
                // It is important to remember the the quantity is attached to the work item.

                // Check the inventory if it has enough now.
                // Make sure to pass the props down to the io items here.
                def combinedMap = combineMap(this.props, io.props)
                log.info "Requesting Inventory: ${io.item} with ${combinedMap} [${io.quantity * this.quantity}"
                if (io.item.request(io.quantity * this.quantity, combinedMap)) {
                    io.taken = io.quantity * this.quantity
                    io.save(flush: true)
                    retval += io.quantity * this.quantity
                } else {
                    ok = false
                }
            }
            if (ok) {
                state = WorkItemState.OPEN
            } else {
                state = WorkItemState.BLOCKED
            }
            this.save(flush: true)
        }
        // This has been blocked because in the past it did not have everything it needed.
        else if (state == WorkItemState.BLOCKED) {
            ok = true
            this.inputs.each { io ->
                // Check and see if there is enough inventory available for each input
                // When the item is requested it is attached to a workItem below. The inventory number is decreased.
                // It is important to remember the the quantity is attached to the work item.
                def combinedMap = combineMap(this.props, io.props)
                log.info "Checking io.taken ${io.taken}"
                log.info "CanTake ${io.item} ${combinedMap} number: ${io.quantity * this.quantity}"
                if (io.taken > 0) {
                    retval += io.taken
                } else if (io.item.canTake(io.quantity * this.quantity, combinedMap)) {
                    io.taken = io.quantity * this.quantity
                    io.save(flush: true)
                    retval += io.quantity * this.quantity
                } else {
                    ok = false
                }
            }
            if (ok) {
                state = WorkItemState.OPEN
            } else {
                state = WorkItemState.BLOCKED
            }
            this.save(flush: true)
        }
        return retval
    }

    def combineMap(Map first, Map second) {
        def retVal = [:]
        // Find keys that match values from the second in the first
        if (first) {
            second.each { k, v ->
                def value
                if (v == "NA") {
                    value = first.getAt(k)
                } else {
                    value = first.getAt(v)
                    if (!value) {
                        value = v
                    }
                }
                retVal.putAt(k, value)
            }
        } else {
            retVal = second
        }
        return retVal
    }

    boolean finish() {
        float cost = 0.00

        this.state = WorkItemState.CLOSED
        this.save(flush: true)

        // Calculate the cost of the inventory being produced.
        // This should include the input and the cost of the step.
        this.step.inputs.each {
            cost += it.item.cost * it.quantity
        }

        cost += this.step.cost

        // If there are outputs then we need to split the cost between the outputs.
        if (this.step.outputs.size() > 0) {
            // Split the cost between the output equality
            float realCost = cost / this.step.outputs.size()
            // Add the Items to inventory based on the step.
            this.step.outputs.each { inv ->
                def item = inv.item?.addItem(inv.quantity * this.quantity, this.step.location, realCost, this.props)
                log.info "Work Item finished. Created: ${inv} ${this.props} from ${this} --- ${item}"
            }
        }
        return true
    }
}
