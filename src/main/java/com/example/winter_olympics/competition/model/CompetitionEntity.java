package com.example.winter_olympics.competition.model;

import com.example.winter_olympics.athlete.model.Gender;
import com.example.winter_olympics.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "competitions")
public class CompetitionEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private CompetitionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "min_age", nullable = false)
    private Integer minAge;

    @Column(name = "competition_date", nullable = false)
    private LocalDate competitionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private CompetitionStatus status = CompetitionStatus.OPEN;
}