package com.homeservice.worker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeservice.worker.domain.Worker;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkerMapper extends BaseMapper<Worker> {
}
