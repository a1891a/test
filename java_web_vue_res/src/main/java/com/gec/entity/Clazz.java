package com.gec.entity;


import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "clazz")
public class Clazz {
    @TableField("clazz_code")
    @ExcelIgnore
    private String clazzCode;

    @TableField("clazz_name")
    @ExcelProperty(value = "班级名称(必填)")
    @ExcelValid(message = "班级名称必填")
    private String clazzName;

    @ExcelProperty(value = "专业名称(必填)")
    @ExcelValid(message = "专业必填")
    private String major;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelIgnore
    private String operator;

    @ExcelProperty(value = "备注")
    private String notes;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;

}
