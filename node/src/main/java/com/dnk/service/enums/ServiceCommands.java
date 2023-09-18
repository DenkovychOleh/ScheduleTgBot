package com.dnk.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    MY_SCHEDULE_TODAY("/MyScheduleToday"),
    MY_SCHEDULE_THIS_WEEK("/MyScheduleThisWeek"),
    MY_SCHEDULE_NEXT_WEEK("/MyScheduleNextWeek"),
    SCHEDULE_THIS_WEEK("/ScheduleThisWeek"),
    SCHEDULE_NEXT_WEEK("/ScheduleNextWeek");
    private final String value;

    ServiceCommands(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommands fromValue(String v) {
        for (ServiceCommands c: ServiceCommands.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }
}
