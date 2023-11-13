package com.niu.springboot.autoconfig.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义 Mysql 配置类
 *
 * @author genlot
 */
@ConfigurationProperties(prefix = "mysql-binlog-connect-java.datasource")
@Data
public class MySqlBinlogConnectJavaProperties {

    /**
     * 数据库IP
     */
    private String hostname;

    /**
     * 数据库名称
     */
    private String schema;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
