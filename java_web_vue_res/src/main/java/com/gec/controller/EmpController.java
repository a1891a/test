package com.gec.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.SessionString;
import com.gec.entity.*;
import com.gec.entity.reqData.ReqData;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Role;
import com.gec.excel.DataListener.DefaultDataListener;
import com.gec.excel.DataListener.UserDataListener;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.convert.RoleConvert;
import com.gec.service.*;
import com.gec.enums.Gender;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import com.gec.utils.EntityUtils;
import com.gec.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/emp")
public class EmpController {
    @Autowired
    private UserService userService;
    @Autowired
    private EmpService empService;
    @Autowired
    private StuService stuService;
    @Autowired
    private MenuService menuService;
    @Autowired
    private DeptService deptService;

    @RequestMapping("/getEmpList")
    public Result getEmpList(@RequestHeader(value = "token") String token, @RequestBody ReqData<Emp> reqData) {
        return new Result(getEmpData(reqData), "获取成功");
    }

    @DeleteMapping("/deleteEmp")
    public Result deleteEmp(@RequestBody ReqData<Emp> reqData, HttpServletRequest request) {
        boolean delete = empService.update(null, EntityUtils.getDeleteWrapper(reqData, "emp_code"));
        if (delete) {
            reqData.setDeleteCode(EntityUtils.getUserDeleteCode(reqData, userService));
            delete = userService.update(null, EntityUtils.getDeleteWrapper(reqData, "user_code"));
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getEmpData(reqData), "删除成功");
            }
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateEmp")
    public Result updateEmp(@RequestBody ReqData<Emp> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //教师只能编辑自己
        if (user.getRole().getValue() == 2) {
            QueryWrapper<Emp> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("emp_code", user.getTargetCode());
            Emp emp = empService.getOne(queryWrapper);
            if (reqData.getEmpCode().equals(emp.getEmpCode())) {
                //如果更改了部门就不允许更改
                System.out.println(reqData);
                if (emp.getDeptCode().equals(reqData.getUpdateObject().get("deptCode"))) {
                    boolean update = empUpdate(reqData);
                    if (update) {
                        //同步更改用户表
                        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                        userUpdateWrapper.eq("target_code", emp.getEmpCode());
                        userUpdateWrapper.set("user_name", emp.getEmpName());
                        userUpdateWrapper.set("gender", emp.getGender());
                        update = userService.update(userUpdateWrapper);
                        if (update) {
                            return new Result(getEmpData(reqData), "更新成功");
                        }
                    }
                } else {
                    return new Result("0", "你没有权限更改自己的部门");
                }
            } else {
                return new Result("0", "你只能编辑你自己的信息");
            }
        } else {
            boolean update = empUpdate(reqData);
            if (update) {
                return new Result(getEmpData(reqData), "更新成功");
            }
        }
        return new Result("0", "更新失败");
    }


    public boolean empUpdate(ReqData<Emp> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData("yyMMdd"));
        updateWrapper.eq("emp_code", reqData.getEmpCode());
        return empService.update(null, updateWrapper);
    }

    @RequestMapping("/addEmp")
    public Result addEmp(@RequestBody ReqData<Emp> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Emp emp = reqData.getInsertObject();
        //查询最新的那条数据的id
        int empCount = empService.count() + 1;
        String empCode = CodeRule.EmpCodeRule + CodeUtils.getCode(empCount, 5);
        emp.setEmpCode(empCode);
        //设置日期
        emp.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        emp.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //权限
        emp.setOperator(user.getUserCode());
        //是否启用
        emp.setIsDelete(Enabled);
        boolean save = empService.save(emp);
        if (save) {
            int userCount = userService.count() + 1;
            String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(userCount, 5);
            user = new User();
            user.setUserCode(userCode);
            user.setTargetCode(emp.getEmpCode());
            user.setUserName("用户" + userCount);
            user.setGender(emp.getGender());
            user.setNotes(emp.getNotes());
            user.setRole(Role.Teacher);
            //设置默认密码
            user.setPwd(Common.DefaultUserPwd);
            user.setIsDelete(Enabled);
            save = userService.save(user);
            if (save) {
                return new Result(getEmpData(reqData), "添加成功");
            }
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Emp> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "姓名(必填)");
            head.put(1, "性别,必填(男，女)");
            head.put(2, "备注");
            EasyExcel.read(file.getInputStream(), Emp.class, new UserDataListener(empService, userService, empService, stuService, head,user))
                    .sheet()
                    .doRead();
            return new Result(getEmpData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Emp> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Emp> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("emp_code", code).or();
        }
        ExcelUtils.getExportExcel(response, empService, Emp.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Emp.class);
    }

    private List<Dept> getDept() {
        //设置用户的部门
        QueryWrapper<Dept> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return deptService.list(queryWrapper);
    }


    private Map getEmpData(ReqData<Emp> reqData) {
        Map<String, Object> data = new HashMap<>();
        System.out.println("——————————————————————————");
        System.out.println(reqData);
        List<Emp> empList = EntityUtils.getListByPage(empService, reqData);
        int empRecord = EntityUtils.getListRecordByQuery(empService, reqData);
        List<Dept> deptList = getDept();
        System.out.println(empList);
        System.out.println(deptList);
        for (int i = 0; i < empList.size(); i++) {
            for (int j = 0; j < deptList.size(); j++) {
                if (empList.get(i).getDeptCode().equals(deptList.get(j).getDeptCode())) {
                    empList.get(i).setDeptName(deptList.get(j).getDeptName());
                }
            }
        }
        //返回前端的数据
        data.put("empList", empList);
        data.put("totalRecord", empRecord);
        data.put("deptList", deptList);
        return data;
    }


}
