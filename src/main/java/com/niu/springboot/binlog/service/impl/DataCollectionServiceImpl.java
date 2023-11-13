package com.niu.springboot.binlog.service.impl;

import com.github.shyiko.mysql.binlog.event.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.niu.springboot.autoconfig.service.DataCollectionService;
import com.niu.springboot.binlog.domain.constant.EventConst;
import com.niu.springboot.binlog.domain.dto.BinlogRowDataBO;
import com.niu.springboot.binlog.domain.enums.SyncConfigEnum;
import com.niu.springboot.binlog.domain.po.TSyncMapping;
import com.niu.springboot.binlog.service.SyncConfigService;
import com.niu.springboot.binlog.service.SyncMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 数据收集业务实现类
 *
 * @author genlot
 */
@Service
@Slf4j
public class DataCollectionServiceImpl implements DataCollectionService {

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private SyncConfigService syncConfigService;
    @Resource
    private SyncMappingService syncMappingService;


    /**
     * binlog文件是否变化
     */
    private Boolean isBinlogChanged = false;

    @Override
    public void collectionIncrementalData(Event event) {

        // 获取到事件类型
        EventType type = event.getHeader().getEventType();

        // 切换了 binlog 文件
        if (handleBinlogFileChange(type, event)) {
            return;
        }

        // 初始化RowData上下文,设置表信息
        BinlogRowDataBO rowData = initRowData(event, type);

        // 判断是否可以收集
        if (!rowData.canCollection(syncMappingService.getAllowSchemeList(), syncMappingService.getAllowTableList())) {
            return;
        }

        // 执行收集逻辑
        doCollection(event, rowData);
    }


    /**
     * 处理 binlog 文件切换事件
     *
     * @param type  事件类型
     * @param event binlog事件
     * @return boolean
     */
    private synchronized boolean handleBinlogFileChange(EventType type, Event event) {
        if (EventType.ROTATE.equals(type)) {
            // 更新 binlog 文件相关记录配置
            String originalFile = syncConfigService.getValByKey(SyncConfigEnum.BIN_LOG_FILE_NAME);
            String binlogFilename = ((RotateEventData) event.getData()).getBinlogFilename();
            // 如果文件未变化忽略即可
            if (StringUtils.equals(binlogFilename, originalFile)) {
                return true;
            }

            isBinlogChanged = true;
            syncConfigService.updateByKey(SyncConfigEnum.BIN_LOG_FILE_NAME, binlogFilename);
            return true;
        }
        if (EventType.FORMAT_DESCRIPTION.equals(type) && isBinlogChanged) {
            // 更新 binlog 开始位置记录配置
            long nextPosition = ((EventHeaderV4) event.getHeader()).getNextPosition();
            syncConfigService.updateByKey(SyncConfigEnum.BIN_LOG_NEXT_POSITION, String.valueOf(nextPosition));
            isBinlogChanged = false;
            return true;
        }
        return false;
    }

    /**
     * 执行收集逻辑 {@link Exception} 收集失败抛出
     *
     * @param event   binlog 事件
     * @param rowData 上下文
     */
    private void doCollection(Event event, BinlogRowDataBO rowData) {
        try {
            // 查询表映射信息
            Map<Integer, String> dbPosMap = getDbPosMap(rowData.getSchemaName(), rowData.getTableName());

            // 构造 BinlogRowData 对象
            buildRowData(event.getData(), rowData, dbPosMap);
            rowData.setNextPosition(((EventHeaderV4) event.getHeader()).getNextPosition());
            rowData.setCurPosition(((EventHeaderV4) event.getHeader()).getPosition());

            log.info("收集完成: {}", rowData);

            // 将数据变动同步到备份表
            doBackup(rowData);


        } catch (Exception ex) {
            log.error("收集增量数据发送异常, 异常信息: ", ex);
        }
    }

    /**
     * 将数据变动同步到备份表
     *
     * @param rowData 源数据
     */
    private void doBackup(BinlogRowDataBO rowData) {
        // 根据原库、表查询目标库、表及其映射字段
        List<TSyncMapping> list = syncMappingService.listBySchemeAndTableName(rowData.getOriginDataBase(), rowData.getOriginTableName());
        for (String sql : rowData.getSql(list)) {
            int res = jdbcTemplate.update(sql);
            log.info("同步完成, 影响行: {}", res);
        }

        // 更新配置表
        syncConfigService.updateByKey(SyncConfigEnum.BIN_LOG_NEXT_POSITION, String.valueOf(rowData.getNextPosition()));
    }


    /**
     * 构建行数据
     *
     * @param data 事件
     */
    private void buildRowData(EventData data, BinlogRowDataBO rowDataBO, Map<Integer, String> dbPosMap) throws Exception {

        List<String> primaryKeys = getPrimaryKeys(rowDataBO.getSchemaName(), rowDataBO.getTableName());
        List<Map<String, String>> after = Lists.newArrayList();
        List<Map<String, String>> before = Lists.newArrayList();

        switch (rowDataBO.getEventType()) {
            case EXT_WRITE_ROWS:
                processWriteRows((WriteRowsEventData) data, dbPosMap, after);
                break;
            case EXT_UPDATE_ROWS:
                processUpdateRows((UpdateRowsEventData) data, dbPosMap, after, before);
                break;
            case EXT_DELETE_ROWS:
                processDeleteRows((DeleteRowsEventData) data, dbPosMap, after);
                break;
            default:
                throw new Exception("非法的数据行类型: " + rowDataBO.getEventType().name());
        }

        rowDataBO.setPrimaryKeys(primaryKeys)
                .setAfter(after)
                .setBefore(before);
    }

