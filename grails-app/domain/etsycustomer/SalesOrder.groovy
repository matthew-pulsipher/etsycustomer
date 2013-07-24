package etsycustomer

enum OrderState {
    INIT, Downloaded, Open, Fulfilled, Shipped, Cancelled
}

class SalesOrder {
    String orderId
    String receiptId
    String buyerId
    String buyerName
    String notes
    Date creationDate
    OrderState state = OrderState.INIT

    static hasMany = [items: ProductRequest]
    static constraints = {
        orderId(blank: true)
        receiptId(blank: false)
        notes(blank: true, nullable: true, maxSize: 5000)
        buyerId(blank: true)
        buyerName(blank: true)
        creationDate(nullable: true)
    }

    static mapping = {
        notes type: 'text'
    }

    void checkItems() {
        boolean finished = true
        items.each { item ->
            if (item.state != InventoryRequestState.FULFILLED) {
                finished = false
            }
        }
        if (finished) {
            this.state = OrderState.Fulfilled
            log.info "Sales Order Finished! ${this}"
        }
        this.save(flush: true)
        log.info "Sales Order State: ${this.state}"
    }

    void open() {
        this.state = OrderState.Open
        this.save()
    }

    void cancel() {
        this.state = OrderState.Cancelled
        this.save()
    }
}