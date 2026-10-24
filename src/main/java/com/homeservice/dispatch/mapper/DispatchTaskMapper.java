package com.homeservice.dispatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeservice.dispatch.domain.DispatchTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DispatchTaskMapper extends BaseMapper<DispatchTask> {
}
