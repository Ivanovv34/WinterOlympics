package com.example.winter_olympics.common.constants;

public final class ValidationMessages {

    private ValidationMessages() {
    }

    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String FIRST_NAME_MAX_LENGTH = "First name must be up to 100 characters";

    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String LAST_NAME_MAX_LENGTH = "Last name must be up to 100 characters";

    public static final String COUNTRY_REQUIRED = "Country is required";
    public static final String COUNTRY_MAX_LENGTH = "Country must be up to 100 characters";

    public static final String GENDER_REQUIRED = "Gender is required";

    public static final String BIRTH_DATE_REQUIRED = "Birth date is required";
    public static final String BIRTH_DATE_PAST = "Birth date must be in the past";

    public static final String COMPETITION_NAME_REQUIRED = "Competition name is required";
    public static final String COMPETITION_NAME_MAX_LENGTH = "Competition name must be up to 150 characters";

    public static final String COMPETITION_TYPE_REQUIRED = "Competition type is required";

    public static final String MINIMUM_AGE_REQUIRED = "Minimum age is required";
    public static final String MINIMUM_AGE_AT_LEAST_10 = "Minimum age must be at least 10";

    public static final String COMPETITION_DATE_REQUIRED = "Competition date is required";
    public static final String COMPETITION_DATE_FUTURE_OR_PRESENT = "Competition date must be today or in the future";

    public static final String COMPETITION_STATUS_REQUIRED = "Competition status is required";

    public static final String ATHLETE_ID_REQUIRED = "Athlete id is required";

    public static final String FIRST_RUN_TIME_POSITIVE = "First run time must be greater than 0";
    public static final String SECOND_RUN_TIME_POSITIVE = "Second run time must be greater than 0";

    public static final String QUALIFICATION_LIMIT_REQUIRED = "Qualification limit is required";
    public static final String QUALIFICATION_LIMIT_MIN = "Qualification limit must be at least 1";

    public static final String SKI_TIME_POSITIVE = "Ski time must be greater than 0";
    public static final String MISSED_SHOTS_REQUIRED = "Missed shots is required";
    public static final String MISSED_SHOTS_MIN = "Missed shots cannot be negative";
}