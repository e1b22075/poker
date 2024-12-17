package hakata.poker.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.match;
import hakata.poker.model.matchMapper;

@Service
public class AsyncDrop {
  boolean dbUpdated = false;
  private final Logger logger = LoggerFactory.getLogger(AsyncReady.class);
  @Autowired
  private matchMapper matchMapper;

  @Transactional
  public void syncDrop1(int id) {
    matchMapper.updateuser1StateById(id, "drop");
    this.dbUpdated = true;
    return;
  }

  @Transactional
  public void syncDrop2(int id) {
    matchMapper.updateuser2StateById(id, "drop");
    this.dbUpdated = true;
    return;
  }

  @Async
  public void AsyncDropSend(SseEmitter emitter) {
    String massage = "drop";
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        emitter.send(massage);
        TimeUnit.MILLISECONDS.sleep(10);
        dbUpdated = false;
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
