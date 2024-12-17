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
public class Asyncresult {

  boolean dbUpdated1 = false;
  boolean dbUpdated2 = false;
  private final Logger logger = LoggerFactory.getLogger(AsyncReady.class);
  @Autowired
  private matchMapper matchMapper;

  @Transactional
  public void syncUser1(int id, int handid) {
    matchMapper.updateUser1HandById(id, handid);
    this.dbUpdated1 = true;
    return;
  }

  @Transactional
  public void syncUser2(int id, int handid) {
    matchMapper.updateUser2HandById(id, handid);
    this.dbUpdated2 = true;
    return;
  }

  @Transactional
  public void syncresult(int id) {
    match match = matchMapper.selectAllById(id);
    matchMapper.updateuser1CoinById(match.getId(), match.getUser1coin() - match.getBet());
    matchMapper.updateuser2CoinById(match.getId(), match.getUser2coin() + match.getBet());
    matchMapper.updateBetById(match.getId(), 1);
    return;
  }

  @Async
  public void AsyncReusltSend(SseEmitter emitter) {
    String massage = "result";
    System.out.println("ここつながるか");
    try {

      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated1 || false == dbUpdated2) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        emitter.send(massage);
        TimeUnit.MILLISECONDS.sleep(10);
        dbUpdated1 = false;
        dbUpdated2 = false;
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
