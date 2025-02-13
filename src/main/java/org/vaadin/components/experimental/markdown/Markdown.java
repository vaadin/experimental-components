package org.vaadin.components.experimental.markdown;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.NpmPackage;

@NpmPackage(value = "react-markdown", version = "9.0.3")
@NpmPackage(value= "rehype-highlight", version ="7.0.2")
// @JsModule("./markdown/MarkdownAdapter.tsx")
@Tag("markdown-element")
public class Markdown extends Component {
    
}
