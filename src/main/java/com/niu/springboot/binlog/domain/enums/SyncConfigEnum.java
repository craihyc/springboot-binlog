package com.niu.springboot.binlog.domain.enums;

import lombok.Getter;

/**
 * 系统字典枚举
 *
 * @author genlot
 */
@Getter
public enum SyncConfigEnum {

    /**
     * 枚举
     */
    BIN_LOG_FILE_NAME("BIN_LOG_FILE_NAME", "binlog文件名"),
    BIN_LOG_NEXT_POSITION("BIN_LOG_NEXT_POSITION", "binlog读取位置");

    SyncConfigEnum(String key, String des) {
        this.key = key;
        this.des = des;
    }

    private final String key;

    private final String des;

}
