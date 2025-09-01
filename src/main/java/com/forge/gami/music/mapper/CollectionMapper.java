package com.forge.gami.music.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.forge.gami.music.model.Collection;

@Mapper
public interface CollectionMapper {
    int insertCollection(Collection collection);

    int deleteCollection(@Param("id") Integer id);

    int updateCollection(Collection collection);

    Collection selectCollectionById(@Param("id") Integer id);

    List<Collection> selectAllCollections();

    List<Collection> selectCollectionsByUserId(@Param("userId") Integer userId);

    // 添加收藏音乐关联
    @Insert("INSERT INTO collection_music(collection_id, music_id) VALUES(#{collectionId}, #{musicId})")
    int insertCollectionMusic(@Param("collectionId") Integer collectionId, @Param("musicId") Integer musicId);

    // 删除收藏音乐关联
    @Delete("DELETE FROM collection_music WHERE collection_id = #{collectionId} AND music_id = #{musicId}")
    int deleteCollectionMusic(@Param("collectionId") Integer collectionId, @Param("musicId") Integer musicId);


}
