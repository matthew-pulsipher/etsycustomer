package etsycustomer
import grails.converters.JSON

@SuppressWarnings("GroovyAssignabilityCheck")
class EtsySyncService {
    def oauthService
    def driveService
    def productService

    def getProductsAsync() {
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetProducts")
        progress.total = 0
        progress.save(flush: true)
        runAsync {
            getProducts()
        }
        return progress
    }

    def getProducts() {
        Token etoken = Token.findByServiceName("etsy")
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetProducts")
        progress.save(flush: true)
        def retval = null
        if (etoken) {
            def resp = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/shops/DaisyBlossomCreation/listings/active?limit=100")
            def js = JSON.parse(resp.parseBodyContents())
            retval = js.results
            progress.total = js.results.size()
            progress.completed = 0
            progress.save(flush: true)
            Integer i = 0
            js.results.each {
                def product = ProductModel.findOrCreateWhere(listingId: it.listing_id.toString())
                def params = [:]
                params.name = it.title
                params.description = it.description
                params.price = it.price?.toFloat()
                params.listingId = it.listing_id
                params.shippingPrice = it.shipping_cost?.toFloat()

                // Handle variations
                if (it.has_variations) {
                    def props = [:]
                    def resp2 = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/listings/${it.listing_id}/variations?limit=100")
                    def js2 = JSON.parse(resp2.parseBodyContents())
                    js2.results.each { vari ->
                        props.putAt(vari.formatted_name, "NA")
                    }
                    params.props = props
                }
                productService.update(product, params)
                product.save(flush: true)
                progress.completed = ++i
                progress.save(flush: true)
            }
        }
        return retval
    }

    def getProductImagesAsync(def etoken, ProductModel product) {
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetProductImages")
        progress.total = 0
        progress.save(flush: true)
        runAsync {
            getProductImages(etoken, product)
        }
        return progress
    }

    def getProductImages(def etoken, ProductModel product) {
        if (product.listingId) {
            def resp = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/listings/${product.listingId}/images")
            def js = JSON.parse(resp.parseBodyContents())
            js.results.each { eimage ->
                def image = Image.findWhere(listingId: "${eimage.listing_image_id}")
                if (!image) {
                    image = new Image(listingId: "${eimage.listing_image_id}")
                    image.save()
                    def info = [title: "${eimage.listing_image_id}_thumbnail.jpg", desc: "Image for ${product.name}", mimeType: "image/jepg"]
                    def imgURL = driveService.writeFromURL("DBImages/${product.id}", info, eimage.url_75x75)
                    image.thumbnailImage = imgURL
                    def infoFull = [title: "${eimage.listing_image_id}_full.jpg", desc: "Image for ${product.name} Full", mimeType: "image/jepg"]
                    def imgURLFull = driveService.writeFromURL("DBImages/${product.id}", infoFull, eimage.url_fullxfull)
                    image.fullImage = imgURLFull
                    image.save(flush: true)
                    product.images.addToImages(image)
                    product.images.save()
                }
            }
        }
    }

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
        def resp = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2/shops/DaisyBlossomCreation/receipts/open?limit=100")
        def js = JSON.parse(resp.parseBodyContents())
        BackgroundProgress progress = BackgroundProgress.findOrCreateWhere(name: "EtsyGetOpenOrders")
        progress.total = js.results.size()
        js.results.each { item ->
            def order = SalesOrder.findWhere(orderId: "${item.order_id}", receiptId: "${item.receipt_id}")
            progress.completed++
            progress.save(flush: true)
            if (!order) {
                order = new SalesOrder(orderId: "${item.order_id}", receiptId: "${item.receipt_id}")
                order.buyerId = "${item.buyer_user_id}"
                order.buyerName = item.name
                order.notes = item.message_from_buyer
                order.creationDate = new Date("${item.creation_tsz}000".toLong())
                order.save(flush: true)
                def resp2 = oauthService.getEtsyResource(etoken.token, "http://openapi.etsy.com/v2//receipts/${item.receipt_id}/transactions?limit=100")
                def js2 = JSON.parse(resp2.parseBodyContents())
                js2.results.each { trans ->
                    // Find the product by the listing_id
                    def product = ProductModel.findWhere(listingId: "${trans.listing_id}")
                    if (!product) {
                        product = productService.create(name: trans.title, description: trans.description, listingId: trans.listing_id)
                    }
                    if (product) {
                        // Create the InventoryRequest and attach it to the order.
                        def ir = new ProductRequest(inventory: product, quantity: trans.quantity)
                        // If the variation is set then use it here.
                        def props = product.props
                        trans.variations.each { variation ->
                            def name = variation.formatted_name
                            def value = variation.formatted_value
                            props.putAt(name, value)
                        }
                        ir.state = InventoryRequestState.PENDING
                        ir.props = props
                        ir.propsValue = "dirty"
                        // Save and add the the order
                        ir.order = order
                        ir.save(flush: true)
                        order.addToItems(ir)

                    } else {
                        log.warn "Could not produce product!"
                    }

                }
                order.state = OrderState.Downloaded
                order.save(flush: true)
            }
        }
        def orders = SalesOrder.findAllWhere(state: OrderState.Downloaded)
        return orders
    }
}
