package com.unknown.guzhenren.item;

import com.unknown.guzhenren.custom.enums.aperture.Rank;
import com.unknown.guzhenren.custom.enums.path.GuPath;

//  Mortal Gu (凡蛊) -- a Gu of ranks 1..5. Concrete: a plain Gu needs no class of its own.
//  ⚠ Both axes are false everywhere today -- nothing feeds a Gu, and no reusable Gu exists.
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
