package net.opencraft.renderer;

import static net.opencraft.OpenCraft.*;
import static org.josl.openic.IC13.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Context;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opencraft.InputHandler;
import net.opencraft.OpenCraft;
import net.opencraft.renderer.screens.F3Screen;
import net.opencraft.renderer.screens.Screen;
import net.opencraft.renderer.texture.Texture;
import net.opencraft.spectoland.SpectoError;
import net.opencraft.util.Files;

/**
 * <h1>Render</h1><br>
 * This class is used for manage drawing process and screen control. It can also
 * determine the best fps configuration and guide the OpenGL usage.
 */
public final class Renderer {

	public static final GraphicsDevice DEF_GRAPHICS_DEVICE = GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();

	public static final GraphicsConfiguration GFX_CONFIG = DEF_GRAPHICS_DEVICE.getDefaultConfiguration();
	public static final DisplayMode GFX_DISPLAY_MODE = DEF_GRAPHICS_DEVICE.getDisplayMode();
	public static final Logger logger = LoggerFactory.getLogger(Renderer.class);

	/**
	 * Creates a new instance of this class.
	 */
	private Renderer() {
	}

	/**
	 * @return a new instance of this class.
	 */
	public static Renderer create() {
		return new Renderer();
	}

	/**
	 * Returns the preferred color model by your device. This possibly decrease the
	 * CPU using.
	 *
	 * @param image The original image
	 * @return The optimized image
	 */
	public static BufferedImage toCompatibleImage(final BufferedImage image) {
		if (image.getColorModel().equals(GFX_CONFIG.getColorModel())) {
			return image;
		}

		final BufferedImage new_image = GFX_CONFIG.createCompatibleImage(image.getWidth(), image.getHeight(),
				image.getTransparency());

		final Graphics2D g2d = (Graphics2D) new_image.getGraphics();

		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		return new_image;
	}

	public void init() {
		// Config display
		configDisplay();

		Context.create();
		InputHandler.bindMouse();

		// Show render details
		logger.info("Render system initialized!");
		SpectoError.info("Render System initialized!");
		logger.info("[OpenGL] Using OpenGL: " + (Context.usesOpenGL() ? "Yes" : "No"));
		if (Context.usesOpenGL())
			SpectoError.info("Using OpenGL!");
		
		// Do "VSync"
		if (!vsync()) {
			SpectoError.warn("Imposible to determinate the refresh rate!");
			logger.warn("[VSync] Imposible to determinate the refresh rate!");
		}

		// Show FPS Rate
		SpectoError.info("FPS Rate is set to " + oc.fpsCap);
		logger.info("FPS Rate: " + oc.fpsCap);
	}

	private void configDisplay() {
		Display.create(854, 480, OpenCraft.DISPLAY_NAME);
		Display.setResizable(false);

		List<String> iconsPath = new ArrayList<>();
		iconsPath.add("/resources/icons/icon_16x16.png");
		iconsPath.add("/resources/icons/icon_24x24.png");
		iconsPath.add("/resources/icons/icon_32x32.png");
		iconsPath.add("/resources/icons/icon_48x48.png");
		iconsPath.add("/resources/icons/icon_256x256.png");

		List<Texture> icons = iconsPath.stream()
				.map(path -> Files.external(path))
				.map(in -> Texture.getTexture("PNG", in))
				.collect(Collectors.toList());

		boolean someNull = icons.stream().anyMatch(tex -> tex.isNull());
		if (!someNull)
			Display.setIcons(icons.stream().map(tex -> (Image) tex.getImage()).collect(Collectors.toList()));
		
		SpectoError.info("Display icons loaded!");
		Display.show();
	}

	public boolean vsync() {
		int fpsRate;
		fpsRate = GFX_DISPLAY_MODE.getRefreshRate();

		if (fpsRate != DisplayMode.REFRESH_RATE_UNKNOWN) {
			oc.fpsCap = fpsRate;
			return true;
		}

		return false;
	}

	/**
	 * Renders the game.
	 */
	public void render() {
		boolean screenshot = icIsKeyPressed(KeyEvent.VK_F2);
		if (screenshot) {
			BufferedImage bi = new BufferedImage(Display.width(), Display.height(), BufferedImage.TYPE_INT_RGB);
			Screen.renderCurrent(bi.createGraphics());
			try {
				ImageIO.write(bi, "PNG", new FileOutputStream(new File(oc.directory, "screenshot.png")));
				F3Screen.setStatus("Taking screenshot...");
			} catch (Exception ex) {
				SpectoError.ignored(ex, getClass());
			}
		}

		if (!Context.shouldRender())
			return;

		Graphics2D g2d = (Graphics2D) Context.getGraphics();
		Screen.renderCurrent(g2d);

		if (icIsKeyPressed(KeyEvent.VK_F3))
			F3Screen.drawF3(g2d);

		g2d.dispose();

		Context.draw();
		Display.update();
	}	

}
