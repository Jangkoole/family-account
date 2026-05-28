package com.family.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.account.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}