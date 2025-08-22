package com.forge.gami.music.service.impl;

import com.forge.gami.music.mapper.MusicMapper;
import com.forge.gami.music.model.Music;
import com.forge.gami.music.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicServiceImpl implements MusicService {
    @Autowired
    private MusicMapper musicMapper;

    @Override
    public int addMusic(Music music) {
        return musicMapper.insertMusic(music);
    }

    @Override
    public int deleteMusic(Integer id) {
        return musicMapper.deleteMusic(id);
    }

    @Override
    public int updateMusic(Music music) {
        return musicMapper.updateMusic(music);
    }

    @Override
    public Music getMusicById(Integer id) {
        return musicMapper.selectMusicById(id);
    }

    @Override
    public List<Music> getAllMusic() {
        return musicMapper.selectAllMusic();
    }
}
