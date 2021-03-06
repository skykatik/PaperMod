package sky.paper;

import arc.*;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.*;
import arc.util.io.Streams;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.type.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawAnimation;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.*;

public final class PaperMod extends Mod{
    public static final String baseNewsUrl = "https://raw.githubusercontent.com/@/main/news/@/@.txt";

    public static Item newspaper;

    public static Block newspaperPress;
    public static Block pneumaticPost;

    public static BaseDialog dialog = null;

    private final DateTimeFormatter directoryFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

    @Override
    public void init(){
        ui.menufrag = new PMenuFragment();
        ui.menufrag.build(ui.menuGroup);
        Core.scene.add(ui.menuGroup);

        Events.on(EventType.WithdrawEvent.class, event -> {
            if(event.item == newspaper && event.amount == 1 && event.player == player){
                Time.runTask(3f, () -> Events.fire(new NewsShowEvent()));
            }
        });

        Events.on(NewsShowEvent.class, event -> {
            dialog = new BaseDialog("@paper-mod.selector");
            Table t = new Table();

            String[] arr = {};
            try{
                arr = Streams.copyString(new URL("https://raw.githubusercontent.com/skykatik/PaperMod/main/news/index.txt").openStream()).split("\\s+");
            }catch(IOException e){
                Log.err("Failed to read index.txt");
                Log.err(e);
            }

            for(String s : arr){
                t.button(s, () -> {
                    dialog.hide();
                    showNews(s);
                }).size(300f, 50f).row();
            }

            dialog.cont.add(t);
            t.button("@ok", dialog::hide).size(100f, 50f).row();
            dialog.show();
        });
    }

    public static final class NewsShowEvent{}

    public void showNews(String url){
        Core.net.httpGet(Strings.format(baseNewsUrl, url, directoryFormatter.format(LocalDateTime.now()), dayFormatter.format(LocalDateTime.now())),
                res -> {
                    BaseDialog dialog = new BaseDialog(url);
                    dialog.cont.add(res.getResultAsString()).row();
                    dialog.cont.button("@ok", dialog::hide).size(100f, 50f);
                    dialog.show();
                },
                Log::err);
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

        pneumaticPost = new ItemTurret("pneumatic-post"){{
            requirements(Category.turret, ItemStack.with(newspaper, 150, Items.titanium, 200, Items.silicon, 250));
            liquidCapacity = 40f;
            maxAmmo = 35;
            size = 4;
            range = 35 * 8f;
            cooldown = 0.5f;
            targetAir = true;
            targetGround = true;
            ammo(newspaper, Bullets.standardThoriumBig);
            health = 1000;
            spread = 0.5f;
        }};
    }
}
