/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizeBag;
import com.baidu.tts.client.SpeechSynthesizerListener;

import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.MenuNode;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.constant.PreviewConfig;
import org.geometerplus.android.fbreader.dict.DictionaryUtil;
import org.geometerplus.android.fbreader.formatPlugin.PluginUtil;
import org.geometerplus.android.fbreader.httpd.DataService;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.sync.SyncOperations;
import org.geometerplus.android.fbreader.tips.TipsActivity;
import org.geometerplus.android.fbreader.tts.TTSProvider;
import org.geometerplus.android.fbreader.ui.BookMarkFragment;
import org.geometerplus.android.fbreader.ui.BookNoteFragment;
import org.geometerplus.android.fbreader.ui.BookTOCFragment;
import org.geometerplus.android.fbreader.util.AndroidImageSynchronizer;
import org.geometerplus.android.fbreader.util.AnimationHelper;
import org.geometerplus.android.fbreader.util.ScreenUtils;
import org.geometerplus.android.fbreader.util.SizeUtils;
import org.geometerplus.android.util.DeviceType;
import org.geometerplus.android.util.SearchDialogUtil;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.CoverUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.DictionaryHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.options.CancelMenuHelper;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.fbreader.formats.ExternalFormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tips.TipsManager;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import skin.support.SkinCompatManager;

