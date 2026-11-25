package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.combat.DroneLauncherShipSystemAPI;

@SuppressWarnings("unused")  // IntelliJ warning suppression
public class diable_calmsystemAI implements ShipSystemAIScript {

    private CombatEngineAPI engine;
    private ShipAPI ship;
    private ShipSystemAPI system;
    private final IntervalUtil timer = new IntervalUtil(0.4f, 0.75f);

    private float getAggroRange(ShipAPI enemy) {
        if (enemy == null) return 0f;
        return switch (enemy.getHullSize()) {
            case FIGHTER    -> 800f;
            case FRIGATE    -> 1100f;
            case DESTROYER  -> 1400f;
            case CRUISER    -> 1800f;
            case CAPITAL_SHIP-> 2400f;
            default         -> 1400f;
        };
    }

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {

        this.ship = ship;
        this.system = system;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused() || system.isActive()) {
            return;
        }

        DroneLauncherShipSystemAPI system2 = (DroneLauncherShipSystemAPI) ship.getSystem();
        ShipAPI nearestEnemy = AIUtils.getNearestEnemy(ship);

        timer.advance(amount);
        //nearestEnemy == null ||
        if (timer.intervalElapsed()) {
            if (!ship.isRetreating()) {

                if (nearestEnemy == null) {
                    if (system2.getDroneOrders() != DroneLauncherShipSystemAPI.DroneOrders.RECALL) {
                        ship.useSystem();
                    }
                } else {
                    float aggroRange = getAggroRange(nearestEnemy);
                    float distance = MathUtils.getDistance(ship, nearestEnemy);
                    //Main AI Logic
                    if (distance > aggroRange) {
                        //Enemy Out of Range + Recall
                        if (system2.getDroneOrders() != DroneLauncherShipSystemAPI.DroneOrders.RECALL) {
                            ship.useSystem();
                        }
                    } else {
                        //Enemy is in Play
                        float shipFlux = ship.getFluxLevel();
                        float enemyFlux = nearestEnemy.getFluxLevel();

                        //if ship flux is high.
                        if (shipFlux > 0.72f) {
                            //Defense Mode
                            if (system2.getDroneOrders() != DroneLauncherShipSystemAPI.DroneOrders.DEPLOY) {
                                ship.useSystem();
                            }
                        }
                        //Potential Enemy on Flux Overload
                        else if (enemyFlux > 0.85f) {
                            if (system2.getDroneOrders() != DroneLauncherShipSystemAPI.DroneOrders.ATTACK) {
                                ship.useSystem();
                            }
                        } else {
                            //Offense Mode Switch
                            if (system2.getDroneOrders() != DroneLauncherShipSystemAPI.DroneOrders.ATTACK) {
                                ship.useSystem();
                            }
                        }
                    }
                }
            }
        }
    }
}