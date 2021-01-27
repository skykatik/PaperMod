package sky.paper;

import arc.*;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.type.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawAnimation;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.player;

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
        Events.on(EventType.WithdrawEvent.class, event -> {
            if(event.item == newspaper && event.amount == 1 && event.player == player){
                Time.runTask(3f, () -> {
                    dialog = new BaseDialog("@paper-mod.selector");
                    Table t= new Table();

                    BufferedReader br= null;
                    try {
                        br = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/skykatik/PaperMod/multiPaper/news/index.txt").openStream()));
                    } catch (MalformedURLException e) {
                        return;
                    } catch (IOException e) {
                        return;
                    }
                    while(true){
                        String baka= null;
                        try {
                            baka = br.readLine();
                        } catch (IOException e) {
                            return;
                        }
                        if(baka==null){break;}
                        String finalBaka = baka;
                        t.button(baka, new Runnable() {
                            @Override
                            public void run() {
                                dialog.hide();
                                showNews(finalBaka);
                            }
                        }).size(200f, 50f);
                    }
                    dialog.cont.add(t);
                    dialog.cont.button("@ok", dialog::hide).size(100f, 50f);
                    dialog.show();
                });
            }
        });
    }

    public void showNews(String url){
        String usrl=Strings.format(baseNewsUrl,url, directoryFormatter.format(LocalDateTime.now()), dayFormatter.format(LocalDateTime.now()));
        Core.net.httpGet(usrl,
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
            requirements(Category.turret,ItemStack.with(newspaper,150,Items.titanium,200,Items.silicon,250));
            liquidCapacity = 40f;
            maxAmmo=35;
            size = 4;
            range =45 * 8f;
            cooldown = 0.5f;
            targetAir=true;
            targetGround=true;
            ammo(newspaper,Bullets.standardThoriumBig);
            health = 1000;
            spread = 0.5f;
        }};
    }
}
