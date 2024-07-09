package com.gec.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.entity.Emp;
import org.apache.ibatis.annotations.Select;
import org.apache.poi.ss.formula.functions.T;

public interface EmpDao extends BaseMapper<Emp> {
}
