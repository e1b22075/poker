package hakata.poker.service;

import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

  private final Map<Integer, Boolean> dbUpdatedMap = new ConcurrentHashMap<>();

  @Transactional
  public void syncNewMatch(match match) {
    matchMapper.insertMatchandIsActive(match);
    // 非同期でDB更新したことを共有する際に利用する
    dbUpdatedMap.put(match.getRid(), true);
    return;
  }

  @Async
  public void AsyncReadySend(SseEmitter emitter, int roomId) {
    String message = "Ready Go!!";

    System.out.println("送信前: " + message + " (ルームID: " + roomId + ")");
    try {
      while (true) {
        // 指定のルームIDの状態を確認
        if (dbUpdatedMap.getOrDefault(roomId, false) == false) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }

        // メッセージ送信
        emitter.send(message);

        // 状態をリセット
        dbUpdatedMap.put(roomId, false);
      }
    } catch (Exception e) {
      logger.warn("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    } finally {
      emitter.complete();
      System.out.println("AsyncDropSend complete for roomId: " + roomId);
    }
  }
}
