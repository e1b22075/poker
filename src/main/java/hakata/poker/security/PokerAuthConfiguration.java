package hakata.poker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class PokerAuthConfiguration {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.formLogin(login -> login
        .permitAll())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")) // ログアウト後に / にリダイレクト
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(AntPathRequestMatcher.antMatcher("/room/**"))
            .authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/poker/**"))
            .authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/sample/**"))
            .authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/**"))
            .permitAll())// 上記以外は全員アクセス可能
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/*")))// h2-console用にCSRF対策を無効化
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions
                .sameOrigin()));
    return http.build();
  }

  /**
   * 認証処理に関する設定（誰がどのようなロールでログインできるか）
   *
   * @return
   */
  @Bean
  public InMemoryUserDetailsManager userDetailsService() {

    // ユーザ名，パスワード，ロールを指定してbuildする
    // このときパスワードはBCryptでハッシュ化されているため，{bcrypt}とつける
    // ハッシュ化せずに平文でパスワードを指定する場合は{noop}をつける

    UserDetails user1 = User.withUsername("よしたに")
        .password("{bcrypt}$2y$05$WOn5z07ZfnsEyp43KElsXOXdE9t0ycNY623nnPBfb4ylAq4a.GrfG").roles("USER").build();
    UserDetails user2 = User.withUsername("まつうら")
        .password("{bcrypt}$2y$05$WOn5z07ZfnsEyp43KElsXOXdE9t0ycNY623nnPBfb4ylAq4a.GrfG").roles("USER").build();
    UserDetails user3 = User.withUsername("おくだ")
        .password("{bcrypt}$2y$05$WOn5z07ZfnsEyp43KElsXOXdE9t0ycNY623nnPBfb4ylAq4a.GrfG").roles("USER").build();

    UserDetails user4 = User.withUsername("ひらお")
        .password("{bcrypt}$2y$05$WOn5z07ZfnsEyp43KElsXOXdE9t0ycNY623nnPBfb4ylAq4a.GrfG").roles("USER").build();

    // 生成したユーザをImMemoryUserDetailsManagerに渡す（いくつでも良い）
    return new InMemoryUserDetailsManager(user1, user2, user3, user4);
  }

}
