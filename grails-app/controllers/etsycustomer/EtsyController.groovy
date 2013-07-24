package etsycustomer

class EtsyController {
    def etsySyncService

    def getProducts() {
        def progress = etsySyncService.getProductsAsync()
        [progress: progress]
        redirect(url: "/")
    }

    def getProductImages() {
        def products = ProductModel.list()
        def etoken = Token.findByServiceName("etsy")
        def progress = BackgroundProgress.findOrCreateByName("EtsyGetProductImages")
        progress.total = products.size()
        progress.completed = 0
        progress.save(flush: true)
        redirect(url: "/")
        products.each { product ->
            progress.completed++
            progress.save(flush: true)
            etsySyncService.getProductImages(etoken, product)
        }
    }

    def getOpenOrders() {
        etsySyncService.getOpenOrdersAsync()
        redirect(url: "/")
    }

    def showProgress() {
        def progress = BackgroundProgress.findByName("EtsyGetProducts")
        [progress: progress]
    }
}
