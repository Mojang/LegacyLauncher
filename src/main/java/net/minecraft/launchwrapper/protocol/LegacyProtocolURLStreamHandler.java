package net.minecraft.launchwrapper.protocol;

import java.io.IOException;
import java.net.URL;
import java.net.Proxy;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class LegacyProtocolURLStreamHandler extends URLStreamHandler {
    private final Class<? extends URLConnection> defaultHttpConnectionClass;

    public LegacyProtocolURLStreamHandler(Class<? extends URLConnection> _defaultHttpConnectionClass) {
        defaultHttpConnectionClass = _defaultHttpConnectionClass;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        // Skins are pulled from the new endpoint and converted to the legacy format as required.
        for (String oldSkinAddress : SkinURLConnection.OLD_SKIN_ADDRESSES) {
            if (url.toString().startsWith(oldSkinAddress))
                return new SkinURLConnection(url);
        }

        // Capes are pulled from the new endpoint, no conversion is required.
        for (String oldCapeAddress : CapeURLConnection.OLD_CAPE_ADDRESSES) {
            if (url.toString().startsWith(oldCapeAddress))
                return new CapeURLConnection(url);
        }

        // Server authentication is done over a newer endpoint.
        if (url.toString().startsWith("http://www.minecraft.net/game/joinserver.jsp")) {
            return new JoinServerURLConnection(url);
        }

        try {
            return defaultHttpConnectionClass.getConstructor(URL.class, Proxy.class).newInstance(url, Proxy.NO_PROXY);
        } catch (Exception e) {
            // If the constructor isn't found, you can log that out. It's not expected.
            return null;
        }
    }
}
