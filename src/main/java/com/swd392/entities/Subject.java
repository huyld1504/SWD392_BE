package com.swd392.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
@SQLDelete(sql = "UPDATE subjects SET is_deleted = true WHERE subject_id = ?")
@Where(clause = "is_deleted = false")
@Getter
@Setter
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

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = false;

    @OneToMany(mappedBy = "subject")
    private List<Topic> topics;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<UserSubject> userSubjects = new ArrayList<>();
}

