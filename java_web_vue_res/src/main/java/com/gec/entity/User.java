package com.gec.entity;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.convert.RoleConvert;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Gender;
import com.gec.enums.Role;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "user")
public class User {
    @TableField("user_code")
    @ExcelIgnore
    private String userCode;

    @TableField("target_code")
    @ExcelIgnore
    private String targetCode;

    @ExcelIgnore
    private String pwd;

    @TableField("user_name")
    @ExcelProperty(value = "用户名(必填)")
    @ExcelValid(message = "用户名必填")
    private String userName;

    @ExcelProperty(value = "性别,必填(男，女)", converter = GenderConvert.class)
    @ExcelValid(message = "性别必填")
    private Gender gender;

    @ExcelProperty(value = "备注")
    private String notes;

    @ExcelProperty(value = "角色,必填(学生，教师，管理员)", converter = RoleConvert.class)
    @ExcelValid(message = "角色必填")
    private Role role;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;

    @ExcelIgnore
    private String token;
}
