package org.vaadin.components.experimental.chat;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoIcon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import reactor.core.publisher.Flux;

@Route("chat")
@RouteAlias("")
public class ChatView extends VerticalLayout implements FlowAiChatService {

  // Store chat history for demo purposes
  private final Map<String, List<Message>> chatHistory = new HashMap<>();

  public ChatView() {
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    var initialChatId = UUID.randomUUID().toString();

    // Add some initial messages for demonstration
    initializeDummyChat(initialChatId);

    // Create a chat component with this view as the service
    var chat = new Chat(this, initialChatId, "image/*");

    var newChatButton = new Button(LumoIcon.PLUS.create());
    newChatButton.addThemeVariants(
        ButtonVariant.LUMO_ICON,
        ButtonVariant.LUMO_SMALL,
        ButtonVariant.LUMO_CONTRAST,
        ButtonVariant.LUMO_TERTIARY);
    newChatButton.addClickListener(
        e -> {
          chat.setChatId(UUID.randomUUID().toString());
        });

    // Add the chat component to the view
    add(newChatButton, chat);
  }

  /** Initialize a chat with some dummy messages */
  private void initializeDummyChat(String chatId) {
    List<Message> messages = new ArrayList<>();

    // Add some initial messages
    messages.add(new Message("user", "Hello! Can you help me with a question?", null));
    messages.add(
        new Message("assistant", "Of course! I'm here to help. What's your question?", null));
    messages.add(new Message("user", "How do I use this chat component?", null));
    messages.add(
        new Message(
            "assistant",
            "Using this chat component is easy! You can:\n\n"
                + "1. Type messages in the input field\n"
                + "2. Press Enter or click the send button to send\n"
                + "3. Upload attachments if enabled\n\n"
                + "The component supports markdown formatting, so you can use **bold**, *italic*, `code`, etc.",
            null));

    chatHistory.put(chatId, messages);
  }

  @Override
  public Flux<String> stream(String chatId, String userMessage, List<AttachmentFile> attachments) {
    // Store the user message
    if (!chatHistory.containsKey(chatId)) {
      chatHistory.put(chatId, new ArrayList<>());
    }

    chatHistory.get(chatId).add(new Message("user", userMessage, null));

    // Generate a dummy response
    String response = generateDummyResponse(userMessage);

    // Store the assistant message
    chatHistory.get(chatId).add(new Message("assistant", response, null));

    // Return the response as a stream, character by character with delays
    // This simulates a streaming response from an AI service
    return Flux.fromArray(response.split("")).delayElements(java.time.Duration.ofMillis(10));
  }

  /** Generate a dummy response based on the user message */
  private String generateDummyResponse(String userMessage) {
    String lowercaseMessage = userMessage.toLowerCase();

    if (lowercaseMessage.contains("hello") || lowercaseMessage.contains("hi")) {
      return "Hello there! How can I assist you today?";
    } else if (lowercaseMessage.contains("help")) {
      return "I'd be happy to help! What do you need assistance with?";
    } else if (lowercaseMessage.contains("thank")) {
      return "You're welcome! Is there anything else you'd like to know?";
    } else if (lowercaseMessage.contains("feature") || lowercaseMessage.contains("function")) {
      return "This chat component has several features:\n\n"
          + "- Real-time message streaming\n"
          + "- Markdown support\n"
          + "- File attachments\n"
          + "- Customizable styling\n\n"
          + "What would you like to know more about?";
    } else {
      return "I understand you're asking about \""
          + userMessage
          + "\". "
          + "This is a demo chat component, so I'm providing a generic response. "
          + "In a real implementation, this would be connected to an actual AI service "
          + "that could provide more meaningful responses.";
    }
  }

  @Override
  public List<Message> getHistory(String chatId) {
    // Return chat history or an empty list if none exists
    return chatHistory.getOrDefault(chatId, List.of());
  }

  @Override
  public void closeChat(String chatId) {
    // Clean up resources
    chatHistory.remove(chatId);
  }
}
