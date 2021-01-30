package org.jpires.dominoes.game.browser.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jpires.dominoes.lib.utils.MessageType;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Web Message represents a message to be trade between web application and web browser.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class WebMessage {

    /**
     * The type of message, representing by {@link MessageType}
     */
    @JsonProperty
    private MessageType type;

    /**
     * The content of the message.
     * Since the message needs to be fully adaptable, the content is a map of string,object.
     */
    @JsonProperty
    private Map<String, Object> content;

    /**
     * Default empty constructor to allow json deserialization.
     */
    public WebMessage() {
    }

    /**
     * Constructs a message with only the type.
     * In this case, the content will be empty.
     *
     * @param type the type of message
     */
    public WebMessage(final MessageType type) {
        this.type = type;
        this.content = Collections.emptyMap();
    }

    /**
     * Constructs a message with type and content.
     *
     * @param type    the type of message
     * @param content the content of message
     */
    public WebMessage(final MessageType type, final Map<String, Object> content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Gets the type of the message.
     *
     * @return the type of message
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Gets the content of the message.
     *
     * @return the content of the message
     */
    public Map<String, Object> getContent() {
        return Collections.unmodifiableMap(content);
    }

    /**
     * To string method, to be easier to append in logs.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "WebMessage{" +
                "type=" + type +
                ", content=" + content +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebMessage message = (WebMessage) o;
        return type == message.type &&
                Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }
}
