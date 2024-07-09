package com.gec.excel.DataListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.entity.Module;
import com.gec.entity.*;
import com.gec.excel.valid.ExcelImportValid;
import com.gec.service.*;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import com.gec.utils.TcTextComparatorUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

@Slf4j
public class TcDataListener extends AnalysisEventListener<Tc> {
    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;

    //传入正确的表头进行校验
    private Map<Integer, String> head;
    //表格内主要的名字不能相同
    private Map<Integer, String> names = new HashMap<>();
    //所属项目
    private String proCode;
    //所属模块
    private Module mod;
    private TcStandardService tcStandardService;
    private TcService tcService;
    private AnalysisService analysisService;
    List<Tc> tcList = new ArrayList();
    List<Analysis> analysisList = new ArrayList();
    //当前操作的用户
    private User sessionUser;
    public TcDataListener( TcStandardService tcStandardService, TcService tcService, AnalysisService analysisService, Map<Integer, String> head, String proCode, Module mod,User sessionUser) {
        this.tcStandardService = tcStandardService;
        this.tcService = tcService;
        this.analysisService = analysisService;
        this.head = head;
        this.proCode = proCode;
        this.mod = mod;
        this.sessionUser = sessionUser;
    }

    /**
     * 表头校验
     *
     * @param headMap
     * @param context
     */
    @SneakyThrows
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (headMap.size() <= 0) {
            throw new Exception("你上传的excel表格格式不对!请不要修改表头!");
        }
        for (Integer key : headMap.keySet()) {
            if (!head.containsValue(headMap.get(key))) {
                throw new Exception("你上传的excel表格格式不对!请不要修改表头!");
            }
        }
    }

    //读取数据会执行这方法
    @SneakyThrows
    @Override
    public void invoke(Tc tc, AnalysisContext analysisContext) {
        //数据校验
        try {
            ExcelImportValid.valid(tc);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        tc = convertTc(tc);
        if (tc != null) {
            Analysis analysis = convertAnalysis(tc, mod);
            if (analysis != null) {
                tcList.add(tc);
                analysisList.add(analysis);
            }
        }
    }

    private Tc convertTc(Tc tc) {
        try {
            int tcCount = tcService.count() + 1;
            String tcCode = CodeRule.TcRule + CodeUtils.getCode(tcCount, 5);
            tc.setTcCode(tcCode);
            tc.setProCode(proCode);
            tc.setModCode(mod.getModCode());
            switch (sessionUser.getRole().getValue()) {
                case 1:
                    tc.setAuthor("学生");
                    break;
                case 2:
                    tc.setAuthor("教师");
                    break;
                case 3:
                    tc.setAuthor("管理员");
                    break;
                case 4:
                    tc.setAuthor("超级管理员");
                    break;
            }
            //设置日期
            tc.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            tc.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            tc.setOperator(sessionUser.getUserCode());
            //是否启用
            tc.setIsDelete(Enabled);
            return tc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Analysis convertAnalysis(Tc tc, Module mod) {
        try {
            Analysis analysis = new Analysis();
            analysis.setTcCode(tc.getTcCode());
            analysis.setProCode(tc.getProCode());
            analysis.setModCode(tc.getModCode());
            switch (sessionUser.getRole().getValue()) {
                case 1:
                    analysis.setAuthor("学生");
                    break;
                case 2:
                    analysis.setAuthor("教师");
                    break;
                case 3:
                    analysis.setAuthor("管理员");
                    break;
                case 4:
                    analysis.setAuthor("超级管理员");
                    break;
            }
            TcTextComparatorUtils.AnalysisSimilarComputed(tc, analysis, mod, tcStandardService);
            analysis.setCreateDate(DateUtils.getCurrentData(DateString.yyMMdd));
            return analysis;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
    }

    private void saveData() {
        tcService.saveBatch(tcList);
        analysisService.saveBatch(analysisList);
    }
}
