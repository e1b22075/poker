package hakata.poker.model;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface HandMapper {
  @Select("SELECT * from hand")
  ArrayList<Hand> selectAll();

  @Select("SELECT * from hand where id = #{id}")
  ArrayList<Hand> selectAllById(int id);

  @Select("SELECT * from hand where userid = #{id} AND isActive = true")
  Hand selectByUserId(int id);

  @Insert("INSERT INTO Hand (userid,hand1id,hand2id,hand3id,hand4id,hand5id,turn,isActive) VALUES (#{userid},#{hand1id},#{hand2id},#{hand3id},#{hand4id},#{hand5id},#{turn},true);")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertHandandIsActive(Hand hand);


  @Insert("INSERT INTO Hand (userid,hand1id,hand2id,hand3id,hand4id,hand5id,turn,roleid,rolenum,onepairkickernum,onepairkickerid,twopairid,isActive) VALUES (#{userid},#{hand1id},#{hand2id},#{hand3id},#{hand4id},#{hand5id},#{turn},#{roleid},#{rolenum},#{onepairkickernum},#{onepairkickerid},#{twopairid},true);")
  @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
  void insertHandandIsActive2(Hand hand);


  @Update("UPDATE HAND SET ROLEID=#{ROLEID}, WHERE ID=#{id}")
  void updateRoleIdById(int id, int roleid);

  @Update("UPDATE HAND SET ROLEID=#{ROLEID},ROLENUM=#{ROLENUM} WHERE ID=#{id}")
  void updateRoleIdRoleNumById(int id);

  @Update("UPDATE HAND SET isActive = true WHERE isActive = false")
  void updateIsActivefalsetotrueByfalse();

  @Update("UPDATE HAND SET isActive = false WHERE isActive = true AND userid = #{id}")
  void updateIsActivefalsetotrueByfalseAndUserId(int id);

  @Update("UPDATE HAND SET isActive = false WHERE isActive = true")
  void updateIsActivetruetofalseBytrue();

  @Delete("DELETE FROM hand WHERE ID =#{id}")
  boolean deleteById(int id);

  @Delete("DELETE FROM hand WHERE isActive =false")
  boolean deleteByfalse(int id);
}
