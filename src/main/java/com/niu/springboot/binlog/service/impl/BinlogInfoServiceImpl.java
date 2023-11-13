package com.niu.springboot.binlog.service.impl;

import com.niu.springboot.autoconfig.service.BinlogInfoService;
import com.niu.springboot.binlog.domain.enums.SyncConfigEnum;
import com.niu.springboot.binlog.service.SyncConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * binlog 详情实现类
 *
 * @author [nza]
 * @version 1.0 [2020/12/24 10:02]
 * @createTime [2020/12/24 10:02]
 */
@Service
@Slf4j
@AllArgsConstructor
public class BinlogInfoServiceImpl implements BinlogInfoService {


    private final SyncConfigService syncConfigService;

    @Override
    public String getBinlogFileName(Object... param) {
        String filename = syncConfigService.getValByKey(SyncConfigEnum.BIN_LOG_FILE_NAME);
        if (StringUtils.isEmpty(filename)) {
            return null;
        }
        return filename;
    }

    @Override
    public Long getBinlogNextPosition(Object... param) {
        String position = syncConfigService.getValByKey(SyncConfigEnum.BIN_LOG_NEXT_POSITION);
        if (StringUtils.isEmpty(position)) {
            return null;
        }
        return Long.parseLong(position);
    }
}
