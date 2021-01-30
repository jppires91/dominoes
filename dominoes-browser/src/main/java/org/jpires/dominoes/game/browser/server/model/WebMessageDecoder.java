package org.jpires.dominoes.game.browser.server.model;

import org.jpires.dominoes.lib.utils.Constants;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;

/**
 * WebMessage decoder, to decode string json messages.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class WebMessageDecoder implements Decoder.Text<WebMessage> {

    /**
     * Decodes a web message, from json string to {@link WebMessage}.
     *
     * @param s the json string
     * @return the decoded {@link WebMessage}
     * @throws DecodeException in case of any error decoding the exception
     */
    @Override
    public WebMessage decode(String s) throws DecodeException {
        try {
            return Constants.OBJECT_MAPPER.readValue(s, WebMessage.class);
        } catch (final IOException e) {
            throw new DecodeException(s, "Error decoding message", e);
        }
    }

    /**
     * Determine if the message can be decoded.
     *
     * @param s the message
     * @return true if the message can be decoded, false otherwise
     */
    @Override
    public boolean willDecode(final String s) {
        return s != null;
    }

    /**
     * Initializes the decoder with endpoint configuration.
     * This method doesn't do anything.
     *
     * @param endpointConfig the endpoint configuration
     */
    @Override
    public void init(final EndpointConfig endpointConfig) {
    }

    /**
     * Destroys the decoder.
     * This method doesn't do anything.
     */
    @Override
    public void destroy() {

    }
}
