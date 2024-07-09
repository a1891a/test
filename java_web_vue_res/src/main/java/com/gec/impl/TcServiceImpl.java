package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.TcDao;
import com.gec.entity.Tc;
import com.gec.service.TcService;
import org.springframework.stereotype.Service;

@Service
public class TcServiceImpl extends ServiceImpl<TcDao, Tc> implements TcService {
}
