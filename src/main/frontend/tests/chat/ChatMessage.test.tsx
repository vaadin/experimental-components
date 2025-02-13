import React from "react";
import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import ChatMessage from "../../../resources/META-INF/resources/frontend/chat/ChatMessage.js";

describe("ChatMessage", () => {
  it("renders a message", () => {
    render(<ChatMessage message={{ role: "user", content: "Hello" }} />);

    expect(screen.getByText("Hello")).to.exist;
  });

  it("renders as a placeholder", async () => {
    render(
      <ChatMessage message={{ role: "user", content: "Hello" }} waiting />
    );

    expect(
      screen.getByLabelText("Message content").parentElement?.classList
    ).toContain("waiting-message");
  });

  it("renders an assistant message", () => {
    render(<ChatMessage message={{ role: "assistant", content: "Hi" }} />);

    expect(screen.getByText("Hi")).to.exist;
  });

  it("renders an image attachment", () => {
    render(
      <ChatMessage
        message={{
          role: "assistant",
          content: "Here's an image",
          attachments: [
            {
              type: "image",
              key: "1",
              fileName: "image.png",
              url: "image.png",
            },
          ],
        }}
      />
    );

    expect(screen.getByAltText("image.png")).to.exist;
  });

  it("renders a document attachment", () => {
    render(
      <ChatMessage
        message={{
          role: "assistant",
          content: "Here's a document",
          attachments: [
            {
              type: "document",
              key: "1",
              fileName: "document.pdf",
              url: "document.pdf",
            },
          ],
        }}
      />
    );

    expect(screen.getByText("document.pdf")).to.exist;
  });
});
