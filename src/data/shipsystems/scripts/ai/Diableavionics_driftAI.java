package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;





public class Diableavionics_driftAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private boolean runOnce = false;
    private final float checkAgain=0.25f;
    private float delay=0f, timer=0f;



    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }

        if(!runOnce){
            runOnce=true;

            delay=(float)Math.random()/4+5;
        }

        timer+=amount;

        if (timer>(delay+checkAgain)) {
            timer=0;

            if (!system.isActive() //the system is off
                            &&  AIUtils.getNearbyEnemies(ship, 2500).isEmpty() // or is alone
                            &&  ship.getSystem().getAmmo()>ship.getSystem().getMaxAmmo()-1  //and have full charges.
            ) {
                // then the system should activate, but can it?
                if (AIUtils.canUseSystemThisFrame(ship)){
                    //if the system can be activated, activate and set a minimum delay of 2 seconds before checking again
                    ship.useSystem();
                    delay = 2f;
                    return;
                }
            }else if(!system.isActive()) //the system is off and now it` is engaging with enemy.
                 {
                // then the system should activate, but can it?
                if (AIUtils.canUseSystemThisFrame(ship)){
                    //if the system can be activated, activate and set a minimum delay of 2 seconds before checking again
                    float hardfluxlevel =ship.getFluxTracker().getHardFlux()/ship.getFluxTracker().getMaxFlux();
                    float currsoftflux = ship.getFluxTracker().getCurrFlux()-ship.getFluxTracker().getHardFlux();

                    // if the ship has at least 3K softflux, then should use system to vent it.And this has a priority.
                    if(currsoftflux>=3000f){
                        ship.useSystem();
                        delay = 2f;
                        return;
                    }
                    // if the ship at least have 40%~80% hard-flux, and safe enough, down shield and use system to max the flux vent.
                    if(hardfluxlevel<=0.8f&&hardfluxlevel>=0.4f&&!isTorpedoComing(ship)){
                        if(ship.getShield().isOn()) {
                            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                        }
                        ship.useSystem();
                        delay = 2f;
                        return;
                    }
                    else if(hardfluxlevel>=0.8f){
                        //The ship is in danger,make it use system to retreat.
                        ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS,null,0);
                        ship.useSystem();
                        delay = 2f;
                        return;
                    }


                }
            }

            //remove the delay
            delay = 0f;
        }
    }


    public boolean isTorpedoComing(ShipAPI ship){

        List <MissileAPI> EnemyMissiles = AIUtils.getNearbyEnemyMissiles(ship,1000);

        if(!EnemyMissiles.isEmpty()){
            for(MissileAPI m : EnemyMissiles){
                if(m.getDamageAmount()>=1000.0f)
                    return true;
            }
        }
        return false;
    }

}
