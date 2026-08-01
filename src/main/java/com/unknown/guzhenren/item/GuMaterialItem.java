package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;

public class GuMaterialItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu_material";

    public GuMaterialItem(Properties properties, Rank rank, GuPath path) {
        super(properties, rank, path);
    }

    @Override
    protected String kindKey() {return KIND_KEY;}
}
