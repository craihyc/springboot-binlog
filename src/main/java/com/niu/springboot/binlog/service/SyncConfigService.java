package com.niu.springboot.binlog.service;

import com.niu.springboot.binlog.domain.enums.SyncConfigEnum;

/**
 * 系统字典业务类
 *
 * @author genlot
 */
public interface SyncConfigService {

    /**
     * 根据Key更新值
     *
     * @param key 键
     * @param val 值
     */
    void updateByKey(SyncConfigEnum key, String val);

    /**
     * 根据Key获取值
     *
     * @param key 键
     * @return {@link String} 获取键的值
     */
    String getValByKey(SyncConfigEnum key);
}
