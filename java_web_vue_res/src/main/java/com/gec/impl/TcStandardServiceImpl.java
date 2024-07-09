package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.TcStandardDao;
import com.gec.entity.TcStandard;
import com.gec.service.TcStandardService;
import org.springframework.stereotype.Service;

@Service
public class TcStandardServiceImpl extends ServiceImpl<TcStandardDao, TcStandard> implements TcStandardService {
}
