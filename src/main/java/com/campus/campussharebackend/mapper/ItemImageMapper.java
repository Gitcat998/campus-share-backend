package com.campus.campussharebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.campus.campussharebackend.entity.ItemImage;
import com.campus.campussharebackend.vo.ItemImageVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ItemImageMapper extends BaseMapper<ItemImage> {
    // 查询物品所有图片URL
    @Select("select image_url from item_image where item_id = #{itemId} order by is_main desc")
    List<String> selectImageUrlsByItemId(Long itemId);

    // 查询主图URL
    @Select("select image_url from item_image where item_id = #{itemId} and is_main = 1 limit 1")
    String selectMainImageUrl(Long itemId);

    // 查询物品图片详情（含ID和主图标识）
    @Select("select id, image_url, is_main from item_image where item_id = #{itemId} order by is_main desc, create_time asc")
    List<ItemImageVO> selectImagesByItemId(Long itemId);

    // 将物品所有图片设为非主图
    @Update("update item_image set is_main = 0, update_time = now() where item_id = #{itemId}")
    void updateAllNonMain(Long itemId);

    // 按ID查询图片（用于权限校验）
    @Select("select * from item_image where id = #{imageId}")
    ItemImage selectById(Long imageId);
}