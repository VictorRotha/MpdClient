package de.nif.data;

import java.util.HashMap;
import java.util.Map;

public class AudioOutput {

    private int id;
    private String name;
    private boolean enabled;
    private String plugin;

    private final HashMap<String, String> attributes;

    public AudioOutput() {
        id = -1;
        attributes = new HashMap<>();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    @Override
    public String toString() {
        return String.format("AudioOutput {id=%s, enabled=%s, name=%s, plugin=%s, attributes=%s}",
                id, enabled, name, plugin, attributes);
    }
}
