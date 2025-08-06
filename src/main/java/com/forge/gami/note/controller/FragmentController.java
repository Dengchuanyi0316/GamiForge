package com.forge.gami.note.controller;

import com.forge.gami.note.model.Fragment;
import com.forge.gami.note.service.FragmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fragments")
public class FragmentController {
    private final FragmentService fragmentService;

    @Autowired
    public FragmentController(FragmentService fragmentService) {
        this.fragmentService = fragmentService;
    }

    // 查询单条
    @GetMapping("/{id}")
    public Fragment getById(@PathVariable Long id) {
        return fragmentService.getById(id);
    }

    // 查询全部
    @GetMapping
    public List<Fragment> getAll() {
        return fragmentService.getAll();
    }

    // 添加
    @PostMapping
    public void add(@RequestBody Fragment fragment) {
        fragmentService.add(fragment);
    }

    // 更新
    @PutMapping
    public void update(@RequestBody Fragment fragment) {
        fragmentService.update(fragment);
    }

    // 删除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fragmentService.delete(id);
    }
}
