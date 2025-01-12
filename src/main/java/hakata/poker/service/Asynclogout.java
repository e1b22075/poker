package hakata.poker.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
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
public class Asynclogout {
  private final Logger logger = LoggerFactory.getLogger(AsyncDrop.class);
  private final Map<Integer, RoomState> roomStates = new ConcurrentHashMap<>();
  @Autowired
  private matchMapper matchMapper;

  private static class RoomState {
    private final List<SseEmitter> emitters = new ArrayList<>();
    private boolean updated = false;

    public synchronized void addEmitter(SseEmitter emitter) {
      emitters.add(emitter);
    }

    public synchronized void setUpdated(boolean updated) {
      this.updated = updated;
    }

    public synchronized boolean isUpdated() {
      return updated;
    }

    public synchronized List<SseEmitter> getEmitters() {
      return new ArrayList<>(emitters); // コピーを返す
    }

    public synchronized void clearEmitters() {
      emitters.clear();
    }
  }

  @Transactional
  public void synclogput(int id, int roomId) {

    setRoomUpdated(roomId);
  }

  private void setRoomUpdated(int roomId) {
    roomStates.computeIfAbsent(roomId, k -> new RoomState()).setUpdated(true);
  }

  @Async
  public void startRoomMonitor(int roomId) {
    RoomState roomState = roomStates.computeIfAbsent(roomId, k -> new RoomState());

    try {
      while (true) {
        synchronized (roomState) {
          if (!roomState.isUpdated()) {
            roomState.wait(500); // 状態が更新されるのを待機
            continue;
          }

          // 状態が更新された場合、すべてのエミッタに通知
          List<SseEmitter> emitters = roomState.getEmitters();
          Iterator<SseEmitter> iterator = emitters.iterator();

          while (iterator.hasNext()) {
            SseEmitter emitter = iterator.next();
            try {
              emitter.send("logout");
            } catch (Exception e) {
              // エラーが発生したエミッタは削除
              iterator.remove();
            }
          }

          // 状態リセット
          roomState.setUpdated(false);
        }
      }
    } catch (InterruptedException e) {
      logger.info("Room monitor interrupted for roomId: " + roomId);
    } finally {
      roomState.clearEmitters();
      logger.info("Room monitor stopped for roomId: " + roomId);
    }
  }

  public void registerEmitter(int roomId, SseEmitter emitter) {
    RoomState roomState = roomStates.computeIfAbsent(roomId, k -> new RoomState());
    roomState.addEmitter(emitter);

    emitter.onCompletion(() -> removeEmitter(roomId, emitter));
    emitter.onTimeout(() -> removeEmitter(roomId, emitter));
    emitter.onError((e) -> removeEmitter(roomId, emitter));
  }

  private void removeEmitter(int roomId, SseEmitter emitter) {
    RoomState roomState = roomStates.get(roomId);
    if (roomState != null) {
      synchronized (roomState) {
        roomState.getEmitters().remove(emitter);
      }
    }
  }
}
