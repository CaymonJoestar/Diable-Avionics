package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.EscortPackage;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.combat.ai.AI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicIncompatibleHullmods;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.*;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableAvionicsSiroccoCenter extends BaseHullMod {


    private static final float COMMANDPOINT_BUFF = 200f;
    public static float DAMAGE_BONUS = 10f;
    public static float COOLDOWN_BONUS = 20f;
    private Map<String,ShipAPI> buffed_map= new HashMap<>();
    public static float EFFECT_RANGE = 1500f;
    public static float EFFECT_FADE = 500f;
    public static String SIROCCO_DATA_KEY = "sirocco_buff_data_key";
    IntervalUtil check_tick = new IntervalUtil(0.9f, 1.1f);

    public static Object STATUS_KEY = new Object();
    private int buffedfighters = 0;
    private int buffedships =0;
    private boolean runOnce=false;
    private float range=EFFECT_RANGE;

    private final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        BLOCKED_HULLMODS.add("operations_center");
    }

    @Override
    public void init(HullModSpecAPI spec) {


        super.init(spec);
    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){
        //blocked hullmods
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {
                MagicIncompatibleHullmods.removeHullmodWithWarning(ship.getVariant(), tmp, "diableavionics_cramped");
            }
        }

    }


    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return ( ship.getHullSpec().getHullId().startsWith("diableavionics_"));
    }





    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if (!ship.isAlive()) return;


        CombatEngineAPI engine = Global.getCombatEngine();


        if(!runOnce){



            if(ship.getOriginalCaptain().getStats().hasSkill("systems_expertise")){
                range*=1.5f;
            }


            //以防在尚未部署就吃到buff
            CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
            if (manager == null) return;
            DeployedFleetMemberAPI member = manager.getDeployedFleetMember(ship);
            if (member == null) return;
            ship.getMutableStats().getDynamic().getMod(Stats.COMMAND_POINT_RATE_FLAT).modifyFlat("sirocco_commandpoint_buff", COMMANDPOINT_BUFF*0.01f);
            runOnce=true;
        }





        check_tick.advance(amount);
        if(check_tick.intervalElapsed()){

            //检查汐风附近符合条件的目标 施加buff并且更新注册
            for(ShipAPI allie: AIUtils.getNearbyAllies(ship,range)){

//              //Global.getLogger(this.getClass()).info("check:"+allie.getId()+" "+allie.getHullSpec().getHullId());

                if(ship.getHullSpec().getHullId().contains("diableavionics_strifeShield")) continue;
                if(ship.getId().isEmpty()) continue;

                if(allie.isFighter()){

                    String key = SIROCCO_DATA_KEY+"_fighter" + "_" + allie.getId();

                    if(buffed_map.containsKey(key)) continue;
                    else{

                        buffed_map.put(key,allie);
                        engine.getCustomData().put(key, allie);
                    }

                }else{
                    String key = SIROCCO_DATA_KEY+ "_" + allie.getId();
                    if(buffed_map.containsKey(key)) continue;
                    else{
                        buffed_map.put(key,allie);
                        engine.getCustomData().put(key, allie);
                    }
                }
                applyEffect(allie);

            }

            //检查注册的舰船和飞机是否满足距离要求 不满足则剔除并且撤销buff

            Iterator<Map.Entry<String, ShipAPI>> entries = buffed_map.entrySet().iterator();

            while(entries.hasNext()){
                Map.Entry<String, ShipAPI> entry = entries.next();

                if(entry.getKey().equals("sirocco_buff_data_key_fighter_")){

                    ShipAPI buffedship = (ShipAPI) entry.getValue();
                    unapplyEffect(buffedship);

                    Global.getCombatEngine().getCustomData().remove(entry.getKey());
                    entries.remove();
                    continue;
                }


                if(entry.getKey().contains(SIROCCO_DATA_KEY)){

                    ShipAPI buffedship = (ShipAPI) entry.getValue();
                    if(!buffedship.isAlive()){

                        Global.getCombatEngine().getCustomData().remove(entry.getKey());
                        entries.remove();
                        continue;
                    }

                    float dist = MathUtils.getDistance(buffedship, ship);
                    if(dist>range){
                        unapplyEffect(buffedship);
                        Global.getCombatEngine().getCustomData().remove(entry.getKey());
                        entries.remove();
                    }
                }
            }

//            for(Map.Entry<String, Object> entry: Global.getCombatEngine().getCustomData().entrySet()) {
//
//
//                if(entry.getKey().contains(SIROCCO_DATA_KEY)){
//
//
//                    ShipAPI buffedship = (ShipAPI) entry.getValue();
//
//
//                   if(!buffedship.isAlive()){
//                       Global.getCombatEngine().getCustomData().remove(entry.getKey());
//                       continue;
//                   }
//
//                    float dist = MathUtils.getDistance(buffedship, ship);
//                    if(dist>range){
//
//                        unapplyEffect(buffedship);
//                        Global.getCombatEngine().getCustomData().remove(entry.getKey());
//                    }
//
//                }
//            }

            // 侧栏计数更新
            buffedships=0;
            buffedfighters=0;

            for(Map.Entry<String, ShipAPI> entry: buffed_map.entrySet()) {

                if(entry.getKey().contains(SIROCCO_DATA_KEY)){
                    ShipAPI buffedship = (ShipAPI) entry.getValue();
                    if (buffedship.isFighter()){
                        buffedfighters++;
                    }else{
                        buffedships++;
                    }
                }

            }
        }

        //在侧栏显示必要的信息
        boolean playerShip = ship == Global.getCombatEngine().getPlayerShip();

        if (playerShip) {

                String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_escort_package");

                Global.getCombatEngine().maintainStatusForPlayerShip(
                        STATUS_KEY, icon, txt("hm_siroccocenter_icon_title"),"fighters: " + buffedfighters
                                +"  ships: "+buffedships, false);

        }

    }


    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        final Color green = new Color(55,245,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float padQuote = 10f;
        final float padSig = 2f;
        final float padS = 0f;

        tooltip.addSectionHeading(txt("hm_tooltip_title_specific"), Alignment.MID, pad);



        tooltip.addPara(txt("hm_siroccocenter_01")
                        + txt("hm_siroccocenter_02")
                        + txt("hm_siroccocenter_03"),
                pad,Misc.getHighlightColor(),
                (int)COMMANDPOINT_BUFF+"%","20%","10%","1500");


        tooltip.addSectionHeading(txt("hm_tooltip_title_traningbuff"), Alignment.MID, pad);

        tooltip.addPara(txt("hm_siroccocenter_04"),
                pad,Misc.getHighlightColor(),
                txt("hm_siroccocenter_05"));



    }
    public void applyEffect(ShipAPI ship) {
        String id = "sirocco_center_bonus" + ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();


            if(ship.isFighter()){

                stats.getDamageToMissiles().modifyPercent(id,DAMAGE_BONUS);
                stats.getDamageToFighters().modifyPercent(id,DAMAGE_BONUS);
                stats.getDamageToFrigates().modifyPercent(id,DAMAGE_BONUS);
                stats.getDamageToDestroyers().modifyPercent(id,DAMAGE_BONUS);
                stats.getDamageToCruisers().modifyPercent(id,DAMAGE_BONUS);
                stats.getDamageToCapital().modifyPercent(id,DAMAGE_BONUS);
                stats.getSystemCooldownBonus().modifyMult(id,COOLDOWN_BONUS);
            }

            if(ship.isFrigate()){
                stats.getSystemCooldownBonus().modifyMult(id,COOLDOWN_BONUS);
            }



    }

    public void unapplyEffect(ShipAPI ship){

        String id = "sirocco_center_bonus" + ship.getId();
        MutableShipStatsAPI stats = ship.getMutableStats();

        stats.getDamageToMissiles().unmodify(id);
        stats.getDamageToFighters().unmodify(id);
        stats.getDamageToFrigates().unmodify(id);
        stats.getDamageToDestroyers().unmodify(id);
        stats.getDamageToCruisers().unmodify(id);
        stats.getDamageToCapital().unmodify(id);
        stats.getSystemCooldownBonus().unmodify(id);

    }

}
