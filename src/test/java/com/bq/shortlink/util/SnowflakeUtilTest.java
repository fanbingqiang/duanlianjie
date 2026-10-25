package com.bq.shortlink.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 雪花算法单元测试。
 */
class SnowflakeUtilTest {

    /**
     * 连续生成的 ID 必须唯一且趋势递增。
     */
    @Test
    void shouldGenerateUniqueAndIncreasingIds() {
        SnowflakeUtil snowflake = new SnowflakeUtil(1L);
        Set<Long> ids = new HashSet<>();
        long last = 0L;

        for (int i = 0; i < 5000; i++) {
            long id = snowflake.nextId();
            assertTrue(id > 0, "ID 必须为正数");
            assertTrue(id > last, "ID 必须趋势递增");
            assertTrue(ids.add(id), "ID 不能重复");
            last = id;
        }
    }

    /**
     * workerId 越界时应拒绝创建。
     */
    @Test
    void shouldRejectInvalidWorkerId() {
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeUtil(-1L));
        assertThrows(IllegalArgumentException.class, () -> new SnowflakeUtil(1024L));
    }

    /**
     * 不同机器 ID 生成的 ID 不能相等。
     */
    @Test
    void differentWorkerShouldNotProduceSameId() {
        SnowflakeUtil worker1 = new SnowflakeUtil(1L);
        SnowflakeUtil worker2 = new SnowflakeUtil(2L);
        assertEquals(false, worker1.nextId() == worker2.nextId());
    }
}
