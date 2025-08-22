package com.forge.gami.music.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.forge.gami.music.model.Music;
import com.forge.gami.music.service.MusicService;

import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/music")
public class MusicController {
    @Autowired
    private MusicService musicService;

    // 查询所有音乐
    @GetMapping("/all")
    public List<Music> getAllMusic() {
        return musicService.getAllMusic();
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public Music getMusicById(@PathVariable Integer id) {
        return musicService.getMusicById(id);
    }

    // 添加音乐
    @PostMapping("/add")
    public String addMusic(@RequestBody Music music) {
        int result = musicService.addMusic(music);
        return result > 0 ? "添加成功，ID=" + music.getId() : "添加失败";
    }

    // 修改音乐
    @PutMapping("/update")
    public String updateMusic(@RequestBody Music music) {
        int result = musicService.updateMusic(music);
        return result > 0 ? "修改成功" : "修改失败";
    }

    // 删除音乐
    @DeleteMapping("/delete/{id}")
    public String deleteMusic(@PathVariable Integer id) {
        int result = musicService.deleteMusic(id);
        return result > 0 ? "删除成功" : "删除失败";
    }
}
