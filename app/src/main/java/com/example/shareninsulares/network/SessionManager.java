package com.example.shareninsulares.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ShareNInsularesSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_STUDENT_ID = "studentId";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_CAMPUS = "campus";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, long userId, String studentId,
                            String email, String fullName, String campus, String role) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_STUDENT_ID, studentId)
                .putString(KEY_EMAIL, email)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_CAMPUS, campus)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, null); }
    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }
    public String getStudentId() { return prefs.getString(KEY_STUDENT_ID, null); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, null); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, null); }
    public String getCampus() { return prefs.getString(KEY_CAMPUS, null); }
    public String getRole() { return prefs.getString(KEY_ROLE, null); }

    public boolean isLoggedIn() { return getToken() != null; }

    public void clearSession() { prefs.edit().clear().apply(); }
}
