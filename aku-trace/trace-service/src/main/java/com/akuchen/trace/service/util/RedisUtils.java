//package com.akuchen.trace.service.util;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.Objects;
//import java.util.function.Function;
//
///**
// * redis工具类
// * @author zhangyue1
// */
//@Component
//public class RedisUtils {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    private static final String LOCK_PREFIX = "trace:lock:";
//
//    /**
//     * 分布式锁定并处理
//     * @param lockKey
//     * @param leaveTimeSeconds
//     * @param runnable
//     */
//    public void lockAndProcess(String lockKey, long leaveTimeSeconds, Runnable runnable) {
//        if(lock(lockKey, leaveTimeSeconds)) {
//            try{
//                runnable.run();
//            }finally {
//                releaseLock(lockKey);
//            }
//        }
//    }
//    public Object lockAndProcess(String lockKey, long leaveTimeSeconds, Function<Object,Object> function) {
//        if(lock(lockKey, leaveTimeSeconds)) {
//            try{
//                return function.apply(null);
//            }finally {
//                releaseLock(lockKey);
//            }
//        }
//        return null;
//    }
//
//    /**
//     *  最终加强分布式锁
//     *
//     * @param lockKey key值
//     * @return 是否获取到
//     */
//    public Boolean lock(String lockKey, long leaveTimeSeconds){
//        String lock = LOCK_PREFIX + lockKey;
//        // 利用lambda表达式
//        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
//            long expireAt = getLockExpireTime(leaveTimeSeconds);
//            Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());
//            if (acquire != null && acquire) {
//                return true;
//            } else {
//
//                byte[] value = connection.get(lock.getBytes());
//
//                if (Objects.nonNull(value) && value.length > 0) {
//
//                    long expireTime = Long.parseLong(new String(value));
//                    // 如果锁已经过期
//                    if (expireTime < System.currentTimeMillis()) {
//                        // 重新加锁，防止死锁
//                        byte[] oldValue = connection.getSet(lock.getBytes(), String.valueOf(getLockExpireTime(leaveTimeSeconds)).getBytes());
//                        return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
//                    }
//                }
//            }
//            return false;
//        });
//    }
//
//    private long getLockExpireTime(long leaveTimeSeconds) {
//        return System.currentTimeMillis() + leaveTimeSeconds*1000 + 1;
//    }
//
//    /**
//     * 删除锁
//     * @param lockKey 锁的唯一标识
//     */
//    public void releaseLock(String lockKey) {
//        redisTemplate.delete(LOCK_PREFIX + lockKey);
//    }
//}
