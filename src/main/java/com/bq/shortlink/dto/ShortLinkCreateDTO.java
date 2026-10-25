package com.bq.shortlink.dto;

import lombok.Data;

/**
 * 创建短链接请求参数。
 */
@Data
public class ShortLinkCreateDTO {

    /** 长链接，必填，必须以 http:// 或 https:// 开头 */
    private String longUrl;

    /** 可选：过期天数；不传或 <=0 表示永不过期 */
    private Integer expireDays;
}
