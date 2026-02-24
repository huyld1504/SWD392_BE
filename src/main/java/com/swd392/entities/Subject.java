package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@Getter @Setter
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "subject_code", length = 50, nullable = false, unique = true)
    private String subjectCode;

    @Column(length = 150, nullable = false)
    private String name;

    @Lob
    private String description;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<Topic> topics = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<UserSubject> userSubjects = new ArrayList<>();
}

