package com.bq.shortlink.controller;

import com.bq.shortlink.service.ShortLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 跳转控制器：短码 → 302 跳转长链接。
 * <p>只匹配"单个路径段 + 68.. 62 进制字符集"，不会抢 /api/**、/index.html 等路由。</p>
 */
@RestController
public class RedirectController {

    @Autowired
    private ShortLinkService shortLinkService;

    /**
     * 短码跳转。
     * <p>GET /{shortCode} → 302 Location: 长链接；短码不存在或已过期 → 404。</p>
     */
    @GetMapping("/{shortCode:[0-9a-zA-Z]+}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        String longUrl = shortLinkService.getLongUrl(shortCode);

        if (longUrl == null) {
            return ResponseEntity.notFound().build();
        }

        // 302 临时重定向：便于统计访问、避免浏览器永久缓存
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }
}
