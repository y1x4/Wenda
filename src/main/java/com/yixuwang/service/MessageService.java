package com.yixuwang.service;

import com.yixuwang.dao.MessageDAO;
import com.yixuwang.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * Created by yixu on 2018/6/28.
 */
@Service
public class MessageService {

    @Autowired
    MessageDAO messageDAO;

    public int addMessage(Message message) {
        return messageDAO.addMessage(message);
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    public int getConvesationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }

    public int getConvesationCount(String conversationId) {
        return messageDAO.getConversationCount(conversationId);
    }

    public void setConvesationRead(String conversationId) {
        messageDAO.updateStatus(conversationId, 1);
    }
}
