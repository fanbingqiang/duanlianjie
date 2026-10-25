package com.bq.shortlink.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果（复用秒杀项目，阶段1暂未使用，阶段2列表查询会用到）。
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> list;
}
