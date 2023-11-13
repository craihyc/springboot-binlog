package com.niu.springboot.binlog.domain.po;

import lombok.Data;

import java.io.Serializable;

/**
 * tableName: t_sync_mapping
 *
 * @author genlot
 */
@Data
public class TSyncMapping implements Serializable {

    /**
     * 主键ID
     */
    private Integer id;
    /**
     * 原始数据库
     */
    private String originDb;
    /**
     * 原始表名
     */
    private String originTableName;
    /**
     * 原始表字段
     */
    private String originTableField;
    /**
     * 目标数据库
     */
    private String targetDb;
    /**
     * 目标表名
     */
    private String targetTableName;
    /**
     * 目标表字段
     */
    private String targetTableField;

}
