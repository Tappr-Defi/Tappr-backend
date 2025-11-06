package com.semicolon.africa.tapprbackend.general.messages;

public final class ErrorMessages {


    private ErrorMessages() {}

    public static final String USER_NOT_FOUND = "User not found.";
    public static final String EMAIL_ALREADY_EXISTS = "Email already registered.";
    public static final String INVALID_CREDENTIALS = "Invalid username or password.";
    public static final String EMAIL_NOT_VERIFIED = "Email not verified. Please verify before login.";
    public static final String PASSWORD_MISMATCH = "Passwords do not match.";
    public static final String TOKEN_EXPIRED = "Refresh token expired.";
    public static final String INVALID_TOKEN = "Invalid token.";
    public static final String EMAIL_FAILED = "Failed to send email.";
    public static final String OPERATION_FAILED = "Operation failed.";
    public static final String ACCESS_DENIED = "You do not have permission to perform this action.";
    public static final String INCORRECT_PIN_FORMAT = "PIN must be exactly 4 digits";
    public static final String USER_ALREADY_EXISTS ="User already exists in db";
    public static final String USER_NOT_AUTHENTICATED = "User not authenticated";
    public static final String QUEUE_NOT_FOUND = "No Queue found.";
    public static final String QUEUE_ALREADY_EXIST = "Currently Have an Opened Queue";
    public static final String QUEUE_NOT_OPENED = "Queue is not opened";
    public static final String QUEUE_FULL = "Queue is full";
    public static final String QUEUE_CLOSED = "Queue is closed";
    public static final String QUEUE_NOT_JOINED = "User is not joined to the queue";
    public static final String USER_ALREADY_IN_QUEUE = "User Already exist in this queue";
    public static final String PHONE_ALREADY_EXISTS = "Phone number already exists, choose another one";

}
