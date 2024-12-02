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
public class AsyncMatch {
  int count = 1;
  boolean dbUpdated = false;

  private final Logger logger = LoggerFactory.getLogger(AsyncMatch.class);

  @Autowired
  private matchMapper matchMapper;

  @Transactional
  public void syncNewMatch(Match match) {
    matchMapper.insertMatchandIsActive(match);

    // 非同期でDB更新したことを共有する際に利用する
    this.dbUpdated = true;

    return;
  }

  /**
   * dbUpdatedがtrueのときのみブラウザにDBからフルーツリストを取得して送付する
   *
   * @param emitter
   */

  @Async
  public void AsyncMatchsend(SseEmitter emitter) {
    String massage = "相手が待っています。ポーカーにアクセスしましょう！";
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        emitter.send(massage);

        TimeUnit.MILLISECONDS.sleep(500);
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
