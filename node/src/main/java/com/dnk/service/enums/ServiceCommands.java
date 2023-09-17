package com.dnk.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    MY_SCHEDULE_THIS_WEEK("/MyScheduleThisWeek"),
    MY_SCHEDULE_NEXT_WEEK("/MyScheduleNextWeek"),
    SCHEDULE_THIS_WEEK("/ScheduleThisWeek"),
    SCHEDULE_NEXT_WEEK("/ScheduleNextWeek");
    private final String cmd;

    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }

    private boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }
}
