/*
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.fightcave;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImagePanelComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;


import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FightCaveOverlay extends Overlay
{
	private static final Color NOT_ACTIVATED_BACKGROUND_COLOR = new Color(150, 0, 0, 150);

	private final Client client;
	private final FightCavePlugin plugin;

	private BufferedImage protectFromMagicImg;
	private BufferedImage protectFromMissilesImg;

	@Inject
	FightCaveOverlay(Client client, FightCavePlugin plugin)
	{
		setPosition(OverlayPosition.BOTTOM_RIGHT);
		setPriority(OverlayPriority.HIGH);
		this.client = client;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		JadAttack attack = plugin.getAttack();
		if (attack == null)
		{
			int wave = plugin.getWave();
			if (wave == 0)
			{
				return null;
			}
			int[] enemies = plugin.getEnemies(wave);
			if (enemies.length > 0)
			{
				Map<Integer, Integer> numberOfEnemies = new HashMap<>();
				for (int i = 0; i < enemies.length; i++)
				{
					if (numberOfEnemies.containsKey(enemies [i]))
					{
						numberOfEnemies.put(enemies[i], numberOfEnemies.get(enemies[i]) + 1);
					}
					else
					{
						numberOfEnemies.put(enemies[i], 1);
					}
				}
				PanelComponent waveInfo = new PanelComponent();
				waveInfo.setTitle("Current Wave: " + Integer.toString(wave));
				List<PanelComponent.Line> lines = waveInfo.getLines();

				if (numberOfEnemies.containsKey(360))
				{
					lines.add(new PanelComponent.Line("360 Mage: ", Integer.toString(numberOfEnemies.get(360))));
				}
				if (numberOfEnemies.containsKey(180))
				{
					lines.add(new PanelComponent.Line("180 Melee: ", Integer.toString(numberOfEnemies.get(180))));
				}
				if (numberOfEnemies.containsKey(90))
				{
					lines.add(new PanelComponent.Line("90 Ranger: ", Integer.toString(numberOfEnemies.get(90))));
				}
				if (numberOfEnemies.containsKey(45))
				{
					lines.add(new PanelComponent.Line("45 Melee: ", Integer.toString(numberOfEnemies.get(45))));
				}
				if (numberOfEnemies.containsKey(22))
				{
					lines.add(new PanelComponent.Line("22 Melee: ", Integer.toString(numberOfEnemies.get(22))));
				}

				return waveInfo.render(graphics);
			}
			else
			{
				return null;
			}
		}
		else
		{
			BufferedImage prayerImage = getPrayerImage(attack);
			ImagePanelComponent imagePanelComponent = new ImagePanelComponent();
			imagePanelComponent.setTitle("TzTok-Jad");
			imagePanelComponent.getImages().add(prayerImage);
			if (!client.isPrayerActive(attack.getPrayer()))
			{
				imagePanelComponent.setBackgroundColor(NOT_ACTIVATED_BACKGROUND_COLOR);
			}
			return imagePanelComponent.render(graphics);
		}
	}

	private BufferedImage getPrayerImage(JadAttack attack)
	{
		return attack == JadAttack.MAGIC ? getProtectFromMagicImage() : getProtectFromMissilesImage();
	}

	private BufferedImage getProtectFromMagicImage()
	{
		if (protectFromMagicImg == null)
		{
			String path = "/prayers/protect_from_magic.png";
			protectFromMagicImg = getImage(path);
		}
		return protectFromMagicImg;
	}

	private BufferedImage getProtectFromMissilesImage()
	{
		if (protectFromMissilesImg == null)
		{
			String path = "/prayers/protect_from_missiles.png";
			protectFromMissilesImg = getImage(path);
		}
		return protectFromMissilesImg;
	}

	private BufferedImage getImage(String path)
	{
		BufferedImage image = null;
		try
		{
			synchronized (ImageIO.class)
			{
				image = ImageIO.read(FightCaveOverlay.class.getResourceAsStream(path));
			}
		}
		catch (IOException e)
		{
			log.warn("Error loading image", e);
		}
		return image;
	}
}
