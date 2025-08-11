package com.forge.gami.note.mapper;

import com.forge.gami.note.model.Note;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoteMapper {
    // 根据ID查询笔记
    Note selectById(Integer id);

    // 查询所有笔记
    List<Note> selectAll();

    // 新增笔记
    int insert(Note note);

    // 更新笔记
    int update(Note note);

    // 根据ID删除笔记
    int deleteById(Integer id);
}
