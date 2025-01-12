package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.fasterxml.jackson.databind.deser.DataFormatReaders.Match;

import hakata.poker.model.match;

@Mapper
public interface matchMapper {
  @Select("SELECT * from match where user1id = #{id} AND isActive = true")
  match selectAllByuser1Id(int id);

  @Select("SELECT * from match where user2id = #{id} AND isActive = true")
  match selectAllByuser2Id(int id);

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

  @Update("UPDATE match SET user1hand =#{handid} WHERE id =#{id} AND isActive = true")
  void updateUser1HandById(int id, int handid);

  @Update("UPDATE match SET user2hand =#{handid} WHERE id =#{id} AND isActive = true")
  void updateUser2HandById(int id, int handid);

  @Update("UPDATE match SET round =#{round} WHERE id =#{id} AND isActive = true")
  void updateRoundById(int id, int round);

  @Insert("INSERT INTO match (user1id,user2id,user1coin,user2coin,bet,round,rid,isActive) VALUES (#{user1id},#{user2id},#{user1coin},#{user2coin},#{bet},#{round},#{rid},true);")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertMatchandIsActive(match match);

  @Select("SELECT * from match where (user1id = #{id} or user2id = #{id}) AND isActive = true")
  match selectAllById(int id);

  @Delete("DELETE FROM match WHERE ID =#{id} AND isActive = true")
  boolean deleteById(int id);
}
