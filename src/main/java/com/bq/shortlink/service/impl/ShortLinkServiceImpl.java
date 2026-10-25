package com.bq.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bq.shortlink.dto.ShortLinkCreateDTO;
import com.bq.shortlink.entity.ShortLink;
import com.bq.shortlink.exception.BizException;
import com.bq.shortlink.mapper.ShortLinkMapper;
import com.bq.shortlink.service.ShortLinkService;
import com.bq.shortlink.util.ShortCodeUtil;
import com.bq.shortlink.util.SnowflakeUtil;
import com.bq.shortlink.vo.ShortLinkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 短链接业务实现（阶段1：创建、跳转、详情）。
 */


@Slf4j
@Service
public class ShortLinkServiceImpl implements ShortLinkService {

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Autowired
    private SnowflakeUtil snowflakeUtil;

    @Autowired
    private ShortCodeUtil shortCodeUtil;

    /** 完整短链接域名前缀，如 http://localhost:8080/ */
    @Value("${shortlink.domain:http://localhost:8080/}")
    private String domain;

    /**
     * 创建短链接。
     * <p>流程：参数校验 → 幂等查询 → 雪花 ID + 62 进制短码 → 写库 → 返回 VO。</p>
     */
    @Override
    public ShortLinkVO create(ShortLinkCreateDTO dto) {
        // 1. 校验长链接：空串和全空白都按“空”处理，统一去掉首尾空白
        String longUrl = dto.getLongUrl() == null ? null : dto.getLongUrl().trim();
        if (longUrl == null || longUrl.isEmpty()) {
            throw new BizException(50001, "长链接不能为空");
        }
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            throw new BizException(50002, "长链接格式错误");
        }

        // 2. 幂等：同一长链接已创建过且未过期，直接返回已有短码
        ShortLink exist = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getLongUrl, longUrl)
                        .orderByDesc(ShortLink::getId)
                        .last("LIMIT 1")
        );
        if (exist != null && !isExpired(exist)) {
            log.info("短链接已存在，幂等返回: {} -> {}", longUrl, exist.getShortCode());
            return toVO(exist);
        }

        // 3. 生成雪花 ID + 62 进制短码
        long id = snowflakeUtil.nextId();
        String shortCode = shortCodeUtil.encode(id);

        // 4. 组装并写库
        ShortLink link = new ShortLink();
        link.setId(id);
        link.setShortCode(shortCode);
        link.setLongUrl(longUrl);
        link.setVisitCount(0L);
        link.setCreateTime(LocalDateTime.now());

        if (dto.getExpireDays() != null && dto.getExpireDays() > 0) {
            link.setExpireTime(LocalDateTime.now().plusDays(dto.getExpireDays()));
        }

        shortLinkMapper.insert(link);

        log.info("短链接创建: {} -> {}", longUrl, shortCode);
        return toVO(link);
    }

    /**
     * 根据短码查长链接，供 302 跳转使用。
     */
    @Override
    public String getLongUrl(String shortCode) {
        ShortLink link = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getShortCode, shortCode)
        );
        if (link == null) {
            return null;
        }
        // 过期检查：过期后不再跳转
        if (isExpired(link)) {
            log.info("短链接已过期: {}", shortCode);
            return null;
        }
        return link.getLongUrl();
    }

    /**
     * 查询短链接详情。
     */
    @Override
    public ShortLinkVO detail(String shortCode) {
        ShortLink link = shortLinkMapper.selectOne(
                new LambdaQueryWrapper<ShortLink>()
                        .eq(ShortLink::getShortCode, shortCode)
        );
        if (link == null) {
            throw new BizException(50003, "短链接不存在");
        }
        return toVO(link);
    }

    /**
     * 判断短链接是否已过期。
     *
     * @param link 短链接
     * @return true 已过期
     */
    private boolean isExpired(ShortLink link) {
        return link.getExpireTime() != null && link.getExpireTime().isBefore(LocalDateTime.now());
    }

    /**
     * 实体转 VO，并拼接完整短链接。
     *
     * @param link 短链接实体
     * @return 返回对象
     */
    private ShortLinkVO toVO(ShortLink link) {
        ShortLinkVO vo = new ShortLinkVO();
        vo.setShortCode(link.getShortCode());
        vo.setShortUrl(buildShortUrl(link.getShortCode()));
        vo.setLongUrl(link.getLongUrl());
        vo.setVisitCount(link.getVisitCount());
        vo.setCreateTime(link.getCreateTime());
        vo.setExpireTime(link.getExpireTime());
        return vo;
    }

    /**
     * 拼接完整短链接，兼容 domain 末尾有无斜杠两种配置。
     *
     * @param shortCode 短码
     * @return 完整短链接
     */
    private String buildShortUrl(String shortCode) {
        String base = domain.endsWith("/") ? domain : domain + "/";
        return base + shortCode;
    }
}
