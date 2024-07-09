package com.gec.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.gec.enums.ApprovalStatus;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Importance;
import com.gec.excel.convert.ApprovalConvert;
import com.gec.excel.convert.ImportanceConvert;
import com.gec.excel.valid.ExcelValid;
import lombok.Data;

@ContentRowHeight(20)
@HeadRowHeight(30)
@ColumnWidth(40)
@Data
@TableName(value = "analysis")
public class Analysis {
    @TableId
    @TableField("tc_code")
    @ExcelIgnore
    private String tcCode;

    @TableField("pro_code")
    @ExcelIgnore
    private String proCode;

    @TableField("mod_code")
    @ExcelIgnore
    private String modCode;

    @TableField(exist = false)
    @ExcelIgnore
    private String modName;

    @TableField("stad_code")
    @ExcelIgnore
    private String stadCode;

    @ExcelIgnore
    private String author;

    @TableField("tc_title")
    @ExcelProperty(value = "标题")
    private String tcTitle;

    @TableField("tc_desc")
    @ExcelProperty(value = "描述")
    private String tcDesc;

    @TableField("tc_condition")
    @ExcelProperty(value = "前置条件")
    private String tcCondition;

    @ExcelProperty(value = "输入")
    private String input;

    @ExcelProperty(value = "步骤")
    private String step;

    @ExcelProperty(value = "预期结果")
    private String result;

    @ExcelProperty(value = "重要程度(低，中，高)", converter = ImportanceConvert.class)
    private Importance importance;

    @TableField("desc_simi")
    @ExcelProperty(value = "描述相似度")
    private String descSimi;

    @TableField("result_simi")
    @ExcelProperty(value = "结果相似度")
    private String resultSimi;

    @TableField("comp_score")
    @ExcelProperty(value = "单项分数")
    private String compScore;

    @ExcelProperty(value = "备注")
    private String notes;

    @TableField("is_approval")
    @ExcelProperty(value = "是否审核(审核,不审核)", converter = ApprovalConvert.class)
    private ApprovalStatus isApproval;

    @TableField("create_date")
    @ExcelIgnore
    private String createDate;

    @TableField("update_date")
    @ExcelIgnore
    private String updateDate;

    @ExcelIgnore
    private String operator;

    @TableField(exist = false)
    @ExcelProperty(value = "提交人")
    private String operatorName;

    @TableField("is_delete")
    @ExcelIgnore
    private EnabledStatus isDelete;
}
