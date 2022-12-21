package net.minecraft.launchwrapper.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.net.Proxy;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

public class CapeURLConnection extends HttpURLConnection {

    public final static String[] OLD_CAPE_ADDRESSES = new String[] {
            "http://www.minecraft.net/cloak/get.jsp?user=", // Introduced Beta 1.0 (when capes were added)
            "http://s3.amazonaws.com/MinecraftCloaks/",     // Introduced Beta 1.2
            "http://skins.minecraft.net/MinecraftCloaks/"   // Introduced Release 1.3.1
    };

    public CapeURLConnection(URL url) {
        super(url);
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    InputStream inputStream = null;
    int responseCode = 200;

    private String getUsernameFromURL() {
        String username = this.url.toString();

        // We get the username from the skin by replacing the url up to the username with whitespace.
        for (String oldCapeAddress : OLD_CAPE_ADDRESSES) {
            username = username.replace(oldCapeAddress, "");
        }
        /// ... and dropping the .png.
        username = username.replace(".png", "");

        return username;
    }

    @Override
    public void connect() throws IOException {
        String username = getUsernameFromURL();

        try {
            MinecraftProfileTexture cape = getUserCape(username);
            inputStream = new URL(cape.getUrl()).openConnection().getInputStream();
        } catch (Exception ex) {
            responseCode = 404;
        }
    }

    YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, (String)null);
    GameProfile gameProfile = null;

    private MinecraftProfileTexture getUserCape(String username) {
        authenticationService.createProfileRepository().findProfilesByNames(new String[] { username }, Agent.MINECRAFT, new ProfileLookupCallback() {
            public void onProfileLookupSucceeded(GameProfile paramGameProfile) {
                gameProfile = paramGameProfile;
            }
            public void onProfileLookupFailed(GameProfile paramGameProfile, Exception paramException) {
            }
        });

        if (gameProfile == null)
            return null;

        gameProfile = authenticationService.createMinecraftSessionService().fillProfileProperties(gameProfile, true);

        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = authenticationService.createMinecraftSessionService().getTextures(gameProfile, true);
        if (textures.containsKey(MinecraftProfileTexture.Type.CAPE))
            return textures.get(MinecraftProfileTexture.Type.CAPE);

        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }
}
