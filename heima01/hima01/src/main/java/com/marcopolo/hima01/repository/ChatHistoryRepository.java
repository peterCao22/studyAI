package com.marcopolo.hima01.repository;

import java.util.List;

// 会话历史
public interface ChatHistoryRepository {

    List<String> getChatIds(String type);

    void save(String type, String chatId);
}
