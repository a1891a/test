package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "dept")
public class Dept {
    @TableField("dept_code")
    @ExcelIgnore
    private String deptCode;

    @TableField("dept_name")
    @ExcelProperty(value = "部门名称(必填)")
    @ExcelValid(message = "部门名称必填")
    private String deptName;

    @ExcelProperty(value = "地址(必填)")
    @ExcelValid(message = "地址必填")
    private String address;

    @TableField("parent_code")
    @ExcelIgnore
    private String parentCode;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelProperty(value = "部门主任")
    private String director;

    @ExcelIgnore
    private String operator;

    @ExcelIgnore
    private String notes;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;

}
