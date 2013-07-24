package etsycustomer

class BOM {
    def workService

    BOMStep firstStep
    static transients = ['workService']
    static belongsTo = [inventory: Inventory]

    static constraints = {
        firstStep(nullable: true)
    }


    def getSteps() {
        def retval = [firstStep]
        firstStep.inputs.each {
            def substeps = it.item.bom?.getSteps()
            if (substeps?.size() > 0) {
                retval << substeps
            }
        }
        return retval.flatten()
    }

    def getRaw() {
        def retval = []
        firstStep.inputs.each {
            if (it.item.bom == null || it.item.material) {
                retval << it.item
            } else {
                retval << it.item.bom.getRaw()
            }
        }
        return retval.flatten()
    }

    def getRawInput() {
        def retval = []
        firstStep.inputs.each {
            if (it.item.bom == null || it.item.material) {
                retval << it
            } else {
                retval << it.item.bom.getRawInput()
            }
        }
        return retval.flatten()
    }
}

