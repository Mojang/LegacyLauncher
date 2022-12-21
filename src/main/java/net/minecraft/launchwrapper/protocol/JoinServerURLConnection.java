package net.minecraft.launchwrapper.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Proxy;
import java.util.Map;
import java.util.HashMap;

import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

public class JoinServerURLConnection extends HttpURLConnection {
    public JoinServerURLConnection(URL url) {
        super(url);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    private String response = "bad login";

    @Override
    public void connect() throws IOException {

    }

    YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, (String)null);

    GameProfile gameProfile = null;

    @Override
    public InputStream getInputStream() throws IOException {
        // Pull params from the URL query.
        String[] params = this.url.getQuery().split("&");
        Map<String, String> queryMap = new HashMap<String, String>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            queryMap.put(name, value);
        }

        String username = queryMap.get("user");
        // sessionId is token:<accessToken>:<playerId>. We want the access token.
        String accessToken = queryMap.get("sessionId").split(":")[1];
        String serverId = queryMap.get("serverId");

        // Lookup the game profile by username.
        authenticationService.createProfileRepository().findProfilesByNames(new String[] { username }, Agent.MINECRAFT, new ProfileLookupCallback() {
            public void onProfileLookupSucceeded(GameProfile paramGameProfile) {
                gameProfile = paramGameProfile;
            }
            public void onProfileLookupFailed(GameProfile paramGameProfile, Exception paramException) {
            }
        });

        if (gameProfile == null)
            return null;

        // Send the join request.
        try {
            authenticationService.createMinecraftSessionService().joinServer(
                    gameProfile,
                    accessToken,
                    serverId
            );

            response = "ok";
        } catch (AuthenticationException ex) {
            // response defaults to "bad login"
        }

        return new ByteArrayInputStream(response.getBytes());
    }

    @Override
    public int getResponseCode() {
        return 200;
    }

    @Override
    public String getResponseMessage() {
        return "ok";
    }
}