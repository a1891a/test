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
import com.gec.service.ModuleService;
import com.gec.service.ProjectService;
import com.gec.service.TcService;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import com.gec.utils.EntityUtils;
import com.gec.utils.ExcelUtils;
import org.apache.poi.ss.formula.functions.T;
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
@RequestMapping("/module")
public class ModuleController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private TcService tcService;

    @RequestMapping("/getModuleList")
    public Result getModuleList(@RequestBody ReqData<Module> reqData) {
        return new Result(getModuleData(reqData), "请求成功");
    }

    private Map getModuleData(ReqData<Module> reqData) {
        List<Module> moduleList = EntityUtils.getListByPage(moduleService, reqData);
        int modelRecord = EntityUtils.getListRecordByQuery(moduleService, reqData);
        List<Project> proList = getProject();
        for (int i = 0; i < moduleList.size(); i++) {
            for (int j = 0; j < proList.size(); j++) {
                if (moduleList.get(i).getProCode().equals(proList.get(j).getProCode())) {
                    moduleList.get(i).setProName(proList.get(j).getProName());
                }
            }
        }
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("modList", moduleList);
        data.put("totalRecord", modelRecord);
        data.put("proList", proList);
        return data;
    }

    @DeleteMapping("/deleteModule")
    public Result deleteModule(@RequestBody ReqData<Module> reqData) {
        //如果这个模块下有用例了就不允许删除
        UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "mod_code");
        List<Tc> tcList = tcService.list();
        List<String> deleteCode = reqData.getDeleteCode();
        for (int i = 0; i < tcList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                if (tcList.get(i).getModCode().equals(deleteCode.get(j))) {
                    reqData.getDeleteCode().remove(j);
                }
            }
        }
        if (deleteCode.size() > 0) {
            boolean delete = moduleService.update(null, updateWrapper);
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getModuleData(reqData), "删除成功");
            }
        } else {
            return new Result("0", "删除失败,该模块下已存在用例");
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateModule")
    public Result updateModule(@RequestBody ReqData<Module> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("mod_code", reqData.getModCode());
        boolean update = moduleService.update(null, updateWrapper);
        if (update) {
            return new Result(getModuleData(reqData), "更新成功");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addModule")
    public Result addModule(@RequestBody ReqData<Module> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Module module = reqData.getInsertObject();
        //项目名不能一致
        QueryWrapper<Module> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mod_name", module.getModName());
        Module one = moduleService.getOne(queryWrapper);
        if (one != null) {
            return new Result("0", "添加失败，该项目已存在");
        }
        //查询最新的那条数据的id
        int modCount = moduleService.count() + 1;
        String modCode = CodeRule.ModuleCodeRule + CodeUtils.getCode(modCount, 5);
        module.setModCode(modCode);
        //设置日期
        module.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        module.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        module.setOperator(user.getUserCode());
        //是否启用
        module.setIsDelete(Enabled);
        boolean save = moduleService.save(module);
        if (save) {
            return new Result(getModuleData(reqData), "添加成功");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Module> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "模块名称(必填)");
            head.put(1, "模块描述(必填)");
            head.put(2, "用例总数(必填)");
            head.put(3, "是否审核,必填(审核，不审核)");
            head.put(4, "模块备注");
            EasyExcel.read(file.getInputStream(), Module.class, new DefaultDataListener(moduleService, head, reqData,user))
                    .sheet()
                    .doRead();
            return new Result(getModuleData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Module> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Module> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("mod_code", code).or();
        }
        ExcelUtils.getExportExcel(response, moduleService, Module.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Module.class);
    }

    private List<Project> getProject() {
        //设置用户的部门
        QueryWrapper<Project> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return projectService.list(queryWrapper);
    }
}