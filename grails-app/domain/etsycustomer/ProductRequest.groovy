package etsycustomer

class ProductRequest extends InventoryRequest {

    SalesOrder order
    WorkItem workItem

    static constraints = {
        order(nullable: true)
        workItem(nullable: true)
    }

    void fulfilled() {
        this.state = InventoryRequestState.FULFILLED
        this.save()
        this.order?.checkItems()
    }
}