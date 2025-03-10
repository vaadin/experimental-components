package org.vaadin.components.experimental.chat;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.components.experimental.chat.FlowAiChatService.AttachmentFile;
import org.vaadin.components.experimental.markdown.Markdown;

/**
 * A chat component that integrates with AI chat services. This component handles user messages,
 * file attachments, and streaming responses.
 */
@NpmPackage(value = "dropzone", version = "6.0.0-beta.2")
@NpmPackage(value = "@types/dropzone", version = "5.7.9")
@JsModule("./chat/ChatElement.tsx")
@Tag("chat-element")
@Uses(Markdown.class)
public class Chat extends ReactAdapterComponent implements HasSize, BeforeEnterObserver {

  private final List<AttachmentFile> attachments = new ArrayList<>();
  private final FlowAiChatService service;
  private String chatId;

  public Chat(FlowAiChatService service, String chatId, String acceptedFiles) {
    this(service, chatId);
    setAcceptedFiles(acceptedFiles);
  }

  public Chat(FlowAiChatService service, String chatId) {
    super();
    this.service = service;

    setChatId(chatId);

    var target =
        new StreamReceiver(getElement().getNode(), "chat-upload", new AttachmentStreamVariable());
    getElement().setAttribute("target", target);

    // The component needs to poll while streaming a response
    getElement().addEventListener("poll", event -> {});

    getElement().addEventListener("stream", this::onStreamEvent).addEventData("event.detail");

    getElement()
        .addEventListener("removeAttachment", this::onRemoveAttachmentEvent)
        .addEventData("event.detail");
  }

  /**
   * Handles the stream event triggered when a user sends a message. Streams the response from the
   * AI service back to the client.
   *
   * @param event The DOM event containing the user message
   */
  private void onStreamEvent(DomEvent event) {
    var userMessage = event.getEventData().getString("event.detail");
    var flux = service.stream(chatId, userMessage, attachments);
    attachments.clear();

    flux.subscribe(
        token -> getUI().get().access(() -> getElement().callJsFunction("updateStream", token)),
        error ->
            getUI()
                .get()
                .access(() -> getElement().callJsFunction("handleError", error.getMessage())),
        () -> getUI().get().access(() -> getElement().callJsFunction("handleComplete")));
  }

  /**
   * Handles the removal of an attachment.
   *
   * @param event The DOM event containing the attachment ID to remove
   */
  private void onRemoveAttachmentEvent(DomEvent event) {
    var attachmentId = event.getEventData().getString("event.detail");
    attachments.removeIf(attachment -> attachment.fileName().equals(attachmentId));
  }

  private void updateHistory() {
    var history = service.getHistory(chatId);
    getElement().setPropertyList("history", history);
  }

  /**
   * Gets the accepted attachment file types.
   *
   * @return the accepted file types
   */
  public String getAcceptedFiles() {
    return getElement().getProperty("acceptedFiles");
  }

  /**
   * Sets the accepted attachment file types.
   *
   * @param acceptedFiles the accepted file types (e.g., "image/*")
   */
  public void setAcceptedFiles(String acceptedFiles) {
    getElement().setProperty("acceptedFiles", acceptedFiles);
  }

  /**
   * Gets the chat ID.
   *
   * @return the chat ID
   */
  public String getChatId() {
    return chatId;
  }

  /**
   * Sets the chat ID.
   *
   * @param chatId the chat ID
   */
  public void setChatId(String chatId) {
    this.chatId = chatId;
    attachments.clear();

    updateHistory();
    getElement().setProperty("chatId", chatId);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    if (event.isRefreshEvent()) {
      updateHistory();
    }
  }

  private class AttachmentStreamVariable implements StreamVariable {

    private ByteArrayOutputStream outputStream;

    @Override
    public OutputStream getOutputStream() {
      this.outputStream = new ByteArrayOutputStream();
      return this.outputStream;
    }

    @Override
    public boolean listenProgress() {
      return false;
    }

    @Override
    public void onProgress(StreamingProgressEvent event) {
      // Ignore
    }

    @Override
    public void streamingStarted(StreamingStartEvent event) {
      // Ignore
    }

    @Override
    public void streamingFinished(StreamingEndEvent event) {
      var byteArray = outputStream.toByteArray();
      var file = new AttachmentFile(event.getFileName(), event.getMimeType(), byteArray);
      attachments.add(file);
    }

    @Override
    public void streamingFailed(StreamingErrorEvent event) {
      System.err.println("Streaming failed: " + event.getException().getMessage());
    }

    @Override
    public boolean isInterrupted() {
      return false;
    }
  }
}
