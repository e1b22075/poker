package hakata.poker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hakata.poker.model.User;

@Service
public class UserRegistrationService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // 新しいユーザーを登録
    @Transactional
    public void registerUser(String username,String password,String pass_check) throws Exception {
        // 二重登録のチェック
        if (userService.getUserByUsername(username) != null) {
            throw new Exception("ユーザ名が既に存在します。");
        }

        // パスワード一致のチェック
        if (!password.equals(pass_check)) {
            throw new Exception("パスワードと確認用パスワードが一致しません。");
        }

        // 新しいユーザーエンティティの作成と保存
        User user = new User(username,passwordEncoder.encode(password));
        userService.createUser(user);
    }
}
