package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.ClazzDao;
import com.gec.entity.Clazz;
import com.gec.service.ClazzService;
import org.springframework.stereotype.Service;

@Service
public class ClazzServiceImpl extends ServiceImpl<ClazzDao, Clazz> implements ClazzService {
}
