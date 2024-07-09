package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.DeptDao;
import com.gec.entity.Dept;
import com.gec.service.DeptService;
import org.springframework.stereotype.Service;

@Service
public class DeptServiceImpl extends ServiceImpl<DeptDao, Dept> implements DeptService {
}
