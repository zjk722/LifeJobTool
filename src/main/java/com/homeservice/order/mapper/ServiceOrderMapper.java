package com.homeservice.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homeservice.order.domain.ServiceOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceOrderMapper extends BaseMapper<ServiceOrder> {
}
