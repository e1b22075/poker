package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface RoomMapper {
  @Select("SELECT * from room")
  ArrayList<Room> selectAll();

  @Select("SELECT * from room where id =#{id}")
  Room selectAllByid(int id);

  @Update("UPDATE room SET user1id=#{userid} WHERE id=#{roomid}")
  void updateUser1idByRoomId(int userid, int roomid);

  @Update("UPDATE room SET user2id=#{userid} WHERE id=#{roomid}")
  void updateUser2idByRoomId(int userid, int roomid);
}
