package org.entropy.couponseckill.utils;

public interface ILock {

    /**
     * 获取锁
     * @param timeoutSec 超时时间，过期后自动释放
     * @return true获取锁成功，false获取锁失败
     */
    boolean lock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
