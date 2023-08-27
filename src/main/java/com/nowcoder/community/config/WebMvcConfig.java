package com.nowcoder.community.config;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.controller.LoginRequiredInterceptor;
import com.nowcoder.community.controller.LoginTicketInterceptor;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;
    @Autowired
    private LoginRequiredInterceptor loginRequiredInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png",
                "/**/*.jpeg","/**/*.jpg");
        registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png",
                "/**/*.jpeg","/**/*.jpg");
    }
}
