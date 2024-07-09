package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.StuDao;
import com.gec.entity.Stu;
import com.gec.service.StuService;
import org.springframework.stereotype.Service;

@Service
public class StuServiceImpl extends ServiceImpl<StuDao, Stu> implements StuService {
}
