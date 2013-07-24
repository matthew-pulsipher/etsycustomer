package etsycustomer

class BOMMaterialStep extends BOMStep {

    static constraints = {
    }
    // This Process looks to see if which of the inputs has an exact match to the props.
    // Example Paint(Color=White) should match one of the inputs White Paint(Color=White)
    // The Work item should only have the White Paint inventory item in it. This helps
    // handle the specialization of the paint to work with raw materials.
    def process(Integer pQuantity, Map props) {
        Date now = new Date()
        def wi = new WorkItem(step: this, created: now, location: this.location, quantity: pQuantity, state: WorkItemState.INIT)
        log.info "MaterialStep Process ${this}"
        wi.props = props
        // Add InventoryIO to the workItem from the BOMStep. This allows each workitem to keep track of things outside of the template BOMStep.
        this.outputs.each {
            wi.addToOutputs(item: it.item, quantity: it.quantity, taken: 0, props: it.props)
        }
        this.inputs.each {
            def io
            boolean test = true
            def iprops = it.item.props
            iprops.each { k, v ->
                if (props.getAt(k) != v) {
                    test = false
                }
            }
            if (test) {
                wi.addToInputs(io = new InventoryIO(item: it.item, quantity: it.quantity, taken: 0))
                io.props = it.props
                io.save(flush: true)
            }
        }
        wi.save(flush: true)
        wi.process()
        // Because this step should be automatic.
        // We need to run finish if the work item is OPEN.
        // Meaning we have the raw materials.
        log.info "Test to call finish on workitem  this step ${this}"
        if (wi.state == WorkItemState.OPEN) {
            log.info "Calling Workitem Finish for Step ${this} ==== ${wi}"
            wi.finish()
        }
        return wi
    }
}