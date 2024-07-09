package com.gec.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.common.SessionString;
import com.gec.entity.Dept;
import com.gec.entity.Emp;
import com.gec.entity.Result;
import com.gec.entity.User;
import com.gec.entity.reqData.ReqData;
import com.gec.excel.DataListener.DefaultDataListener;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.convert.RoleConvert;
import com.gec.service.DeptService;
import com.gec.service.EmpService;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import com.gec.utils.EntityUtils;
import com.gec.utils.ExcelUtils;
import com.mysql.cj.xdevapi.JsonString;
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
@RequestMapping("/dept")
public class DeptController {
    @Autowired
    private DeptService deptService;
    @Autowired
    private EmpService empService;

    @RequestMapping("/getDeptList")
    public Result getDeptList(@RequestHeader(value = "token") String token, @RequestBody ReqData<Dept> reqData) {
        return new Result(getDeptData(reqData), "请求成功");
    }

    @DeleteMapping("/deleteDept")
    public Result deleteDept(@RequestHeader(value = "token") String token, @RequestBody ReqData<Dept> reqData) {
        //部门下还有教师的话不允许删除
        List<Emp> empList = empService.list();
        List<String> deleteCode = reqData.getDeleteCode();
        for (int i = 0; i < empList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                if (empList.get(i).getDeptCode().equals(deleteCode.get(j))) {
                    reqData.getDeleteCode().remove(j);
                }
            }
        }
        if (reqData.getDeleteCode().size() > 0) {
            boolean delete = deptService.update(null, EntityUtils.getDeleteWrapper(reqData, "dept_code"));
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getDeptData(reqData), "删除成功");
            }
        } else {
            return new Result("0", "删除失败,该部门下还有教师未遣散");
        }
        return new Result("0", "删除失败");

    }

    @RequestMapping("/updateDept")
    public Result updateEmp(@RequestBody ReqData<Dept> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("dept_code", reqData.getDeptCode());
        boolean update = deptService.update(null, updateWrapper);
        if (update) {
            return new Result(getDeptData(reqData), "更新成功");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addDept")
    public Result addDept(@RequestBody ReqData<Dept> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Dept dept = reqData.getInsertObject();
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dept_name", dept.getDeptName());
        Dept one = deptService.getOne(queryWrapper);
        if (one != null) {
            return new Result("0", "添加失败,部门名称不能一致");
        }
        //查询最新的那条数据的id
        int depCount = deptService.count() + 1;
        String deptCode = CodeRule.DeptCodeRule + CodeUtils.getCode(depCount, 5);
        dept.setDeptCode(deptCode);
        //设置日期
        dept.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        dept.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        dept.setOperator(user.getUserCode());
        //设置上级部门
        dept.setParentCode("0");
        //是否启用
        dept.setIsDelete(Enabled);
        boolean save = deptService.save(dept);
        if (save) {
            return new Result(getDeptData(reqData), "添加成功");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Dept> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "部门名称(必填)");
            head.put(1, "地址(必填)");
            head.put(2, "部门主任");
            EasyExcel.read(file.getInputStream(), Dept.class, new DefaultDataListener(deptService, head,user))
                    .registerConverter(new GenderConvert())
                    .registerConverter(new RoleConvert())
                    .sheet()
                    .doRead();
            return new Result(getDeptData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Dept> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Dept> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("dept_code", code).or();
        }
        ExcelUtils.getExportExcel(response, deptService, Dept.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Dept.class);
    }

    private Map getDeptData(ReqData<Dept> reqData) {
        List<Dept> deptList = EntityUtils.getListByPage(deptService, reqData);
        int deptRecord = EntityUtils.getListRecordByQuery(deptService, reqData);
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("deptList", deptList);
        data.put("totalRecord", deptRecord);
        return data;
    }

}
