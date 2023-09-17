package com.dnk.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "teacher")
    private String teacher;

    @Column(name = "office")
    private Integer office;

    @Column(name = "start_lesson")
    private String startLesson;

    @Column(name = "end_lesson")
    private String endLesson;

    @ManyToOne
    @JoinColumn(name = "schedule_day_id", referencedColumnName = "id")
    private ScheduleDay scheduleDay;
}
