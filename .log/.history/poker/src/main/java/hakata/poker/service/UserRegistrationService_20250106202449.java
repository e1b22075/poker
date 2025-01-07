package hakata.poker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hakata.poker.model.User;

public class UserRegistrationService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // 新しいユーザーを登録
    @Transactional
    public void registerUser(Sring username,String password,String pass_check) throws Exception {
        // 二重登録のチェック
        if (userService.getUserByUsername(user.getUserName()) != null) {
            throw new Exception("ユーザ名が既に存在します。");
        }

        // パスワード一致のチェック
        if (!password.equals(pass_check)) {
            throw new Exception("パスワードと確認用パスワードが一致しません。");
        }

        // 新しいユーザーエンティティの作成と保存
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        // パスワードをハッシュ化してセットする
        user.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        userService.createUser(user);
    }
}
