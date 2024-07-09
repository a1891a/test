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
import com.gec.entity.reqData.ReqData;
import com.gec.enums.EnabledStatus;
import com.gec.excel.DataListener.DefaultDataListener;
import com.gec.excel.DataListener.UserDataListener;
import com.gec.service.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

@RestController
@RequestMapping("/clazz")
public class ClazzController {
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private StuService stuService;

    @RequestMapping("/getClazzList")
    public Result getClazzList(@RequestBody ReqData<Clazz> reqData) {
        return new Result(getClazzData(reqData), "请求成功");
    }

    @DeleteMapping("/deleteClazz")
    public Result deleteClazz(@RequestBody ReqData<Clazz> reqData) {
        //班级下还有学生的话不允许删除
        List<Stu> stuList = stuService.list();
        List<String> deleteCode = reqData.getDeleteCode();
        for (int i = 0; i < stuList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                if (stuList.get(i).getClazzCode().equals(deleteCode.get(j))) {
                    reqData.getDeleteCode().remove(j);
                }
            }
        }
        if (reqData.getDeleteCode().size() > 0) {
            UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "clazz_code");
            boolean delete = clazzService.update(null, updateWrapper);
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getClazzData(reqData), "删除成功");
            }
        } else {
            return new Result("0", "删除失败,该班级下还有学生未移除");
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateClazz")
    public Result updateClazz(@RequestBody ReqData<Clazz> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("clazz_code", reqData.getClazzCode());
        boolean update = clazzService.update(null, updateWrapper);
        if (update) {
            return new Result(getClazzData(reqData), "更新成功");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addClazz")
    public Result addClazz(@RequestBody ReqData<Clazz> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Clazz clazz = reqData.getInsertObject();
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("clazz_name", clazz.getClazzName());
        Clazz one = clazzService.getOne(queryWrapper);
        if (one != null) {
            return new Result("0", "添加失败,部门名称不能一致");
        }
        //查询最新的那条数据的id
        int clazzCount = clazzService.count() + 1;
        String clazzCode = CodeRule.ClazzCodeRule + CodeUtils.getCode(clazzCount, 5);
        clazz.setClazzCode(clazzCode);
        //设置日期
        clazz.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        clazz.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        clazz.setOperator(user.getUserCode());
        //是否启用
        clazz.setIsDelete(Enabled);
        boolean save = clazzService.save(clazz);
        if (save) {
            return new Result(getClazzData(reqData), "添加成功");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Clazz> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "班级名称(必填)");
            head.put(1, "专业名称(必填)");
            head.put(2, "备注");
            EasyExcel.read(file.getInputStream(), Clazz.class, new DefaultDataListener(clazzService, head,user))
                    .sheet()
                    .doRead();
            return new Result(getClazzData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Clazz> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("clazz_code", code).or();
        }
        ExcelUtils.getExportExcel(response, clazzService, Clazz.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Clazz.class);
    }

    private Map getClazzData(ReqData<Clazz> reqData) {
        List<Clazz> clazzList = EntityUtils.getListByPage(clazzService, reqData);
        int clazzRecord = EntityUtils.getListRecordByQuery(clazzService, reqData);
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("clazzList", clazzList);
        data.put("totalRecord", clazzRecord);
        return data;
    }

}
