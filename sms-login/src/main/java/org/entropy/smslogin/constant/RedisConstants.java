package org.entropy.smslogin.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY_PREFIX = "login:code:";
    public static final long LOGIN_CODE_TTL = 1L;

    public static final String LOGIN_USER_KEY_PREFIX = "login:token:";

    public static final long LOGIN_USER_TTL = 30L;

}
