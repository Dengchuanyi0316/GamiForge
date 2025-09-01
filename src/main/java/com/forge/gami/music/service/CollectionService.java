package com.forge.gami.music.service;

import com.forge.gami.music.model.Collection;

import java.util.List;

public interface CollectionService {
    int addCollection(Collection collection);

    int removeCollection(Integer id);

    int modifyCollection(Collection collection);

    Collection getCollectionById(Integer id);

    List<Collection> getAllCollections();

    List<Collection> getCollectionsByUserId(Integer userId);

    // 添加收藏音乐关联
    int addCollectionMusic(Integer collectionId, Integer musicId);

    // 删除收藏音乐关联
    int removeCollectionMusic(Integer collectionId, Integer musicId);


}
