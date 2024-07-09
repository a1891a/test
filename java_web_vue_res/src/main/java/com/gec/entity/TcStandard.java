package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Importance;
import com.gec.excel.convert.ImportanceConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;
@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "tc_standard")
public class TcStandard {
    @TableField("stad_code")
    @ExcelIgnore
    private String stadCode;

    @TableField("mod_code")
    @ExcelIgnore
    private String modCode;

    @TableField(exist = false)
    @ExcelIgnore
    private String modName;

    @TableField("stad_title")
    @ExcelProperty(value = "标准标题(必填)")
    @ExcelValid(message = "标准标题必填")
    private String stadTitle;

    @TableField("stad_desc")
    @ExcelProperty(value = "标准描述(必填)")
    @ExcelValid(message = "标准描述必填")
    private String stadDesc;

    @TableField("stad_condition")
    @ExcelProperty(value = "前置条件(必填)")
    @ExcelValid(message = "前置条件必填")
    private String stadCondition;

    @ExcelProperty(value = "预期结果(必填)")
    @ExcelValid(message = "预期结果必填")
    private String result;

    @ExcelProperty(value = "输入(必填)")
    @ExcelValid(message = "输入必填")
    private String input;

    @ExcelProperty(value = "步骤(必填)")
    @ExcelValid(message = "步骤必填")
    private String step;

    @ExcelProperty(value = "重要程度,必填(低，中，高)", converter = ImportanceConvert.class)
    @ExcelValid(message = "重要程度必填")
    private Importance importance;

    @ExcelProperty(value = "备注")
    private String notes;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelIgnore
    private String operator;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;
}