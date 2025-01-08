package hakata.poker.dto;

import org.springframework.beans.factory.annotation.Autowired;

import hakata.poker.service.UserService;

public class SignupRequestDto {
  @Autowired
  private UserService userService;

  private String username;
  private String email;
  private String password;
  private String passwordConfirm;

  // コンストラクタ
  public SignupRequestDto(String username, String email, String password, String passwordConfirm) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.passwordConfirm = passwordConfirm;
  }

  // ゲッターとセッター
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPasswordConfirm() {
    return passwordConfirm;
  }

  public void setPasswordConfirm(String passwordConfirm) {
    this.passwordConfirm = passwordConfirm;
  }

  // バリデーションロジック
  public String validate() {
    StringBuilder errors = new StringBuilder();
    // ユーザー名のバリデーション
    if (username == null || username.isBlank()) {
      errors.append("ユーザー名は必須です。\n");
    } else if (username.length() < 3 || username.length() > 255) {
      errors.append("ユーザー名は3文字以上255文字以下で入力してください。\n");
    }
    // メールアドレスのバリデーション
    if (email == null || email.isBlank()) {
      errors.append("メールアドレスは必須です。\n");
    } else if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
      errors.append("無効なメールアドレスです。\n");
    }
    // パスワードのバリデーション
    if (password == null || password.isBlank()) {
      errors.append("パスワードは必須です。\n");
    } else if (password.length() < 5) {
      errors.append("パスワードは5文字以上で入力してください。\n");
    }
    // パスワード確認のバリデーション
    if (passwordConfirm == null || passwordConfirm.isBlank()) {
      errors.append("パスワード確認は必須です。\n");
    } else if (!password.equals(passwordConfirm)) {
      errors.append("パスワードと確認用パスワードが一致しません。\n");
    }
    return errors.toString();
  }
}
