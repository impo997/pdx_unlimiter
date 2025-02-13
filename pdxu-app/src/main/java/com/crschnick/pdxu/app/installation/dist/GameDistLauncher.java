package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallType;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.GameMod;
import com.crschnick.pdxu.app.savegame.FileExportTarget;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.integration.IronyHelper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameDistLauncher {

    public static void startLauncher() {
        try {
            var game = SavegameManagerState.get().current();
            if (game == null) {
                return;
            }

            if (Settings.getInstance().launchIrony.getValue()) {
                IronyHelper.launchEntry(game, false);
            } else {
                GameInstallation.ALL.get(game).getDist().startLauncher(Map.of());
            }
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }
    }

    public static void startLauncherWithContinueGame(SavegameEntry<?, ?> e) {
        GuiSavegameNotes.showSavegameNotesReminderDialog(e.getNotes());

        try {
            setupContinueGame(e);
            startLauncherDirectly();
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
        }
    }

    public static <T, I extends SavegameInfo<T>> void continueSavegame(SavegameEntry<T, I> e, boolean debug) {
        SavegameContext.withSavegameContext(e, ctx -> {
            if (ctx.getInfo() == null) {
                return;
            }

            if (SavegameCompatibility.determineForEntry(e) != SavegameCompatibility.Compatbility.COMPATIBLE) {
                boolean startAnyway = GuiIncompatibleWarning.showIncompatibleWarning(
                        ctx.getInstallation(), e);
                if (!startAnyway) {
                    return;
                }
            }

            GuiSavegameNotes.showSavegameNotesReminderDialog(e.getNotes());

            try {
                setupContinueGame(e);
                startGameDirectly(e, debug);
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        });
    }

    private static <T, I extends SavegameInfo<T>> void setupContinueGame(SavegameEntry<T, I> e) throws Exception {
        var ctxOpt = SavegameContext.getContextIfExistent(e).filter(c -> c.getInfo() != null);
        if (ctxOpt.isEmpty()) {
            return;
        }

        var ctx = ctxOpt.get();
        var exportTarget = FileExportTarget.createExportTarget(e);
        var path = exportTarget.export();
        ctx.getInstallation().getType().writeLaunchConfig(
                ctx.getInstallation().getUserDir(),
                ctx.getStorage().getEntryName(e),
                ctx.getCollection().getLastPlayed(),
                path, ctx.getInfo().getData().getVersion()
        );
        ctx.getCollection().lastPlayedProperty().setValue(Instant.now());

        var dlcs = ctx.getInfo().getData().getDlcs().stream()
                .map(d -> ctx.getInstallation().getDlcForName(d))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        var mods = ctx.getInfo().getData().getMods() != null ? ctx.getInfo().getData().getMods().stream()
                .map(m -> ctx.getInstallation().getModForSavegameId(m))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()) : List.<GameMod>of();

        ctx.getInstallation().getType().writeModAndDlcLoadFile(ctx.getInstallation(), mods, dlcs);
    }

    public static boolean canChangeMods(Game game) {
        return Settings.getInstance().launchIrony.getValue() ||
                GameInstallation.ALL.get(game).getDist().supportsLauncher();
    }

    private static void startLauncherDirectly() throws IOException {
        var game = SavegameManagerState.get().current();
        if (Settings.getInstance().launchIrony.getValue()) {
            IronyHelper.launchEntry(game, true);
        } else {
            if (!GameInstallation.ALL.get(game).getDist().supportsLauncher()) {
                return;
            }

            GameInstallation.ALL.get(game).getDist().startLauncher(Map.of());
        }
    }

    private static void startGameDirectly(SavegameEntry<?, ?> e, boolean debug) throws Exception {
        var ctx = SavegameContext.getContext(e);
        if (ctx.getGame().getInstallType().getModInfoStorageType() ==
                GameInstallType.ModInfoStorageType.SAVEGAME_DOESNT_STORE_INFO) {
            var r = GuiIncompatibleWarning.showNoSavedModsWarning(
                    ctx.getGame(), ctx.getInstallation().queryEnabledMods());
            if (r.isPresent()) {
                var b = r.get();
                if (b) {
                    ctx.getInstallation().startDirectly(debug);
                } else {
                    startLauncherDirectly();
                }
            }
            return;
        }

        if (Settings.getInstance().launchIrony.getValue()) {
            IronyHelper.launchEntry(ctx.getGame(), true);
        } else {
            ctx.getInstallation().startDirectly(debug);
        }
    }
}
