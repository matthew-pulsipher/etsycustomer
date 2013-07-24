package etsycustomer

class Image {
    String fullImage
    String thumbnailImage
    String listingId = "default"

    static constraints = {
        fullImage(nullable: true)
        thumbnailImage(nullable: true)
    }
}