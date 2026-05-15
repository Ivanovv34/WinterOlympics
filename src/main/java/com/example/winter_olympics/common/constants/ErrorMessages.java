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
}