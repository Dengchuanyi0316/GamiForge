package com.forge.gami.note.service;

import com.forge.gami.note.model.Note;

import java.util.List;

public interface NoteService {
    // 根据ID查询笔记
    Note getNoteById(Integer id);

    // 查询所有笔记
    List<Note> getAllNotes();

    // 创建新笔记
    boolean createNote(Note note);

    // 更新笔记
    boolean updateNote(Note note);

    // 删除笔记
    boolean deleteNoteById(Integer id);
}
