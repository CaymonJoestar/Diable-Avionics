package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.*;

import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.Diableavionics_UniThreatenJudge;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;


public class Diableavionics_damperwaveAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private final IntervalUtil tick = new IntervalUtil(0.05f,0.05f);
    private static final Color engineColor = new Color(0x00FF80);
    private float degress =0f;
    private boolean Need_dodge = false;
    private boolean Need_attack= false;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){


        if(Diableavionics_UniThreatenJudge.isTreatenedbyProjectile(ship,300) )
        {
            Need_dodge=true;
        }

        if(Diableavionics_UniThreatenJudge.BeamweaponFiring(ship,2000)){
            Need_dodge=true;
        }
        if (Need_dodge== true){
            ship.useSystem();
        }

        if (target != null) {
            float score = 0f;

            if (target.getFluxTracker().isOverloadedOrVenting()) {
                score += 6f;
            } else {
                score += target.getFluxLevel() * 6f;
            }

            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());

//            float avgRange = aiData.getAverageWeaponRange(false);
            score += Math.min(dist/200f, 4f);

            if(score > 8f){
                Need_attack=true;
            }
            if (Need_attack== true){
                ship.useSystem();
            }

        }
    }
}