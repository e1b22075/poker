package hakata.poker.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.User;
import hakata.poker.model.UserMapper;

@Service
public class AsyncUser {
  private final Logger logger = LoggerFactory.getLogger(AsyncUser.class);
  boolean dbUpdated = false;

  @Autowired
  UserMapper uMapper;

  @Transactional
  public User syncUpdateUserName(int userId, String newUserName) {
    User user = uMapper.selectAllById(userId);
    uMapper.updateUserName(userId, newUserName);
    this.dbUpdated = true;
    return user;
  }

  @Transactional
  public void syncInsertUser(User user) {
    int userid;
    uMapper.insertUser(user);
    this.dbUpdated = true;
  }

  public ArrayList<User> syncShowUsersList() {
    return uMapper.selectAll();
  }

  public User syncShowUserById(int Id) {
    return uMapper.selectAllById(Id);
  }

  @Async
  public void asyncShowUsersList(SseEmitter emitter) {
    dbUpdated = true;
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        // DBが更新されていれば更新後のフルーツリストを取得してsendし，1s休み，dbUpdatedをfalseにする
        ArrayList<User> users3 = this.syncShowUsersList();
        emitter.send(SseEmitter.event().name("users").data(users3));
        TimeUnit.MILLISECONDS.sleep(1000);
        dbUpdated = false;
      }
    } catch (Exception e) {
      // 例外の名前とメッセージだけ表示する
      logger.warn("Exception:" + e.getClass().getName() + ":" + e.getMessage());
    } finally {
      emitter.complete();
    }
    System.out.println("asyncShowUsersList complete");
  }
}
