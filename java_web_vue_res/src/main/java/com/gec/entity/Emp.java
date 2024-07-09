package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Gender;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;

@Data
@TableName(value = "emp")
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
/**
 * 如果数据库中，该字段带有下划线，那么就需要使用     @TableField("字段")
 *                                           private 类型 变量名改为驼峰命名法
 */
public class Emp {
    @TableField("emp_code")
    @ExcelIgnore
    private String empCode;

    @TableField("dept_code")
    @ExcelIgnore
    private String deptCode;

    @TableField(exist = false)
    @ExcelIgnore
    private String deptName;

    @TableField("emp_name")
    @ExcelProperty(value = "姓名(必填)")
    @ExcelValid(message = "姓名必填")
    private String empName;

    @ExcelProperty(value = "性别,必填(男，女)", converter = GenderConvert.class)
    @ExcelValid(message = "性别必填")
    private Gender gender;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelIgnore
    private String address;

    @ExcelProperty(value = "备注")
    private String notes;

    @ExcelIgnore
    private String operator;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;
}
