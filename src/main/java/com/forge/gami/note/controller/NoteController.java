package com.forge.gami.note.controller;

import com.forge.gami.note.model.Note;
import com.forge.gami.note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    @Autowired
    private NoteService noteService;

    // 获取单个笔记
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable Integer id) {
        Note note = noteService.getNoteById(id);
        if (note == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(note);
    }

    // 获取所有笔记
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        List<Note> notes = noteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    // 创建新笔记
    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note) {
        boolean success = noteService.createNote(note);
        if (success) {
            return ResponseEntity.status(HttpStatus.CREATED).body(note);
        }
        return ResponseEntity.badRequest().build();
    }

    // 更新笔记
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable Integer id,
            @RequestBody Note note) {
        note.setId(id); // 确保ID一致
        boolean success = noteService.updateNote(note);
        if (success) {
            return ResponseEntity.ok(note);
        }
        return ResponseEntity.notFound().build();
    }

    // 删除笔记
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNoteById(@PathVariable Integer id) {
        boolean success = noteService.deleteNoteById(id);
        if (success) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
