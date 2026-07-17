package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;

//  凡蛊 -- 一转~五转的蛊虫. The two axes are a 蛊虫's own; a 蛊材 has neither.
//  ⚠ feedable only declares -- nothing feeds a 蛊 yet. Concrete: a plain 蛊 needs no class of its own.
public class MortalGuItem extends GuItem {

    private static final String KIND_KEY = "guzhenren.display.gu";

    private final boolean reusable;
    private final boolean feedable;

    public MortalGuItem(Properties properties, Rank rank, GuPath path, boolean reusable, boolean feedable) {
        super(properties, rank, path);
        this.reusable = reusable;
        this.feedable = feedable;
    }

    @Override
    protected String kindKey() {return KIND_KEY;}
    public boolean reusable() {return reusable;}
    public boolean feedable() {return feedable;}
}
