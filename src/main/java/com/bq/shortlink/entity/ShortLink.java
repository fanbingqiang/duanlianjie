package com.bq.shortlink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接实体，对应表 t_short_link。
 */
@Data
@TableName("t_short_link")
public class ShortLink {

    /** 雪花 ID，由程序生成后手动写入，不用数据库自增 */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /** 短码（62 进制） */
    private String shortCode;

    /** 原始长链接 */
    private String longUrl;

    /** 创建人（阶段1无登录体系，暂为 null） */
    private Long userId;

    /** 访问次数（冗余字段，避免每次 count 查询） */
    private Long visitCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 过期时间，null 表示永不过期 */
    private LocalDateTime expireTime;
}
