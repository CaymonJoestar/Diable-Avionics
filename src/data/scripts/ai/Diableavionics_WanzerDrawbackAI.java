package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.WanzerBurstHullmod;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.ai.Diableavionics_BaseShipAI;

import data.scripts.ai.Diableavionics_BaseShipAI.*;

public class Diableavionics_WanzerDrawbackAI implements ShipAIPlugin {

    private final ShipwideAIFlags flags = new ShipwideAIFlags();
    private final ShipAIConfig config = new ShipAIConfig();

    private IntervalUtil ai_tick= new IntervalUtil(0.1f,0.1f);
    private ShipAPI WANZER;
    private float DISTANCE_TO_KEEP;
    private ShipAPI Target;

    Diableavionics_WanzerDrawbackAI(ShipAPI wanzer,float distance){
        this.WANZER=wanzer;
        this.DISTANCE_TO_KEEP=distance;
    }


    @Override
    public void setDoNotFireDelay(float amount) {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public void advance(float amount) {
        ai_tick.advance(amount);

        //使万泽持续转向面对敌人
        Target=WANZER.getShipTarget();
        if(WANZER.getShipTarget()!=null){
            Diableavionics_BaseShipAI.turn(WANZER,Target,false);
        }



        if(ai_tick.intervalElapsed()){
          //MissileAPI threatmissile = AIUtils.getNearestEnemyMissile(WANZER);

             forceCircumstanceEvaluation();
            //勇士：勇士的ai需要在接敌之后取消掉
            if(WANZER.getShipTarget()!=null)
            {
                if(WANZER.getHullSpec().getHullId().contains("valiant")){
                    ValiantSystemAI();
                }








                for(WeaponAPI w:WANZER.getAllWeapons())
                {
                    //check weapon
                    if(w.isDecorative()){continue;}


                    // if the weapon is run of ammo continue
                    if (w.getAmmoTracker().getReloadProgress()>0){
                        if(w.getAmmoTracker().getAmmo()==0){continue;}
                       }

                    if(w.hasAIHint(WeaponAPI.AIHints.PD)&&w.getType().equals(WeaponAPI.WeaponType.MISSILE))
                    {
                        continue;
                    }

                    float dist = MathUtils.getDistance(WANZER,Target);
                    dist -= Global.getSettings().getTargetingRadius(WANZER.getLocation(), Target, false);
                    if(w.getRange()>dist){

                            int groupnumber = WANZER.getWeaponGroupsCopy().indexOf(WANZER.getWeaponGroupFor(w));

                            WANZER.giveCommand(ShipCommand.FIRE, Target.getLocation(), groupnumber);

                    }
                }

            }

            WANZER.giveCommand(ShipCommand.ACCELERATE_BACKWARDS,null,0);

        }


    }

    private void ValiantSystemAI() {
        ShipSystemAPI system=WANZER.getSystem();
        ShipAPI ship=WANZER;
        ShipAPI target=WANZER.getShipTarget();
        if (
                system.isActive() // the system is on
                        && !ship.isRetreating() // and the fighter is not retreating
                        && ( // and
                        ship.isLanding() // the fighter is either landing
                                || (MathUtils.getDistance(ship.getLocation(),target.getLocation())<=400f) // or is near an enemy and has flux to spare
                )
        ) {
            // 使勇士结束战机形态
            ship.useSystem();
            return;
        }

    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return flags;
    }


    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return config;
    }






}
