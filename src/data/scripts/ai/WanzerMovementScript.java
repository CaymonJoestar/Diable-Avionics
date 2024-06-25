package data.scripts.ai;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;

public class WanzerMovementScript implements AdvanceableListener {
    private ShipAPI ship = null;
    private float maxDistance = Float.MAX_VALUE;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.25f, 0.5f);   //判定减速的间隔
    private boolean canDecelerate = true;
    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定加速的间隔

    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    public WanzerMovementScript(ShipAPI ship) {
        this.ship = ship;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative() || weapon.hasAIHint(WeaponAPI.AIHints.PD)) continue; //对于飞机,遍历他的所有武器,取无pd词条的射程最小的武器作为最大判定距离
            if (weapon.getRange() < maxDistance) {
                maxDistance = weapon.getRange() * 0.9f;  //最大判定距离设置为这个武器的90% tart写的 我懒得改了 可以自己修改
            }
        }
        maxDistance = maxDistance * maxDistance; //这里貌似是在做某种转化 最后计算的过程会再开平方不用担心
    }

    @Override
    public void advance(float amount) {
        strafeInterval.advance(amount);
        if (strafeInterval.intervalElapsed()) {
            strafeState = randomStrafeDir();     //随机挑选一个方向运动
        }

        ShipAPI target = ship.getShipTarget();  //获取飞机目标
        if (target != null) {
            float distance = MathUtils.getDistanceSquared(ship, target);  //目标不为空获取与目标的距离
            if (distance <= maxDistance) {
                ship.giveCommand(strafeState, null, 0); //如果飞机与目标的距离小于最大判定距离,那么一边交火并前进
            }

            if (distance <= maxDistance * 0.8f) {  //如果飞机与目标的距离小于最大判定距离的80%
                decelerateInterval.advance(amount);             //判断是否度过一定时间
                if (decelerateInterval.intervalElapsed()) {
                    canDecelerate = true;       //判定可以减速
                }

                if (canDecelerate) {
                     ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0); //让飞机减速后退
                }
            } else {
                canDecelerate = false;
            }
        }
    }

    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }
}
