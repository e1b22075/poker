package hakata.poker.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import hakata.poker.model.matchMapper;
import hakata.poker.model.match;

@Service
public class AsyncResultbySSE {
  boolean dbUpdated = false;
  private final Logger logger = LoggerFactory.getLogger(AsyncReady.class);
  @Autowired
  private matchMapper matchMapper;

  @Transactional
  public void syncResult1(int id, int coin, int bet) {
    matchMapper.updateuser1CoinById(id, coin + bet);
    this.dbUpdated = true;
    return;
  }

  @Transactional
  public void syncResult2(int id, int coin, int bet) {
    matchMapper.updateuser1CoinById(id, coin + bet);
    this.dbUpdated = true;
    return;
  }

  @Async
  public void AsyncReusltSend(SseEmitter emitter) {
    String massage = "result";
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        emitter.send(massage);
        TimeUnit.MILLISECONDS.sleep(100);
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
