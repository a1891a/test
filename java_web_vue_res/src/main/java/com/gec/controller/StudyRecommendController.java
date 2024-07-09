package com.gec.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gec.common.Common;
import com.gec.common.SessionString;
import com.gec.entity.Module;
import com.gec.entity.*;
import com.gec.entity.reqData.ReqData;
import com.gec.service.AnalysisService;
import com.gec.service.ModuleService;
import com.gec.service.ProjectService;
import com.gec.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.gec.enums.EnabledStatus.Enabled;

@RestController
@RequestMapping("/studyRecommend")
public class StudyRecommendController {
    @Autowired
    UserService userService;
    @Autowired
    ProjectService proService;
    @Autowired
    ModuleService modService;
    @Autowired
    AnalysisService analysisService;

    @RequestMapping("/getStudyData")
    public Result getStudyData(@RequestBody ReqData<User> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        String userName = (String) reqData.getQueryObject().get("user_name");
        QueryWrapper<Analysis> analysisQueryWrapper = new QueryWrapper<>();
        //老师和学生看到的是不一样的
        switch (user.getRole().getValue()) {
            //学生
            case 1:
                analysisQueryWrapper.eq("operator", user.getUserCode());
                //教师
                //管理员
                //超级管理员
            case 2:
            case 3:
            case 4:
                //没输入就是查自己的
                if (!userName.isEmpty() && !userName.equals("") && userName != null) {
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    userQueryWrapper.eq("user_name", userName);
                    userQueryWrapper.eq("is_delete", Enabled);
                    user = userService.getOne(userQueryWrapper);
                    //如果用户不存在
                    if (user != null) {
                        analysisQueryWrapper.eq("operator", user.getUserCode());
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put("studyResultData", null);
                        return new Result(data, "请求成功");
                    }
                } else {
                    analysisQueryWrapper.eq("operator", user.getUserCode());
                }
        }
        List<Analysis> analysisList = analysisService.list(analysisQueryWrapper);
        List<StudyData> studyResultData = new ArrayList<>();
        StudyData studyData;

        //计算项目数量
        studyData = computedProCount(analysisList);
        studyResultData.add(studyData);
        //计算模块数量
        studyData = computedModCount(analysisList);
        studyResultData.add(studyData);
        //计算用例数量
        studyData = computedTcCount(analysisList);
        studyResultData.add(studyData);
        //计算用例数量达标率
        studyData = computedTcCountAttainmentRate(analysisList);
        studyResultData.add(studyData);
        //计算用例平均分
        studyData = computedTcAverageScore(analysisList);
        studyResultData.add(studyData);
        //计算总分
        studyData = computedTcScore(analysisList);
        studyResultData.add(studyData);
        Map<String, Object> data = new HashMap<>();
        data.put("studyResultData", studyResultData);
        return new Result(data, "请求成功");
    }

    //实践项目数量
    private StudyData computedProCount(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        //查询实践的项目数量
        Map<Integer, String> projects = new HashMap<>();
        for (int i = 0; i < analysisList.size(); i++) {
            if (projects.size() > 0 && projects.containsValue(analysisList.get(i).getProCode())) {
                continue;
            }
            projects.put(projects.size(), analysisList.get(i).getProCode());
        }
        //查询评价
        String comments;
        if (projects.size() < 3) {
            comments = "偏少";
        } else if (projects.size() < 5) {
            comments = "合格";
        } else if (projects.size() < 7) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("实践项目数量");
        studyData.setValue(String.valueOf(projects.size()));
        studyData.setComments(comments);
        return studyData;
    }

    //实践模块数量
    private StudyData computedModCount(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        //查询实践的模块数量
        Map<Integer, String> modules = new HashMap<>();
        for (int i = 0; i < analysisList.size(); i++) {
            if (modules.size() > 0 && modules.containsValue(analysisList.get(i).getModCode())) {
                continue;
            }
            modules.put(modules.size(), analysisList.get(i).getProCode());
        }
        //查询评价
        String comments;
        if (modules.size() < 10) {
            comments = "偏少";
        } else if (modules.size() < 15) {
            comments = "合格";
        } else if (modules.size() < 20) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("实践模块数量");
        studyData.setValue(String.valueOf(modules.size()));
        studyData.setComments(comments);
        return studyData;
    }

