package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.common.Common;
import com.gec.common.SessionString;
import com.gec.entity.Module;
import com.gec.entity.*;
import com.gec.entity.reqData.ReqData;
import com.gec.service.*;
import com.gec.utils.EntityUtils;
import com.gec.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.ApprovalStatus.Approval;
import static com.gec.enums.EnabledStatus.Enabled;

@RestController
@RequestMapping("/analysisResult")
public class AnalysisResultController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmpService empService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private TcStandardService tcStandardService;
    @Autowired
    private AnalysisService analysisService;

    @RequestMapping("/getAnalysisResultList")
    public Result getAnalysisResultList(@RequestBody ReqData<Analysis> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //如果用户名称对应很多个用户
        if (reqData.getQueryObject().get("operator") != "" && reqData.getQueryObject().get("operator") != null) {
            QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.like("user_name", reqData.getQueryObject().get("operator"));
            List<User> userList = userService.list(userQueryWrapper);
            if(userList.size()>0){
                List<String> userCode = new ArrayList();
                for (int i = 0; i < userList.size(); i++) {
                    userCode.add(userList.get(i).getUserCode());
                }
                reqData.getQueryObject().put("operator", userCode);
            }

        }
        //老师和学生看到的是不一样的
        switch (user.getRole().getValue()) {
            //学生
            case 1:
                return new Result(getStuAnalysisResultData(reqData,user), "请求成功");
            //教师
            //管理员
            //超级管理员
            case 2:
            case 3:
            case 4:
                return new Result(getAnalysisResultData(reqData), "请求成功");
        }
        return new Result("0", "请求失败");
    }

    //学生查看的
    private Map getStuAnalysisResultData(ReqData<Analysis> reqData,User user) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("operator", user.getUserCode());
        keyMap.put("is_approval", Approval);
        List<Analysis> analysisList = EntityUtils.getListByPage(analysisService, reqData, keyMap);
        int analysisCount = EntityUtils.getListRecordByQuery(analysisService, reqData, keyMap);
        List<User> userList = getUserList(analysisList);
        List<Project> projectList = getProjectList();
        List<Module> modList = getModuleList();
        Map<String, Object> data = getData(analysisList, analysisCount, userList, projectList, modList);
        return data;
    }

    //教师查看的
    private Map getAnalysisResultData(ReqData<Analysis> reqData) {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("is_approval", Approval);
        List<Analysis> analysisList = EntityUtils.getListByPage(analysisService, reqData, keyMap);
        int analysisCount = EntityUtils.getListRecordByQuery(analysisService, reqData, keyMap);
        List<User> userList = getUserList(analysisList);
        List<Project> projectList = getProjectList();
        List<Module> modList = getModuleList();
        Map<String, Object> data = getData(analysisList, analysisCount, userList, projectList, modList);
        return data;
    }

    private Map<String, Object> getData(List<Analysis> analysisList, Integer analysisCount, List<User> userList, List<Project> projectList, List<Module> modList) {
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
        for (int i = 0; i < analysisList.size(); i++) {
            for (int j = 0; j < modList.size(); j++) {
                if (analysisList.get(i).getModCode().equals(modList.get(j).getModCode())) {
                    analysisList.get(i).setModName(modList.get(j).getModName());
                }
            }
        }
        //设置提交人名称
        for (int i = 0; i < analysisList.size(); i++) {
            for (int j = 0; j < userList.size(); j++) {
                if (analysisList.get(i).getOperator().equals(userList.get(j).getUserCode())) {
                    analysisList.get(i).setOperatorName(userList.get(j).getUserName());
                }
            }
        }
        List<TcStandard> tcStandardList = getTcStandardList(analysisList);
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("analysisList", analysisList);
        data.put("totalRecord", analysisCount);
        data.put("projectList", projectList);
        data.put("modList", modList);
        data.put("tcStandardList", tcStandardList);
        return data;
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Module> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Analysis> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("tc_code", code).or();
        }
        ExcelUtils.getAnalysisExportExcel(response, analysisService, userService, queryWrapper);
    }

    private List<User> getUserList(List<Analysis> analyses) {
        //获取提交用户
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        for (int i = 0; i < analyses.size(); i++) {
            queryWrapper.eq("user_code", analyses.get(i).getOperator()).or();
        }
        return userService.list(queryWrapper);
    }

    private List<Project> getProjectList() {
        //获取所属项目
        QueryWrapper<Project> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return projectService.list(queryWrapper);
    }

    private List<Module> getModuleList() {
        //获取所属模型
        QueryWrapper<Module> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return moduleService.list(queryWrapper);
    }

    private List<TcStandard> getTcStandardList(List<Analysis> analyses) {
        //查询标准
        QueryWrapper<TcStandard> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        for (int i = 0; i < analyses.size(); i++) {
            queryWrapper.eq("stad_code", analyses.get(i).getStadCode()).or();
        }
        return tcStandardService.list(queryWrapper);
    }
}
