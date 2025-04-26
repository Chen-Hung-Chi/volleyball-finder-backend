package com.volleyball.finder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volleyball.finder.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}