    /**
     * 处理删除行操作
     *
     * @param data     binlog 源数据
     * @param dbPosMap 表映射
     * @param after    变更后的数据
     */
    private void processDeleteRows(DeleteRowsEventData data, Map<Integer, String> dbPosMap, List<Map<String, String>> after) {
        BitSet columns = data.getIncludedColumns();
        List<Serializable[]> rows = data.getRows();
        addRowData(dbPosMap, after, columns, rows);
    }

    /**
     * 处理插入数据
     *
     * @param data     binlog 数据
     * @param dbPosMap 数据库映射
     * @param after    变更后的数据
     */
    private void processWriteRows(WriteRowsEventData data, Map<Integer, String> dbPosMap, List<Map<String, String>> after) {
        BitSet columns = data.getIncludedColumns();
        List<Serializable[]> rows = data.getRows();
        addRowData(dbPosMap, after, columns, rows);
    }

    /**
     * 添加行数据
     *
     * @param dbPosMap 表映射
     * @param rowList  行数据列表
     * @param columns  行信息
     * @param rows     需要转换的binlog源数据
     */
    private void addRowData(Map<Integer, String> dbPosMap, List<Map<String, String>> rowList, BitSet columns, List<Serializable[]> rows) {
        for (Serializable[] row : rows) {
            Map<String, String> afterRow = Maps.newHashMap();
            for (int i = 0; i < row.length; i++) {
                Object item = row[i];

                // todo：这里需要做数据类型转换
                afterRow.put(dbPosMap.get(columns.nextSetBit(i)), convert2SqlStr(item));
            }
            rowList.add(afterRow);
        }
    }

    /**
     * 转换Sql字符串
     *
     * @param item 参数
     * @return {@link java.lang.String}
     */
    private String convert2SqlStr(Object item) {
        if (item == null) {
            return null;
        }
        if (EventConst.NORMAL_TYPE.contains(item.getClass())) {
            return String.valueOf(item);
        }
        if (item instanceof Boolean) {
            return (boolean) item ? String.valueOf(1) : String.valueOf(0);
        }
        if (item instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item);
        }
        return null;
    }

    /**
     * 处理更新行
     *
     * @param data     binlog 数据
     * @param dbPosMap 数据库字段映射
     * @param after    变更前
     * @param before   变更后
     */
    private void processUpdateRows(UpdateRowsEventData data, Map<Integer, String> dbPosMap, List<Map<String, String>> after, List<Map<String, String>> before) {
        BitSet columns = data.getIncludedColumns();
        List<Map.Entry<Serializable[], Serializable[]>> rows = data.getRows();
        for (Map.Entry<Serializable[], Serializable[]> entry : rows) {

            // 添加变动前的数据列表
            addRowData(dbPosMap, before, columns, Collections.singletonList(entry.getKey()));

            // 添加变动后的数据列表
            addRowData(dbPosMap, after, columns, Collections.singletonList(entry.getValue()));
        }
    }

    /**
     * 设置表信息
     *
     * @param event 事件
     * @param type  类型
     */
    private BinlogRowDataBO initRowData(Event event, EventType type) {
        // 如果是 TABLE_MAP 事件，可以从中获取到操作的库名和表名
        if (type == EventType.TABLE_MAP) {
            BinlogRowDataBO rowData = new BinlogRowDataBO();
            TableMapEventData data = event.getData();
            rowData.setOriginTableName(data.getTable());
            rowData.setOriginDataBase(data.getDatabase());
            rowData.setEventType(type);
            return rowData;
        } else {
            return new BinlogRowDataBO();
        }
    }

    @Override
    public Map<Integer, String> getDbPosMap(String schema, String tableName) {

        Map<Integer, String> posMap = Maps.newHashMap();

        jdbcTemplate.query(EventConst.SQL_SCHEMA, new String[]{schema, tableName}, (rs, i) -> {
            int pos = rs.getInt("ORDINAL_POSITION");
            String colName = rs.getString("COLUMN_NAME");

            posMap.put(pos - 1, colName);
            return posMap;
        });

        return posMap;
    }

    @Override
    public List<String> getPrimaryKeys(String schema, String tableName) {
        List<String> primaryKeys = Lists.newArrayList();
        jdbcTemplate.query(EventConst.SQL_PRIMARY_SCHEMA, new String[]{schema, tableName}, (rs, i) -> {
            String columnName = rs.getString("column_name");
            if (!StringUtils.isEmpty(columnName)) {
                primaryKeys.add(columnName);
            }
            return primaryKeys;
        });

        return primaryKeys;
    }
}
