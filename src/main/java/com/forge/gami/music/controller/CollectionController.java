package com.forge.gami.music.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import com.forge.gami.music.model.Collection;
import com.forge.gami.music.service.CollectionService;

import java.util.List;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    @Resource
    private CollectionService collectionService;

    // 新增
    @PostMapping
    public String addCollection(@RequestBody Collection collection) {
        int result = collectionService.addCollection(collection);
        return result > 0 ? "添加成功" : "添加失败";
    }

    // 删除
    @DeleteMapping("/{id}")
    public String deleteCollection(@PathVariable Integer id) {
        int result = collectionService.removeCollection(id);
        return result > 0 ? "删除成功" : "删除失败";
    }

    // 修改
    @PutMapping
    public String updateCollection(@RequestBody Collection collection) {
        int result = collectionService.modifyCollection(collection);
        return result > 0 ? "更新成功" : "更新失败";
    }

    // 查询单个
    @GetMapping("/{id}")
    public Collection getCollectionById(@PathVariable Integer id) {
        return collectionService.getCollectionById(id);
    }

    // 查询所有
    @GetMapping
    public List<Collection> getAllCollections() {
        return collectionService.getAllCollections();
    }

    // 根据用户查
    @GetMapping("/user/{userId}")
    public List<Collection> getCollectionsByUserId(@PathVariable Integer userId) {
        return collectionService.getCollectionsByUserId(userId);
    }

    // 添加音乐到收藏
    @PostMapping("/{collectionId}/music/{musicId}")
    public String addMusicToCollection(@PathVariable Integer collectionId, @PathVariable Integer musicId) {
        int result = collectionService.addCollectionMusic(collectionId, musicId);
        return result > 0 ? "收藏音乐成功" : "收藏音乐失败";
    }

    // 从收藏中移除音乐
    @DeleteMapping("/{collectionId}/music/{musicId}")
    public String removeMusicFromCollection(@PathVariable Integer collectionId, @PathVariable Integer musicId) {
        int result = collectionService.removeCollectionMusic(collectionId, musicId);
        return result > 0 ? "移除收藏音乐成功" : "移除收藏音乐失败";
    }
}
