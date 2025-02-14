package org.vaadin.components.experimental.chat;

import org.vaadin.components.experimental.markdown.Markdown;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;

@NpmPackage(value= "dropzone", version ="6.0.0-beta.2")
@NpmPackage(value= "@types/dropzone", version="5.7.9")
@NpmPackage(value= "webm-to-wav-converter", version="1.1.0")
// @JsModule("./chat/ChatAdapter.tsx")
@Tag("chat-element")
@Uses(Markdown.class)
public class Chat extends Component {
    
}
