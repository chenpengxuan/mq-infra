package com.ymatou.mq.infrastructure.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baidu.disconf.client.config.DisClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ymatou.mq.infrastructure.model.*;
import com.ymatou.mq.infrastructure.repository.MessageCompensateRepository;
import com.ymatou.mq.infrastructure.repository.MessageDispatchDetailRepository;
import com.ymatou.mq.infrastructure.repository.MessageRepository;
import com.ymatou.mq.infrastructure.support.enums.DispatchStatusEnum;
import org.springframework.util.CollectionUtils;

/**
 * 消息服务
 * Created by zhangzhihua on 2017/3/24.
 */
@Component("messageService")
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageConfigService messageConfigService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageDispatchDetailRepository messageDispatchDetailRepository;

    @Autowired
    private MessageCompensateRepository messageCompensateRepository;

    /**
     * 保存消息及分发明细
     *
     * @param msg
     */
    public boolean saveMessage(Message msg) {

        boolean success = messageRepository.save(msg);
        // 保存消息
        if (success) {
            // 获取消息分发明细列表
            List<MessageDispatchDetail> detailList = this.buildMessageDispatchDetailList(msg);
            // 保存分发明细列表
            if (detailList != null && detailList.size() > 0) {
                for (MessageDispatchDetail detail : detailList) {
                    if (!messageDispatchDetailRepository.saveDetail(detail)) {
                        // 有一条没保存成功 直接退出 等待下回再次保存
                        success = false;
                    }
                }
            }
        }

        logger.info("save Message to mongo success:{},message:{}",success,msg);

        return success;
    }

    /**
     * 根据配置构造所有要分发的明细列表
     * @param msg
     * @return
     */
    List<MessageDispatchDetail> buildMessageDispatchDetailList(Message msg){
        List<MessageDispatchDetail> detailList = new ArrayList<MessageDispatchDetail>();
        List<CallbackConfig> callbackConfigList =  messageConfigService.getCallbackConfigList(msg.getAppId(),msg.getQueueCode());
        if(!CollectionUtils.isEmpty(callbackConfigList)){
            for(CallbackConfig callbackConfig:callbackConfigList){
                //若未开启或不写日志，则跳过
                if(!callbackConfig.isReceiveEnable() || callbackConfig.getAbandonQueue()){
                    logger.debug("don't need to save cause config: {},env:{}",callbackConfig, DisClientConfig.getInstance().ENV);
                    continue;
                }
                MessageDispatchDetail detail = new MessageDispatchDetail();
                detail.setId(String.format("%s_%s",msg.getId(),callbackConfig.getCallbackKey()));
                detail.setMsgId(msg.getId());
                detail.setBizId(msg.getBizId());
                detail.setAppId(msg.getAppId());
                detail.setQueueCode(msg.getQueueCode());
                detail.setConsumerId(callbackConfig.getCallbackKey());
                detail.setStatus(DispatchStatusEnum.INIT.getCode());
                detail.setCreateTime(msg.getCreateTime() != null ? msg.getCreateTime() : new Date());
                detail.setUpdateTime(msg.getCreateTime() != null ? msg.getCreateTime() : new Date());
                detailList.add(detail);
            }
        }
        return detailList;
    }

    /**
     * 更新消息状态
     */
    public void updateDispatchDetail(CallbackResult callbackResult){
        MessageDispatchDetail detail = new MessageDispatchDetail();
        detail.setId(String.format("%s_%s",callbackResult.getMsgId(),callbackResult.getConsumerId()));
        detail.setAppId(callbackResult.getAppId());
        detail.setQueueCode(callbackResult.getQueueCode());
        detail.setConsumerId(callbackResult.getConsumerId());
        detail.setMsgId(callbackResult.getMsgId());
        detail.setBizId(callbackResult.getBizId());
        detail.setStatus(callbackResult.getStatus());
        detail.setLastFrom(callbackResult.getFrom());
        detail.setLastTime(callbackResult.getReqTime());
        detail.setLastResp(callbackResult.getResponse());
        detail.setCreateTime(callbackResult.getCreateTime()!=null?callbackResult.getCreateTime():new Date());
        detail.setUpdateTime(new Date());
        messageDispatchDetailRepository.updateDetail(detail);
    }


    /**
     * 插补单
     * @param messageCompensate
     */
    public void insertCompensate(MessageCompensate messageCompensate){
        messageCompensateRepository.saveCompensate(messageCompensate);
    }
}
