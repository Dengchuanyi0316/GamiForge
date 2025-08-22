package com.forge.gami.music.service;

import com.forge.gami.music.model.Music;

import java.util.List;

public interface MusicService {
    // 插入音乐
    int addMusic(Music music);

    // 删除音乐
    int deleteMusic(Integer id);

    // 修改音乐
    int updateMusic(Music music);

    // 根据ID查询
    Music getMusicById(Integer id);

    // 查询所有音乐
    List<Music> getAllMusic();
}
