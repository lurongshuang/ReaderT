/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.fbreader.options;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.fbreader.fbreader.DurationEnum;

/**
 * 其他设置
 */
public class MiscOptions {

    /**
     * 允许屏幕亮度调节（手势调节）
     */
    public final ZLBooleanOption AllowScreenBrightnessAdjustment;
    public final ZLStringOption TextSearchPattern;

    public final ZLBooleanOption EnableDoubleTap;
    public final ZLBooleanOption NavigateAllWords;

    public enum WordTappingActionEnum {
        doNothing, selectSingleWord, startSelecting, openDictionary
    }

    public final ZLEnumOption<WordTappingActionEnum> WordTappingAction;

    public final ZLIntegerRangeOption ToastFontSizePercent;

    public enum FootnoteToastEnum {
        never, footnotesOnly, footnotesAndSuperscripts, allInternalLinks
    }

    public final ZLEnumOption<FootnoteToastEnum> ShowFootnoteToast;
    public final ZLEnumOption<DurationEnum> FootnoteToastDuration;

    public MiscOptions() {
        AllowScreenBrightnessAdjustment =
                new ZLBooleanOption("LookNFeel", "AllowScreenBrightnessAdjustment", false);
        TextSearchPattern =
                new ZLStringOption("TextSearch", "Pattern", "");

        EnableDoubleTap =
                new ZLBooleanOption("Options", "EnableDoubleTap", false);
        NavigateAllWords =
                new ZLBooleanOption("Options", "NavigateAllWords", false);

        WordTappingAction =
                new ZLEnumOption<>("Options", "WordTappingAction", WordTappingActionEnum.startSelecting);

        ToastFontSizePercent =
                new ZLIntegerRangeOption("Options", "ToastFontSizePercent", 25, 100, 90);
        ShowFootnoteToast =
                new ZLEnumOption<>("Options", "ShowFootnoteToast", FootnoteToastEnum.footnotesAndSuperscripts);
        FootnoteToastDuration =
                new ZLEnumOption<>("Options", "FootnoteToastDuration", DurationEnum.duration5);
    }
}