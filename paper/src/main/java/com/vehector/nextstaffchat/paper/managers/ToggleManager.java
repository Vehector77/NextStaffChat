package com.vehector.nextstaffchat.paper.managers;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ToggleManager {
    private final Set<UUID> toggled = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean isToggled(UUID uuid) { return toggled.contains(uuid); }

    public boolean toggle(UUID uuid) {
        if (toggled.contains(uuid)) { toggled.remove(uuid); return false; }
        toggled.add(uuid); return true;
    }

    public void clear(UUID uuid) { toggled.remove(uuid); }
}
