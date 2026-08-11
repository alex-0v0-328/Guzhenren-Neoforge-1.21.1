package com.unknown.guzhenren.item.material;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;
import com.unknown.guzhenren.item.GuItem;

/**
 * Gu material [蛊材], concrete and with no axes of its own: a material with no behavior is one of these.
 *
 * @author Alex
 * @since 1.0.0
 */
public class GuMaterialItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu_material";

    public GuMaterialItem(Properties properties, Rank rank, GuPath path) {
        super(properties, rank, path);
    }

    @Override
    protected String kindKey() {return KIND_KEY;}
}
