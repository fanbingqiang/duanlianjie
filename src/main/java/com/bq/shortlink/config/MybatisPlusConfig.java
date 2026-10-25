package com.bq.shortlink.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：Mapper 扫描 + 分页插件。
 */
@Configuration
@MapperScan("com.bq.shortlink.mapper")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器（分页插件）。
     * <p>阶段1没有分页接口，但先装好，阶段2列表查询直接可用。</p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
