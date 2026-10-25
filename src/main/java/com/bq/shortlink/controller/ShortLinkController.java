package com.bq.shortlink.controller;

import com.bq.shortlink.common.Result;
import com.bq.shortlink.dto.ShortLinkCreateDTO;
import com.bq.shortlink.service.ShortLinkService;
import com.bq.shortlink.vo.ShortLinkVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接 API：创建 + 查询详情。
 */
@RestController
@RequestMapping("/api/shortlink")
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;

    /**
     * 创建短链接。
     * <p>POST /api/shortlink/create</p>
     */
    @PostMapping("/create")
    public Result<ShortLinkVO> create(@RequestBody ShortLinkCreateDTO dto) {
        return Result.success(shortLinkService.create(dto));
    }

    /**
     * 查询短链接详情。
     * <p>GET /api/shortlink/{shortCode}</p>
     */
    @GetMapping("/{shortCode}")
    public Result<ShortLinkVO> detail(@PathVariable String shortCode) {
        return Result.success(shortLinkService.detail(shortCode));
    }
}
