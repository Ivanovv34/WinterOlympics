package com.example.winter_olympics.registration.model;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.common.model.BaseEntity;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "competition_registrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_competition_registrations_competition_athlete",
                        columnNames = {"competition_id", "athlete_id"}
                )
        }
)
public class CompetitionRegistrationEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private CompetitionEntity competition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "athlete_id", nullable = false)
    private AthleteEntity athlete;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();
}