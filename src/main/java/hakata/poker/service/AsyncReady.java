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
public class AsyncReady {
  int count = 1;
  boolean dbUpdated = false;
  private final Logger logger = LoggerFactory.getLogger(AsyncReady.class);
  @Autowired
  private matchMapper matchMapper;

  @Transactional
  public void syncNewMatch(match match) {
    matchMapper.insertMatchandIsActive(match);
    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;
    return;
  }

  @Async
  public void AsyncReadySend(SseEmitter emitter) {
    String massage = "準備が完了しました";
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        emitter.send(massage);
        TimeUnit.MILLISECONDS.sleep(1000);
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
