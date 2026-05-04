package com.example.winter_olympics.result.model;

import com.example.winter_olympics.athlete.model.AthleteEntity;
import com.example.winter_olympics.common.model.BaseEntity;
import com.example.winter_olympics.competition.model.CompetitionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "slalom_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_slalom_results_competition_athlete",
                        columnNames = {"competition_id", "athlete_id"}
                )
        }
)
public class SlalomResultEntity extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    private CompetitionEntity competition;

    @ManyToOne(optional = false)
    @JoinColumn(name = "athlete_id", nullable = false)
    private AthleteEntity athlete;

    @Column(name = "first_run_time", precision = 10, scale = 3)
    private BigDecimal firstRunTime;

    @Column(name = "second_run_time", precision = 10, scale = 3)
    private BigDecimal secondRunTime;

    @Column(name = "did_not_finish_first_run", nullable = false)
    private boolean didNotFinishFirstRun = false;

    @Column(name = "did_not_finish_second_run", nullable = false)
    private boolean didNotFinishSecondRun = false;

    @Column(name = "qualified_for_second_run", nullable = false)
    private boolean qualifiedForSecondRun = false;

    @Column(name = "total_time", precision = 10, scale = 3)
    private BigDecimal totalTime;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Enumerated(EnumType.STRING)
    @Column(name = "medal", nullable = false, length = 20)
    private MedalType medal = MedalType.NONE;
}