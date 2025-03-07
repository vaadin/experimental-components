package org.vaadin.components.experimental.chat;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.react.ReactAdapterComponent;
import com.vaadin.flow.server.StreamReceiver;
import com.vaadin.flow.server.StreamVariable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import org.vaadin.components.experimental.markdown.Markdown;

@NpmPackage(value = "dropzone", version = "6.0.0-beta.2")
@NpmPackage(value = "@types/dropzone", version = "5.7.9")
@JsModule("./chat/ChatElement.tsx")
@Tag("chat-element")
@Uses(Markdown.class)
public class Chat extends ReactAdapterComponent implements HasSize, StreamVariable {

  private String chatId;
  private ByteArrayOutputStream outputStream;
  private List<MultipartFile> attachments = new ArrayList<>();

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
              attachments.removeIf(
                  attachment -> attachment.getOriginalFilename().equals(attachmentId));
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
    var mediaName = UUID.randomUUID().toString();
    var file =
        new MockMultipartFile(mediaName, event.getFileName(), event.getMimeType(), byteArray);
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

  public class MockMultipartFile implements MultipartFile {

    private final String name;

    private final String originalFilename;

    @Nullable private final String contentType;

    private final byte[] content;

    /**
     * Create a new MockMultipartFile with the given content.
     *
     * @param name the name of the file
     * @param originalFilename the original filename (as on the client's machine)
     * @param contentType the content type (if known)
     * @param content the content of the file
     */
    public MockMultipartFile(
        String name,
        @Nullable String originalFilename,
        @Nullable String contentType,
        @Nullable byte[] content) {

      Assert.hasLength(name, "Name must not be empty");
      this.name = name;
      this.originalFilename = (originalFilename != null ? originalFilename : "");
      this.contentType = contentType;
      this.content = (content != null ? content : new byte[0]);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    @NonNull
    public String getOriginalFilename() {
      return this.originalFilename;
    }

    @Override
    @Nullable
    public String getContentType() {
      return this.contentType;
    }

    @Override
    public boolean isEmpty() {
      return (this.content.length == 0);
    }

    @Override
    public long getSize() {
      return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
      return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
      FileCopyUtils.copy(this.content, dest);
    }
  }
}
