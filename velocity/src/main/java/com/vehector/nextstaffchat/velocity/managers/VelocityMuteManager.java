package com.vehector.nextstaffchat.velocity.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityMuteManager {
    private final Set<UUID> muted = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public boolean isMuted(UUID u) { return muted.contains(u); }
    public boolean toggle(UUID u) {
        if (muted.contains(u)) { muted.remove(u); return false; }
        muted.add(u); return true;
    }
}
