package com.homeservice.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeservice.catalog.domain.ServiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {
}
