package com.gec.excel.DataListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.entity.*;
import com.gec.enums.Role;
import com.gec.excel.valid.ExcelImportValid;
import com.gec.service.EmpService;
import com.gec.service.StuService;
import com.gec.service.UserService;
import com.gec.utils.CodeUtils;
import com.gec.utils.DateUtils;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Enabled;

public class UserDataListener<T> extends AnalysisEventListener<T> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;

    //传入正确的表头进行校验
    private Map<Integer, String> head;
    //表格内主要的名字不能相同
    private Map<Integer, String> names = new HashMap<>();
    //要调用的service
    private IService<T> service;
    //工具service
    private UserService userService;
    private EmpService empService;
    private StuService stuService;
    List<T> entityList = new ArrayList();
    //当前操作的用户
    private User sessionUser;
    public UserDataListener(IService<T> service, UserService userService, EmpService empService, StuService stuService, Map<Integer, String> head,User sessionUser) {
        this.service = service;
        this.userService = userService;
        this.empService = empService;
        this.stuService = stuService;
        this.head = head;
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
    public void invoke(T t, AnalysisContext analysisContext) {
        //数据校验
        try {
            ExcelImportValid.valid(t);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        T entity = convertEntity(t);
        if (entity != null) {
            entityList.add(entity);
        }
    }

    private T convertEntity(T t) {
        int codeCount = service.count() + 1 + entityList.size();
        if (t.getClass() == User.class) {
            User user = (User) t;
            //表格内名字相同的直接只处理第一个
            if (names.size() > 0 && names.containsValue(user.getUserName())) {
                return null;
            }
            //用户名不能一致不然不添加
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_name", user.getUserName());
            User one = userService.getOne(queryWrapper);
            if (one != null) {
                return null;
            }
            names.put(entityList.size(), user.getUserName());
            //无法添加比自己权限大的用户
            if (sessionUser.getRole().getValue() < user.getRole().getValue()) {
                return null;
            }
            //是否添加成功
            boolean save = false;
            //查询最新的那条数据的id
            String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(codeCount, 5);
            user.setUserCode(userCode);
            //设置默认密码
            user.setPwd(Common.DefaultUserPwd);
            user.setIsDelete(Enabled);
            user.setTargetCode("0");
            switch (user.getRole()) {
                //学生
                case Student:
                    Stu stu = new Stu();
                    int stuCount = stuService.count() + 1;
                    String stuCode = CodeRule.StudentCodeRule + CodeUtils.getCode(stuCount, 5);
                    user.setTargetCode(stuCode);
                    stu.setStuCode(stuCode);
                    stu.setStuName(user.getUserName());
                    stu.setClazzCode("null");                    //待修改
                    stu.setGender(user.getGender());
                    stu.setPhone("null");
                    stu.setAddress("null");
                    stu.setNotes(user.getNotes());
                    stu.setOperator(sessionUser.getTargetCode());
                    stu.setCreateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                    stu.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                    stu.setIsDelete(Enabled);
                    save = stuService.save(stu);
                    break;
                //教师
                case Teacher:
                    Emp emp = new Emp();
                    int empCount = empService.count() + 1;
                    String empCode = CodeRule.EmpCodeRule + CodeUtils.getCode(empCount, 5);
                    user.setTargetCode(empCode);
                    emp.setEmpCode(empCode);
                    emp.setEmpName(user.getUserName());
                    emp.setDeptCode("null");
                    emp.setGender(user.getGender());
                    emp.setAddress("null");
                    emp.setNotes(user.getNotes());
                    emp.setOperator(sessionUser.getTargetCode());
                    emp.setCreateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                    emp.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                    emp.setIsDelete(Enabled);
                    save = empService.save(emp);
                    break;
                //管理员
                case Admin:
                    save = true;
                    break;
            }
            if (save == false) {
                return null;
            }
            return (T) user;
        } else if (t.getClass() == Emp.class) {
            Emp emp = (Emp) t;
            names.put(entityList.size(), emp.getEmpName());
            //无法添加比自己权限大的用户
            if (sessionUser.getRole().getValue() < Role.Teacher.getValue()) {
                return null;
            }
            //是否添加成功
            boolean save = false;
            //查询最新的那条数据的id
            String empCode = CodeRule.EmpCodeRule + CodeUtils.getCode(codeCount, 5);
            emp.setEmpCode(empCode);
            emp.setDeptCode("null");
            //设置日期
            emp.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            emp.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //权限
            emp.setOperator(sessionUser.getUserCode());
            //是否启用
            emp.setIsDelete(Enabled);
            int userCount = userService.count() + 1;
            String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(userCount, 5);
            User user = new User();
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
            if (save == false) {
                return null;
            }
            return (T) emp;
        } else if (t.getClass() == Stu.class) {
            Stu stu = (Stu) t;
            names.put(entityList.size(), stu.getStuName());
            //无法添加比自己权限大的用户
            if (sessionUser.getRole().getValue() < Role.Teacher.getValue()) {
                return null;
            }
            //是否添加成功
            boolean save = false;
            String stuCode = CodeRule.StudentCodeRule + CodeUtils.getCode(codeCount, 5);
            stu.setStuCode(stuCode);
            //设置日期
            stu.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
            stu.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
            //设置操作员
            stu.setOperator(sessionUser.getUserCode());
            //是否启用
            stu.setIsDelete(Enabled);
            int userCount = userService.count() + 1;
            String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(userCount, 5);
            User user = new User();
            user.setUserCode(userCode);
            user.setTargetCode(stu.getStuCode());
            user.setUserName("用户"+userCount);
            user.setGender(stu.getGender());
            user.setNotes(stu.getNotes());
            user.setRole(Role.Student);
            //设置默认密码
            user.setPwd(Common.DefaultUserPwd);
            user.setIsDelete(Enabled);
            save = userService.save(user);
            if (save ==false) {
                return null;
            }
            return (T) stu;
        }
        return null;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
    }

    private void saveData() {
        service.saveBatch(entityList);
    }
}
