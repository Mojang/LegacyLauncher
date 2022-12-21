package net.minecraft.launchwrapper.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.Map;
import java.net.Proxy;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.Agent;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

public class SkinURLConnection extends HttpURLConnection {

    public final static String[] OLD_SKIN_ADDRESSES = new String[] {
            "http://www.minecraft.net/skin/",               // Introduced Classic 0.0.18a (when skins were added)
            "http://s3.amazonaws.com/MinecraftSkins/",      // Introduced Beta 1.2
            "http://skins.minecraft.net/MinecraftSkins/"    // Introduced Release 1.3.1
    };

    public SkinURLConnection(URL url) {
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
        for (String oldSkinAddress : OLD_SKIN_ADDRESSES) {
            username = username.replace(oldSkinAddress, "");
        }
        /// ... and dropping the .png.
        username = username.replace(".png", "");

        return username;
    }

    @Override
    public void connect() throws IOException {
        String username = getUsernameFromURL();

        try {
            MinecraftProfileTexture skin = getUserSkin(username);
            boolean slim = "slim".equals(skin.getMetadata("model"));
            inputStream = convertModernSkin(new URL(skin.getUrl()), slim);
        } catch (Exception ex) {
            responseCode = 404;
        }
    }

    GameProfile gameProfile = null;
    YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, (String)null);

    private MinecraftProfileTexture getUserSkin(String username) {
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

        if (textures.containsKey(MinecraftProfileTexture.Type.SKIN))
            return textures.get(MinecraftProfileTexture.Type.SKIN);

        return null;
    }

    public static InputStream convertModernSkin(URL skinUrl, boolean slim) throws IOException {
        InputStream inputStream = skinUrl.openStream();
        BufferedImage skin = ImageIO.read(inputStream);
        boolean tall = skin.getHeight() > 32;
        BufferedImage movePart = null;
        Graphics2D graphics = skin.createGraphics();
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        graphics.setComposite(alpha);

        if (tall) {
            // Flatten second layers.
            movePart = skin.getSubimage(0, 32, 56, 16);
            graphics.drawImage(movePart, 0, 16, null);
        }

        if (slim) {
            // Convert alex to steve.

            // Stretch right arm.
            movePart = skin.getSubimage(45, 16, 9, 16);
            graphics.drawImage(movePart, 46, 16, null);
            movePart = skin.getSubimage(49, 16, 2, 4);
            graphics.drawImage(movePart, 50, 16, null);
            movePart = skin.getSubimage(53, 20, 2, 12);
            graphics.drawImage(movePart, 54, 20, null);
        }

        graphics.dispose();

        // Crop
        BufferedImage croppedSkin = skin.getSubimage(0, 0, 64, 32);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(croppedSkin, "png", os);
        byte[] bytes = os.toByteArray();
        return new ByteArrayInputStream(bytes);
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
