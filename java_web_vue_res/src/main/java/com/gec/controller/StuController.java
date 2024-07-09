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
import com.gec.enums.Role;
import com.gec.excel.DataListener.UserDataListener;
import com.gec.service.ClazzService;
import com.gec.service.EmpService;
import com.gec.service.StuService;
import com.gec.service.UserService;
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
@RequestMapping("/student")
public class StuController {
    @Autowired
    private UserService userService;
    @Autowired
    private StuService stuService;
    @Autowired
    private ClazzService clazzService;
    @Autowired
    private EmpService empService;

    @RequestMapping("/getStudentList")
    public Result getStudentList(@RequestHeader(value = "token") String token, @RequestBody ReqData<Stu> reqData) {
        return new Result(getStudentData(reqData), "请求成功");
    }

    @DeleteMapping("/deleteStudent")
    public Result deleteStudent(@RequestBody ReqData<Stu> reqData, HttpServletRequest request) {
        UpdateWrapper updateWrapper = EntityUtils.getDeleteWrapper(reqData, "stu_code");
        boolean delete = stuService.update(null, updateWrapper);
        if (delete) {
            reqData.setDeleteCode(EntityUtils.getUserDeleteCode(reqData, userService));
            delete = userService.update(null, EntityUtils.getDeleteWrapper(reqData, "user_code"));
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getStudentData(reqData), "删除成功");
            }
        }
        return new Result("0", "删除失败");
    }

    @RequestMapping("/updateStudent")
    public Result updateStudent(@RequestBody ReqData<Stu> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //学生只能编辑自己
        if (user.getRole().getValue() == 1) {
            QueryWrapper<Stu> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("stu_code", user.getTargetCode());
            Stu stu = stuService.getOne(queryWrapper);
            if (reqData.getStuCode().equals(stu.getStuCode())) {
                //学生没有更改自己班级的权限
                if (stu.getClazzCode().equals(reqData.getUpdateObject().get("clazzCode"))) {
                    boolean update = studentUpdate(reqData);
                    if (update) {
                        //同步更改用户表
                        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                        userUpdateWrapper.eq("target_code", stu.getStuCode());
                        userUpdateWrapper.set("user_name", stu.getStuName());
                        userUpdateWrapper.set("gender", stu.getGender());
                        update = userService.update(userUpdateWrapper);
                        if (update) {
                            return new Result(getStudentData(reqData), "更新成功");
                        }
                    }
                } else {
                    return new Result("0", "你没有更改自己班级的权限");
                }
            } else {
                return new Result("0", "你只能编辑你自己的信息");
            }
        } else {
            boolean update = studentUpdate(reqData);
            if (update) {
                return new Result(getStudentData(reqData), "更新成功");
            }
        }
        return new Result("0", "更新失败");
    }

    public boolean studentUpdate(ReqData<Stu> reqData) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
        updateWrapper.eq("stu_code", reqData.getStuCode());
        return stuService.update(null, updateWrapper);
    }

    @RequestMapping("/addStudent")
    public Result addStudent(@RequestHeader(value = "token") String token, HttpServletRequest request, @RequestBody ReqData<Stu> reqData) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        Stu stu = reqData.getInsertObject();
        //查询最新的那条数据的id
        int stuCount = stuService.count() + 1;
        String stuCode = CodeRule.StudentCodeRule + CodeUtils.getCode(stuCount, 5);
        stu.setStuCode(stuCode);
        //设置日期
        stu.setCreateDate(DateUtils.getCurrentData("yyMMdd"));
        stu.setUpdateDate(DateUtils.getCurrentData("yyMMdd"));
        //设置操作员
        stu.setOperator(user.getUserCode());
        //是否启用
        stu.setIsDelete(Enabled);
        boolean save = stuService.save(stu);
        if (save) {
            int userCount = userService.count() + 1;
            String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(userCount, 5);
            user = new User();
            user.setUserCode(userCode);
            user.setTargetCode(stu.getStuCode());
            user.setUserName("用户" + userCount);
            user.setGender(stu.getGender());
            user.setNotes(stu.getNotes());
            user.setRole(Role.Student);
            //设置默认密码
            user.setPwd(Common.DefaultUserPwd);
            user.setIsDelete(Enabled);
            save = userService.save(user);
            if (save) {
                return new Result(getStudentData(reqData), "添加成功");
            }
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<Stu> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "姓名(必填)");
            head.put(1, "性别,必填(男，女)");
            head.put(2, "手机号");
            head.put(3, "地址(必填)");
            head.put(4, "备注");
            EasyExcel.read(file.getInputStream(), Stu.class, new UserDataListener(stuService, userService, empService, stuService, head,user))
                    .sheet()
                    .doRead();
            return new Result(getStudentData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<Stu> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<Stu> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("stu_code", code).or();
        }
        ExcelUtils.getExportExcel(response, stuService, Stu.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, Stu.class);
    }

    private List<Clazz> getClazz() {
        //设置学生的班级
        QueryWrapper<Clazz> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_delete", Enabled);
        return clazzService.list(queryWrapper);
    }

    private Map getStudentData(ReqData<Stu> reqData) {
        List<Stu> stuList = EntityUtils.getListByPage(stuService, reqData);
        int studentRecord = EntityUtils.getListRecordByQuery(stuService, reqData);
        List<Clazz> clazzList = getClazz();
        //找班级
        for (int i = 0; i < stuList.size(); i++) {
            for (int j = 0; j < clazzList.size(); j++) {
                if (stuList.get(i).getClazzCode().equals(clazzList.get(j).getClazzCode())) {
                    stuList.get(i).setClazzName(clazzList.get(j).getClazzName());
                }
            }
        }
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("studentList", stuList);
        data.put("totalRecord", studentRecord);
        data.put("clazzList", clazzList);
        return data;
    }
}
