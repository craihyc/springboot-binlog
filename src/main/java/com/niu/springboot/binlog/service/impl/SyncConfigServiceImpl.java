package com.niu.springboot.binlog.service.impl;

import com.niu.springboot.binlog.domain.enums.SyncConfigEnum;
import com.niu.springboot.binlog.service.SyncConfigService;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

/**
 * 系统字典业务实现类
 *
 * @author genlot
 */
@Service
@AllArgsConstructor
@SuppressWarnings("all")
public class SyncConfigServiceImpl implements SyncConfigService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 更新SQL模板
     */
    private static final String UPDATE_SQL_TEMPLATE = "UPDATE `t_sync_config` SET `value` = ''{0}'' WHERE `key` = ''{1}''";

    /**
     * 查询SQL模板
     */
    private static final String SELECT_SQL_TEMPLATE = "SELECT `value` FROM `t_sync_config` WHERE `key` = ?";

    @Override
    public void updateByKey(SyncConfigEnum key, String val) {
        String sql = MessageFormat.format(UPDATE_SQL_TEMPLATE, val, key.getKey());
        jdbcTemplate.update(sql);
    }

    @Override
    public String getValByKey(SyncConfigEnum key) {
        String sql = MessageFormat.format(SELECT_SQL_TEMPLATE, key.getKey());
        List<String> res = jdbcTemplate.query(sql,
                new String[]{key.getKey()},
                (rs, i) -> rs.getString("value"));
        return res.get(0);
    }
}
