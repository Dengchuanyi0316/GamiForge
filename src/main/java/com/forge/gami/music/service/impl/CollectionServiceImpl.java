package com.forge.gami.music.service.impl;

import com.forge.gami.music.mapper.CollectionMapper;
import com.forge.gami.music.model.Collection;
import com.forge.gami.music.service.CollectionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CollectionServiceImpl implements CollectionService {
    @Resource
    private CollectionMapper collectionMapper;

    @Override
    public int addCollection(Collection collection) {
        return collectionMapper.insertCollection(collection);
    }

    @Override
    public int removeCollection(Integer id) {
        return collectionMapper.deleteCollection(id);
    }

    @Override
    public int modifyCollection(Collection collection) {
        return collectionMapper.updateCollection(collection);
    }

    @Override
    public Collection getCollectionById(Integer id) {
        return collectionMapper.selectCollectionById(id);
    }

    @Override
    public List<Collection> getAllCollections() {
        return collectionMapper.selectAllCollections();
    }

    @Override
    public List<Collection> getCollectionsByUserId(Integer userId) {
        return collectionMapper.selectCollectionsByUserId(userId);
    }

    @Override
    public int addCollectionMusic(Integer collectionId, Integer musicId) {
        return collectionMapper.insertCollectionMusic(collectionId, musicId);
    }

    @Override
    public int removeCollectionMusic(Integer collectionId, Integer musicId) {
        return collectionMapper.deleteCollectionMusic(collectionId, musicId);
    }

}
