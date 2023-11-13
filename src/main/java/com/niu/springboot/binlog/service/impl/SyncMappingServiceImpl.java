package com.niu.springboot.binlog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.niu.springboot.binlog.domain.po.TSyncMapping;
import com.niu.springboot.binlog.mapper.SyncMappingMapper;
import com.niu.springboot.binlog.service.SyncMappingService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author genlot
 **/
@Service
@Slf4j
public class SyncMappingServiceImpl extends ServiceImpl<SyncMappingMapper, TSyncMapping> implements SyncMappingService, BeanPostProcessor {

    /**
     * 允许收集的数据库
     */
    private static final Set<String> ALLOW_COLLECTION_SCHEMAS = Sets.newHashSet();
    /**
     * 允许收集的数据库表
     */
    private static final Set<String> ALLOW_COLLECTION_TABLES = Sets.newHashSet();

    /**
     * 本地缓存映射
     */
    private final LoadingCache<String, List<TSyncMapping>> syncMappingCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, List<TSyncMapping>>() {
                @Override
                public List<TSyncMapping> load(String key) {
                    String originDataBase = key.split(":")[0];
                    String originTableName = key.split(":")[1];
                    return baseMapper.selectList(Wrappers.lambdaQuery(TSyncMapping.class)
                            .eq(TSyncMapping::getOriginDb, originDataBase)
                            .eq(TSyncMapping::getOriginTableName, originTableName));
                }
            });

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        List<TSyncMapping> list = this.list();
        for (TSyncMapping tSyncMapping : list) {
            ALLOW_COLLECTION_SCHEMAS.add(tSyncMapping.getOriginDb());
            ALLOW_COLLECTION_TABLES.add(tSyncMapping.getOriginTableName());
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public Set<String> getAllowSchemeList() {
        if (CollectionUtils.isEmpty(ALLOW_COLLECTION_SCHEMAS)) {
            log.warn("未配置允许收集的数据库");
        }
        return ALLOW_COLLECTION_SCHEMAS;
    }

    @Override
    public Set<String> getAllowTableList() {
        if (CollectionUtils.isEmpty(ALLOW_COLLECTION_SCHEMAS)) {
            log.warn("未配置允许收集的数据库表");
        }
        return ALLOW_COLLECTION_TABLES;
    }

    @Override
    public List<TSyncMapping> listBySchemeAndTableName(String originDataBase, String originTableName) {
        try {
            return syncMappingCache.get(originDataBase + ":" + originTableName);
        } catch (ExecutionException e) {
            log.warn("未知的数据库表: {}:{}", originDataBase, originTableName);
        }
        return Lists.emptyList();
    }
}

