package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.enums.EnabledStatus;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;

@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "project")
public class Project {
    @TableField("pro_code")
    @ExcelIgnore
    private String proCode;

    @TableField("pro_name")
    @ExcelProperty(value = "项目名称(必填)")
    @ExcelValid(message = "项目名称必填")
    private String proName;

    @TableField("pro_desc")
    @ExcelProperty(value = "项目描述(必填)")
    @ExcelValid(message = "项目描述必填")
    private String proDesc;

    @ExcelProperty(value = "项目经理")
    private String manager;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelProperty(value = "备注")
    private String notes;

    @ExcelIgnore
    private String operator;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;
}
