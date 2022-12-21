package net.minecraft.launchwrapper.protocol;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class LegacyProtocolURLStreamHandlerFactory implements URLStreamHandlerFactory {
    private final Class<? extends URLConnection> defaultHttpConnectionClass;

    public LegacyProtocolURLStreamHandlerFactory() {
        try {
            URL foo = new URL("http://example.com");
            // Doesn't actually establish a connection
            defaultHttpConnectionClass = foo.openConnection().getClass();
        } catch (Exception e) {
            // this should never happen as the URL is hardcoded, shouldn't be invalid.
            throw new RuntimeException(e);
        }
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("http".equals(protocol)) {
            return new LegacyProtocolURLStreamHandler(defaultHttpConnectionClass);
        }

        return null;
    }
}
