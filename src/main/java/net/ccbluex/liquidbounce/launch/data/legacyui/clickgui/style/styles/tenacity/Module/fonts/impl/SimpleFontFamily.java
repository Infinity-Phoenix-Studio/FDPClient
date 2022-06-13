/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.impl;

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontFamily;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontRenderer;
import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.style.styles.tenacity.Module.fonts.api.FontType;

final class SimpleFontFamily implements FontFamily {

	private final FontType fontType;
	private final java.awt.Font awtFont;

	private SimpleFontFamily(FontType fontType, java.awt.Font awtFont) {
		this.fontType = fontType;
		this.awtFont = awtFont;
	}

	static FontFamily create(FontType fontType, java.awt.Font awtFont) {
		return new SimpleFontFamily(fontType, awtFont);
	}

	@Override
	public FontRenderer ofSize(int size) {
			return SimpleFontRenderer.create(awtFont.deriveFont(java.awt.Font.PLAIN, size));
	}

	@Override
	public FontType font() { return fontType; }
}