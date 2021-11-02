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

    @Override
    public void connect() throws IOException {
        String url = this.url.toString();
        String username = url.contains("/MinecraftCloaks/")
                ? url.substring(url.indexOf("/MinecraftCloaks/"))
                .replace("/MinecraftCloaks/", "")
                .replace(".png", "")
                : url.substring(url.indexOf("/cloak/get.jsp?user="))
                .replace("/cloak/get.jsp?user=", "");

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
