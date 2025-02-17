package org.vaadin.components.experimental.chat;

import jakarta.annotation.Nullable;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService<T> {

    record Attachment(
        String type,
        String key,
        String fileName,
        String url
    ) {}

    record Message(
        String role,
        String content,
        @Nullable List<Attachment> attachments
    ) {}

    Flux<String> stream(String chatId, String userMessage, @Nullable T options);

    String streamAudio(String chatId, MultipartFile file, @Nullable T options);

    String uploadAttachment(String chatId, MultipartFile file);

    void removeAttachment(String chatId, String attachmentId);

    List<Message> getHistory(String chatId);

    void closeChat(String chatId);
}
