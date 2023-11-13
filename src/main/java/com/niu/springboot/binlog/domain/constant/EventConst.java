package com.niu.springboot.binlog.domain.constant;

import com.github.shyiko.mysql.binlog.event.EventType;
import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author genlot
 */
public class EventConst {

    /**
     * 允许收集的数据类型
     */
    public static final List<EventType> ALLOW_COLLECTION_TYPES = Lists.newArrayList(EventType.EXT_UPDATE_ROWS, EventType.EXT_WRITE_ROWS, EventType.EXT_DELETE_ROWS);
    /**
     * 查询表信息
     */
    public static final String SQL_SCHEMA = "select table_schema, table_name, column_name, ordinal_position from information_schema.columns where table_schema = ? and table_name = ?";
    /**
     * 查询表主键
     */
    public static final String SQL_PRIMARY_SCHEMA = "select column_name, column_key from information_schema.columns where table_schema = ? and table_name = ? and column_key = 'PRI'";

    /**
     * 可以直接转字符串的类型
     */
    public static final List<Class<?>> NORMAL_TYPE = Lists.newArrayList(byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            String.class,
            BigDecimal.class);
}
