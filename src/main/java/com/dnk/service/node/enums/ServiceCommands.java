package com.dnk.service.node.enums;

public enum ServiceCommands {
    SCHEDULE_TODAY("/scheduletoday"),
    MY_SCHEDULE_TODAY("/myscheduletoday"),
    SCHEDULE_TOMORROW("/scheduletomorrow"),
    MY_SCHEDULE_TOMORROW("/myscheduletomorrow"),
    SCHEDULE_THIS_WEEK("/schedulethisweek"),
    MY_SCHEDULE_THIS_WEEK("/myschedulethisweek"),
    SCHEDULE_NEXT_WEEK("/schedulenextweek"),
    MY_SCHEDULE_NEXT_WEEK("/myschedulenextweek"),
    STUDENTS_THIS_WEEK("/studentsthisweek"),
    STUDENTS_NEXT_WEEK("/studentsnextweek"),
    HELP("/help"),
    SEND("/send"),
    SET_STUDENT("/setstudent"),
    SET_NOTIFICATION_ON("/seton"),
    SET_NOTIFICATION_OFF("/setoff");
    private final String value;

    ServiceCommands(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
