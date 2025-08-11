package com.forge.gami.note.service.impl;

import com.forge.gami.note.mapper.NoteMapper;
import com.forge.gami.note.model.Note;
import com.forge.gami.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    private NoteMapper noteMapper;

    @Override
    public Note getNoteById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return noteMapper.selectById(id);
    }

    @Override
    public List<Note> getAllNotes() {
        return noteMapper.selectAll();
    }

    @Override
    public boolean createNote(Note note) {
        if (note == null || note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return false;
        }
        // 设置默认值
        if (note.getStatus() == null) {
            note.setStatus("draft");
        }
        return noteMapper.insert(note) > 0;
    }

    @Override
    public boolean updateNote(Note note) {
        if (note == null || note.getId() == null) {
            return false;
        }
        return noteMapper.update(note) > 0;
    }

    @Override
    public boolean deleteNoteById(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        return noteMapper.deleteById(id) > 0;
    }
}
