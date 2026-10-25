package com.bq.shortlink.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接信息返回对象。
 */
@Data
public class ShortLinkVO {

    /** 短码 */
    private String shortCode;

    /** 完整短链接，如 http://localhost:8080/aB3dK9 */
    private String shortUrl;

    /** 原始长链接 */
    private String longUrl;

    /** 访问次数 */
    private Long visitCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 过期时间，null 表示永不过期 */
    private LocalDateTime expireTime;
}
