package etsycustomer


enum BackgroundState {
    Init, Running, Completed, Failed
}

class BackgroundProgress {
    String name
    Integer total = 0
    Integer completed = 0
    Date lastUpdated
    String message = ""
    BackgroundState state = BackgroundState.Init

    static constraints = {
        message(blank: true)
    }
}
