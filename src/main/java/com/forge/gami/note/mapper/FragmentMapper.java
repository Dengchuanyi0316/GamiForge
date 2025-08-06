package com.forge.gami.note.mapper;

import com.forge.gami.note.model.Fragment;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FragmentMapper {
    Fragment selectById(Long id);
    List<Fragment> selectAll();
    int insert(Fragment fragment);
    int update(Fragment fragment);
    int deleteById(Long id);
}
