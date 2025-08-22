package com.forge.gami.music.mapper;

import com.forge.gami.music.model.Music;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MusicMapper {
    // 插入
    int insertMusic(Music music);

    // 删除
    int deleteMusic(Integer id);

    // 修改
    int updateMusic(Music music);

    // 根据ID查询
    Music selectMusicById(Integer id);

    // 查询全部
    List<Music> selectAllMusic();
}
