/*
 *
 * (C) Copyright 2017 Ymatou (http://www.ymatou.com/). All rights reserved.
 *
 */

package com.ymatou.mq.infrastructure.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ymatou.mq.infrastructure.model.AppConfig;
import com.ymatou.mq.infrastructure.model.CallbackConfig;
import com.ymatou.mq.infrastructure.model.QueueConfig;
import com.ymatou.mq.infrastructure.repository.AppConfigRepository;
import com.ymatou.mq.infrastructure.support.ConfigReloadListener;
import com.ymatou.mq.infrastructure.support.ScheduledExecutorHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ymatou.mq.infrastructure.support.SemaphorManager.initSemaphores;

/**
 * 配置缓存 重新设置缓存，找出需要删除的等
 * 
 * @author luoshiqian 2017/2/21 14:05
 */
@Component
public class MessageConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConfigService.class);

    @Autowired
    private AppConfigRepository appConfigRepository;

    // app配置map缓存 key为 appId
    public static Map<String, AppConfig> appConfigMap = Maps.newConcurrentMap();

    // 回调配置map缓存 key为 callbackKey
    public static Map<String, CallbackConfig> callbackConfigMap = Maps.newConcurrentMap();

    // 配置删除的回调配置
    public static List<CallbackConfig> needRemoveCallBackConfigList = Lists.newArrayList();

    private List<ConfigReloadListener> configReloadListeners = Lists.newArrayList();

    @Order(1)
    @PostConstruct
    public void initConfig() throws Exception {
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        if(allAppConfig != null && !allAppConfig.isEmpty()){
            resetConfig(allAppConfig);
        }
        ScheduledExecutorHelper.newSingleThreadScheduledExecutor("reload-config-cache").scheduleAtFixedRate(
                () -> {
                    try {
                        List<AppConfig> appConfigList = appConfigRepository.getAllAppConfig();
                        resetConfig(appConfigList);
                        //回调通知
                        configReloadListeners.stream().forEach(ConfigReloadListener::callback);

                        LOGGER.info("reload-config-cache and callback success!");
                    } catch (Exception e) {
                        // 所有异常都catch到 防止异常导致定时任务停止
                        LOGGER.error("reload-config-cache and callback error ", e);
                    }
                }, 60, 60L * 1000, TimeUnit.MILLISECONDS);


        // 初始化信号量
        initSemaphores(callbackConfigMap.values());

        //reload 信号量
        this.addConfigCacheListener(() -> initSemaphores(callbackConfigMap.values()));
    }

    /**
     * reset config
     * 
     * @param appConfigs
     */
    private void resetConfig(List<AppConfig> appConfigs) {

        if (!appConfigMap.isEmpty()) {
            // 重新设置
            List<CallbackConfig> callbackConfigNewList = getCallbackConfigsFromApps(appConfigs);
            needRemoveCallBackConfigList = callbackConfigMap.values().stream()
                    .filter(callbackConfig -> !callbackConfigNewList.contains(callbackConfig))
                    .collect(Collectors.toList());
        }
        appConfigMap = listToMap(appConfigs, AppConfig::getAppId);
        callbackConfigMap = listToMap(getCallbackConfigsFromApps(appConfigs), CallbackConfig::getCallbackKey);
    }

    /**
     * 将list转为map工具
     * @param list
     * @param function
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> Map<K, V> listToMap(List<V> list, Function<V, K> function) {
        Map<K, V> map = Maps.newConcurrentMap();
        forEachList(list, v -> {
            K k = function.apply(v);
            if (k != null) {
                map.put(k, v);
            }
        });
        return map;
    }

    <T> void forEachList(List<T> list, Consumer<T> consumer) {
        if (!list.isEmpty()) {
            list.forEach(t -> consumer.accept(t));
        }
    }

    /**
     * 增加配置监听器，配置刷新 就通知
     * @param listener
     */
    public void addConfigCacheListener(ConfigReloadListener listener){
        if(listener != null){
            configReloadListeners.add(listener);
        }
    }

    /**
     * 获取所有的事app configs
     * @return
     */
    public List<AppConfig> getAllAppConfig(){
        List<AppConfig> allAppConfig = appConfigRepository.getAllAppConfig();
        for(AppConfig appConfig: allAppConfig){
            for(QueueConfig queueConfig :appConfig.getMessageCfgList()){
                queueConfig.setAppConfig(appConfig);
                for(CallbackConfig callbackConfig:queueConfig.getCallbackCfgList()){
                    callbackConfig.setQueueConfig(queueConfig);
                }
            }
        }
        return allAppConfig;
    }

    /**
     * appid获取appconfig
     * @param appId
     * @return
     */
    public AppConfig getAppConfig(String appId) {
        if(StringUtils.isBlank(appId)){
            return null;
        }
        return appConfigMap.get(appId);
    }

    /**
     * 获取队列配置
     * @param appId
     * @param queueCode
     * @return
     */
    public QueueConfig getQueueConfig(String appId, String queueCode){
        AppConfig appConfig = this.getAppConfig(appId);
        if(appConfig == null){
            return null;
        }
        return appConfig.getMessageConfig(queueCode);
    }

    /**
     * 从appconfigs中获取 callbackConfig list
     * @param appConfigs
     * @return
     */
    public static List<CallbackConfig> getCallbackConfigsFromApps(List<AppConfig> appConfigs) {
        List<CallbackConfig> callbackConfigs = Lists.newArrayList();
        for(AppConfig appConfig: appConfigs){
            for(QueueConfig queueConfig :appConfig.getMessageCfgList()){
                queueConfig.setAppConfig(appConfig);
                for(CallbackConfig callbackConfig:queueConfig.getCallbackCfgList()){
                    callbackConfig.setQueueConfig(queueConfig);
                    callbackConfigs.add(callbackConfig);
                }
            }
        }
        return callbackConfigs;
    }

    /**
     * 根据appId，queueCode获取订阅者配置列表
     * @param appId
     * @param queueCode
     * @return
     */
    public List<CallbackConfig> getCallbackConfigList(String appId, String queueCode){
        QueueConfig queueConfig = this.getQueueConfig(appId,queueCode);
        if(queueConfig == null){
            return new ArrayList<CallbackConfig>();
        }
        List<CallbackConfig> callbackConfigList = queueConfig.getCallbackCfgList();
        //set queueConfig
        for(CallbackConfig callbackConfig:callbackConfigList){
            callbackConfig.setQueueConfig(queueConfig);
        }
        return callbackConfigList;
    }

    /**
     * 查找callback config
     * @param callbackKey
     * @return
     */
    public CallbackConfig getCallbackConfig(String callbackKey){
        return callbackConfigMap.get(callbackKey);
    }

    /**
     * 获取callback config map
     * @return
     */
    public Map<String, CallbackConfig> getCallbackConfigMap(){
        return callbackConfigMap;
    }
}
