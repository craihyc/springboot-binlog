package com.niu.springboot.binlog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.niu.springboot.binlog.domain.po.TSyncMapping;

import java.util.List;
import java.util.Set;

/**
 *
 * @author genlot
 */
public interface SyncMappingService extends IService<TSyncMapping> {


    /**
     * 获取允许收集的数据库
     * @return 数据库集合
     */
    Set<String> getAllowSchemeList();

    /**
     * 获取允许收集的数据库表
     * @return 数据库表集合
     */
    Set<String> getAllowTableList();

    /**
     * 根据数据库和表名获取映射关系
     * @param originDataBase 原始数据库
     * @param originTableName 原始表名
     * @return 映射关系
     */
    List<TSyncMapping> listBySchemeAndTableName(String originDataBase, String originTableName);

}