    //实践提交测试用例总数
    private StudyData computedTcCount(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        //查询评价
        String comments;
        if (analysisList.size() < 60) {
            comments = "偏少";
        } else if (analysisList.size() < 70) {
            comments = "合格";
        } else if (analysisList.size() < 100) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("实践提交测试用例总数");
        studyData.setValue(String.valueOf(analysisList.size()));
        studyData.setComments(comments);
        return studyData;
    }

    //模块设计数量达标率
    private StudyData computedTcCountAttainmentRate(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        //查询模块
        QueryWrapper<Module> moduleQueryWrapper = new QueryWrapper<>();
        for (int i = 0; i < analysisList.size(); i++) {
            moduleQueryWrapper.eq("mod_code", analysisList.get(i).getModCode()).or();
        }
        List<Module> modList = modService.list(moduleQueryWrapper);
        double correctlyCount = 0;
        for (int i = 0; i < modList.size(); i++) {
            int analysisCount = 0;
            for (int j = 0; j < analysisList.size(); j++) {
                if (modList.get(i).getModCode().equals(analysisList.get(j).getModCode())) {
                    analysisCount++;
                }
            }
            if (analysisCount >= modList.get(i).getTcAmount()) {
                correctlyCount++;
            }
        }
        int tcCountAttainmentRate = (int) (correctlyCount / modList.size() * 100);
        //查询评价
        String comments;
        if (tcCountAttainmentRate < 60) {
            comments = "不合格";
        } else if (tcCountAttainmentRate < 70) {
            comments = "合格";
        } else if (tcCountAttainmentRate < 90) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("模块设计数量达标率");
        studyData.setValue(String.valueOf(tcCountAttainmentRate));
        studyData.setComments(comments);
        return studyData;
    }

    //标准用例设计综合平均得分(最高2分,1.2分合格)
    private StudyData computedTcAverageScore(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        double sum = 0;
        for (int i = 0; i < analysisList.size(); i++) {
            sum += Double.parseDouble(analysisList.get(i).getCompScore());
        }
        double tcAverageScore = sum / analysisList.size();
        if (analysisList.size() == 0) {
            tcAverageScore = 0;
        }
        //查询评价
        String comments;
        if (tcAverageScore < 1.2) {
            comments = "不合格";
        } else if (tcAverageScore < 1.6) {
            comments = "合格";
        } else if (tcAverageScore < 1.8) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("标准用例设计综合平均得分(最高2分,1.2分合格)");
        studyData.setValue(String.format("%.1f", tcAverageScore));
        studyData.setComments(comments);
        return studyData;
    }

    //实践综合得分(满分100,60分合格)
    private StudyData computedTcScore(List<Analysis> analysisList) {
        StudyData studyData = new StudyData();
        //查询模块
        QueryWrapper<Module> moduleQueryWrapper = new QueryWrapper<>();
        for (int i = 0; i < analysisList.size(); i++) {
            moduleQueryWrapper.eq("mod_code", analysisList.get(i).getModCode()).or();
        }
        List<Module> modList = modService.list(moduleQueryWrapper);
        double totalScore = 0;
        for (int i = 0; i < modList.size(); i++) {
            //计算每个模块的成绩
            int tcAmount = modList.get(i).getTcAmount();
            double modScore = 0;
            List<Double> analysisScore = new ArrayList<>();
            for (int j = 0; j < analysisList.size(); j++) {
                if (modList.get(i).getModCode().equals(analysisList.get(j).getModCode())) {
                    analysisScore.add(Double.parseDouble(analysisList.get(j).getCompScore()));
                }
            }
            if (analysisScore.size() >= tcAmount) {
                Collections.sort(analysisScore, new Comparator<Double>() {
                    @Override
                    public int compare(Double o1, Double o2) {
                        return o2.compareTo(o1);
                    }
                });
                for (int j = 0; j < tcAmount; j++) {
                    modScore += (analysisScore.get(j) * 100 / tcAmount);
                }
            } else {
                for (int j = 0; j < analysisScore.size(); j++) {
                    modScore += (analysisScore.get(j) * 100 / tcAmount);
                }
            }
            totalScore += modScore;

        }
        double TcScore = totalScore / modList.size();
        //查询评价
        String comments;
        if (TcScore < 60) {
            comments = "不合格";
        } else if (TcScore < 70) {
            comments = "合格";
        } else if (TcScore < 90) {
            comments = "良好";
        } else {
            comments = "优秀";
        }
        studyData.setName("实践综合得分(满分100,60分合格)");
        studyData.setValue(String.format("%.1f", TcScore));
        studyData.setComments(comments);
        return studyData;
    }
}
