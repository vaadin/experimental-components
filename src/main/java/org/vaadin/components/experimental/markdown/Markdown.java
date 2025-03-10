package org.vaadin.components.experimental.markdown;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.react.ReactAdapterComponent;

/**
 * A component for rendering Markdown content with syntax highlighting. Uses react-markdown and
 * rehype-highlight under the hood.
 */
@NpmPackage(value = "react-markdown", version = "9.0.3")
@NpmPackage(value = "rehype-highlight", version = "7.0.2")
@JsModule("./markdown/MarkdownElement.tsx")
@Tag("markdown-element")
public class Markdown extends ReactAdapterComponent implements HasSize {

  /** Creates a new Markdown component with empty content. */
  public Markdown() {
    super();
    setContent("");
  }

  /**
   * Creates a new Markdown component with the given content.
   *
   * @param content the markdown content to render
   */
  public Markdown(String content) {
    super();
    setContent(content);
  }

  /**
   * Sets the markdown content to be rendered.
   *
   * @param content the markdown content
   */
  public void setContent(String content) {
    getElement().setProperty("content", content);
  }

  /**
   * Gets the current markdown content.
   *
   * @return the current markdown content
   */
  public String getContent() {
    return getElement().getProperty("content");
  }
}
