package com.brevity.gware.mapper;

import com.brevity.gware.bean.WareInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @param
 * @return
 */
public interface WareInfoMapper extends Mapper<WareInfo> {

    public List<WareInfo> selectWareInfoBySkuid(String skuid);
}
