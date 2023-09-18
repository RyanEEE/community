package com.nowcoder.community.config;

import com.mysql.cj.x.protobuf.MysqlxSession;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
    @Autowired
    private UserService userService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }
    //认证核心接口
    //构建对象工具
    //ProviderManager:接口默认实现类
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        // 内置认证规则
//        auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("123"));
        // 自定义认证规则
        //Manager 持有一组provider，每个provider负责一种认证
//        auth.authenticationProvider(new AuthenticationProvider() {
//            //Authentication 用于封装账号密码等认证信息的接口，不同实现类代表不同类型认证信息。
//            @Override
//            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//                String username = authentication.getName();
//                String password = (String) authentication.getCredentials();
//                User user = userService.findUserByName(username);
//                if(user == null){
//                    throw new UsernameNotFoundException("账号不存在");
//                }
//
//                password = CommunityUtil.md5(password+user.getSalt());
//                if(!user.getPassword().equals(password)){
//                    throw new BadCredentialsException("密码错误");
//                }
//
//                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
//            }
//
//            // 返回当前支持的认证类型
//            @Override
//            public boolean supports(Class<?> aClass) {
//                //UsernamePasswordAuthenticationToken
//                //接口常用实现类
//                return UsernamePasswordAuthenticationToken.class.equals(aClass);
//            }
//        });
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //登录配置
//        http.formLogin().loginPage("/loginpage")
//                .loginProcessingUrl("/login")
//                .successHandler(new AuthenticationSuccessHandler() {
//                    @Override
//                    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//                        httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/index");
//                    }
//                })
//                .failureHandler (new AuthenticationFailureHandler(){
//                    @Override
//                    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
//                        httpServletRequest.setAttribute("error",e.getMessage());
//                        httpServletRequest.getRequestDispatcher("/loginpage").forward(httpServletRequest,httpServletResponse);
//                    }
//                });
        //退出  默认使用
        http.logout().logoutUrl("/securitylogout");
//                .logoutSuccessHandler(new LogoutSuccessHandler() {
//                    @Override
//                    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
//                        httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/index");
//                    }
//                });

        //授权
        http.authorizeRequests()
                .antMatchers("/uer/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(AUTHORITY_ADMIN,
                                AUTHORITY_USER,
                                AUTHORITY_MODERATOR
                )
                .antMatchers("/discuss/top",
                        "/discuss/wonderful")
                .hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers("/discuss/delete",
                        "/data/**")
                .hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();
        //权限不足时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            httpServletResponse.setContentType("application/plain;charest=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"未登录"));
                        }else {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest Request, HttpServletResponse Response, AccessDeniedException e) throws IOException, ServletException {
                        String xRequestedWith = Request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            Response.setContentType("application/plain;charest=utf-8");
                            PrintWriter writer = Response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"权限不足"));
                        }else {
                            Response.sendRedirect(Request.getContextPath()+"/denied");
                        }
                    }
                });


        //增加filter， 验证码
//        http.addFilterBefore(new Filter() {
//            @Override
//            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//                HttpServletRequest request = (HttpServletRequest) servletRequest;;
//                HttpServletResponse response = (HttpServletResponse) servletResponse;
//                if(request.getServletPath().equals("/login")){
//                    String verifyCode = request.getParameter("verifyCode");
//
//                }
//                filterChain.doFilter(request,response);
//            }
//        }, UsernamePasswordAuthenticationFilter.class);

//        http.rememberMe()
//                .tokenRepository(new InMemoryTokenRepositoryImpl())
//                .tokenValiditySeconds(3600*24)
//                .userDetailsService(userService);
    }
}
