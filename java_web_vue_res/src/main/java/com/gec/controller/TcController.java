package com.gec.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.common.SessionString;
import com.gec.entity.Module;
import com.gec.entity.*;
import com.gec.entity.reqData.ReqData;
import com.gec.excel.DataListener.TcDataListener;
import com.gec.service.*;
import com.gec.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

@RestController
@RequestMapping("/tc")
public class TcController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private TcStandardService tcStandardService;
    @Autowired
    private TcService tcService;
    @Autowired
    private AnalysisService analysisService;

    @RequestMapping("/getTcList")
    public Result getTcList(@RequestBody ReqData<Tc> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //老师和学生 看到的是不一样的
        switch (user.getRole().getValue()) {
            //学生
            case 1:
                return new Result(getStuTcData(reqData, request), "请求成功");
            //教师
            //管理员
            //超级管理员
            case 2:
            case 3:
            case 4:
                return new Result(getTcData(reqData), "请求成功");
        }
        return new Result("0", "请求失败");
    }

    private Map getStuTcData(ReqData<Tc> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("operator", user.getUserCode());
        List<Tc> tcList = EntityUtils.getListByPage(tcService, reqData, keyMap);
        int tcCount = EntityUtils.getListRecordByQuery(tcService, reqData, keyMap);
        return getResultData(tcList, tcCount);
    }

    private Map getTcData(ReqData<Tc> reqData) {
        List<Tc> tcList = EntityUtils.getListByPage(tcService, reqData);
        int tcCount = EntityUtils.getListRecordByQuery(tcService, reqData);
        return getResultData(tcList, tcCount);
    }

    private Map<String, Object> getResultData(List<Tc> tcList, int tcCount) {
        List<Module> modList = getModuleList();
        //目标模块必须里面有标准才能显示
        for (int i = 0; i < modList.size(); i++) {
            QueryWrapper<TcStandard> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("mod_code", modList.get(i).getModCode());
            List<TcStandard> tcStandards = tcStandardService.list(queryWrapper);
            if (tcStandards.size() < 1) {
                modList.remove(i);
                //一定要i--，for中删除或添加会出问题，此为方案一
                i--;
            }
        }
        //设置模块名称
        for (int i = 0; i < tcList.size(); i++) {
            for (int j = 0; j < modList.size(); j++) {
                if (tcList.get(i).getModCode().equals(modList.get(j).getModCode())) {
                    tcList.get(i).setModName(modList.get(j).getModName());
                }
            }
        }
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("tcList", tcList);
        data.put("totalRecord", tcCount);
        data.put("modList", modList);
        return data;
    }

    @DeleteMapping("/deleteTc")
    public Result deleteTc(@RequestHeader(value = "token") String token, @RequestBody ReqData<Tc> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "tc_code");
        boolean delete = tcService.update(null, updateWrapper);
        if (delete) {
            //更新删除后的列表给前端
            return new Result(getTcData(reqData), "删除成功");
        }
        return new Result("0", "删除失败");
    }


    @RequestMapping("/updateTc")
    public Result updateTc(@RequestBody ReqData<Tc> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        QueryWrapper<Tc> queryWrapper = new QueryWrapper();
        queryWrapper.eq("tc_code", reqData.getTcCode());
        Tc tc = tcService.getOne(queryWrapper);
        //超级管理员和管理员可以随意修改
        if (user.getRole().getValue() < 3) {
            //只能更改自己的用例
            if (tc.getOperator().equals(user.getUserCode())) {
                if (tcUpdate(reqData)) {
                    return new Result(getTcData(reqData), "更新成功");
                }
            } else {
                return new Result("0", "你只能修改你自己的用例");
            }
        } else {
            if (tcUpdate(reqData)) {
                return new Result(getTcData(reqData), "更新成功");
            }
        }

        return new Result("0", "更新失败");
    }

    private boolean tcUpdate(@RequestBody ReqData<Tc> reqData) {
        EntityUtils.enumUpdateObject(reqData.getUpdateObject());
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("tc_code", reqData.getTcCode());
        boolean update = tcService.update(null, updateWrapper);
        if (update) {
            QueryWrapper<Tc> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("tc_code", reqData.getTcCode());
            Tc tc = tcService.getOne(queryWrapper);
            update = updateAnalysis(tc);
            if (update) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping("/addTc")
    public Result addTc(@RequestBody ReqData<Tc> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Tc tc = reqData.getInsertObject();
        //查询最新的那条数据的id
        int tcCount = tcService.count() + 1;
        String tcCode = CodeRule.TcRule + CodeUtils.getCode(tcCount, 5);
        tc.setTcCode(tcCode);
        //获取模块编号
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("mod_code", reqData.getInsertObject().getModCode());
        Module mod = moduleService.getOne(queryWrapper);
        if (mod != null) {
            //获取项目编号
            queryWrapper.clear();
            queryWrapper.eq("pro_code", mod.getProCode());
            Project pro = projectService.getOne(queryWrapper);
            tc.setProCode(pro.getProCode());
        }
        switch (user.getRole().getValue()) {
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
        tc.setOperator(user.getUserCode());
        //是否启用
        tc.setIsDelete(Enabled);
        boolean save = tcService.save(tc);
        if (save) {
            save = addAnalysis(tc, mod,user);
            if (save) {
                return new Result(getTcData(reqData), "添加成功");
            }
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Tc> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "标题(必填)");
            head.put(1, "描述(必填)");
            head.put(2, "前置条件(必填)");
            head.put(3, "预期结果(必填)");
            head.put(4, "输入(必填)");
            head.put(5, "步骤(必填)");
            head.put(6, "重要程度,必填(低，中，高)");
            head.put(7, "备注");
            String proCode = "";
            String modCode = reqData.getModCode();
            //获取模块编号
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("mod_code", modCode);
            Module mod = moduleService.getOne(queryWrapper);
            if (mod != null) {
                //获取项目编号
                queryWrapper.clear();
                queryWrapper.eq("pro_code", mod.getProCode());
                Project pro = projectService.getOne(queryWrapper);
                proCode = pro.getProCode();
            }
            EasyExcel.read(file.getInputStream(), Tc.class, new TcDataListener( tcStandardService, tcService, analysisService, head, proCode, mod,user))
                    .sheet()
                    .doRead();
            return new Result(getTcData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }
    @RequestMapping("/export")
    public void export(@RequestBody ReqData<TcStandard> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Tc> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("tc_code", code).or();
        }
        ExcelUtils.getExportExcel(response, tcService, Tc.class, queryWrapper);
    }
    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Tc.class);
    }

    private List<Module> getModuleList() {
        //设置用户的部门
        QueryWrapper<Module> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return moduleService.list(queryWrapper);
    }


    private boolean updateAnalysis(Tc tc) {
        //用例对应的模块
        QueryWrapper<Module> moduleQueryWrapper = new QueryWrapper<>();
        moduleQueryWrapper.eq("mod_code", tc.getModCode());
        Module mod = moduleService.getOne(moduleQueryWrapper);
        //用例对应的用例分析
        QueryWrapper<Analysis> analysisQueryWrapper = new QueryWrapper<>();
        analysisQueryWrapper.eq("tc_code", tc.getTcCode());
        Analysis analysis = analysisService.getOne(analysisQueryWrapper);
        //根据最相似的标题查找标准
        TcTextComparatorUtils.AnalysisSimilarComputed(tc, analysis, mod, tcStandardService);
        boolean update = analysisService.updateById(analysis);
        return update;
    }

    private boolean addAnalysis(Tc tc, Module mod,User user) {
        Analysis analysis = new Analysis();
        analysis.setTcCode(tc.getTcCode());
        analysis.setProCode(tc.getProCode());
        analysis.setModCode(tc.getModCode());
        switch (user.getRole().getValue()) {
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
        return analysisService.save(analysis);
    }
}