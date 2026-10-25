package com.bq.shortlink.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 62 进制短码工具单元测试。
 */
class ShortCodeUtilTest {

    private final ShortCodeUtil shortCodeUtil = new ShortCodeUtil();

    /**
     * 基本编码结果校验。
     */
    @Test
    void shouldEncodeKnownValues() {
        assertEquals("0", shortCodeUtil.encode(0));
        assertEquals("1", shortCodeUtil.encode(1));
        assertEquals("z", shortCodeUtil.encode(61));
        assertEquals("10", shortCodeUtil.encode(62));
    }

    /**
     * 编码后再解码应还原原始 ID。
     */
    @Test
    void shouldRoundTripEncodeAndDecode() {
        long[] samples = {1L, 62L, 63L, 4096L, 123456789L, 3700000000000000000L};
        for (long id : samples) {
            String code = shortCodeUtil.encode(id);
            assertEquals(id, shortCodeUtil.decode(code), "短码 " + code + " 解码失败");
        }
    }

    /**
     * 当前时间下的雪花 ID 转短码后长度应不超过 10 位（short_code 字段长度）。
     */
    @Test
    void shouldFitColumnLength() {
        long snowflakeLikeId = 3700000000000000000L;
        String code = shortCodeUtil.encode(snowflakeLikeId);
        assertTrue(code.length() <= 10, "短码长度不能超过 10，实际：" + code.length());
        assertTrue(code.matches("[0-9a-zA-Z]+"), "短码只能包含 62 进制字符");
    }
}
