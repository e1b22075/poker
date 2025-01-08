package hakata.poker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import hakata.poker.dto.SignupRequestDto;
import hakata.poker.model.User;
import hakata.poker.service.UserService;

@Service
public class UserRegistrationService {
  @Autowired
  private UserService userService;

  @Transactional
  public void registerUser(SignupRequestDto userDto) throws Exception {
    // 二重登録のチェック
    if (userService.getUserByUsername(userDto.getUsername()) != null) {
      throw new Exception("ユーザ名が既に存在します。");
    }
    // パスワード一致のチェック
    if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
      throw new Exception("パスワードと確認用パスワードが一致しません。");
    }
    // 新しいユーザーエンティティの作成と保存
    User user = new User(userDto.getUsername(), userDto.getPassword());
    user.setEmail(userDto.getEmail());
    System.out.println("入力内容" + user.getEmail() + user.getPassword_hash() + user.getUserName());
    // パスワードをハッシュ化してセットする
    userService.createUser(user);
  }
}
