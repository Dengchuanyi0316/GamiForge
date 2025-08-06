package com.forge.gami.note.service;

import com.forge.gami.note.model.Fragment;

import java.util.List;

public interface FragmentService {
    Fragment getById(Long id);

    List<Fragment> getAll();

    void add(Fragment fragment);

    void update(Fragment fragment);

    void delete(Long id);

}
