package etsycustomer
import grails.converters.JSON

@SuppressWarnings("GroovyAssignabilityCheck")
class EtsySyncService {
    def oauthService
    def driveService
    def productService

    def getOpenOrdersAsync() {
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetOpenOrders")
        progress.total = 0
        progress.completed = 0
        progress.save(flush: true)
        runAsync {
            getOpenOrders()
        }
        return progress
    }

    def getOpenOrders() {
        Token etoken = Token.findByServiceName("etsy")
        def resp = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/shops/DaisyBlossomCreation/receipts?limit=100")
        def js = JSON.parse(resp.parseBodyContents())
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetOpenOrders")
        progress.total = js.results.size()
        // Internal data structure to hold information.
        def orders = [:]
        def buyers = [:]
        js.results.each { item ->
            def order = orders.getAt(orderId)
            if (!order) {
                order = orders.putAt(item.order_id, [buyerid:item.buyer_user_id,recieptid:item.receipt_id]);
                def buyer = buyers.getAt(item.buyer_user_id)
                if(!buyer) {
                    buyers.putAt(item.buyer_user_id, [name:item.name, adress1:item.first_line, adress2:item.second_line, city:item.city, state:item.state, zip:item.zip, email:item.payment_email, email2:item.buyer_email])
                }
            }
        }
    }
}
