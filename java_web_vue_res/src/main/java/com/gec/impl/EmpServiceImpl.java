package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.EmpDao;
import com.gec.entity.Emp;
import com.gec.service.EmpService;
import org.springframework.stereotype.Service;

@Service
public class EmpServiceImpl extends ServiceImpl<EmpDao, Emp> implements EmpService {
}

