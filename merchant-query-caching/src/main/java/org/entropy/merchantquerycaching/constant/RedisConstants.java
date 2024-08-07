package org.entropy.merchantquerycaching.constant;

public class RedisConstants {
    public static final String CACHE_SHOP_KEY_PREFIX = "cache:shop:";
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type";
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final Long CACHE_NULL_TTL = 2L;
    public static final String LOCK_SHOP_KEY_PREFIX = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;
}
