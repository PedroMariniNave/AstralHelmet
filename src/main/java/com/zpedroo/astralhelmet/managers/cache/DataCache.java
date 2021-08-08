package com.zpedroo.astralhelmet.managers.cache;

import com.zpedroo.astralhelmet.objects.Helmet;

import java.util.*;

public class DataCache {

    private Map<String, Helmet> helmets;

    public DataCache() {
        this.helmets = new HashMap<>(4);
    }

    public Map<String, Helmet> getHelmets() {
        return helmets;
    }
}