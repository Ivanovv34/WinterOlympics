package com.example.winter_olympics.common.constants;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static final String ATHLETE_NOT_FOUND = "Athlete with id %d was not found";
    public static final String COMPETITION_NOT_FOUND = "Competition with id %d was not found";

    public static final String REGISTRATION_NOT_FOUND =
            "Registration for competition id %d and athlete id %d was not found";

    public static final String ATHLETE_ALREADY_REGISTERED =
            "Athlete with id %d is already registered for competition with id %d";

    public static final String ATHLETE_GENDER_DOES_NOT_MATCH =
            "Athlete gender does not match competition gender";

    public static final String ATHLETE_DOES_NOT_MEET_MINIMUM_AGE =
            "Athlete does not meet the minimum age requirement";

    public static final String COMPETITION_NOT_OPEN =
            "Competition is not open for registrations";

    public static final String COMPETITION_IS_NOT_SLALOM =
            "Competition with id %d is not a slalom competition";

    public static final String ATHLETE_NOT_REGISTERED_FOR_COMPETITION =
            "Athlete with id %d is not registered for competition with id %d";

    public static final String SLALOM_RESULT_NOT_FOUND =
            "Slalom result for competition id %d and athlete id %d was not found";

    public static final String ATHLETE_NOT_QUALIFIED_FOR_SECOND_RUN =
            "Athlete with id %d is not qualified for the second run";

    public static final String SECOND_RUN_CANNOT_BE_ENTERED_BEFORE_QUALIFICATION =
            "Second run results cannot be entered before second run qualification";

    public static final String SLALOM_RANKING_CANNOT_BE_CALCULATED_BEFORE_SECOND_RUN =
            "Slalom ranking cannot be calculated before second run results are entered";

    public static final String NO_VALID_SLALOM_RESULTS_FOR_RANKING =
            "There are no valid slalom results for ranking";

    public static final String COMPETITION_IS_NOT_BIATHLON =
            "Competition with id %d is not a biathlon competition";

    public static final String BIATHLON_RESULT_NOT_FOUND =
            "Biathlon result for competition id %d and athlete id %d was not found";

    public static final String NO_VALID_BIATHLON_RESULTS_FOR_RANKING =
            "There are no valid biathlon results for ranking";
}