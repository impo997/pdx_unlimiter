package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class GameDlc {

    private boolean expansion;
    private Path filePath;
    private Path dataPath;
    private String name;

    public static Optional<GameDlc> fromDirectory(Path p) throws Exception {
        if (!Files.isDirectory(p)) {
            return Optional.empty();
        }

        String dlcName = p.getFileName().toString();
        String dlcId = dlcName.split("_")[0];
        Path filePath = p.resolve(dlcId + ".dlc");
        Path dataPath = p.resolve(dlcId + ".zip");

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        Node node = TextFormatParser.text().parse(filePath);
        GameDlc dlc = new GameDlc();
        dlc.expansion = node.getNodeForKeyIfExistent("category")
                .map(n -> n.getString().equals("expansion"))
                .orElse(false);
        dlc.filePath = filePath;
        dlc.dataPath = dataPath;
        dlc.name = node.getNodeForKey("name").getString();
        return Optional.of(dlc);
    }

    public boolean isExpansion() {
        return expansion;
    }

    public Path getInfoFilePath() {
        return filePath;
    }

    public String getName() {
        return name;
    }

    public Path getDataPath() {
        return dataPath;
    }
}
