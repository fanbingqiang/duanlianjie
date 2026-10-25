package com.bq.shortlink.service;

import com.bq.shortlink.dto.ShortLinkCreateDTO;
import com.bq.shortlink.vo.ShortLinkVO;

/**
 * 短链接业务接口。
 */
public interface ShortLinkService {

    /**
     * 创建短链接（同一长链接已存在时幂等返回已有短码）。
     *
     * @param dto 创建参数
     * @return 短链接信息
     */
    ShortLinkVO create(ShortLinkCreateDTO dto);

    /**
     * 根据短码查询长链接（跳转用）。
     *
     * @param shortCode 短码
     * @return 长链接；不存在或已过期返回 null
     */
    String getLongUrl(String shortCode);

    /**
     * 查询短链接详情（含访问次数）。
     *
     * @param shortCode 短码
     * @return 短链接信息
     */
    ShortLinkVO detail(String shortCode);
}
