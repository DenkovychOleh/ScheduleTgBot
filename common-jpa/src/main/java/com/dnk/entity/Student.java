package com.dnk.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
//@EqualsAndHashCode(exclude = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name")
    private String fistName;
    @Column(name = "last_name")
    private String lastName;

    @OneToOne()
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private AppUser appUser;

    @OneToMany(mappedBy = "student")
    private List<Schedule> schedules;
}
