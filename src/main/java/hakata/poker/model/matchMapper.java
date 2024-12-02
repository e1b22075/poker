package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface matchMapper {

  @Select("SELECT * from match where (user1id = #{id} or user2id = #{id}) AND isActive = true")
  ArrayList<Match> selectAllById(int id);

  @Select("SELECT * from match where user1id = #{id} AND isActive = true")
  Match selectAllByuser1Id(int id);

  @Select("SELECT * from match where user2id = #{id} AND isActive = true")
  Match selectAllByuser2Id(int id);

  @Update("UPDATE match SET user1state =#{state} WHERE id =#{id} AND isActive = true")
  void updateuser1StateById(int id, String state);

  @Update("UPDATE match SET user1coin =#{coin} WHERE id =#{id} AND isActive = true")
  void updateuser1CoinById(int id, int coin);

  @Update("UPDATE match SET user2coin =#{coin} WHERE id =#{id} AND isActive = true")
  void updateuser2CoinById(int id, int coin);

  @Update("UPDATE match SET  user2state =#{state} WHERE id =#{id} AND isActive = true")
  void updateuser2StateById(int id, String state);

  @Update("UPDATE match SET bet =#{bet} WHERE id =#{id} AND isActive = true")
  void updateBetById(int id, int bet);

  @Insert("INSERT INTO match (user1id,user2id,user1coin,user2coin,bet,isActive) VALUES (#{user1id},#{user2id},#{user1coin},#{user2coin},#{bet},true);")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchandIsActive(Match match);
}
