package org.vaadin.components.experimental.chat;

import jakarta.annotation.Nullable;
import java.util.List;
import reactor.core.publisher.Flux;

public interface FlowAiChatService {

  record AttachmentFile(String fileName, String contentType, byte[] data) {}

  record Attachment(String type, String fileName, String url) {}

  record Message(String role, String content, @Nullable List<Attachment> attachments) {}

  Flux<String> stream(String chatId, String userMessage, List<AttachmentFile> attachments);

  List<Message> getHistory(String chatId);

  void closeChat(String chatId);
}
