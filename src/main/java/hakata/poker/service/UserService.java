package hakata.poker.service;

import org.springframework.stereotype.Service;

import hakata.poker.model.User;
import hakata.poker.model.UserMapper;

@Service
public class UserService {
  private final UserMapper userMapper;

  public UserService(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  public User getUserByUsername(String username) {
    return userMapper.findByUsername(username);
  }

  public void createUser(User user) {
    userMapper.insertUser(user);
  }
}
