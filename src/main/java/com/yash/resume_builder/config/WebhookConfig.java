package com.yash.resume_builder.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

@Configuration
public class WebhookConfig {

    @Bean
    public FilterRegistrationBean<HiddenHttpMethodFilter> disableHiddenHttpMethodFilter() {
        FilterRegistrationBean<HiddenHttpMethodFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new HiddenHttpMethodFilter());
        bean.setEnabled(false);
        return bean;
    }
}