public final class FBReader extends FBReaderMainActivity implements ZLApplicationWindow {
    public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
    public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;
    private static final String PLUGIN_ACTION_PREFIX = "___";
    final DataService.Connection DataConnection = new DataService.Connection();
    private final FBReaderApp.Notifier myNotifier = new AppNotifier(this);
    private final List<PluginApi.ActionInfo> myPluginActions =
            new LinkedList<>();
    private final HashMap<MenuItem, String> myMenuItemMap = new HashMap<>();
    private final AndroidImageSynchronizer myImageSynchronizer = new AndroidImageSynchronizer(this);
    volatile boolean IsPaused = false;
    volatile Runnable OnResumeAction = null;
    List<Fragment> fragments = new ArrayList<>();
    private FBReaderApp myFBReaderApp;
    private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).getParcelableArrayList(PluginApi.PluginInfo.KEY);
            if (actions != null) {
                synchronized (myPluginActions) {
                    int index = 0;
                    while (index < myPluginActions.size()) {
                        myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
                    }
                    myPluginActions.addAll(actions);
                    index = 0;
                    for (PluginApi.ActionInfo info : myPluginActions) {
                        myFBReaderApp.addAction(
                                PLUGIN_ACTION_PREFIX + index++,
                                new RunPluginAction(FBReader.this, myFBReaderApp, info.getId())
                        );
                    }
                }
            }
        }
    };
    private final MenuItem.OnMenuItemClickListener myMenuListener =
            new MenuItem.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    myFBReaderApp.runAction(myMenuItemMap.get(item));
                    return true;
                }
            };
    private volatile Book myBook;
    private RelativeLayout myRootView;
    private ZLAndroidWidget myMainView;
    private volatile boolean myShowStatusBarFlag;
    private String myMenuLanguage;
    private volatile long myResumeTimestamp;
    private Intent myCancelIntent = null;
    private Intent myOpenBookIntent = null;
    private PowerManager.WakeLock myWakeLock;
    private boolean myWakeLockToCreate;
    private boolean myStartTimer;
    private int myBatteryLevel;
    private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final int level = intent.getIntExtra("level", 100);
            setBatteryLevel(level);
            switchWakeLock(
                    hasWindowFocus() &&
                            getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
            );
        }
    };
    private BroadcastReceiver mySyncUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            myFBReaderApp.useSyncInfo(myResumeTimestamp + 10 * 1000 > System.currentTimeMillis(), myNotifier);
        }
    };
    /**
     * 菜单
     */
    private View firstMenu;
    private boolean isLoad = false;
    private TTSProvider ttsProvider;
    /**
     * 文本内容（断句好了的）
     */
    private HashMap<String, Boolean> textMap = new HashMap<>();
    private ImageView ivPlayer;
    /**
     * 是否正在播放
     */
    private boolean isPlaying = false;

    public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
        final Intent intent = defaultIntent(context);
        FBReaderIntents.putBookExtra(intent, book);
        FBReaderIntents.putBookmarkExtra(intent, bookmark);
        context.startActivity(intent);
    }

    public static Intent defaultIntent(Context context) {
        return new Intent(context, FBReader.class)
                .setAction(FBReaderIntents.Action.VIEW)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 321);

        ttsProvider = new TTSProvider(FBReader.this);

        bindService(
                new Intent(this, DataService.class),
                DataConnection,
                DataService.BIND_AUTO_CREATE
        );

        final Config config = Config.Instance();
        config.runOnConnect(new Runnable() {
            public void run() {
                config.requestAllValuesForGroup("Options");
                config.requestAllValuesForGroup("Style");
                config.requestAllValuesForGroup("LookNFeel");
                config.requestAllValuesForGroup("Fonts");
                config.requestAllValuesForGroup("Colors");
                config.requestAllValuesForGroup("Files");
            }
        });

        final ZLAndroidLibrary zLibrary = getZLibrary();
        myShowStatusBarFlag = zLibrary.ShowStatusBarOption.getValue();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 程序的界面，这个界面是ZLAndroidWidget类
        setContentView(R.layout.main);
        myRootView = findViewById(R.id.root_view);
        myMainView = findViewById(R.id.main_view);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        firstMenu = findViewById(R.id.firstMenu);
        ivPlayer = findViewById(R.id.ivPlay);

        initListener();

        myFBReaderApp = (FBReaderApp) FBReaderApp.Instance();
        if (myFBReaderApp == null) {
            myFBReaderApp = new FBReaderApp(Paths.systemInfo(this), new BookCollectionShadow());
        }
        getCollection().bindToService(this, null);
        myBook = null;

        myFBReaderApp.setWindow(this);
        myFBReaderApp.initWindow();

        myFBReaderApp.setExternalFileOpener(new ExternalFileOpener(this));

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                myShowStatusBarFlag ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
            new TextSearchPopup(myFBReaderApp);
        }
        if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
            new NavigationPopup(myFBReaderApp);
        }
        if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
            new SelectionPopup(myFBReaderApp);
        }

        myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.DISPLAY_BOOK_POPUP, new DisplayBookPopupAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.HIDE_TOAST, new HideToastAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.OPEN_START_SCREEN, new StartScreenAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
        myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
        myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
        myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
        if (getZLibrary().supportsAllOrientations()) {
            myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
            myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
        }
        myFBReaderApp.addAction(ActionCode.OPEN_WEB_HELP, new OpenWebHelpAction(this, myFBReaderApp));
        myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS, new InstallPluginsAction(this, myFBReaderApp));

        myFBReaderApp.addAction(ActionCode.SWITCH_THEME_WHITE_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.THEME_WHITE));
        myFBReaderApp.addAction(ActionCode.SWITCH_THEME_YELLOW_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.THEME_YELLOW));
        myFBReaderApp.addAction(ActionCode.SWITCH_THEME_GREEN_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.THEME_GREEN));
        myFBReaderApp.addAction(ActionCode.SWITCH_THEME_BLACK_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.THEME_BLACK));

        final Intent intent = getIntent();
        final String action = intent.getAction();

        myOpenBookIntent = intent;
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            if (FBReaderIntents.Action.CLOSE.equals(action)) {
                myCancelIntent = intent;
                myOpenBookIntent = null;
            } else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(action)) {
                myFBReaderApp.ExternalBook = null;
                myOpenBookIntent = null;
                getCollection().bindToService(this, new Runnable() {
                    public void run() {
                        myFBReaderApp.openBook(null, null, null, myNotifier);
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case REQUEST_PREFERENCES:
                if (resultCode != RESULT_DO_NOTHING && data != null) {
                    final Book book = FBReaderIntents.getBookExtra(data, myFBReaderApp.Collection);
                    if (book != null) {
                        getCollection().bindToService(this, new Runnable() {
                            public void run() {
                                onPreferencesUpdate(book);
                            }
                        });
                    }
                }
                break;
            case REQUEST_CANCEL_MENU:
                runCancelAction(data);
                break;
        }
    }

    public void hideDictionarySelection() {
        myFBReaderApp.getTextView().hideOutline();
        myFBReaderApp.getTextView().removeHighlightings(DictionaryHighlighting.class);
        myFBReaderApp.getViewWidget().reset();
        myFBReaderApp.getViewWidget().repaint();
    }

    private void onPreferencesUpdate(Book book) {
        AndroidFontUtil.clearFontCache();
        myFBReaderApp.onBookUpdated(book);
    }

    private void runCancelAction(Intent intent) {
        final CancelMenuHelper.ActionType type;
        try {
            type = CancelMenuHelper.ActionType.valueOf(
                    intent.getStringExtra(FBReaderIntents.Key.TYPE)
            );
        } catch (Exception e) {
            // invalid (or null) type value
            return;
        }
        Bookmark bookmark = null;
        if (type == CancelMenuHelper.ActionType.returnTo) {
            bookmark = FBReaderIntents.getBookmarkExtra(intent);
            if (bookmark == null) {
                return;
            }
        }
        myFBReaderApp.runCancelAction(type, bookmark);
    }

    private void initListener() {
        firstMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Empty body.
            }
        });
        firstMenu.findViewById(R.id.quick_theme_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myFBReaderApp.isActionVisible(ActionCode.SWITCH_THEME_BLACK_PROFILE)) {
                    myFBReaderApp.runAction(ActionCode.SWITCH_THEME_BLACK_PROFILE);
                    SkinCompatManager.getInstance().loadSkin("themeBlack", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                } else {
                    myFBReaderApp.runAction(ActionCode.SWITCH_THEME_WHITE_PROFILE);
                    SkinCompatManager.getInstance().loadSkin("themeWhite", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                }
            }
        });

        findViewById(R.id.menuTop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Empty body.
            }
        });

        // 更多菜单
        findViewById(R.id.ivMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMoreBookInfoView();
                AnimationHelper.openBottomMenu(findViewById(R.id.menuMore));
                AnimationHelper.closeBottomMenu(findViewById(R.id.firstMenu));
                AnimationHelper.closeBottomMenu(findViewById(R.id.menuSetting));
                AnimationHelper.closePreview(myMainView);
            }
        });

        findViewById(R.id.book_mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 添加书签
                getCollection().saveBookmark(myFBReaderApp.createBookmark(20, Bookmark.Type.BookMark));
                Toast.makeText(FBReader.this, "书签已添加", Toast.LENGTH_SHORT).show();
            }
        });

        firstMenu.findViewById(R.id.shangyizhang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFBReaderApp.runAction(ActionCode.TURN_PAGE_BACK);
            }
        });

        firstMenu.findViewById(R.id.xiayizhang).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFBReaderApp.runAction(ActionCode.TURN_PAGE_FORWARD);
            }
        });

        SeekBar seekBar = firstMenu.findViewById(R.id.bookProgress);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    final int page = progress + 1;
                    gotoPage(page);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            private void gotoPage(int page) {
                FBView textView = myFBReaderApp.getTextView();
                if (page == 1) {
                    textView.gotoHome();
                } else {
                    textView.gotoPage(page);
                }
                myFBReaderApp.getViewWidget().reset();
                myFBReaderApp.getViewWidget().repaint();
            }
        });

        // 亮度设置
        SeekBar lightProgress = findViewById(R.id.lightProgress);
        lightProgress.setProgress(myMainView.getScreenBrightness());
        lightProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                myMainView.setScreenBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        firstMenu.findViewById(R.id.open_slid_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSideMenuFragment();
                openSlideMenu();
            }
        });

        findViewById(R.id.slideMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Empty body.
            }
        });

        // 返回
        findViewById(R.id.viewBackground).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    closeSlideMenu();
                }
                return true;
            }
        });

        // 设置菜单
        firstMenu.findViewById(R.id.showSetMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.menuSetting).getVisibility() == View.VISIBLE) {
                    AnimationHelper.closeBottomMenu(findViewById(R.id.menuSetting));
                } else {
                    // 主题
                    RadioGroup radioGroup = findViewById(R.id.book_menu_color_group);
                    if (!myFBReaderApp.isActionVisible(ActionCode.SWITCH_THEME_WHITE_PROFILE)) {
                        radioGroup.check(R.id.color_white);
                    }
                    if (!myFBReaderApp.isActionVisible(ActionCode.SWITCH_THEME_YELLOW_PROFILE)) {
                        radioGroup.check(R.id.color_yellow);
                    }
                    if (!myFBReaderApp.isActionVisible(ActionCode.SWITCH_THEME_GREEN_PROFILE)) {
                        radioGroup.check(R.id.color_green);
                    }
                    if (!myFBReaderApp.isActionVisible(ActionCode.SWITCH_THEME_BLACK_PROFILE)) {
                        radioGroup.check(R.id.color_black);
                    }
                    AnimationHelper.closeBottomMenu(findViewById(R.id.firstMenu));
                    AnimationHelper.openBottomMenu(findViewById(R.id.menuSetting));
                    AnimationHelper.closePreview(myMainView);
                }
            }
        });

        // 字体大小
        findViewById(R.id.font_small).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFBReaderApp.runAction(ActionCode.DECREASE_FONT);
            }
        });
        findViewById(R.id.font_big).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myFBReaderApp.runAction(ActionCode.INCREASE_FONT);
            }
        });

        RadioGroup radioGroup = findViewById(R.id.book_menu_color_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.color_white:
                        myFBReaderApp.runAction(ActionCode.SWITCH_THEME_WHITE_PROFILE);
                        SkinCompatManager.getInstance().restoreDefaultTheme();
                        break;
                    case R.id.color_yellow:
                        myFBReaderApp.runAction(ActionCode.SWITCH_THEME_YELLOW_PROFILE);
                        SkinCompatManager.getInstance().loadSkin("themeYellow", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                        break;
                    case R.id.color_green:
                        myFBReaderApp.runAction(ActionCode.SWITCH_THEME_GREEN_PROFILE);
                        SkinCompatManager.getInstance().loadSkin("themeGreen", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                        break;
                    case R.id.color_black:
                        myFBReaderApp.runAction(ActionCode.SWITCH_THEME_BLACK_PROFILE);
                        SkinCompatManager.getInstance().loadSkin("themeBlack", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
                        break;
                    default:
                        break;
                }
            }
        });

        // TTS的状态回调
        ttsProvider.mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
            @Override
            public void onSynthesizeStart(String s) {

            }

            @Override
            public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

            }

            @Override
            public void onSynthesizeFinish(String s) {

            }

            @Override
            public void onSpeechStart(String s) {

            }

            @Override
            public void onSpeechProgressChanged(String tag, int i) {
                String[] split = tag.split("-");
                if (split.length < 3) {
                    return;
                }
                // 如果已经标记过就算了
                Boolean isPlayed = textMap.get(tag);
                if (isPlayed == null || isPlayed) {
                    return;
                }
                myFBReaderApp.getTextView().highlight(new ZLTextFixedPosition(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 0),
                        new ZLTextFixedPosition(Integer.parseInt(split[0]), Integer.parseInt(split[2]), 0));
                textMap.put(tag, true);
                // 翻页
                int endPIndex = myFBReaderApp.getTextView().getEndCursor().getParagraphIndex();
                int endEIndex = myFBReaderApp.getTextView().getEndCursor().getElementIndex();

                // 判断是否是本页的最后
                if (Integer.parseInt(split[0]) == endPIndex && Integer.parseInt(split[2]) > endEIndex) {
                    myFBReaderApp.runAction(ActionCode.TURN_PAGE_FORWARD);
                }
            }

            @Override
            public void onSpeechFinish(String s) {

            }

            @Override
            public void onError(String s, SpeechError speechError) {
            }
        });

        findViewById(R.id.goto_tts_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                splitText();
                AnimationHelper.closeBottomMenu(findViewById(R.id.firstMenu));
                AnimationHelper.closeBottomMenu(findViewById(R.id.menuTop));
                AnimationHelper.closePreview(myMainView);
                findViewById(R.id.menuPlayer).setVisibility(View.VISIBLE);
                isPlaying = true;
                ivPlayer.setImageResource(R.drawable.reader_player_pause_icon);
                Toast.makeText(FBReader.this, "语音合成中", Toast.LENGTH_SHORT).show();
            }
        });

        // 本地书库
        findViewById(R.id.book_library).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationHelper.closeBottomMenu(findViewById(R.id.menuMore));
                AnimationHelper.closeBottomMenu(findViewById(R.id.menuTop));
                myFBReaderApp.runAction(ActionCode.SHOW_LIBRARY);
            }
        });

        // 播放栏
        findViewById(R.id.menuPlayer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    ivPlayer.setImageResource(R.drawable.reader_player_start_icon);
                    ttsProvider.mSpeechSynthesizer.pause();
                } else {
                    ivPlayer.setImageResource(R.drawable.reader_player_pause_icon);
                    ttsProvider.mSpeechSynthesizer.resume();
                }
                isPlaying = !isPlaying;
            }
        });

        // 关闭播放栏
        findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsProvider.mSpeechSynthesizer.stop();
                myFBReaderApp.getTextView().clearHighlighting();
                findViewById(R.id.menuPlayer).setVisibility(View.GONE);
            }
        });
    }

    private BookCollectionShadow getCollection() {
        return (BookCollectionShadow) myFBReaderApp.Collection;
    }

    /**
     * 初始化点击more后的图书信息
     */
    private void initMoreBookInfoView() {
        final Book book = myFBReaderApp.getCurrentBook();
        if (book == null) {
            return;
        }
        final TextView tvBookName = findViewById(R.id.book_name);
        tvBookName.setText(book.getTitle());
        final TextView tvAuthor = findViewById(R.id.author);
        tvAuthor.setText(book.authorsString(""));
        final ImageView coverView = findViewById(R.id.book_img);
        final PluginCollection pluginCollection =
                PluginCollection.Instance(Paths.systemInfo(this));
        final ZLImage image = CoverUtil.getCover(book, pluginCollection);

        if (image == null) {
            return;
        }

        if (image instanceof ZLImageProxy) {
            ((ZLImageProxy) image).startSynchronization(myImageSynchronizer, new Runnable() {
                public void run() {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            setCover(coverView, image);
                        }
                    });
                }
            });
        } else {
            setCover(coverView, image);
        }
    }

    /**
     * 初始化侧边栏Fragment
     */
    private void initSideMenuFragment() {
        if (isLoad) {
            ((BookTOCFragment) fragments.get(0)).initTree();
            return;
        }
        isLoad = true;
        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        fragments.add(new BookTOCFragment());
        fragments.add(new BookMarkFragment());
        fragments.add(new BookNoteFragment());
        final String[] titles = new String[]{"目录", "书签", "笔记"};
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles[position];
            }
        });
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * 关闭侧边栏
     */
    private void openSlideMenu() {
        // 关闭侧边栏（侧边栏位移，侧边栏蒙层背景淡入淡出，阅读器视图位移）
        AnimationHelper.openSlideMenu(findViewById(R.id.slideMenu), findViewById(R.id.viewBackground), findViewById(R.id.readerView));
        // 关闭底部菜单
        AnimationHelper.closeBottomMenu(findViewById(R.id.firstMenu));
        // 阅读器内容预览关闭
        AnimationHelper.closePreview(myMainView);
        // 关闭-->顶部菜单
        AnimationHelper.closeTopMenu(findViewById(R.id.menuTop));
    }

    /**
     * 关闭侧边栏菜单
     */
    public void closeSlideMenu() {
        AnimationHelper.closeSlideMenu(findViewById(R.id.slideMenu), findViewById(R.id.viewBackground), findViewById(R.id.readerView));
    }

    /**
     * 文本分割
     */
    private void splitText() {
        // 当前的TOC
        final TOCTree tocElement = myFBReaderApp.getCurrentTOCElement();
        // 段落索引
        if (tocElement == null) {
            return;
        }
        // 起始段索引，和元素索引
        int currentPIndex = myFBReaderApp.getTextView().getStartCursor().getParagraphIndex();
        int currentEIndex = myFBReaderApp.getTextView().getStartCursor().getElementIndex();
        int currentCIndex = myFBReaderApp.getTextView().getStartCursor().getCharIndex();

        System.out.println("p" + currentPIndex + "e" + currentEIndex + "c" + currentCIndex);

        int pIndex;
        int startEIndex;
        int endEIndex;
        boolean isChange = false;
        // 清空内容
        textMap.clear();
        // 该章节的起始段落索引
        int paragraphIndex = tocElement.getReference().ParagraphIndex;
        StringBuilder builder = new StringBuilder();
        // 段落游标
        ZLTextParagraphCursor zlTextParagraphCursor = new ZLTextParagraphCursor(myFBReaderApp.Model.getTextModel(), paragraphIndex);
        // 如果不是章节结束
        while (!zlTextParagraphCursor.isEndOfSection()) {
            final ZLTextWordCursor cursor = new ZLTextWordCursor(myFBReaderApp.getTextView().getStartCursor());
            pIndex = zlTextParagraphCursor.Index;
            // 移动到章节起始段落
            cursor.moveToParagraph(zlTextParagraphCursor.Index);
            // 段落起始
            cursor.moveToParagraphStart();
            // 如果不是段落最后
            builder.setLength(0);
            // 开始元素位置索引
            startEIndex = cursor.getElementIndex();
            while (!cursor.isEndOfParagraph()) {
                // 元素
                ZLTextElement element = cursor.getElement();
                if (element instanceof ZLTextWord) {
                    // 该页面起始之前的都不记录
                    if (pIndex == currentPIndex && startEIndex < currentEIndex) {
                        builder.setLength(0);
                        // 游标右移
                        cursor.nextWord();
                        startEIndex = cursor.getElementIndex();
                        isChange = false;
                        continue;
                    }
                    builder.append(element);
                    // 以标点符号断句
                    if (element.toString().matches(".*[。？！;；，!、]+.*")) {
                        // 结束元素位置索引
                        endEIndex = cursor.getElementIndex();
                        String tag = pIndex + "-" + startEIndex + "-" + endEIndex;
                        textMap.put(tag, false);
                        // 当该页面之前的都算了
                        if (pIndex < currentPIndex) {
                            builder.setLength(0);
                            // 游标右移
                            cursor.nextWord();
                            startEIndex = cursor.getElementIndex();
                            isChange = false;
                            continue;
                        }
                        ttsProvider.mSpeechSynthesizer.speak(builder.toString(), tag);
                        builder.setLength(0);
                        isChange = true;
                    }
                }
                // 游标右移
                cursor.nextWord();
                if (isChange) {
                    startEIndex = cursor.getElementIndex();
                    isChange = false;
                }
            }
            // 段落游标右移
            zlTextParagraphCursor = zlTextParagraphCursor.next();
            // 段落位置
            if (zlTextParagraphCursor == null) {
                break;
            }
        }
    }

    /**
     * 设置封面
     */
    private void setCover(ImageView coverView, ZLImage image) {
        final ZLAndroidImageData data =
                ((ZLAndroidImageManager) ZLAndroidImageManager.Instance()).getImageData(image);
        if (data == null) {
            return;
        }

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final Bitmap coverBitmap = data.getBitmap((int) getResources().getDisplayMetrics().density * 56,
                (int) getResources().getDisplayMetrics().density * 74);
        if (coverBitmap == null) {
            return;
        }

        coverView.setImageBitmap(coverBitmap);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getCollection().bindToService(this, new Runnable() {
            public void run() {
                new Thread() {
                    public void run() {
                        getPostponedInitAction().run();
                    }
                }.start();

                myFBReaderApp.getViewWidget().repaint();
            }
        });

        initPluginActions();

        final ZLAndroidLibrary zlibrary = getZLibrary();

        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                final boolean showStatusBar = zlibrary.ShowStatusBarOption.getValue();
                if (showStatusBar != myShowStatusBarFlag) {
                    finish();
                    startActivity(new Intent(FBReader.this, FBReader.class));
                }
                zlibrary.ShowStatusBarOption.saveSpecialValue();
                myFBReaderApp.ViewOptions.ColorProfileName.saveSpecialValue();
                SetScreenOrientationAction.setOrientation(FBReader.this, zlibrary.getOrientationOption().getValue());
            }
        });

        ((PopupPanel) myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
        ((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
        ((PopupPanel) myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
    }

    private Runnable getPostponedInitAction() {
        return new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        new TipRunner().start();
                        DictionaryUtil.init(FBReader.this, null);
                        final Intent intent = getIntent();
                        if (intent != null && FBReaderIntents.Action.PLUGIN.equals(intent.getAction())) {
                            new RunPluginAction(FBReader.this, myFBReaderApp, intent.getData()).run();
                        }
                    }
                });
            }
        };
    }

    private void initPluginActions() {
        synchronized (myPluginActions) {
            int index = 0;
            while (index < myPluginActions.size()) {
                myFBReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
            }
            myPluginActions.clear();
        }

        sendOrderedBroadcast(
                new Intent(PluginApi.ACTION_REGISTER),
                null,
                myPluginInfoReceiver,
                null,
                RESULT_OK,
                null,
                null
        );
    }

    @Override
    protected void onStop() {
        ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
        // TODO: 2019/5/9 移除后状态恢复有问题
        // PopupPanel.removeAllWindows(myFBReaderApp, this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getCollection().unbind();
        unbindService(DataConnection);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onLowMemory() {
        myFBReaderApp.onWindowClosing();
        super.onLowMemory();
    }

    @Override
    protected void onPause() {
        SyncOperations.quickSync(this, myFBReaderApp.SyncOptions);

        IsPaused = true;
        try {
            unregisterReceiver(mySyncUpdateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(myBatteryInfoReceiver);
        } catch (IllegalArgumentException e) {
            // do nothing, this exception means that myBatteryInfoReceiver was not registered
        }

        myFBReaderApp.stopTimer();
        if (getZLibrary().DisableButtonLightsOption.getValue()) {
            setButtonLight(true);
        }
        myFBReaderApp.onWindowClosing();

        super.onPause();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final String action = intent.getAction();
        final Uri data = intent.getData();

        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            super.onNewIntent(intent);
        } else if (Intent.ACTION_VIEW.equals(action)
                && data != null && "fbreader-action".equals(data.getScheme())) {
            myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
        } else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
            myOpenBookIntent = intent;
            if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
                final BookCollectionShadow collection = getCollection();
                final Book b = FBReaderIntents.getBookExtra(intent, collection);
                if (!collection.sameBook(b, myFBReaderApp.ExternalBook)) {
                    try {
                        final ExternalFormatPlugin plugin =
                                (ExternalFormatPlugin) BookUtil.getPlugin(
                                        PluginCollection.Instance(Paths.systemInfo(this)),
                                        myFBReaderApp.ExternalBook
                                );
                        startActivity(PluginUtil.createIntent(plugin, FBReaderIntents.Action.PLUGIN_KILL));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (FBReaderIntents.Action.PLUGIN.equals(action)) {
            new RunPluginAction(this, myFBReaderApp, data).run();
        } else if (Intent.ACTION_SEARCH.equals(action)) {
            final String pattern = intent.getStringExtra(SearchManager.QUERY);
            final Runnable runnable = new Runnable() {
                public void run() {
                    final TextSearchPopup popup = (TextSearchPopup) myFBReaderApp.getPopupById(TextSearchPopup.ID);
                    popup.initPosition();
                    myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
                    if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                myFBReaderApp.showPopup(popup.getId());
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                UIMessageUtil.showErrorMessage(FBReader.this, "textNotFound");
                                popup.StartPosition = null;
                            }
                        });
                    }
                }
            };
            UIUtil.wait("search", runnable, this);
        } else if (FBReaderIntents.Action.CLOSE.equals(intent.getAction())) {
            myCancelIntent = intent;
            myOpenBookIntent = null;
        } else if (FBReaderIntents.Action.PLUGIN_CRASH.equals(intent.getAction())) {
            final Book book = FBReaderIntents.getBookExtra(intent, myFBReaderApp.Collection);
            myFBReaderApp.ExternalBook = null;
            myOpenBookIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    final BookCollectionShadow collection = getCollection();
                    Book b = collection.getRecentBook(0);
                    if (collection.sameBook(b, book)) {
                        b = collection.getRecentBook(1);
                    }
                    myFBReaderApp.openBook(b, null, null, myNotifier);
                }
            });
        } else {
            super.onNewIntent(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        myStartTimer = true;
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                SyncOperations.enableSync(FBReader.this, myFBReaderApp.SyncOptions);

                final int brightnessLevel =
                        getZLibrary().ScreenBrightnessLevelOption.getValue();
                if (brightnessLevel != 0) {
                    getViewWidget().setScreenBrightness(brightnessLevel);
                } else {
                    setScreenBrightnessAuto();
                }
                if (getZLibrary().DisableButtonLightsOption.getValue()) {
                    setButtonLight(false);
                }

                getCollection().bindToService(FBReader.this, new Runnable() {
                    public void run() {
                        final BookModel model = myFBReaderApp.Model;
                        if (model == null || model.Book == null) {
                            return;
                        }
                        onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
                    }
                });
            }
        });

        registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        IsPaused = false;
        myResumeTimestamp = System.currentTimeMillis();
        if (OnResumeAction != null) {
            final Runnable action = OnResumeAction;
            OnResumeAction = null;
            action.run();
        }

        registerReceiver(mySyncUpdateReceiver, new IntentFilter(FBReaderIntents.Event.SYNC_UPDATED));

        SetScreenOrientationAction.setOrientation(this, getZLibrary().getOrientationOption().getValue());
        if (myCancelIntent != null) {
            final Intent intent = myCancelIntent;
            myCancelIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    runCancelAction(intent);
                }
            });
            return;
        } else if (myOpenBookIntent != null) {
            final Intent intent = myOpenBookIntent;
            myOpenBookIntent = null;
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    openBook(intent, null, true);
                }
            });
        } else if (myFBReaderApp.getCurrentServerBook(null) != null) {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.useSyncInfo(true, myNotifier);
                }
            });
        } else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null, null, myNotifier);
                }
            });
        } else {
            getCollection().bindToService(this, new Runnable() {
                public void run() {
                    myFBReaderApp.useSyncInfo(true, myNotifier);
                }
            });
        }

        PopupPanel.restoreVisibilities(myFBReaderApp);
        ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
    }

    private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
        if (!force && myBook != null) {
            return;
        }

        myBook = FBReaderIntents.getBookExtra(intent, myFBReaderApp.Collection);
        final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
        if (myBook == null) {
            final Uri data = intent.getData();
            if (data != null) {
                myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
            }
        }
        if (myBook != null) {
            ZLFile file = BookUtil.fileByBook(myBook);
            if (!file.exists()) {
                if (file.getPhysicalFile() != null) {
                    file = file.getPhysicalFile();
                }
                UIMessageUtil.showErrorMessage(this, "fileNotFound", file.getPath());
                myBook = null;
            } else {
                NotificationUtil.drop(this, myBook);
            }
        }
        Config.Instance().runOnConnect(new Runnable() {
            public void run() {
                myFBReaderApp.openBook(myBook, bookmark, action, myNotifier);
                AndroidFontUtil.clearFontCache();
            }
        });
    }

    private Book createBookForFile(ZLFile file) {
        if (file == null) {
            return null;
        }
        Book book = myFBReaderApp.Collection.getBookByFile(file.getPath());
        if (book != null) {
            return book;
        }
        if (file.isArchive()) {
            for (ZLFile child : file.children()) {
                book = myFBReaderApp.Collection.getBookByFile(child.getPath());
                if (book != null) {
                    return book;
                }
            }
        }
        return null;
    }

    private void setButtonLight(boolean enabled) {
        setButtonLightInternal(enabled);
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void setButtonLightInternal(boolean enabled) {
        final WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.buttonBrightness = enabled ? -1.0f : 0.0f;
        getWindow().setAttributes(attrs);
    }

    public void showSelectionPanel() {
        final ZLTextView view = myFBReaderApp.getTextView();
        // 显示弹框
        myFBReaderApp.showPopup(SelectionPopup.ID);
        // 位置移动
        ((SelectionPopup) myFBReaderApp.getPopupById(SelectionPopup.ID)).move(view.getSelectionStartY(), view.getSelectionEndY());
    }

    public void hideSelectionPanel() {
        final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
        if (popup != null && popup.getId() == SelectionPopup.ID) {
            myFBReaderApp.hideActivePopup();
        }
    }

    public void navigate() {
        ((NavigationPopup) myFBReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
    }

    private Menu addSubmenu(Menu menu, String id) {
        return menu.addSubMenu(ZLResource.resource("menu").getResource(id).getValue());
    }

    private void addMenuItem(Menu menu, String actionId, Integer iconId, String name) {
        if (name == null) {
            name = ZLResource.resource("menu").getResource(actionId).getValue();
        }
        final MenuItem menuItem = menu.add(name);
        if (iconId != null) {
            menuItem.setIcon(iconId);
        }
        menuItem.setOnMenuItemClickListener(myMenuListener);
        myMenuItemMap.put(menuItem, actionId);
    }

    private void addMenuItem(Menu menu, String actionId, String name) {
        addMenuItem(menu, actionId, null, name);
    }

    private void addMenuItem(Menu menu, String actionId, int iconId) {
        addMenuItem(menu, actionId, iconId, null);
    }

    private void addMenuItem(Menu menu, String actionId) {
        addMenuItem(menu, actionId, null, null);
    }

    private void fillMenu(Menu menu, List<MenuNode> nodes) {
        for (MenuNode n : nodes) {
            if (n instanceof MenuNode.Item) {
                final Integer iconId = ((MenuNode.Item) n).IconId;
                if (iconId != null) {
                    addMenuItem(menu, n.Code, iconId);
                } else {
                    addMenuItem(menu, n.Code);
                }
            } else /* if (n instanceof MenuNode.Submenu) */ {
                final Menu submenu = addSubmenu(menu, n.Code);
                fillMenu(submenu, ((MenuNode.Submenu) n).Children);
            }
        }
    }

    private void setupMenu(Menu menu) {
        final String menuLanguage = ZLResource.getLanguageOption().getValue();
        if (menuLanguage.equals(myMenuLanguage)) {
            return;
        }
        myMenuLanguage = menuLanguage;

        menu.clear();
        fillMenu(menu, MenuData.topLevelNodes());
        synchronized (myPluginActions) {
            int index = 0;
            for (PluginApi.ActionInfo info : myPluginActions) {
                if (info instanceof PluginApi.MenuActionInfo) {
                    addMenuItem(
                            menu,
                            PLUGIN_ACTION_PREFIX + index++,
                            ((PluginApi.MenuActionInfo) info).MenuItemName
                    );
                }
            }
        }

        refresh();
    }

    protected void onPluginNotFound(final Book book) {
        final BookCollectionShadow collection = getCollection();
        collection.bindToService(this, new Runnable() {
            public void run() {
                final Book recent = collection.getRecentBook(0);
                if (recent != null && !collection.sameBook(recent, book)) {
                    myFBReaderApp.openBook(recent, null, null, null);
                } else {
                    myFBReaderApp.openHelpBook();
                }
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        switchWakeLock(hasFocus &&
                getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
                        myFBReaderApp.getBatteryLevel()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        setupMenu(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setStatusBarVisibility(true);
        setupMenu(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setStatusBarVisibility(false);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        setStatusBarVisibility(false);
    }

    @Override
    public boolean onSearchRequested() {
        final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
        myFBReaderApp.hideActivePopup();
        if (DeviceType.Instance().hasStandardSearchDialog()) {
            final SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
            manager.setOnCancelListener(new SearchManager.OnCancelListener() {
                public void onCancel() {
                    if (popup != null) {
                        myFBReaderApp.showPopup(popup.getId());
                    }
                    manager.setOnCancelListener(null);
                }
            });
            startSearch(myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), true, null, false);
        } else {
            SearchDialogUtil.showDialog(
                    this, FBReader.class, myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface di) {
                            if (popup != null) {
                                myFBReaderApp.showPopup(popup.getId());
                            }
                        }
                    }
            );
        }
        return true;
    }

    private void setStatusBarVisibility(boolean visible) {
        if (DeviceType.Instance() != DeviceType.KINDLE_FIRE_1ST_GENERATION && !myShowStatusBarFlag) {
            if (visible) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            }
        }
    }

    private void switchWakeLock(boolean on) {
        if (on) {
            if (myWakeLock == null) {
                myWakeLockToCreate = true;
            }
        } else {
            if (myWakeLock != null) {
                synchronized (this) {
                    if (myWakeLock != null) {
                        myWakeLock.release();
                        myWakeLock = null;
                    }
                }
            }
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    public final void createWakeLock() {
        if (myWakeLockToCreate) {
            synchronized (this) {
                if (myWakeLockToCreate) {
                    myWakeLockToCreate = false;
                    myWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
                    myWakeLock.acquire();
                }
            }
        }
        if (myStartTimer) {
            myFBReaderApp.startTimer();
            myStartTimer = false;
        }
    }

    @Override
    public void setWindowTitle(final String title) {
        runOnUiThread(new Runnable() {
            public void run() {
                setTitle(title);
            }
        });
    }

    // methods from ZLApplicationWindow interface
    @Override
    public void showErrorMessage(String key) {
        UIMessageUtil.showErrorMessage(this, key);
    }

    @Override
    public void showErrorMessage(String key, String parameter) {
        UIMessageUtil.showErrorMessage(this, key, parameter);
    }

    @Override
    public FBReaderApp.SynchronousExecutor createExecutor(String key) {
        return UIUtil.createExecutor(this, key);
    }

    @Override
    public void processException(Exception exception) {
        exception.printStackTrace();

        final Intent intent = new Intent(
                FBReaderIntents.Action.ERROR,
                new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
        );
        intent.setPackage(FBReaderIntents.DEFAULT_PACKAGE);
        intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
		/*
		if (exception instanceof BookReadingException) {
			final ZLFile file = ((BookReadingException)exception).File;
			if (file != null) {
				intent.putExtra("file", file.getPath());
			}
		}
		*/
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // ignore
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() {
        runOnUiThread(new Runnable() {
            public void run() {
                for (Map.Entry<MenuItem, String> entry : myMenuItemMap.entrySet()) {
                    final String actionId = entry.getValue();
                    final MenuItem menuItem = entry.getKey();
                    menuItem.setVisible(myFBReaderApp.isActionVisible(actionId) && myFBReaderApp.isActionEnabled(actionId));
                    switch (myFBReaderApp.isActionChecked(actionId)) {
                        case TRUE:
                            menuItem.setCheckable(true);
                            menuItem.setChecked(true);
                            break;
                        case FALSE:
                            menuItem.setCheckable(true);
                            menuItem.setChecked(false);
                            break;
                        case UNDEFINED:
                            menuItem.setCheckable(false);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public ZLViewWidget getViewWidget() {
        return myMainView;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public int getBatteryLevel() {
        return myBatteryLevel;
    }

    private void setBatteryLevel(int percent) {
        myBatteryLevel = percent;
    }

    public void outlineRegion(ZLTextRegion.Soul soul) {
        myFBReaderApp.getTextView().outlineRegion(soul);
        myFBReaderApp.getViewWidget().repaint();
    }

    /**
     * 显示菜单
     */
    public void openMenu() {
        if (findViewById(R.id.firstMenu).getVisibility() == View.VISIBLE) { // 第一菜单 -- > 隐藏之
            AnimationHelper.closeTopMenu(findViewById(R.id.menuTop));
            AnimationHelper.closeBottomMenu(findViewById(R.id.firstMenu));
            AnimationHelper.closePreview(myMainView);
        } else if (findViewById(R.id.menuSetting).getVisibility() == View.VISIBLE) { // 设置菜单 -- > 隐藏之
            AnimationHelper.closeTopMenu(findViewById(R.id.menuTop));
            AnimationHelper.closeBottomMenu(findViewById(R.id.menuSetting));
        } else if (findViewById(R.id.menuMore).getVisibility() == View.VISIBLE) { // 更多菜单 --> 隐藏之
            AnimationHelper.closeTopMenu(findViewById(R.id.menuTop));
            AnimationHelper.closeBottomMenu(findViewById(R.id.menuMore));
        } else { // 没菜单显示 --> 显示一级菜单
            initBookInfoView();
            AnimationHelper.openTopMenu(findViewById(R.id.menuTop));
            AnimationHelper.openBottomMenu(findViewById(R.id.firstMenu));
            // 阅读器内容预览关闭
            AnimationHelper.openPreview(myMainView);

            // 设置阅读进度
            SeekBar seekBar = findViewById(R.id.bookProgress);
            final FBView textView = myFBReaderApp.getTextView();
            ZLTextView.PagePosition pagePosition = textView.pagePosition();
            if (seekBar.getMax() != pagePosition.Total - 1 || seekBar.getProgress() != pagePosition.Current - 1) {
                seekBar.setMax(pagePosition.Total - 1);
                seekBar.setProgress(pagePosition.Current - 1);
            }
        }
    }

    /**
     * 初始化书籍信息
     */
    private void initBookInfoView() {
        Book book = myFBReaderApp.getCurrentBook();
        if (book == null) {
            return;
        }
        String title = book.getTitle();

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }

    public FBReaderApp getMyFBReaderApp() {
        return myFBReaderApp;
    }

    /**
     * 小提示
     */
    private class TipRunner extends Thread {

        TipRunner() {
            setPriority(MIN_PRIORITY);
        }

        public void run() {
            final TipsManager manager = new TipsManager(Paths.systemInfo(FBReader.this));
            switch (manager.requiredAction()) {
                case Initialize:
                    startActivity(new Intent(
                            TipsActivity.INITIALIZE_ACTION, null, FBReader.this, TipsActivity.class
                    ));
                    break;
                case Show:
                    startActivity(new Intent(
                            TipsActivity.SHOW_TIP_ACTION, null, FBReader.this, TipsActivity.class
                    ));
                    break;
                case Download:
                    manager.startDownloading();
                    break;
                case None:
                    break;
            }
        }
    }
}
