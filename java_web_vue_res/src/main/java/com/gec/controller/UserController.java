package com.gec.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.common.CodeRule;
import com.gec.common.Common;
import com.gec.common.DateString;
import com.gec.common.SessionString;
import com.gec.entity.*;
import com.gec.entity.reqData.ReqData;
import com.gec.enums.Gender;
import com.gec.enums.Role;
import com.gec.excel.DataListener.UserDataListener;
import com.gec.service.*;
import com.gec.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gec.enums.EnabledStatus.Disabled;
import static com.gec.enums.EnabledStatus.Enabled;
import static com.gec.enums.Role.SuperAdmin;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmpService empService;

    @Autowired
    private StuService stuService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRoleMenuRelationService userRoleMenuRelationService;

    @Autowired
    private UserRolePermissionRelationService userRolePermissionRelationService;

    @RequestMapping("/login")
    public Result login(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        String userCode = String.valueOf(map.get("account"));
        String userPwd = String.valueOf(map.get("password"));
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_code", userCode);
        queryWrapper.eq("pwd", userPwd);
        User user = userService.getOne(queryWrapper);
        //查询用户是否存在
        if (user != null) {
            //查询用户是否启用
            if (user.getIsDelete().equals(Enabled)) {
                //更新用户token
                user = updateUserToken(user);
                //设置session
                setUserSession(user, request);
                //将session中要传输的数据放入data中
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("user", request.getSession().getAttribute(SessionString.USER));
                data.put("menu", request.getSession().getAttribute(SessionString.USER_MENU));
                return new Result(data);
            } else {
                return new Result("1", "用户无法使用，请联系管理员");
            }
        }
        return new Result("0", "用户名或密码错误");
    }

    @RequestMapping("/logout")
    public Result logout(HttpServletRequest request) {
        request.getSession().removeAttribute(SessionString.USER);
        request.getSession().removeAttribute(SessionString.USER_MENU);
        request.getSession().removeAttribute(SessionString.USER_PERMISSION);
        return new Result("200", "登出成功");
    }

    @RequestMapping("/getUserList")
    public Result getUserList(@RequestBody ReqData<User> reqData) {
        return new Result(getUserData(reqData), "请求成功");
    }

    @DeleteMapping("/deleteUser")
    public Result deleteUser(@RequestBody ReqData<User> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //所有用户
        List<User> userAllList = userService.list();
        //需要删除的用户
        List<User> userDeleteList = new ArrayList<>();
        //要更新的用户是否是自己
        boolean isDeleteMyself = false;
        //只有管理员和超级管理员能删除学生和老师，只有超级管理员能删除管理员，用户无法删除自己
        List<String> deleteCode = reqData.getDeleteCode();
        for (int i = 0; i < userAllList.size(); i++) {
            for (int j = 0; j < deleteCode.size(); j++) {
                //两个user_code相同
                if (userAllList.get(i).getUserCode().equals(deleteCode.get(j))) {
                    //只有管理员和超级管理员能删除学生和老师，只有超级管理员能删除管理员，用户无法删除自己
                    if (user.getRole().getValue() > userAllList.get(i).getRole().getValue()) {
                        userDeleteList.add(userAllList.get(i));
                    } else {
                        reqData.getDeleteCode().remove(j);
                    }
                }
            }
        }
        if (reqData.getDeleteCode().size() > 0) {
            //要删除的是是学生、老师、管理员的哪一种
            for (int i = 0; i < userDeleteList.size(); i++) {
                boolean delete = false;
                //只能删除权限<自己的
                if (user.getRole().getValue() > userDeleteList.get(i).getRole().getValue()) {
                    switch (userDeleteList.get(i).getRole().getValue()) {
                        //学生
                        case 1:
                            UpdateWrapper stuUpdateWrapper = new UpdateWrapper<>();
                            stuUpdateWrapper.eq("stu_code", userDeleteList.get(i).getTargetCode());
                            stuUpdateWrapper.set("is_delete", Disabled);
                            delete = stuService.update(null, stuUpdateWrapper);
                            break;
                        //教师
                        case 2:
                            UpdateWrapper empUpdateWrapper = new UpdateWrapper<>();
                            empUpdateWrapper.eq("emp_code", userDeleteList.get(i).getTargetCode());
                            empUpdateWrapper.set("is_delete", Disabled);
                            delete = empService.update(null, empUpdateWrapper);
                            break;
                        //管理员
                        case 3:
                            //管理员不需要删除其他内容，直接删除即可
                            break;
                    }
                }
            }
            boolean delete = userService.update(null, EntityUtils.getDeleteWrapper(reqData, "user_code"));
            if (delete) {
                //更新删除后的列表给前端
                return new Result(getUserData(reqData), "删除成功");
            }
        } else {
            return new Result("0", "删除失败,你没有权限删除该用户");
        }
        return new Result("0", "删除失败,权限不够");
    }

    @RequestMapping("/updatePassword")
    public Result updatePassword(@RequestBody ReqData<User> reqData, HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //当前的User
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_code", user.getUserCode());
        updateWrapper.set("pwd", reqData.getUpdatePassword());
        boolean update = userService.update(null, updateWrapper);
        if (update) {
            request.getSession().removeAttribute(SessionString.USER);
            request.getSession().removeAttribute(SessionString.USER_MENU);
            request.getSession().removeAttribute(SessionString.USER_PERMISSION);
            return new Result("200", "用户已更新，请重新登录");
        }
        return new Result("0", "更新密码失败");
    }

    @RequestMapping("/updateUser")
    public Result updateUser(@RequestBody ReqData<User> reqData,HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(SessionString.USER);
        //更新前的User
        QueryWrapper<User> oldUserWrapper = new QueryWrapper<>();
        oldUserWrapper.eq("user_code", reqData.getUserCode());
        User oldUser = userService.getOne(oldUserWrapper);
        //要更新的用户是否是自己
        boolean isUpdateMyself = oldUser.getUserCode().equals(user.getUserCode());
        //处理前端传入的数据
        EntityUtils.enumUpdateObject(reqData.getUpdateObject());
        //获取更新条件
        Map<String, Object> updateObject = reqData.getUpdateObject();
        String name_update = updateObject.get("userName").toString();
        Gender gender_update = (Gender) updateObject.get("gender");
        Role role_update = (Role) updateObject.get("role");
        String notes_update = updateObject.get("notes").toString();
        //不能更改除了自己以外 权限 ≥ 自己权限的用户
        if (user.getRole().getValue() > oldUser.getRole().getValue() || isUpdateMyself) {
            //如果不更改职位
            if (role_update.equals(oldUser.getRole())) {
                boolean isUpdate = false;
                UpdateWrapper updateWrapper = new UpdateWrapper();
                updateWrapper.set("gender", gender_update);
                updateWrapper.set("operator", user.getTargetCode());
                updateWrapper.set("update_date", DateUtils.getCurrentData(DateString.yyMMdd));
                //根据职位更改不同表
                switch (role_update) {
                    //学生
                    case Student:
                        updateWrapper.eq("stu_code", oldUser.getTargetCode());
                        updateWrapper.set("stu_name", name_update);
                        isUpdate = stuService.update(null, updateWrapper);
                        break;
                    //教师
                    case Teacher:
                        updateWrapper.eq("emp_code", oldUser.getTargetCode());
                        updateWrapper.set("emp_name", name_update);
                        isUpdate = empService.update(null, updateWrapper);
                        break;
                    //管理员
                    //超级管理员
                    case Admin:
                    case SuperAdmin:
                        isUpdate = true;
                        break;
                }
                //如果更改成功就改用户
                if (isUpdate) {
                    updateWrapper = EntityUtils.getUpdateWrapper(reqData);
                    updateWrapper.eq("user_code", reqData.getUserCode());
                    boolean userUpdate = userService.update(null, updateWrapper);
                    if (userUpdate) {
                        Map data = getUserData(reqData);
                        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("user_code", user.getUserCode());
                         user = userService.getOne(queryWrapper);
                        data.put("user", user);
                        return new Result(data, "更新成功");
                    }
                }
            }
            //职位更改了
            else {
                //管理员无法更改自己的权限,只能更改下级权限,只有超级管理员才有更改管理员职位的权限
                UpdateWrapper deleteWrapper;
                boolean save;
                switch (role_update) {
                    //更新为学生，原先可能为老师或者管理员
                    case Student:
                        //查询最新的那条数据的id
                        int stuCount = stuService.count() + 1;
                        String stuCode = CodeRule.StudentCodeRule + CodeUtils.getCode(stuCount, 5);
                        Stu stu = new Stu();
                        stu.setStuCode(stuCode);
                        stu.setStuName(name_update);
                        stu.setClazzCode("null");
                        //待修改
                        stu.setGender(gender_update);
                        stu.setPhone("null");
                        stu.setAddress("null");
                        stu.setNotes(notes_update);
                        stu.setOperator(user.getTargetCode());
                        stu.setCreateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                        stu.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                        stu.setIsDelete(Enabled);
                        //查找原先是什么职位
                        switch (oldUser.getRole()) {
                            //原先是教师
                            case Teacher:
                                //删除wrapper
                                deleteWrapper = new UpdateWrapper();
                                deleteWrapper.eq("emp_code", oldUser.getTargetCode());
                                deleteWrapper.set("is_delete", Disabled);
                                //添加进学生
                                save = stuService.save(stu);
                                if (save) {
                                    return getUpdateResult(reqData, stu.getStuCode(), deleteWrapper, empService);
                                }
                                break;
                            //原先是管理员
                            case Admin:
                                //管理员、超级管理员不能更改自己的权限,只有超级管理员才能更改为管理员权限
                                if (!isUpdateMyself && user.getRole().getValue().equals(SuperAdmin)) {
                                    //添加进学生
                                    save = stuService.save(stu);
                                    if (save) {
                                        return getUpdateResult(reqData, stuCode);
                                    }
                                }
                                return new Result("0", "管理员无法修改自己的权限");
                            case SuperAdmin:
                                return new Result("0", "超级管理员无法修改自己的权限!");
                        }
                        break;
                    //更新为老师，原先可能为学生或管理员
                    case Teacher:
                        //设置
                        int empCount = empService.count() + 1;
                        String empCode = CodeRule.EmpCodeRule + CodeUtils.getCode(empCount, 5);
                        Emp emp = new Emp();
                        emp.setEmpCode(empCode);
                        emp.setEmpName(name_update);
                        emp.setDeptCode("null");
                        emp.setGender(gender_update);
                        emp.setAddress("null");
                        emp.setNotes(notes_update);
                        emp.setOperator(user.getTargetCode());
                        emp.setCreateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                        emp.setUpdateDate(DateUtils.getCurrentData(DateString.yyMMdd));
                        emp.setIsDelete(Enabled);
                        //查找原先是什么职位
                        switch (oldUser.getRole()) {
                            //原先是学生
                            case Student:
                                //删除wrapper
                                deleteWrapper = new UpdateWrapper();
                                deleteWrapper.eq("stu_code", oldUser.getTargetCode());
                                deleteWrapper.set("is_delete", Disabled);
                                //添加进老师
                                save = empService.save(emp);
                                if (save) {
                                    return getUpdateResult(reqData, emp.getEmpCode(), deleteWrapper, stuService);
                                }
                                break;
                            //原先是管理员
                            case Admin:
                                //管理员、超级管理员不能更改自己的权限,只有超级管理员才能更改为管理员权限
                                if (!isUpdateMyself &&user.getRole().equals(SuperAdmin)) {
                                    //添加进老师
                                    save = empService.save(emp);
                                    if (save) {
                                        return getUpdateResult(reqData, empCode);
                                    }
                                }
                                return new Result("0", "管理员无法修改自己的权限");
                            case SuperAdmin:
                                return new Result("0", "超级管理员无法修改自己的权限!");
                        }
                        break;
                    case Admin:
                        //管理员、超级管理员不能更改自己的权限,只有超级管理员才能更改为管理员权限
                        if (!isUpdateMyself && user.getRole().equals(SuperAdmin)) {
                            //原先可能是教师或学生
                            switch (oldUser.getRole()) {
                                //学生
                                case Student:
                                    //删除wrapper
                                    deleteWrapper = new UpdateWrapper();
                                    deleteWrapper.eq("stu_code", oldUser.getTargetCode());
                                    deleteWrapper.set("is_delete", Disabled);
                                    return getUpdateResult(reqData, "0", deleteWrapper, stuService);
                                //教师
                                case Teacher:
                                    //删除wrapper
                                    deleteWrapper = new UpdateWrapper();
                                    deleteWrapper.eq("emp_code", oldUser.getTargetCode());
                                    deleteWrapper.set("is_delete", Disabled);
                                    return getUpdateResult(reqData, "0", deleteWrapper, empService);
                            }
                        }
                        return new Result("0", "超级管理员无法修改自己的权限!");
                    case SuperAdmin:
                        return new Result("0", "超级管理员权限只能数据库手动录入!无法更改");
                }
            }
        } else {
            return new Result("0", "你没有权限更改该数据");
        }
        return new Result("0", "更新失败");
    }

    @RequestMapping("/addUser")
    public Result addUser(@RequestBody ReqData<User> reqData) {
        //要添加的user
        User user = reqData.getInsertObject();
        //用户名不能一致
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", reqData.getInsertObject().getUserName());
        User one = userService.getOne(queryWrapper);
        if (one != null) {
            return new Result("0", "添加失败，用户名已存在");
        }
        //是否添加成功
        boolean save = false;
        //查询最新的那条数据的id
        int userCount = userService.count() + 1;
        String userCode = CodeRule.UserCodeRule + CodeUtils.getCode(userCount, 5);
        user.setUserCode(userCode);
        //设置默认密码
        user.setTargetCode("0");
        user.setPwd(Common.DefaultUserPwd);
        user.setIsDelete(Enabled);
        //无法添加权限>=自己的用户
        if (user.getRole().getValue() > reqData.getInsertObject().getRole().getValue()) {
            switch (user.getRole()) {
                //学生
                case Student:
                    Stu stu = new Stu();
                    int stuCount = stuService.count() + 1;
                    String stuCode = CodeRule.StudentCodeRule + CodeUtils.getCode(stuCount, 5);
                    user.setTargetCode(stuCode);
                    stu.setStuCode(stuCode);
                    stu.setStuName(reqData.getInsertObject().getUserName());
                    stu.setClazzCode("null");                    //待修改
                    stu.setGender(reqData.getInsertObject().getGender());
                    stu.setPhone("null");
                    stu.setAddress("null");
                    stu.setNotes(reqData.getInsertObject().getNotes());
                    stu.setOperator(user.getTargetCode());
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
                    emp.setEmpName(reqData.getInsertObject().getUserName());
                    emp.setDeptCode("null");
                    emp.setGender(reqData.getInsertObject().getGender());
                    emp.setAddress("null");
                    emp.setNotes(reqData.getInsertObject().getNotes());
                    emp.setOperator(user.getTargetCode());
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
            if (save) {
                save = userService.save(user);
                if (save) {
                    return new Result(getUserData(reqData), "添加成功");
                }
            }
        } else {
            return new Result("0", "添加失败,你的权限不够");
        }
        return new Result("0", "添加失败");
    }

    @RequestMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("reqData") String reqDataString,HttpServletRequest request) throws IOException {
        try {
            User user = (User) request.getSession().getAttribute(SessionString.USER);
            ReqData<User> reqData = JSON.parseObject(reqDataString, ReqData.class);
            Map<Integer, String> head = new HashMap<>();
            head.put(0, "用户名(必填)");
            head.put(1, "性别,必填(男，女)");
            head.put(2, "备注");
            head.put(3, "角色,必填(学生，教师，管理员)");
            EasyExcel.read(file.getInputStream(), User.class, new UserDataListener(userService, userService, empService, stuService, head,user))
                    .sheet()
                    .doRead();
            return new Result(getUserData(reqData), "导入成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result("0", e.getMessage());
        }
    }

    @RequestMapping("/export")
    public void export(@RequestBody ReqData<User> reqData, HttpServletResponse response) throws Exception {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String code : reqData.getExportCode()) {
            queryWrapper.eq("user_code", code).or();
        }
        ExcelUtils.getExportExcel(response, userService, User.class, queryWrapper);
    }

    @RequestMapping("/downModule")
    public void downModule(HttpServletResponse response) throws Exception {
        ExcelUtils.getModuleExcel(response, User.class);
    }

    private Map getUserData(ReqData<User> reqData) {
        List<User> userList = EntityUtils.getListByPage(userService, reqData);
        int userRecord = EntityUtils.getListRecordByQuery(userService, reqData);
        List<UserRole> roleList = userRoleService.list();
        //返回前端的数据
        Map<String, Object> data = new HashMap<>();
        data.put("userList", userList);
        data.put("totalRecord", userRecord);
        data.put("roleList", roleList);
        return data;
    }

    public Result getUpdateResult(ReqData<User> reqData, String targetCode) {
        UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
        updateWrapper.eq("user_code", reqData.getUserCode());
        updateWrapper.set("target_code", targetCode);
        boolean update = userService.update(null, updateWrapper);
        if (update) {
            return new Result(getUserData(reqData), "更新成功");
        } else {
            return new Result("0", "更新失败");
        }
    }

    public Result getUpdateResult(ReqData<User> reqData, String targetCode, UpdateWrapper deleteWrapper, IService service) {
        //先删除对应表
        boolean update = service.update(null, deleteWrapper);
        if (update) {
            UpdateWrapper updateWrapper = EntityUtils.getUpdateWrapper(reqData);
            updateWrapper.eq("user_code", reqData.getUserCode());
            updateWrapper.set("target_code", targetCode);
            update = userService.update(null, updateWrapper);
            if (update) {
                return new Result(getUserData(reqData), "更新成功");
            }
        }
        return new Result("0", "更新失败");
    }


    private User updateUserToken(User user) {
        String userCode = user.getUserCode();
        String pwd = user.getPwd();
        Role role = user.getRole();
        //创建一个新的令牌给用户
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("user_code", userCode);
        userMap.put("pwd", pwd);
        userMap.put("role", role);
        String userToken = JWTUtils.getToken(userMap);

        //将令牌插入到实体
        user.setToken(userToken);

        //更新用户的令牌
        UpdateWrapper<User> empUpdateWrapper = new UpdateWrapper<>();
        empUpdateWrapper.eq("user_code", userCode).eq("pwd", pwd);

        userService.update(user, empUpdateWrapper);
        return user;
    }

    private List<Menu> getMenuByUserRole(User user) {
        //查询用户是什么角色
        QueryWrapper<UserRole> userRoleQueryWrapper = new QueryWrapper<>();
        //根据类型值查找对应角色
        userRoleQueryWrapper.eq("id", user.getRole());
        //查询该职位是否启用
        userRoleQueryWrapper.eq("is_delete", Enabled);
        UserRole userRole = userRoleService.getOne(userRoleQueryWrapper);
        //如果查询到了
        if (userRole != null) {
            //查询该角色的能看的菜单id
            QueryWrapper<UserRoleMenuRelation> roleMenuRelationQueryWrapper = new QueryWrapper<>();
            roleMenuRelationQueryWrapper.eq("role_id", userRole.getId());
            List<UserRoleMenuRelation> roleMenuRelationList = userRoleMenuRelationService.list(roleMenuRelationQueryWrapper);
            //查询对应的菜单
            QueryWrapper<Menu> menuQueryWrapper = new QueryWrapper<>();
            menuQueryWrapper.eq("hidden", "0");
            for (UserRoleMenuRelation item : roleMenuRelationList) {
                menuQueryWrapper.eq("id", item.getMenuId()).or();
            }
            List<Menu> menuList = menuService.list(menuQueryWrapper);
            return menuList;
        }
        return null;
    }

    private List<Permission> getPermissionByUserRole(User user) {
        //查询用户是什么角色
        QueryWrapper<UserRole> userRoleQueryWrapper = new QueryWrapper<>();
        //根据类型值查找对应角色
        userRoleQueryWrapper.eq("id", user.getRole());
        //查询该职位是否启用
        userRoleQueryWrapper.eq("is_delete", Enabled);
        UserRole userRole = userRoleService.getOne(userRoleQueryWrapper);
        //如果查询到了
        if (userRole != null) {
            //查询该角色的对应的权限的id
            QueryWrapper<UserRolePermissionRelation> rolePermissionRelationQueryWrapper = new QueryWrapper<>();
            rolePermissionRelationQueryWrapper.eq("role_id", userRole.getId());
            List<UserRolePermissionRelation> rolePermissionRelationList = userRolePermissionRelationService.list(rolePermissionRelationQueryWrapper);
            //查询对应的权限
            QueryWrapper<Permission> permissionQueryWrapper = new QueryWrapper<>();
            permissionQueryWrapper.eq("is_delete", Enabled);
            for (UserRolePermissionRelation item : rolePermissionRelationList) {
                permissionQueryWrapper.eq("id", item.getPermissionId()).or();
            }
            List<Permission> permissionList = permissionService.list(permissionQueryWrapper);
            return permissionList;
        }
        return null;
    }


    private void setUserSession(User user, HttpServletRequest request) {
        //将用户放入session中
        request.getSession().setAttribute(SessionString.USER, user);
        //根据用户的角色获取用户可以看的菜单
        List<Menu> menuList = getMenuByUserRole(user);
        //将用户可以看的菜单放入session
        request.getSession().setAttribute(SessionString.USER_MENU, menuList);
        //根据用户的角色获取用户可以获取的后端接口权限
        List<Permission> permissionList = getPermissionByUserRole(user);
        //将用户对应的权限放入session
        request.getSession().setAttribute(SessionString.USER_PERMISSION, permissionList);
    }
}
