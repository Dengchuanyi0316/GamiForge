package com.forge.gami.note.service.impl;

import com.forge.gami.note.mapper.FragmentMapper;
import com.forge.gami.note.model.Fragment;
import com.forge.gami.note.service.FragmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FragmentServiceImpl implements FragmentService {
    private final FragmentMapper fragmentMapper;

    @Autowired
    public FragmentServiceImpl(FragmentMapper fragmentMapper) {
        this.fragmentMapper = fragmentMapper;
    }

    @Override
    public Fragment getById(Long id) {
        return fragmentMapper.selectById(id);
    }

    @Override
    public List<Fragment> getAll() {
        return fragmentMapper.selectAll();
    }

    @Override
    public void add(Fragment fragment) {
        fragmentMapper.insert(fragment);
    }

    @Override
    public void update(Fragment fragment) {
        fragmentMapper.update(fragment);
    }

    @Override
    public void delete(Long id) {
        fragmentMapper.deleteById(id);
    }
}
