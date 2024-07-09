package com.gec.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.common.SessionString;
import com.gec.entity.*;
import com.gec.entity.Module;
import com.gec.entity.reqData.ReqData;
import com.gec.excel.DataListener.DefaultDataListener;
import com.gec.service.AnalysisService;
import com.gec.service.ModuleService;
import com.gec.service.TcService;
import com.gec.service.TcStandardService;
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
@RequestMapping("/tcStandard")
public class TcStandardController {
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private TcService tcService;
    @Autowired
    private TcStandardService tcStandardService;
    @Autowired
    private AnalysisService analysisService;

    @RequestMapping("/getTcStandardList")
    public Result getTcStandardList(@RequestBody ReqData<TcStandard> reqData, HttpServletRequest request) {
        return new Result(getTcStandardData(reqData), "请求成功");
    }

    private Map getTcStandardData(ReqData<TcStandard> reqData) {
        List<TcStandard> tcStandardList = EntityUtils.getListByPage(tcStandardService, reqData);
        int tcStandardCount = EntityUtils.getListRecordByQuery(tcStandardService, reqData);
        List<Module> modList = getModuleList();
        //返回前端的数据
        for (int i = 0; i < tcStandardList.size(); i++) {
            for (int j = 0; j < modList.size(); j++) {
                if (tcStandardList.get(i).getModCode().equals(modList.get(j).getModCode())) {
                    tcStandardList.get(i).setModName(modList.get(j).getModName());
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("tcStandardList", tcStandardList);
        data.put("totalRecord", tcStandardCount);
        data.put("modList", modList);
        return data;
    }

    @DeleteMapping("/deleteTcStandard")
    public Result deleteTcStandard(@RequestBody ReqData<TcStandard> reqData, HttpServletRequest request) {
        List<String> deleteCode = reqData.getDeleteCode();
        QueryWrapper<TcStandard> standardQueryWrapper = new QueryWrapper<>();
        for (int i =0;i<deleteCode.size();i++){
            standardQueryWrapper.eq("stad_code", deleteCode.get(i)).or();
        }
        List<TcStandard> standardList = tcStandardService.list(standardQueryWrapper);
        for (int i =0;i<standardList.size();i++){
            QueryWrapper<Tc> tcQueryWrapper = new QueryWrapper<>();
            tcQueryWrapper.eq("mod_code",standardList.get(i).getModCode());
            List<Tc> tcList = tcService.list(tcQueryWrapper);
            //如果模块中还有用例就不允许更改
            if(tcList.size()>0){
                standardQueryWrapper.clear();
                standardQueryWrapper.eq("mod_code",standardList.get(i).getModCode());
                List<TcStandard> tcStandardList = tcStandardService.list(standardQueryWrapper);
                if(tcStandardList.size()<2){
                 for (int j = 0;j<deleteCode.size();j++){
                     if(standardList.get(i).getStadCode().equals(deleteCode.get(j))){
                         deleteCode.remove(j);
                         break;
                     }
                 }
                }
            }
        }
        if(deleteCode.size()<1){
            return new Result("0","删除失败，该模块中仍有用例，请保证至少有一个标准存在");
        }
        UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "stad_code");
        boolean delete = tcStandardService.update(null, updateWrapper);
        if (delete) {
             standardQueryWrapper = new QueryWrapper<>();
            for (int i = 0; i < reqData.getDeleteCode().size(); i++) {
                standardQueryWrapper.eq("stad_code", reqData.getDeleteCode().get(i)).or();
            }
            List<TcStandard> tcStandardList = tcStandardService.list(standardQueryWrapper);
            QueryWrapper<Analysis> analysisQueryWrapper = new QueryWrapper<>();
            for (int i = 0; i < tcStandardList.size(); i++) {
                analysisQueryWrapper.eq("mod_code", tcStandardList.get(i).getModCode()).or();
            }
            List<Analysis> analysisList = analysisService.list(analysisQueryWrapper);
            for (int i = 0; i < analysisList.size(); i++) {
                //用例对应的模块
                QueryWrapper<Module> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("mod_code", analysisList.get(i).getModCode());
                Module mod = moduleService.getOne(queryWrapper);
                //根据最相似的标题查找标准
                TcTextComparatorUtils.AnalysisSimilarComputed(analysisList.get(i), mod, tcStandardService);
                analysisService.updateById(analysisList.get(i));
            }
            //更新删除后的列表给前端
            return new Result(getTcStandardData(reqData), "删除成功");
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateTcStandard")
    public Result updateTcStandard(@RequestBody ReqData<TcStandard> reqData) {
        //标准更新后所有有用该标准的用例分析都要重新分析
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("stad_code", reqData.getStadCode());
        String modCode = (String) reqData.getUpdateObject().get("modCode");
        QueryWrapper<TcStandard> standardQueryWrapper = new QueryWrapper<>();
        standardQueryWrapper.eq("stad_code", reqData.getStadCode());
        TcStandard tcStandard = tcStandardService.getOne(standardQueryWrapper);
        Module mod;
        //如果是更改了该标准的模块
        if (!tcStandard.getModCode().equals(modCode)) {
            QueryWrapper<Tc> tcQueryWrapper = new QueryWrapper<>();
            tcQueryWrapper.eq("mod_code", tcStandard.getModCode());
            List<Tc> tcList = tcService.list(tcQueryWrapper);
            //如果模块中还有用例就不允许更改
            if (tcList.size() > 0) {
                standardQueryWrapper = new QueryWrapper<>();
                standardQueryWrapper.eq("mod_code", tcStandard.getModCode());
                List<TcStandard> tcStandardList = tcStandardService.list(standardQueryWrapper);
                if (tcStandardList.size() < 2) {
                    return new Result("0", "更新失败,该模块下仍有用例，最少要保存一个标准,无法移动到模块中");
                }
            }
        }
        boolean update = tcStandardService.update(null, updateWrapper);
        if (update) {
            standardQueryWrapper = new QueryWrapper<>();
            standardQueryWrapper.eq("stad_code", reqData.getStadCode());
            tcStandard = tcStandardService.getOne(standardQueryWrapper);
            QueryWrapper<Analysis> analysisQueryWrapper = new QueryWrapper<>();
            analysisQueryWrapper.eq("mod_code", reqData.getUpdateObject().get("modCode")).or();
            analysisQueryWrapper.eq("mod_code", tcStandard.getModCode()).or();
            List<Analysis> analysisList = analysisService.list(analysisQueryWrapper);
            for (int i = 0; i < analysisList.size(); i++) {
                //用例对应的模块
                QueryWrapper<Module> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("mod_code", analysisList.get(i).getModCode());
                mod = moduleService.getOne(queryWrapper);
                //根据最相似的标题查找标准
                TcTextComparatorUtils.AnalysisSimilarComputed(analysisList.get(i), mod, tcStandardService);
                update = analysisService.updateById(analysisList.get(i));
            }
            return new Result(getTcStandardData(reqData), "更新成功");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addTcStandard")
    public Result addTcStandard(@RequestBody ReqData<TcStandard> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        TcStandard stad = reqData.getInsertObject();
        //查询最新的那条数据的id
        int stadCount = tcStandardService.count() + 1;
        String stadCode = CodeRule.TcStandardRule + CodeUtils.getCode(stadCount, 5);
        stad.setStadCode(stadCode);
        //设置日期
        stad.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        stad.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        stad.setOperator(user.getUserCode());
        //是否启用
        stad.setIsDelete(Enabled);
        boolean save = tcStandardService.save(stad);
        if (save) {
            return new Result(getTcStandardData(reqData), "添加成功");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<TcStandard> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "标准标题(必填)");
            head.put(1, "标准描述(必填)");
            head.put(2, "前置条件(必填)");
            head.put(3, "预期结果(必填)");
            head.put(4, "输入(必填)");
            head.put(5, "步骤(必填)");
            head.put(6, "重要程度,必填(低，中，高)");
            head.put(7, "备注");
            EasyExcel.read(file.getInputStream(), TcStandard.class, new DefaultDataListener(tcStandardService, head,reqData,user))
                    .sheet()
                    .doRead();
            return new Result(getTcStandardData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<TcStandard> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<TcStandard> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("stad_code", code).or();
        }
        ExcelUtils.getExportExcel(response, tcStandardService, TcStandard.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, TcStandard.class);
    }

    private List<Module> getModuleList() {
        //设置用户的部门
        QueryWrapper<Module> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return moduleService.list(queryWrapper);
    }

}