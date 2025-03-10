package org.vaadin.components.experimental.chat;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.vaadin.components.experimental.chat.FlowAiChatService.AttachmentFile;
import org.vaadin.components.experimental.markdown.Markdown;

@NpmPackage(value = "dropzone", version = "6.0.0-beta.2")
@NpmPackage(value = "@types/dropzone", version = "5.7.9")
@JsModule("./chat/ChatElement.tsx")
@Tag("chat-element")
@Uses(Markdown.class)
public class Chat extends ReactAdapterComponent implements HasSize, StreamVariable {

  private String chatId;
  private ByteArrayOutputStream outputStream;
  private List<AttachmentFile> attachments = new ArrayList<>();

  public Chat(FlowAiChatService service) {
    super();
    getElement().getStyle().setFlexGrow("1");

    var target = new StreamReceiver(getElement().getNode(), "chat-upload", this);
    getElement().setAttribute("target", target);

    // The component needs to poll while streaming a response
    getElement().addEventListener("poll", event -> {});

    getElement()
        .addEventListener(
            "stream",
            event -> {
              var userMessage = event.getEventData().getString("event.detail");
              var flux = service.stream(chatId, userMessage, attachments);
              attachments.clear();

              flux.subscribe(
                  token ->
                      getUI()
                          .get()
                          .access(() -> getElement().callJsFunction("updateStream", token)),
                  error ->
                      getUI()
                          .get()
                          .access(
                              () -> getElement().callJsFunction("handleError", error.getMessage())),
                  () -> getUI().get().access(() -> getElement().callJsFunction("handleComplete")));
            })
        .addEventData("event.detail");

    getElement()
        .addEventListener(
            "getHistory",
            event -> {
              var history = service.getHistory(chatId);
              getElement().setPropertyList("history", history);
            });
    ;

    getElement()
        .addEventListener(
            "removeAttachment",
            event -> {
              var attachmentId = event.getEventData().getString("event.detail");
              attachments.removeIf(attachment -> attachment.fileName().equals(attachmentId));
            })
        .addEventData("event.detail");
  }

  /**
   * Sets the accepted file types for attachments.
   *
   * @param acceptedFiles MIME types to accept, e.g. "image/*"
   * @return this instance for method chaining
   */
  public Chat withAcceptedFiles(String acceptedFiles) {
    getElement().setProperty("acceptedFiles", acceptedFiles);
    return this;
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
   * @param chatId the chat ID to set
   * @return this instance for method chaining
   */
  public Chat withChatId(String chatId) {
    this.chatId = chatId;
    return this;
  }

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
