package org.jpires.dominoes.game.browser.server.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jpires.dominoes.lib.utils.Constants;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 * Web Message encoder to encode {@link WebMessage} into json string.
 *
 * @author Joao Pires (jppires91@gmail.com)
 */
public class WebMessageEncoder implements Encoder.Text<WebMessage> {

    /**
     * Encodes a certain message into json string.
     *
     * @param webMessage the web message
     * @return a string json representation of web message
     * @throws EncodeException in case of any error encoding the message
     */
    @Override
    public String encode(final WebMessage webMessage) throws EncodeException {
        try {
            return Constants.OBJECT_MAPPER.writeValueAsString(webMessage);
        } catch (final JsonProcessingException e) {
            throw new EncodeException(webMessage, "Error encoding object", e);
        }
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
