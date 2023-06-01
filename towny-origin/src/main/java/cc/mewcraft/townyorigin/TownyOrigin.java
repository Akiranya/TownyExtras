package cc.mewcraft.townyorigin;

import cc.mewcraft.mewcore.message.Translations;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

public class TownyOrigin extends ExtendedJavaPlugin {

    private Translations translations;

    @Override protected void enable() {
        translations = new Translations(this, "languages");

        // Register placeholders
        new MiniPlaceholderExpansion(this).register().bindWith(this);

        // Register listeners
        registerListener(new PlayerListener(this)).bindWith(this);
    }

    public Translations getLang() {
        return translations;
    }

}
