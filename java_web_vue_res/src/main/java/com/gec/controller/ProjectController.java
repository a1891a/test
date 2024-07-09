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
import com.gec.enums.EnabledStatus;
import com.gec.excel.DataListener.DefaultDataListener;
import com.gec.service.ModuleService;
import com.gec.service.ProjectService;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import com.gec.utils.EntityUtils;
import com.gec.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

@RestController
@RequestMapping("/project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;

    @RequestMapping("/getProjectList")
    public Result getProjectList(@RequestBody ReqData<Project> reqData) {
        return new Result(getProjectData(reqData), "请求成功");
    }

    private Map getProjectData(ReqData<Project> reqData) {
        List<Project> projectList = EntityUtils.getListByPage(projectService, reqData);
        int projectRecord = EntityUtils.getListRecordByQuery(projectService, reqData);
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("proList", projectList);
        data.put("totalRecord", projectRecord);
        return data;
    }

    @DeleteMapping("/deleteProject")
    public Result deleteProject(@RequestBody ReqData<Project> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "pro_code");
        List<Module> modList = moduleService.list();
        List<String> deleteCode = reqData.getDeleteCode();
        for (int i = 0; i < modList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                if (modList.get(i).getProCode().equals(deleteCode.get(j))) {
                    reqData.getDeleteCode().remove(j);
                }
            }
        }
        if (deleteCode.size() > 0) {
            boolean delete = projectService.update(null, updateWrapper);
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getProjectData(reqData), "删除成功");
            }
        } else {
            return new Result("0", "删除失败,该项目下还有模块未删除");
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateProject")
    public Result updateProject(@RequestBody ReqData<Project> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("pro_code", reqData.getProCode());
        boolean update = projectService.update(null, updateWrapper);
        if (update) {
            return new Result(getProjectData(reqData), "更新成功");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addProject")
    public Result addProject(@RequestBody ReqData<Project> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Project project = reqData.getInsertObject();
        //项目名不能一致
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pro_name", reqData.getInsertObject().getProName());
        Project one = projectService.getOne(queryWrapper);
        if (one != null) {
            return new Result("0", "添加失败，该项目已存在");
        }
        //查询最新的那条数据的id
        int proCount = projectService.count() + 1;
        String proCode = CodeRule.ProjectCodeRule + CodeUtils.getCode(proCount, 5);
        project.setProCode(proCode);
        //设置日期
        project.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        project.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        project.setOperator(user.getUserCode());
        //是否启用
        project.setIsDelete(Enabled);
        boolean save = projectService.save(project);
        if (save) {
            return new Result(getProjectData(reqData), "添加成功");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Project> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "项目名称(必填)");
            head.put(1, "项目描述(必填)");
            head.put(2, "项目经理");
            head.put(3, "备注");
            EasyExcel.read(file.getInputStream(), Project.class, new DefaultDataListener(projectService, head,user))
                    .sheet()
                    .doRead();
            return new Result(getProjectData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Project> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Project> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("pro_code", code).or();
        }
        ExcelUtils.getExportExcel(response, projectService, Project.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Project.class);
    }

}
