package com.bq.shortlink.service.impl;

import com.bq.shortlink.dto.ShortLinkCreateDTO;
import com.bq.shortlink.entity.ShortLink;
import com.bq.shortlink.exception.BizException;
import com.bq.shortlink.mapper.ShortLinkMapper;
import com.bq.shortlink.util.ShortCodeUtil;
import com.bq.shortlink.util.SnowflakeUtil;
import com.bq.shortlink.vo.ShortLinkVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 短链接业务逻辑单元测试（Mock Mapper，不连数据库）。
 */
class ShortLinkServiceImplTest {

    private ShortLinkMapper mapper;
    private ShortLinkServiceImpl service;

    /**
     * 每个用例前重新构建被测对象。
     */
    @BeforeEach
    void setUp() {
        mapper = mock(ShortLinkMapper.class);
        service = new ShortLinkServiceImpl();
        ReflectionTestUtils.setField(service, "shortLinkMapper", mapper);
        ReflectionTestUtils.setField(service, "snowflakeUtil", new SnowflakeUtil(1L));
        ReflectionTestUtils.setField(service, "shortCodeUtil", new ShortCodeUtil());
        ReflectionTestUtils.setField(service, "domain", "http://localhost:8080/");
    }

    /**
     * 长链接为空应抛 50001。
     */
    @Test
    void shouldRejectEmptyLongUrl() {
        ShortLinkCreateDTO dto = new ShortLinkCreateDTO();
        dto.setLongUrl("");
        BizException e = assertThrows(BizException.class, () -> service.create(dto));
        assertEquals(50001, e.getCode());
    }

    /**
     * 非 http/https 链接应抛 50002。
     */
    @Test
    void shouldRejectInvalidScheme() {
        ShortLinkCreateDTO dto = new ShortLinkCreateDTO();
        dto.setLongUrl("ftp://example.com");
        BizException e = assertThrows(BizException.class, () -> service.create(dto));
        assertEquals(50002, e.getCode());
    }

    /**
     * 同一长链接已存在时应幂等返回已有短码，不重复插入。
     */
    @Test
    void shouldReturnExistingWhenSameLongUrl() {
        ShortLink exist = new ShortLink();
        exist.setId(1L);
        exist.setShortCode("abc123");
        exist.setLongUrl("https://www.baidu.com");
        exist.setVisitCount(3L);
        exist.setCreateTime(LocalDateTime.now());
        when(mapper.selectOne(any())).thenReturn(exist);

        ShortLinkCreateDTO dto = new ShortLinkCreateDTO();
        dto.setLongUrl("https://www.baidu.com");
        ShortLinkVO vo = service.create(dto);

        assertEquals("abc123", vo.getShortCode());
        assertEquals("http://localhost:8080/abc123", vo.getShortUrl());
        verify(mapper, never()).insert(any(ShortLink.class));
    }

    /**
     * 短码不存在时详情应抛 50003。
     */
    @Test
    void shouldThrowWhenDetailNotFound() {
        when(mapper.selectOne(any())).thenReturn(null);
        BizException e = assertThrows(BizException.class, () -> service.detail("notExist"));
        assertEquals(50003, e.getCode());
    }

    /**
     * 短链接已过期时跳转应返回 null。
     */
    @Test
    void shouldReturnNullWhenExpired() {
        ShortLink expired = new ShortLink();
        expired.setShortCode("expired1");
        expired.setLongUrl("https://www.baidu.com");
        expired.setExpireTime(LocalDateTime.now().minusDays(1));
        when(mapper.selectOne(any())).thenReturn(expired);

        assertNull(service.getLongUrl("expired1"));
    }

    /**
     * 未过期短链接应返回长链接。
     */
    @Test
    void shouldReturnLongUrlWhenValid() {
        ShortLink link = new ShortLink();
        link.setShortCode("valid123");
        link.setLongUrl("https://www.baidu.com");
        link.setExpireTime(LocalDateTime.now().plusDays(1));
        when(mapper.selectOne(any())).thenReturn(link);

        assertEquals("https://www.baidu.com", service.getLongUrl("valid123"));
    }
}
