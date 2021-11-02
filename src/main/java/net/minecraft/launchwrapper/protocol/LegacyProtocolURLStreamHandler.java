package net.minecraft.launchwrapper.protocol;

import sun.net.www.protocol.http.HttpURLConnection;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class LegacyProtocolURLStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        // Skins are pulled from the new endpoint and converted to the legacy format as required.
        if (url.toString().startsWith("http://s3.amazonaws.com/MinecraftSkins/") || url.toString().contains("/skin/"))
            return new SkinURLConnection(url);
        // Capes are pulled from the new endpoint, no conversion is required.
        else if (url.toString().startsWith("http://s3.amazonaws.com//MinecraftCloaks//") || url.toString().contains("/cloak/get.jsp?user="))
            return new CapeURLConnection(url);

        return new HttpURLConnection(url, null);
    }
}
