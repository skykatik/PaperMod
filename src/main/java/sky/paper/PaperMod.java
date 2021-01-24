package sky.paper;

import arc.*;
import arc.graphics.Color;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.type.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawAnimation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.player;

public final class PaperMod extends Mod{
    public static final String baseNewsUrl = "https://raw.githubusercontent.com/skykatik/PaperMod/main/news/@/@.txt";

    public static Item newspaper;

    public static Block newspaperPress;

    private final DateTimeFormatter directoryFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

    @Override
    public void init(){
        Events.on(EventType.WithdrawEvent.class, event -> {
            if(event.item == newspaper && event.amount == 1 && event.player == player){
                Time.runTask(3f, () -> {
                    Core.net.httpGet(Strings.format(baseNewsUrl, directoryFormatter.format(LocalDateTime.now()), dayFormatter.format(LocalDateTime.now())),
                                     res -> {
                                         BaseDialog dialog = new BaseDialog("@paper-mod.breaking-news");
                                         dialog.cont.add(res.getResultAsString()).row();
                                         dialog.cont.button("@ok", dialog::hide).size(100f, 50f);
                                         dialog.show();
                                     },
                                     Log::err);
                });
            }
        });
    }

    @Override
    public void loadContent(){

        newspaper = new Item("newspaper", Color.valueOf("E2E9EE")){{
            flammability = 1.15f;
            explosiveness = 0.2f;
        }};

        newspaperPress = new GenericCrafter("newspaper-press"){{
            requirements(Category.crafting, ItemStack.with(Items.titanium, 150, Items.silicon, 50));
            liquidCapacity = 40f;
            craftTime = 75f;
            size = 2;
            health = 320;
            outputItem = new ItemStack(newspaper, 1);
            hasLiquids = true;
            hasPower = true;
            craftEffect = Fx.none;
            drawer = new DrawAnimation();
            consumes.liquid(Liquids.water, 3.5f);
            consumes.items(ItemStack.with(Items.sporePod, 1, Items.phaseFabric, 1));
            consumes.power(3f);
        }};
    }
}
