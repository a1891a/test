package com.gec.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.dao.AnalysisDao;
import com.gec.entity.Analysis;
import com.gec.service.AnalysisService;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImpl extends ServiceImpl<AnalysisDao, Analysis> implements AnalysisService {
}
