package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.campussharebackend.entity.Message;
import com.campus.campussharebackend.vo.MessageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {

    // 分页查询用户的消息列表（关联类型文本转换）
    IPage<MessageVO> selectUserMessages(IPage<Message> page, @Param(Constants.WRAPPER) Wrapper<Message> queryWrapper);

    // 批量更新消息为已读
    @Update("<script>" +
            "update message set is_read = 1, update_time = now() " +
            "where id in " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "and user_id = #{userId}" +
            "</script>")
    void batchMarkRead(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);

    // 批量逻辑删除消息（实际项目中可改为物理删除，根据需求调整）
    @Update("<script>" +
            "update message set is_delete = 1, update_time = now() " +
            "where id in " +
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "and user_id = #{userId}" +
            "</script>")
    void batchDelete(@Param("messageIds") List<Long> messageIds, @Param("userId") Long userId);
}