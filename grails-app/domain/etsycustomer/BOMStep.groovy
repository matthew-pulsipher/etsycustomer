package etsycustomer

class BOMStep {
    String steps
    float cost = 0.00
    ImageContainer images
    Location location
    Boolean automatic = false

    static hasMany = [inputs: InventoryIO, outputs: InventoryIO]
    static constraints = {
        steps(blank: true, maxSize: 5000)
        location(nullable: true)
        images(nullable: true)
    }
    static mapping = {
        steps type: 'text'
    }

    String toString() {
        def retVal = ""
        inputs.each {
            retVal += "${it},"
        }
        retVal += " --> "
        outputs.each {
            retVal += "${it},"
        }
        return retVal
    }

    def process(Integer pQuantity, Map props) {
        Date now = new Date()
        def wi = new WorkItem(step: this, created: now, location: this.location, quantity: pQuantity, state: WorkItemState.INIT)
        log.info "Process Step ${this}"
        wi.props = props
        // Add InventoryIO to the workItem from the BOMStep. This allows each workitem to keep track of things outside of the template BOMStep.
        this.outputs.each {
            wi.addToOutputs(item: it.item, quantity: it.quantity, taken: 0, props: it.props)
        }
        this.inputs.each {
            def io
            wi.addToInputs(io = new InventoryIO(item: it.item, quantity: it.quantity, taken: 0))
            io.props = it.props
            io.save(flush: true)
        }
        wi.save(flush: true)

        wi.process()
        if (wi.state == WorkItemState.OPEN && this.automatic) {
            log.info "Calling workitem finish for ${this} because it is automatic"
            wi.finish()
        }
        return wi
    }
}