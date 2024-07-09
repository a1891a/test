package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.ApprovalStatus;
import com.gec.enums.EnabledStatus;
import com.gec.excel.convert.ApprovalConvert;
import com.gec.excel.convert.GenderConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;

@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "module")
public class Module {
    @TableField("mod_code")
    @ExcelIgnore
    private String modCode;

    @TableField("pro_code")
    @ExcelIgnore
    private String proCode;

    @TableField(exist = false)
    @ExcelIgnore
    private String proName;

    @TableField("mod_name")
    @ExcelProperty(value = "模块名称(必填)")
    @ExcelValid(message = "模块名称必填")
    private String modName;

    @TableField("mod_desc")
    @ExcelProperty(value = "模块描述(必填)")
    @ExcelValid(message = "模块描述必填")
    private String modDesc;

    @TableField("tc_amount")
    @ExcelProperty(value = "用例总数(必填)")
    @ExcelValid(message = "用例总数必填")
    private int tcAmount;

    @TableField("is_approval")
    @ExcelProperty(value = "是否审核,必填(审核，不审核)", converter = ApprovalConvert.class)
    @ExcelValid(message = "是否审核")
    private ApprovalStatus isApproval;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelProperty(value = "模块备注")
    private String notes;

    @ExcelIgnore
    private String operator;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;
}
