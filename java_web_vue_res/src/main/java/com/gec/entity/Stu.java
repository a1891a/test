package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Gender;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "student")
public class Stu {
    @TableField("stu_code")
    @ExcelIgnore
    private String stuCode;

    @TableField("clazz_code")
    @ExcelIgnore
    private String clazzCode;

    @TableField(exist = false)
    @ExcelIgnore
    private String clazzName;

    @TableField("stu_name")
    @ExcelProperty(value = "姓名(必填)")
    @ExcelValid(message = "姓名必填")
    private String stuName;

    @ExcelProperty(value = "性别,必填(男，女)", converter = GenderConvert.class)
    @ExcelValid(message = "性别必填")
    private Gender gender;

    @ExcelProperty(value = "手机号")
    private String phone;

    @ExcelProperty(value = "地址(必填)")
    @ExcelValid(message = "地址必填")
    private String address;

    @ExcelProperty(value = "备注")
    private String notes;

    @ExcelIgnore
    private String operator;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;

}
