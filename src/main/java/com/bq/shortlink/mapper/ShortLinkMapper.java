package com.bq.shortlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bq.shortlink.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接 Mapper。
 */
@Mapper
public interface ShortLinkMapper extends BaseMapper<ShortLink> {

    /**
     * 访问次数 +1（原子操作，阶段3统计用）。
     *
     * @param shortCode 短码
     * @return 影响行数
     */
    @Update("UPDATE t_short_link SET visit_count = visit_count + 1 WHERE short_code = #{shortCode}")
    int incrementVisitCount(@Param("shortCode") String shortCode);
}
