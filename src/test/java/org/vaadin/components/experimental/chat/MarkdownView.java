package org.vaadin.components.experimental.chat;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.components.experimental.markdown.Markdown;

@Route("markdown")
public class MarkdownView extends VerticalLayout {

  public MarkdownView() {
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    var markdown = new Markdown();

    markdown.setContent(
        "# Markdown Example\n\n"
            + "This is a simple example of using the Markdown component.\n\n"
            + "## Features\n\n"
            + "- **Bold Text**\n"
            + "- *Italic Text*\n"
            + "- [Link](https://vaadin.com)\n");

    markdown.setSizeFull();

    add(markdown);
  }
}
