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
  Room selectAllById(int id);

  @Update("UPDATE room SET user1id=#{userid},user1Name=#{userName} WHERE id=#{roomid}")
  void updateUser1ByRoomId(int userid, String userName, int roomid);

  @Update("UPDATE room SET user2id=#{userid},user2Name=#{userName} WHERE id=#{roomid}")
  void updateUser2ByRoomId(int userid, String userName, int roomid);
}
