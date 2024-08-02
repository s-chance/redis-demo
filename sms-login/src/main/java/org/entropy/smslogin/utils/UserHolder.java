package org.entropy.smslogin.utils;

public class UserHolder {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    public static void saveUser(String name) {
        tl.set(name);
    }

    public static String getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
