/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.ui.controller;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.common.LookData;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.PlaySoundBrick;
import org.catrobat.catroid.content.bricks.SetLookBrick;
import org.catrobat.catroid.utils.Utils;

import java.util.List;

public final class BackPackSpriteController {
	private static final BackPackSpriteController INSTANCE = new BackPackSpriteController();

	private BackPackSpriteController() {
	}

	public static BackPackSpriteController getInstance() {
		return INSTANCE;
	}

	public Sprite backpack(Sprite spriteToEdit, boolean addToHiddenBackpack) {

		ProjectManager.getInstance().setCurrentSprite(spriteToEdit);

		Sprite backPackSprite = spriteToEdit.cloneForBackPack();

		String newSpriteName = Utils.getUniqueSpriteName(spriteToEdit);
		backPackSprite.setName(newSpriteName);
		backPackSprite.isBackpackLookData = true;

		for (LookData lookData : spriteToEdit.getLookDataList()) {
			if (!lookDataIsUsedInScript(lookData)) {
				backPackSprite.getLookDataList().add(LookController.getInstance().backPackLook(lookData, true));
			}
		}
		for (SoundInfo soundInfo : spriteToEdit.getSoundList()) {
			if (!soundInfoIsUsedInScript(soundInfo)) {
				backPackSprite.getSoundList().add(SoundController.getInstance().backPackSound(soundInfo, true));
			}
		}
		List<Script> backPackedScripts = BackPackScriptController.getInstance().backpack(spriteToEdit.getName(),
				spriteToEdit.getListWithAllBricks(), true);
		if (backPackedScripts != null && !backPackedScripts.isEmpty()) {
			backPackSprite.getScriptList().addAll(backPackedScripts);
		}
		if (addToHiddenBackpack) {
			BackPackListManager.getInstance().addSpriteToHiddenBackpack(backPackSprite);
		} else {
			BackPackListManager.getInstance().addSpriteToBackPack(backPackSprite);
		}
		return backPackSprite;
	}

	public Sprite unpack(Sprite selectedSprite, boolean delete, boolean keepCurrentSprite, boolean fromHiddenBackPack) {
		Sprite unpackedSprite = selectedSprite.cloneForBackPack();
		String newSpriteName = Utils.getUniqueSpriteName(selectedSprite);
		unpackedSprite.setName(newSpriteName);

		Sprite currentSprite = ProjectManager.getInstance().getCurrentSprite();

		ProjectManager.getInstance().setCurrentSprite(unpackedSprite);

		BackPackScriptController.getInstance().unpack(selectedSprite.getName(), delete, false, null, true);

		if (selectedSprite.getLookDataList() != null) {
			for (LookData lookData : selectedSprite.getLookDataList()) {
				if (lookData != null && !lookDataIsUsedInScript(lookData)) {
					LookController.getInstance().unpack(lookData, delete, true);
				}
			}
		}

		if (selectedSprite.getSoundList() != null) {
			for (SoundInfo soundInfo : selectedSprite.getSoundList()) {
				if (soundInfo != null && !soundInfoIsUsedInScript(soundInfo)) {
					SoundController.getInstance().unpack(soundInfo, delete, true);
				}
			}
		}

		ProjectManager.getInstance().addSprite(unpackedSprite);
		if (keepCurrentSprite) {
			ProjectManager.getInstance().setCurrentSprite(currentSprite);
		} else {
			ProjectManager.getInstance().setCurrentSprite(unpackedSprite);
		}

		if (delete) {
			if (fromHiddenBackPack) {
				BackPackListManager.getInstance().removeItemFromSpriteHiddenBackpack(selectedSprite);
			} else {
				BackPackListManager.getInstance().removeItemFromSpriteBackPack(selectedSprite);
			}
		}
		return unpackedSprite;
	}

	private boolean lookDataIsUsedInScript(LookData lookData) {
		for (Sprite sprite : ProjectManager.getInstance().getCurrentProject().getSpriteList()) {
			for (Brick brick : sprite.getListWithAllBricks()) {
				if (brick instanceof SetLookBrick && ((SetLookBrick) brick).getLook().equals(lookData)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean soundInfoIsUsedInScript(SoundInfo soundInfo) {
		for (Sprite sprite : ProjectManager.getInstance().getCurrentProject().getSpriteList()) {
			for (Brick brick : sprite.getListWithAllBricks()) {
				if (brick instanceof PlaySoundBrick && ((PlaySoundBrick) brick).getSound().equals(soundInfo)) {
					return true;
				}
			}
		}
		return false;
	}
}
