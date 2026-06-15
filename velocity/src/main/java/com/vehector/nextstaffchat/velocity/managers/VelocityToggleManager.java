package com.vehector.nextstaffchat.velocity.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityToggleManager {
    private final Set<UUID> toggled = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public boolean isToggled(UUID u) { return toggled.contains(u); }
    public boolean toggle(UUID u) {
        if (toggled.contains(u)) { toggled.remove(u); return false; }
        toggled.add(u); return true;
    }
    public void clear(UUID u) { toggled.remove(u); }
}
