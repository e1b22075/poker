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

import hakata.poker.model.Match;
import hakata.poker.model.matchMapper;

@Service
public class AsyncState {
  boolean dbUpdated = false;
  boolean changeFlag1 = false;
  boolean changeFlag2 = false;
  boolean callflag1 = false;
  boolean callflag2 = false;
  boolean dropflag1 = false;
  boolean dropflag2 = false;
  boolean raysflag1 = false;
  boolean raysflag2 = false;

  @Autowired
  private matchMapper matchMapper;

  private final Logger logger = LoggerFactory.getLogger(AsyncMatch.class);

  @Transactional
  public void syncchange1(int id, String state) {
    matchMapper.updateuser1StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.changeFlag1 = true;
    return;
  }

  @Transactional
  public void syncchange2(int id, String state) {
    matchMapper.updateuser2StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.changeFlag2 = true;
    return;
  }

  @Transactional
  public void syncdrop1(int id, String state) {
    matchMapper.updateuser2StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.dropflag1 = true;
    return;
  }

  @Transactional
  public void syncdrop2(int id, String state) {
    matchMapper.updateuser2StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.dropflag2 = true;
    return;
  }

  @Transactional
  public void syncrays1(int id, String state) {
    matchMapper.updateuser1StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.raysflag1 = true;
    return;
  }

  @Transactional
  public void syncrays2(int id, String state) {
    matchMapper.updateuser2StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.raysflag2 = true;
    return;
  }

  @Transactional
  public void synccall2(int id, String state) {
    matchMapper.updateuser1StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.callflag2 = true;
    return;
  }

  @Transactional
  public void synccall1(int id, String state) {
    matchMapper.updateuser2StateById(id, state);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    this.callflag1 = true;
    return;
  }

  @Async
  public void AsyncMatchsend(SseEmitter emitter) {
    String massage = "各プレイヤーが行動中…";
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(100);
          continue;
        } else {
          if (true == changeFlag1) {
            massage = "プレイヤー1はカードを交換しました";
            changeFlag1 = false;
          } else if (true == changeFlag2) {
            massage = "プレイヤー2はカードを交換しました";
            changeFlag2 = false;
          } else if (true == dropflag1) {
            massage = "プレイヤー1はドロップしました";
            dropflag1 = false;
          } else if (true == dropflag2) {
            massage = "プレイヤー2はドロップしました";
            dropflag2 = false;
          } else if (true == callflag1) {
            massage = "プレイヤー1はコールしました";
            callflag1 = false;
          } else if (true == callflag2) {
            massage = "プレイヤー2はコールしました";
            callflag2 = false;
          } else if (true == raysflag1) {
            massage = "プレイヤー1はレイズしました";
            raysflag1 = false;
          } else if (true == raysflag2) {
            massage = "プレイヤー2はレイズしました";
            raysflag2 = false;
          }
        }
        emitter.send(massage);
        dbUpdated = false;
        TimeUnit.MILLISECONDS.sleep(100);

      }
    } catch (Exception e) {
      // 例外の名前とメッセージだけ表示する
      logger.warn("Exception:" + e.getClass().getName() + ":" + e.getMessage());
    } finally {
      emitter.complete();
    }
    System.out.println("AsyncMatch complete");
  }

}
