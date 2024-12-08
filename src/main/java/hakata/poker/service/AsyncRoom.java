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

import hakata.poker.model.Room;
import hakata.poker.model.RoomMapper;


@Service
public class AsyncRoom {
  private final Logger logger = LoggerFactory.getLogger(AsyncRoom.class);
  boolean dbUpdated = false;

  @Autowired
  RoomMapper rMapper;

  @Transactional
  public int syncEnterRoom(int userIndex,int userId,int roomId){
    rMapper.updateUser2idByRoomId(userId,roomId);
    this.dbUpdated = true;
    return userIndex;
  }

  public ArrayList<Room> syncShowRoomsList() {
    return rMapper.selectAll();
  }

  public Room syncShowRoomById(int roomId) {
    return rMapper.selectAllByid(roomId);
  }



  @Async
  public void asyncShowRoomsList(SseEmitter emitter) {
    dbUpdated = true;
    try {
      while (true) {// 無限ループ
        // DBが更新されていなければ0.5s休み
        if (false == dbUpdated) {
          TimeUnit.MILLISECONDS.sleep(500);
          continue;
        }
        // DBが更新されていれば更新後のフルーツリストを取得してsendし，1s休み，dbUpdatedをfalseにする
        ArrayList<Room> rooms3 = this.syncShowRoomsList();
        emitter.send(rooms3);
        TimeUnit.MILLISECONDS.sleep(1000);
        dbUpdated = false;
      }
    } catch (Exception e) {
      // 例外の名前とメッセージだけ表示する
      logger.warn("Exception:" + e.getClass().getName() + ":" + e.getMessage());
    } finally {
      emitter.complete();
    }
    System.out.println("asyncShowRoomsList complete");
  }
}
