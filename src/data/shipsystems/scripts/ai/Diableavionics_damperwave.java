package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.*;

import org.lwjgl.util.vector.Vector2f;


public class Diableavionics_damperwave implements ShipSystemAIScript {

    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){


    }


}
