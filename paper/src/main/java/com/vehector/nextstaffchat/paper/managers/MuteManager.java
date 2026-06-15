package com.vehector.nextstaffchat.paper.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MuteManager {
    private final Set<UUID> muted = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public boolean isMuted(UUID uuid) { return muted.contains(uuid); }
    public boolean toggle(UUID uuid) {
        if (muted.contains(uuid)) { muted.remove(uuid); return false; }
        muted.add(uuid); return true;
    }
}
