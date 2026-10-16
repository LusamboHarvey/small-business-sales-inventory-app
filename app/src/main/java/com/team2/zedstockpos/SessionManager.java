
package com.team2.zedstockpos;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ZedStockPOS_Session";

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save the user's login session.
     *
     * @param email User's email address
     * @param role  User's role, such as ADMIN or CASHIER
     */
    public void createLoginSession(String email, String role) {
        preferences.edit()
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    /**
     * Check whether a valid login session exists.
     */
    public boolean isLoggedIn() {
        String email = getUserEmail();
        String role = getUserRole();

        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
                && email != null
                && !email.trim().isEmpty()
                && role != null
                && !role.trim().isEmpty();
    }

    /**
     * Get the saved user's email address.
     */
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Get the saved user's role.
     */
    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, "");
    }

    /**
     * Clear the saved session when the user logs out.
     */
    public void logout() {
        preferences.edit()
                .clear()
                .apply();
    }
}
