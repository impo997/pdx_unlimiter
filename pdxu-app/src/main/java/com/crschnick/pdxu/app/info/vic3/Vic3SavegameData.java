package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("vic3")
@Getter
public class Vic3SavegameData extends SavegameData<Vic3Tag> {

    private String campaignName;
    private GameVersion version;
    private Vic3Tag tag;
    private List<Vic3Tag> allTags;

    public Vic3SavegameData() {
    }

    public Vic3Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.VIC3.getCampaignIdHeuristic(content);

        var meta = content.get().getNodeForKey("meta_data");
        campaignName = meta.getNodeForKey("name").getString();
        ironman = meta.getNodeForKeyIfExistent("ironman").map(Node::getBoolean).orElse(false);
        date = GameDateType.VIC3.fromString(content.get().getNodeForKeys("meta_data", "game_date").getString());

        var countryId = content.get().getNodeForKey("previous_played").getNodeArray().get(0).getNodeForKey("idtype").getValueNode().getString();
        var country = content.get().getNodeForKey("country_manager").getNodeForKey("database").getNodeForKey(countryId);
        tag = new Vic3Tag(country.getNodeForKey("definition").getString(), country.getNodeForKey("government").getString());
        allTags = List.of(tag);
        observer = false;

        mods = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("mods")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());
        dlcs = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("dlcs")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());

        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
        var v = n.getNodeForKey("meta_data").getNodeForKey("version").getString();
        Matcher m = p.matcher(v);
        if (m.matches()) {
            var fourth = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    fourth
            );
        } else {
            throw new IllegalArgumentException("Could not parse VIC3 version string: " + v);
        }
    }

    public List<Vic3Tag> getAllTags() {
        return allTags;
    }
}
