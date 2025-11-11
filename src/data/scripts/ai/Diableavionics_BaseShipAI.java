package data.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

public class Diableavionics_BaseShipAI {


    public static ShipCommand turn(ShipAPI ship, ShipAPI target, boolean turnAway) {

        float degreeAngle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
        float angleDif = MathUtils.getShortestRotation(ship.getFacing(), degreeAngle);

        //Check to see if we should slow down to avoid overshooting
        float secondsTilDesiredFacing = angleDif / ship.getAngularVelocity();
        if (secondsTilDesiredFacing > 0) {
            float turnAcc = ship.getMutableStats().getTurnAcceleration().getModifiedValue();
            float rotValWhenAt = Math.abs(ship.getAngularVelocity()) - secondsTilDesiredFacing * turnAcc;
            if (rotValWhenAt > 0) turnAway = !turnAway;
        }

//        if((!turnAway && Math.abs(angleDif) < DEFAULT_FACING_THRESHHOLD)
//                || (turnAway && Math.abs(angleDif) > 180 - DEFAULT_FACING_THRESHHOLD))
//            return null;

        ShipCommand direction = (angleDif > 0) ^ turnAway
                ? ShipCommand.TURN_LEFT
                : ShipCommand.TURN_RIGHT;

        ship.giveCommand(direction, null, 0);

//        float amount = Global.getCombatEngine().getElapsedInLastFrame();
//        float turnAcc = ship.getMutableStats().getTurnAcceleration().getModifiedValue();
//        float maxTurn = ship.getMutableStats().getMaxTurnRate().getModifiedValue();
//        float angleVel = ship.getAngularVelocity();
//        float dAngleVel = turnAcc * ((direction == ShipCommand.TURN_RIGHT) ? -1 : 1) * amount;
//        float newAngleVel = angleVel + dAngleVel;
//
//        ship.setAngularVelocity(Math.max(-maxTurn, Math.min(maxTurn, newAngleVel)));

        return direction;
    }


}




