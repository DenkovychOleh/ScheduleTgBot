package com.dnk.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "schedules_days")
public class ScheduleDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_name")
    private String dayName;

    @Column(name = "is_even_week")
    private Boolean isEvenWeek;


    @OneToMany(mappedBy = "scheduleDay")
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "scheduleDay")
    private List<Lesson> lessons;
}
