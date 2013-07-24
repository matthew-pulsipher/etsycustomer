package etsycustomer

class Location {
    String name
    String notes
    String address
    String city
    String state
    String postalCode
    Float lng
    Float lat

    static constraints = {
        name(blank: false)
        address(blank: true, nullable: true)
        city(blank: true, nullable: true)
        state(blank: true, nullable: true)
        postalCode(blank: true, nullable: true)
        lng(nullable: true)
        lat(nullable: true)
        notes(blank: true, nullable: true)
    }

    String toString() {
        return name
    }

    Integer numberOfOpenWorkItems() {
        return WorkItem.findAllWhere(location: this, state: WorkItemState.OPEN).size()
    }

    Integer numberOfBlockedWorkItems() {
        return WorkItem.findAllWhere(location: this, state: WorkItemState.BLOCKED).size()
    }

    Integer numberOfClosedWorkItems() {
        return WorkItem.findAllWhere(location: this, state: WorkItemState.CLOSED).size()
    }

}
