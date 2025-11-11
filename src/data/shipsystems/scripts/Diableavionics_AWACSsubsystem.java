package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.Diableavionics_finder;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.lang.annotation.Target;
import java.util.*;
import java.util.List;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class Diableavionics_AWACSsubsystem extends MagicSubsystem {

    private static float BASE_SEARCH_RANGE = 1250f;

    private static float EFFECT_MULT = 1.25f;
    private List<ShipAPI> effected_wanzers = new ArrayList<>();
    private List<ShipAPI> remove_wanzers = new ArrayList<>();
    private final IntervalUtil chechtick = new IntervalUtil(2f,2f);
    private float beep = 0;
    private float beep_times = 0;
    private float officer_buff = 0f;
    private float score=0f;
    private float score_limit=15f;
    private String id = this.getClass().getName();
    private boolean runOnce = false;





    @Override
    public float getBaseActiveDuration() {
        return 15f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 20f;
    }

    @Override
    public boolean shouldActivateAI(float amount) {

        chechtick.advance(amount);

        if(chechtick.intervalElapsed()){

            score=0f;
            effected_wanzers = Diableavionics_finder.nearbyWanzerInRange(ship, BASE_SEARCH_RANGE);

            if(!effected_wanzers.isEmpty()){
                for (ShipAPI wanzer:effected_wanzers){

                    String a=wanzer.getHullSpec().getHullId();
                    String b=wanzer.getId();
                    String c=wanzer.getHullSpec().getBaseHullId();
                    String d=wanzer.getWing().getWingId();
                    String e=wanzer.getWing().getSpec().getId();

                    score+=scoreWanzer(wanzer);
                }

            }

            if(score>=score_limit){
                score_limit=15f;
                return true;
            }else{
                if(score_limit>8) {score_limit--;}
                return false;
            }

        }
        return false;
    }

    @Override
    public String getDisplayText() {
        return txt("subsystem_awacs_01");
    }


    public Diableavionics_AWACSsubsystem(ShipAPI ship) {
        super(ship);
    }

    @Override
    public void onActivate() {

        //reset up
        runOnce = false;
        beep = 0;
        beep_times= 0 ;

        if(ship.getCaptain().isDefault())
        {
            officer_buff=0f;
        }else{
            officer_buff = ship.getCaptain().getStats().getLevel() * 0.03f;
        }
        if (officer_buff >= 0.15) {
            officer_buff = 0.15f;
        }



        // Confirm the wanzer did`t effect by other AWACS system so they would effect by multiple times.
        for (ShipAPI wanzer : effected_wanzers) {

            if (Global.getCombatEngine().getCustomData().containsKey("AWACS" + wanzer.getId())) {
                remove_wanzers.add(wanzer);
            }
        }

        effected_wanzers.removeAll(remove_wanzers);
        remove_wanzers.clear();

        //add a targeting ring  (From virtuous)
        MagicRender.objectspace(
                Global.getSettings().getSprite("diableavionics", "RING"),
                ship, //anchor
                new Vector2f(), //offset
                new Vector2f(), //velocity
                new Vector2f(64, 64), //size
                new Vector2f(1250, 1250), //growth
                MathUtils.getRandomNumberInRange(-180, 180), //angle
                0, //spin
                false, //parented
                new Color(0, 200, 255, 128),
                true, //additive
                0, 0, //jitter
                0.5f, 1f, 0.2f, //flicker
                0.05f, 0.45f, 1f, //timing
                true,
                CombatEngineLayers.UNDER_SHIPS_LAYER
        );


        for (ShipAPI wanzer : effected_wanzers) {

            wanzer.getMutableStats().getMaxSpeed().modifyMult(id, EFFECT_MULT + officer_buff);

            Global.getCombatEngine().getCustomData().put("AWACS" + wanzer.getId(), wanzer);

            wanzer.getMutableStats().getBallisticWeaponDamageMult().modifyPercent(id, EFFECT_MULT + officer_buff);
            wanzer.getMutableStats().getEnergyWeaponDamageMult().modifyPercent(id, EFFECT_MULT + officer_buff);
            wanzer.getMutableStats().getMissileWeaponDamageMult().modifyPercent(id, EFFECT_MULT + officer_buff);

            diamondLockAWACS(wanzer);
        }

    }

    @Override
    public void advance(float amount, boolean isPaused) {

        //make sure some special situation
        if (!ship.isAlive() || Global.getCombatEngine().isCombatOver()) {

            for (ShipAPI wanzer : effected_wanzers) {
                Global.getCombatEngine().getCustomData().remove("AWACS" + wanzer.getId());
                wanzer.getMutableStats().getMaxSpeed().unmodify(id);
                wanzer.getMutableStats().getBallisticWeaponDamageMult().unmodify(id);
                wanzer.getMutableStats().getEnergyWeaponDamageMult().unmodify(id);
            }
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }




        if (state == State.IN || state == State.ACTIVE || state == State.OUT) {


            //make some noise for locked on
            beep-=amount;
            if (!runOnce&& beep<=0){

                beep=0.075f;
                //locked on sound
                Global.getSoundPlayer().playSound("diableavionics_virtuousTarget_beep",
                        1, 1, ship.getLocation(), ship.getVelocity());

                beep_times++;
                if (beep_times>=effected_wanzers.size()){
                    runOnce=true;
                }

            }


            Global.getSoundPlayer().playLoop("diableavionics_awacs_loop", ship, 0.9f, 0.65f, ship.getLocation(), ship.getVelocity(), 0.2f, 0.4f);
        }


    }

    @Override
    public void onFinished() {

        for (ShipAPI wanzer : effected_wanzers) {
            Global.getCombatEngine().getCustomData().remove("AWACS" + wanzer.getId());
            wanzer.getMutableStats().getMaxSpeed().unmodify(id);
            wanzer.getMutableStats().getBallisticWeaponDamageMult().unmodify(id);
            wanzer.getMutableStats().getEnergyWeaponDamageMult().unmodify(id);
        }

    }

    private float scoreWanzer(ShipAPI wanzer) {
        score = 0f;
        float maxDistance = 0f;
        // wanzer with no Target didn`t got score
        if (wanzer.getShipTarget() == null) {return score;}
        else {
            //Find the longest  weapon range to judge if wanzer has engage with target.
            for (WeaponAPI weapon : wanzer.getAllWeapons()) {
                if (weapon.isDecorative() || weapon.hasAIHint(WeaponAPI.AIHints.PD)) continue;
                maxDistance = weapon.getRange() * 0.9f;
            }

            Float targetDistance = MathUtils.getDistance(wanzer.getShipTarget().getLocation(), wanzer.getLocation());
            if (targetDistance > maxDistance) {
                return score;
            }  // return score=0
            else {
                String wanzerId = wanzer.getHullSpec().getHullId();
                score = Diableavionics_finder.DIABLE_WANZER_SCORE_MAP.get(wanzerId);
                ShipAPI target = wanzer.getShipTarget();
                if(target.getFluxTracker().isOverloaded()||target.getFluxTracker().isVenting()){
                    score+=1;
                }
                if(target.getEngineController().isDisabled()){
                    score+=1;
                }
            }
        }
        return score;
    }

    private void diamondLockAWACS(ShipAPI wanzer) {


        if (Global.getCombatEngine().isUIShowingHUD()) {
            //add a targeting diamond
            MagicRender.objectspace(
                    Global.getSettings().getSprite("diableavionics", "DIAMONDAWACS"),
                    wanzer, //anchor
                    new Vector2f(), //offset
                    new Vector2f(), //velocity
                    new Vector2f(64, 64), //size
                    new Vector2f(0, 0), //growth
                    -90, //angle
                    0, //spin
                    false, //parented
                    Color.green,
                    false, //additive
                    0, 0, //jitter
                    0, 0, 0, //flicker
                    0.5f, 14f, 0.5f, //timing
                    true,
                    CombatEngineLayers.BELOW_INDICATORS_LAYER
            );
            //exclude the swirly one if it is too far off screen
            if (MagicRender.screenCheck(0.2f, wanzer.getLocation())) {
                MagicRender.objectspace(
                        Global.getSettings().getSprite("diableavionics", "DIAMONDAWACS"),
                        wanzer, //anchor
                        new Vector2f(), //offset
                        new Vector2f(), //velocity
                        new Vector2f(192, 192), //size
                        new Vector2f(-256, -256), //growth
                        -90, //angle
                        360, //spin
                        false, //parented
                        Color.green,
                        false, //additive
                        0, 0, //jitter
                        0, 0, 0, //flicker
                        0.35f, 0.05f, 0.1f, //timing
                        true,
                        CombatEngineLayers.BELOW_INDICATORS_LAYER
                );
            }

        }
    }
}
