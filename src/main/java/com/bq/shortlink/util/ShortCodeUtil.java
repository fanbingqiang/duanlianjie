package com.bq.shortlink.util;

import org.springframework.stereotype.Component;

/**
 * 62 进制短码工具。
 * <p>字符集：0-9 a-z A-Z，共 62 个字符；雪花 ID 转 62 进制，既短又唯一。</p>
 */
@Component
public class ShortCodeUtil {

    /** 62 进制字符集：0-9 + 26 小写 + 26 大写 */
    private static final String CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** 进制 */
    private static final int BASE = 62;

    /**
     * 雪花 ID → 62 进制短码。
     *
     * @param id 雪花 ID
     * @return 短码
     */
    public String encode(long id) {
        if (id <= 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            int mod = (int) (id % BASE);
            sb.append(CHARS.charAt(mod));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    /**
     * 62 进制短码 → 雪花 ID（反查用）。
     * <p>注意：阶段1只用 encode；decode 遇到非法字符会得到错误结果，
     * 调用前应先校验短码格式。</p>
     *
     * @param code 短码
     * @return 雪花 ID
     */
    public long decode(String code) {
        long id = 0;
        for (char c : code.toCharArray()) {
            id = id * BASE + CHARS.indexOf(c);
        }
        return id;
    }
}
