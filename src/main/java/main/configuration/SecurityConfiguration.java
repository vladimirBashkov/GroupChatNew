package main.configuration;

import lombok.RequiredArgsConstructor;
import main.security.UserDetailsServiceImpl;
import main.security.jwt.JwtAuthenticationEntryPoint;
import main.security.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserDetailsServiceImpl userDetailsService;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final JwtTokenFilter jwtTokenFilter;



    @Bean
    protected PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests((auth) -> auth
                    .requestMatchers(HttpMethod.GET, "/", "/index.html", "/auth/auth.html",
                            "/welcome/css/**", "/welcome/js/**", "/welcome/img/**",
                            "/error/**", "/welcome/chatRegulations.html", "/welcome/contact.html",
                            "/user/userInfo/userinfo.html", "/user/css/**", "/user/js/**", "/user/chat/chat.html",
                            "/admin/css/**", "/admin/js/**", "/admin/signorConsole/signorConsole.html",
                            "/admin/adminConsole/adminConsole.html").permitAll()
                    .requestMatchers(HttpMethod.POST, "/auth/application","/auth/signin",
                            "/auth/refresh-token").permitAll()
                    .requestMatchers(HttpMethod.POST, "/user/**", "/auth/logout").hasAnyRole("USER", "OLD", "SIGNOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/admin/signor/**").hasAnyRole("SIGNOR", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/admin/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                )
                .exceptionHandling(configurer -> configurer.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/auth/auth.html")
                        .permitAll()
                        .defaultSuccessUrl("/user/userInfo/userInfo.html")
                )
                .logout(form -> form
                        .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", "POST"))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/auth/auth.html")
                );
        return http.build();
    }
}
