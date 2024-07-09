package com.gec.excel.convert;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.gec.enums.Gender;
import com.gec.enums.Importance;
import com.gec.enums.Role;

public class RoleConvert implements Converter<Role> {
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
    public Role convertToJavaData(ReadConverterContext<?> context) {
        String inputValue = context.getReadCellData().getStringValue();
        //如果传入的是数字，就转换
        if (inputValue == null) {
            inputValue = context.getReadCellData().getNumberValue().toBigInteger().toString();
        }
        return Role.getEnum(inputValue);
    }

    /**
     * 导出用的转换器
     *
     * @param role
     * @param excelContentProperty
     * @param globalConfiguration
     * @return
     * @throws Exception
     */
    @Override
    public WriteCellData<?> convertToExcelData(Role role, ExcelContentProperty excelContentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData(role.getDesc());
    }
}
