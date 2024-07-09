package com.gec.excel.convert;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.gec.enums.ApprovalStatus;
import com.gec.enums.EnabledStatus;
import com.gec.enums.Gender;
import com.gec.enums.Role;

public class EnabledConvert implements Converter<EnabledStatus> {
    @Override
    public Class supportJavaTypeKey() {
        return null;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    /**
     * 导入用的转换器
     *
     * @param context
     * @return
     */
    @Override
    public EnabledStatus convertToJavaData(ReadConverterContext<?> context) {
        String inputValue = context.getReadCellData().getStringValue();
        //如果传入的是数字，就转换
        if (inputValue == null) {
            inputValue = context.getReadCellData().getNumberValue().toBigInteger().toString();
        }
        return EnabledStatus.getEnum(inputValue);
    }

    /**
     * 导出用的转换器
     *
     * @param enabledStatus
     * @param excelContentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public WriteCellData<?> convertToExcelData(EnabledStatus enabledStatus, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData(enabledStatus.getDesc());
    }
}