package etsycustomer

class ImageContainer {

    static hasMany = [images: Image]

    static constraints = {
    }

    def addImageFromFile(def f) {
        // List of OK mime-types
        def okcontents = ['image/png', 'image/jpeg', 'image/gif']
        if (!okcontents.contains(f.getContentType())) {
            log.warn "Wrong content type"
        }

        // Save the image and mime type
        def image = new Image()
        image.image = f.getBytes()
        image.imageType = f.getContentType()
        image.save()
        this.addToImages(image)
        this.save()
    }
}
