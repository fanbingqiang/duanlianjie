package com.bq.shortlink.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雪花算法工具。
 * <p>结构：1 位符号 + 41 位时间戳 + 10 位机器 ID + 12 位序列号。</p>
 * <p>特点：趋势递增、全局唯一、不依赖数据库。</p>
 */
@Component
public class SnowflakeUtil {

    /** 起始时间：2024-01-01 00:00:00 UTC+8 */
    private static final long EPOCH = 1704067200000L;

    /** 机器 ID 占 10 位，最大 1023 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号占 12 位，最大 4095 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器 ID 最大值 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 序列号最大值 */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 当前机器 ID */
    private final long workerId;

    /** 当前毫秒内的序列号 */
    private long sequence = 0L;

    /** 上一次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    /**
     * 构造雪花算法工具。
     *
     * @param workerId 机器 ID，从配置 shortlink.worker-id 读取，默认 1
     */
    public SnowflakeUtil(@Value("${shortlink.worker-id:1}") long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 必须在 0~" + MAX_WORKER_ID + " 之间");
        }
        this.workerId = workerId;
    }

    /**
     * 生成下一个全局唯一 ID。
     *
     * @return 雪花 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨检测：时间倒退时拒绝生成，避免 ID 重复
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成 ID");
        }

        if (timestamp == lastTimestamp) {
            // 同一毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 当前毫秒序列号用完，自旋等待下一毫秒
                while ((timestamp = System.currentTimeMillis()) == lastTimestamp) {
                    // 自旋
                }
            }
        } else {
            // 新的毫秒，序列号归零
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << (WORKER_ID_BITS + SEQUENCE_BITS))
                | (workerId << SEQUENCE_BITS)
                | sequence;
    }
}
