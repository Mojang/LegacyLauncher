package net.minecraft.launchwrapper.protocol;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class LegacyProtocolURLStreamHandlerFactory implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("http".equals(protocol)) {
            return new LegacyProtocolURLStreamHandler();
        }

        return null;
    }
}
